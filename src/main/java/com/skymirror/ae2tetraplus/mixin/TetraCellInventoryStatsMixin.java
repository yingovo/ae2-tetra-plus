package com.skymirror.ae2tetraplus.mixin;

import com.skymirror.ae2tetraplus.KeyTypeCores;
import net.minecraft.world.item.ItemStack;
import net.yiran.ae_tetra.ae.TetraCellInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让 AE Tetra 自己的物品提示（含「Shift 查看部件」）在非物品核心 / 全类型核心时显示正确数值。
 */
@Mixin(value = TetraCellInventory.class, remap = false)
public abstract class TetraCellInventoryStatsMixin {

    @Shadow(remap = false)
    private ItemStack i;

    private long[] ae2tp$stats() {
        try {
            return this.i == null ? null : KeyTypeCores.statsOf(this.i);
        } catch (Throwable t) {
            return null;
        }
    }

    @Inject(method = "getTotalBytes", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2tp$totalBytes(CallbackInfoReturnable<Long> cir) {
        long[] s = ae2tp$stats();
        if (s != null) {
            cir.setReturnValue(s[0]);
        }
    }

    @Inject(method = "getUsedBytes", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2tp$usedBytes(CallbackInfoReturnable<Long> cir) {
        long[] s = ae2tp$stats();
        if (s != null) {
            cir.setReturnValue(s[1]);
        }
    }

    @Inject(method = "getTotalItemTypes", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2tp$totalTypes(CallbackInfoReturnable<Long> cir) {
        long[] s = ae2tp$stats();
        if (s != null) {
            cir.setReturnValue(s[2]);
        }
    }

    @Inject(method = "getStoredItemTypes", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2tp$storedTypes(CallbackInfoReturnable<Long> cir) {
        long[] s = ae2tp$stats();
        if (s != null) {
            cir.setReturnValue(s[3]);
        }
    }
}
