package com.skymirror.ae2tetraplus.mixin;

import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import com.skymirror.ae2tetraplus.Ae2TetraPlus;
import com.skymirror.ae2tetraplus.KeyTypeCores;
import net.minecraft.world.item.ItemStack;
import net.yiran.ae_tetra.ae.TetraCellHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 按「存储类型」核心决定这张盘走哪种存储实现：
 * 物品核心 → 交回 AE Tetra 原本的物品存储；流体 / 魔力核心 → 我们的通用存储。
 */
@Mixin(value = TetraCellHandler.class, remap = false)
public abstract class TetraCellHandlerMixin {

    @Inject(method = "getCellInventory", at = @At("HEAD"), cancellable = true, remap = false)
    private void ae2tp$routeByKeyType(ItemStack stack, ISaveProvider saveProvider,
                                      CallbackInfoReturnable<StorageCell> cir) {
        try {
            KeyTypeCores.ensureTypeConsistency(stack);
            StorageCell typed = KeyTypeCores.storageFor(stack);
            if (typed != null) {
                cir.setReturnValue(typed);
            }
        } catch (Throwable t) {
            Ae2TetraPlus.LOGGER.warn("AE2TetraPlus: 路由存储类型失败", t);
        }
    }
}
