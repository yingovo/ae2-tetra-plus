package com.skymirror.ae2tetraplus;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;

/**
 * 「存储类型」槽位的解析、类型切换与数据清空逻辑。
 */
public final class KeyTypeCores {

    public static final String SLOT = "cell/keytype";
    public static final String NBT_TYPE = "ae2tpKeyType";

    private static AEKeyType manaType;
    private static boolean manaResolved;
    private static AEKeyType feType;
    private static boolean feResolved;
    private static AEKeyType airType;
    private static boolean airResolved;

    private KeyTypeCores() {
    }

    public static boolean isAllType(ItemStack stack) {
        String id = moduleId(stack);
        return id != null && id.endsWith("/all");
    }

    public static AEKeyType keyTypeOf(ItemStack stack) {
        String id = moduleId(stack);
        if (id != null) {
            if (id.endsWith("/fluid")) {
                return AEKeyType.fluids();
            }
            if (id.endsWith("/fe")) {
                AEKeyType type = feType();
                if (type != null) {
                    return type;
                }
            }
            if (id.endsWith("/mana")) {
                AEKeyType type = manaType();
                if (type != null) {
                    return type;
                }
            }
            if (id.endsWith("/air")) {
                AEKeyType type = airType();
                if (type != null) {
                    return type;
                }
            }
        }
        return AEKeyType.items();
    }

    public static AEKeyType manaType() {
        if (!manaResolved) {
            manaResolved = true;
            manaType = reflectField("appbot.ae2.ManaKeyType", "TYPE");
        }
        return manaType;
    }

    public static AEKeyType feType() {
        if (!feResolved) {
            feResolved = true;
            feType = reflectField("com.glodblock.github.appflux.common.me.key.type.FluxKeyType", "TYPE");
        }
        return feType;
    }

    public static AEKeyType airType() {
        if (!airResolved) {
            airResolved = true;
            airType = reflectField("com.wintercogs.appliedpneumatics.common.me.keys.types.AirKeyType", "INSTANCE");
        }
        return airType;
    }

    private static AEKeyType reflectField(String className, String fieldName) {
        try {
            Class<?> cls = Class.forName(className);
            Object value = cls.getField(fieldName).get(null);
            if (value instanceof AEKeyType type) {
                return type;
            }
        } catch (Throwable ignored) {
            // 没装对应扩展
        }
        return null;
    }

    public static String typeId(ItemStack stack) {
        String id = moduleId(stack);
        if (id == null) {
            // 模块读不到时，以 NBT 记录为准（避免误判成物品核心而把数据清掉）
            String recorded = stack.getOrCreateTag().getString(NBT_TYPE);
            if (!recorded.isEmpty()) {
                return recorded;
            }
        }
        if (isAllType(stack)) {
            return "all";
        }
        AEKeyType type = keyTypeOf(stack);
        if (type == AEKeyType.items()) {
            return "item";
        }
        if (type == AEKeyType.fluids()) {
            return "fluid";
        }
        if (same(type, manaType())) {
            return "mana";
        }
        if (same(type, feType())) {
            return "fe";
        }
        if (same(type, airType())) {
            return "air";
        }
        return "item";
    }

    private static boolean same(AEKeyType a, AEKeyType b) {
        return a != null && b != null && a == b;
    }

    /**
     * 防重入：创建存储实现时可能间接再次请求存储实现（物品侧工厂会走 AE2 的 cell handler），
     * 没有这道闸会出现无限递归导致主线程卡死。
     */
    private static final ThreadLocal<Boolean> BUILDING = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** 存储实现：全类型核心 → 合并存储；单类型非物品 → 通用存储；物品 → null（交回 AE Tetra）。 */
    public static StorageCell storageFor(ItemStack stack) {
        if (Boolean.TRUE.equals(BUILDING.get())) {
            return null;
        }
        BUILDING.set(Boolean.TRUE);
        try {
            if (isAllType(stack)) {
                return new UniversalStorageCell(stack);
            }
            AEKeyType type = keyTypeOf(stack);
            if (type == AEKeyType.items()) {
                return null;
            }
            return new FluidStorageCell(stack, type);
        } finally {
            BUILDING.set(Boolean.FALSE);
        }
    }

