package com.skymirror.ae2tetraplus;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

/**
 * 「非物品」类型核心的通用存储实现（流体 / 魔力 ……）。
 *
 * <p>容量从磁盘物品自身读取（AE Tetra 的 getBytes / getBytesPerType / getTotalTypes），
 * 数据存在物品 NBT 的 {@link #TAG} 下方；只接受与当前核心一致的 key 类型。
 */
public class FluidStorageCell implements StorageCell {

    public static final String TAG = "ae2tp_fluid";

    private final ItemStack stack;
    private final AEKeyType keyType;
    private final long totalBytes;
    private final int totalTypes;
    private final int bytesPerType;

    public FluidStorageCell(ItemStack stack) {
        this(stack, AEKeyType.fluids());
    }

    public FluidStorageCell(ItemStack stack, AEKeyType keyType) {
        this.stack = stack;
        this.keyType = keyType;
        this.totalBytes = readLong(stack, "getBytes", 65536L);
        this.totalTypes = (int) Math.max(1L, readLong(stack, "getTotalTypes", 1L));
        this.bytesPerType = (int) Math.max(1L, readLong(stack, "getBytesPerType", 1L));
    }

    private static long readLong(ItemStack stack, String method, long fallback) {
        try {
            Item item = stack.getItem();
            Method m = item.getClass().getMethod(method, ItemStack.class);
            Object value = m.invoke(item, stack);
            if (value instanceof Number n) {
                return n.longValue();
            }
        } catch (Throwable ignored) {
            // 忽略
        }
        return fallback;
    }

    private ListTag list() {
        CompoundTag root = stack.getOrCreateTag().getCompound(TAG);
        return root.getList("e", Tag.TAG_COMPOUND);
    }

    private void save(ListTag entries) {
        CompoundTag root = new CompoundTag();
        root.put("e", entries);
        stack.getOrCreateTag().put(TAG, root);
    }

    /** 按 key 类型各自的「每字节容量」计算已用字节（物品 8/字节、流体 1000mB/字节、FE 等由扩展定义）。 */
    private long usedBytes(ListTag entries) {
        long bytes = (long) entries.size() * bytesPerType;
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            AEKey key = AEKey.fromTagGeneric(entry.getCompound("k"));
            long amount = entry.getLong("a");
            int perByte = 8;
            if (key != null) {
                try {
                    perByte = Math.max(1, key.getType().getAmountPerByte());
                } catch (Throwable ignored) {
                    perByte = 8;
                }
            }
            bytes += (amount + perByte - 1L) / perByte;
        }
        return bytes;
    }

    /** 每类型开销（对外暴露给全类型存储共用）。 */
    public int bytesPerTypePublic() {
        return bytesPerType;
    }

    // ---- 供 tooltip 使用的统计 ----

    public long statTotalBytes() {
        return totalBytes;
    }

    public long statUsedBytes() {
        KeyCounter counter = snapshot();
        long bytes = (long) counter.keySet().size() * bytesPerType;
        for (AEKey key : counter.keySet()) {
            long amount = counter.get(key);
            int perByte = 8;
            try {
                perByte = Math.max(1, key.getType().getAmountPerByte());
            } catch (Throwable ignored) {
                perByte = 8;
            }
            bytes += (amount + perByte - 1L) / perByte;
        }
        return Math.min(bytes, totalBytes);
    }

    public long statTotalTypes() {
        return totalTypes;
    }

    public long statStoredTypes() {
        return snapshot().keySet().size();
    }

    /** 合并重复条目后的统计（旧数据里可能残留同一 key 的多条记录）。 */
    public KeyCounter snapshot() {
        KeyCounter counter = new KeyCounter();
        getAvailableStacks(counter);
        return counter;
    }

    @Override
    public Component getDescription() {
        return Component.translatable("item.ae_tetra.modular_cell");
    }

    @Override
    public CellState getStatus() {
        ListTag entries = list();
        if (entries.isEmpty()) {
            return CellState.EMPTY;
        }
        return usedBytes(entries) >= totalBytes ? CellState.FULL : CellState.NOT_EMPTY;
    }

    @Override
    public double getIdleDrain() {
        return 0.0;
    }

    @Override
    public void persist() {
        // NBT 直接写回物品
    }

    @Override
    public void getAvailableStacks(KeyCounter out) {
        ListTag entries = list();
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            AEKey key = AEKey.fromTagGeneric(entry.getCompound("k"));
            long amount = entry.getLong("a");
            // 只认当前核心对应的 key 类型，避免换核心后旧数据“露出来”；
            // keyType 为 null 表示“全类型核心”：接受一切非物品类型
            if (key != null && amount > 0) {
                AEKeyType actual = key.getType();
                boolean accept = keyType == null
                        ? actual != AEKeyType.items()
                        : keyType.equals(actual);
                if (accept) {
                    out.add(key, amount);
                }
            }
        }
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (what == null || amount <= 0) {
            return 0;
        }
        if (keyType == null) {
            if (what.getType() == AEKeyType.items()) {
                return 0;
            }
        } else if (!keyType.equals(what.getType())) {
            return 0;
        }
        // 容量检查：剩余字节 × 该类型的每字节容量
        ListTag current = list();
        long remaining = Math.max(0L, totalBytes - usedBytes(current));
        int perByte = 8;
        try {
            perByte = Math.max(1, what.getType().getAmountPerByte());
        } catch (Throwable ignored) {
            perByte = 8;
        }
        long maxByBytes = remaining * perByte;
        if (maxByBytes <= 0) {
            return 0;
        }
        amount = Math.min(amount, maxByBytes);
        ListTag entries = list();
        int index = -1;
        CompoundTag incoming = what.toTagGeneric();
        for (int i = 0; i < entries.size(); i++) {
            if (entries.getCompound(i).getCompound("k").equals(incoming)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            // 用「去重后的实际类型数」判断，避免历史重复条目把上限算大/算小
            if (snapshot().keySet().size() >= totalTypes) {
                return 0;
            }
            if (mode == Actionable.MODULATE) {
                CompoundTag entry = new CompoundTag();
                entry.put("k", incoming);
                entry.putLong("a", amount);
                entries.add(entry);
                save(entries);
            }
            return amount;
        }
        if (mode == Actionable.MODULATE) {
            CompoundTag entry = entries.getCompound(index);
            entry.putLong("a", entry.getLong("a") + amount);
            entries.set(index, entry);
            save(entries);
        }
        return amount;
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (what == null || amount <= 0) {
            return 0;
        }
        ListTag entries = list();
        CompoundTag incoming = what.toTagGeneric();
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            if (!entry.getCompound("k").equals(incoming)) {
                continue;
            }
            long stored = entry.getLong("a");
            long taken = Math.min(stored, amount);
            if (mode == Actionable.MODULATE) {
                long left = stored - taken;
                if (left <= 0) {
                    entries.remove(i);
                } else {
                    entry.putLong("a", left);
                    entries.set(i, entry);
                }
                save(entries);
            }
            return taken;
        }
        return 0;
    }
}
