package com.skymirror.ae2tetraplus;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.StorageCell;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.yiran.ae_tetra.ae.TetraCellInventory;

/**
 * 「全类型核心」的存储实现：物品部分交给 AE Tetra 原本的存储，
 * 非物品部分（流体 / 魔力 / FE / 空气）用我们自己的 NBT 存储。
 *
 * <p>容量按选项 A 处理：<b>两者共享同一个总字节池</b>——插入前用
 * {@code 总字节 - 物品已用 - 非物品已用} 估算还能放多少。
 */
public class UniversalStorageCell implements StorageCell {

    private final ItemStack stack;
    private TetraCellInventory itemCell;
    private final FluidStorageCell nonItem;
    private final long totalBytes;

    public UniversalStorageCell(ItemStack stack) {
        this.stack = stack;
        this.nonItem = new FluidStorageCell(stack, null);
        this.totalBytes = nonItem.statTotalBytes();
    }

    /** 物品部分延迟创建，避免在构造期触发放射性调用链。 */
    private TetraCellInventory item() {
        if (itemCell == null) {
            try {
                itemCell = TetraCellInventory.createInventory(stack, null);
            } catch (Throwable t) {
                itemCell = null;
            }
        }
        return itemCell;
    }

    private long itemUsed() {
        try {
            return itemCell == null ? 0L : itemCell.getUsedBytes();
        } catch (Throwable t) {
            return 0L;
        }
    }

    /**
     * 统一按「实际内容 × 各类型换算率」计算已用字节。
     *
     * <p>不再把 AE Tetra 物品侧自报的 used 相加——那会和我们的统计重叠，
     * 导致全类型盘在 50% 左右就被判定为满。
     */
    private long usedTotal() {
        try {
            KeyCounter counter = snapshot();
            long used = (long) counter.keySet().size() * nonItem.bytesPerTypePublic();
            for (AEKey key : counter.keySet()) {
                long amount = counter.get(key);
                int perByte = 8;
                try {
                    perByte = Math.max(1, key.getType().getAmountPerByte());
                } catch (Throwable ignored) {
                    perByte = 8;
                }
                used += (amount + perByte - 1L) / perByte;
            }
            return Math.min(used, totalBytes);
        } catch (Throwable t) {
            return Math.min(itemUsed() + nonItem.statUsedBytes(), totalBytes);
        }
    }

    private long remainingBytes() {
        return Math.max(0L, totalBytes - usedTotal());
    }

    @Override
    public Component getDescription() {
        return Component.translatable("item.ae_tetra.modular_cell");
    }

    @Override
    public CellState getStatus() {
        if (usedTotal() <= 0) {
            return CellState.EMPTY;
        }
        return usedTotal() >= totalBytes ? CellState.FULL : CellState.NOT_EMPTY;
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
        try {
            if (item() != null) {
                item().getAvailableStacks(out);
            }
        } catch (Throwable ignored) {
            // 忽略
        }
        nonItem.getAvailableStacks(out);
    }

    @Override
    public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (what == null || amount <= 0) {
            return 0;
        }
        AEKeyType type = what.getType();
        // 类型数上限：物品与非物品共用同一份类型预算
        try {
            KeyCounter existing = snapshot();
            boolean known = false;
            for (AEKey key : existing.keySet()) {
                if (key != null && key.equals(what)) {
                    known = true;
                    break;
                }
            }
            if (!known && existing.keySet().size() >= nonItem.statTotalTypes()) {
                return 0;
            }
        } catch (Throwable ignored) {
            // 忽略，继续按字节限流
        }
        long remaining = remainingBytes();
        if (remaining <= 0) {
            return 0;
        }
        long amountLimit = Math.max(1L, remaining * Math.max(1, type.getAmountPerByte()));
        long capped = Math.min(amount, amountLimit);
        if (type == AEKeyType.items()) {
            try {
                return item() == null ? 0 : item().insert(what, capped, mode, source);
            } catch (Throwable t) {
                return 0;
            }
        }
        return nonItem.insert(what, capped, mode, source);
    }

    @Override
    public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
        if (what == null || amount <= 0) {
            return 0;
        }
        if (what.getType() == AEKeyType.items()) {
            try {
                return item() == null ? 0 : item().extract(what, amount, mode, source);
            } catch (Throwable t) {
                return 0;
            }
        }
        return nonItem.extract(what, amount, mode, source);
    }

    /** 供 tooltip 统计使用：合并两边的内容。 */
    public KeyCounter snapshot() {
        KeyCounter counter = new KeyCounter();
        getAvailableStacks(counter);
        return counter;
    }

    public long statTotalBytes() {
        return totalBytes;
    }

    public long statUsedBytes() {
        return usedTotal();
    }

    public long statTotalTypes() {
        // 类型预算只有一份：物品与非物品共用，不重复相加
        return nonItem.statTotalTypes();
    }

    public long statStoredTypes() {
        return snapshot().keySet().size();
    }

    private long safeTotalTypes() {
        try {
            return item().getTotalItemTypes();
        } catch (Throwable t) {
            return 0L;
        }
    }

    /** 清理非物品数据（切到物品核心时用）。 */
    public void wipeNonItem() {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(FluidStorageCell.TAG);
    }

    /** 清理物品数据（切到非物品核心时用）。 */
    public void wipeItem() {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove("ic");
        tag.remove("keys");
        tag.remove("amts");
        tag.remove("config");
        tag.remove("FuzzyMode");
    }
}
