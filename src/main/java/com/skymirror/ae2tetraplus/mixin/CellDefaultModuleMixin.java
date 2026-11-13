package com.skymirror.ae2tetraplus.mixin;

import net.minecraft.world.item.ItemStack;
import net.yiran.ae_tetra.ae.TetraModularCellItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.IModularItem;

/**
 * 让磁盘「出厂」就带一个默认的存储类型核心（物品存储核心），
 * 对应 Tetra 的默认实例方法（srg 名 m_7968_ = getDefaultInstance）。
 */
@Mixin(value = TetraModularCellItem.class, remap = false)
public abstract class CellDefaultModuleMixin {

    @Inject(method = "m_7968_", at = @At("RETURN"), remap = false)
    private void ae2tp$addDefaultKeyTypeCore(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = cir.getReturnValue();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        try {
            IModularItem.putModuleInSlot(stack, "cell/keytype", "cell/keytype/item", "cell/keytype/item");
        } catch (Throwable ignored) {
            // 模块不存在时忽略（不影响其它功能）
        }
    }
}