    /** 当前盘的内容快照（全类型时合并物品与非物品）。 */
    public static KeyCounter snapshotOf(ItemStack stack) {
        KeyCounter counter = new KeyCounter();
        StorageCell cell = storageFor(stack);
        if (cell != null) {
            cell.getAvailableStacks(counter);
        } else {
            try {
                net.yiran.ae_tetra.ae.TetraCellInventory inv =
                        net.yiran.ae_tetra.ae.TetraCellInventory.createInventory(stack, null);
                if (inv != null) {
                    inv.getAvailableStacks(counter);
                }
            } catch (Throwable ignored) {
                // 忽略
            }
        }
        return counter;
    }

    /** 返回 {总字节, 已用字节, 总类型, 已存类型}；非物品/全类型时用。 */
    public static long[] statsOf(ItemStack stack) {
        StorageCell cell = storageFor(stack);
        if (cell instanceof UniversalStorageCell universal) {
            return new long[]{universal.statTotalBytes(), universal.statUsedBytes(),
                    universal.statTotalTypes(), universal.statStoredTypes()};
        }
        if (cell instanceof FluidStorageCell typed) {
            return new long[]{typed.statTotalBytes(), typed.statUsedBytes(),
                    typed.statTotalTypes(), typed.statStoredTypes()};
        }
        return null;
    }

    /** 类型一致性检查 + 切换时的数据裁剪。 */
    public static void ensureTypeConsistency(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        if (moduleId(stack) == null) {
            String recorded = tag.getString(NBT_TYPE);
            String restore = recorded.isEmpty() ? "item" : recorded;
            try {
                IModularItem.putModuleInSlot(stack, SLOT,
                        "cell/keytype/" + restore, "cell/keytype/" + restore);
            } catch (Throwable ignored) {
                // 忽略
            }
        }
        String want = typeId(stack);
        String have = tag.getString(NBT_TYPE);
        if (have.isEmpty()) {
            tag.putString(NBT_TYPE, want);
            return;
        }
        if (have.equals(want)) {
            return;
        }
        switch (want) {
            case "all":
                // 换成全类型：什么都不清，两边数据都留着
                tag.putString(NBT_TYPE, want);
                return;
            case "item":
                // 只保留物品数据
                tag.remove(FluidStorageCell.TAG);
                break;
            default:
                // 换成某个具体非物品类型：删物品数据，非物品只留匹配类型
                tag.remove("ic");
                tag.remove("keys");
                tag.remove("amts");
                tag.remove("config");
                tag.remove("FuzzyMode");
                pruneNonItem(tag, keyTypeOf(stack));
                break;
        }
        tag.putString(NBT_TYPE, want);
    }

    /** 只保留与目标 key 类型匹配的非物品条目。 */
    private static void pruneNonItem(CompoundTag tag, AEKeyType target) {
        CompoundTag root = tag.getCompound(FluidStorageCell.TAG);
        ListTag entries = root.getList("e", Tag.TAG_COMPOUND);
        ListTag kept = new ListTag();
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            AEKey key = AEKey.fromTagGeneric(entry.getCompound("k"));
            if (key != null && target != null && target.equals(key.getType())) {
                kept.add(entry);
            }
        }
        CompoundTag next = new CompoundTag();
        next.put("e", kept);
        tag.put(FluidStorageCell.TAG, next);
    }

    /** 读指定槽位的模块 key（对外公开）。 */
    public static String moduleIdOf(ItemStack stack, String slot) {
        return moduleId(stack, slot);
    }

    private static String moduleId(ItemStack stack) {
        return moduleId(stack, SLOT);
    }

    private static String moduleId(ItemStack stack, String slot) {
        try {
            if (!(stack.getItem() instanceof IModularItem modular)) {
                return null;
            }
            Object module = modular.getModuleFromSlot(stack, slot);
            if (module == null) {
                return null;
            }
            try {
                java.lang.reflect.Method getKey = module.getClass().getMethod("getKey");
                Object value = getKey.invoke(module);
                if (value instanceof String s) {
                    return s;
                }
            } catch (Throwable ignored) {
                // 落到字段
            }
            try {
                java.lang.reflect.Field field = module.getClass().getField("key");
                Object value = field.get(module);
                if (value instanceof String s) {
                    return s;
                }
            } catch (Throwable ignored) {
                // 忽略
            }
        } catch (Throwable ignored) {
            // 忽略
        }
        return null;
    }
}
