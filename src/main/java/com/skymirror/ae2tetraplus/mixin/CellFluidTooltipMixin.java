package com.skymirror.ae2tetraplus.mixin;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.items.storage.StorageCellTooltipComponent;
import com.skymirror.ae2tetraplus.KeyTypeCores;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.yiran.ae_tetra.ae.TetraModularCellItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 非物品核心 / 全类型核心时，用 AE2 自己的 StorageCellTooltipComponent 渲染内容网格。
 */
@Mixin(value = TetraModularCellItem.class, remap = false)
public abstract class CellFluidTooltipMixin {

    @Inject(method = "m_142422_", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2tp$typedTooltipImage(ItemStack stack,
                                         CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        try {
            KeyCounter counter = KeyTypeCores.snapshotOf(stack);
            if (counter.keySet().isEmpty()) {
                if (KeyTypeCores.storageFor(stack) == null) {
                    return; // 物品核心：交回 AE Tetra
                }
            }
            List<GenericStack> content = new ArrayList<>();
            for (AEKey key : counter.keySet()) {
                long amount = counter.get(key);
                if (amount > 0) {
                    content.add(new GenericStack(key, amount));
                }
            }
            if (content.isEmpty() && KeyTypeCores.storageFor(stack) == null) {
                return;
            }
            content.sort((a, b) -> Long.compare(b.amount(), a.amount()));
            boolean hasMore = content.size() > 8;
            if (hasMore) {
                content.subList(8, content.size()).clear();
            }
            cir.setReturnValue(Optional.of(
                    new StorageCellTooltipComponent(List.of(), content, hasMore, true)));
        } catch (Throwable ignored) {
            // 出错走原版
        }
    }
}
