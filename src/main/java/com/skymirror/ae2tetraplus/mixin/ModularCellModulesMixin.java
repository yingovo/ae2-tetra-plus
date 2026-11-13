package com.skymirror.ae2tetraplus.mixin;

import net.minecraft.world.item.ItemStack;
import net.yiran.ae_tetra.ae.TetraModularCellItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;

import java.util.Arrays;

/**
 * 给磁盘加槽后，必须让 {@code getMinorModules} 的长度和 {@code getMinorModuleNames/Keys} 对齐，
 * 否则 Tetra 的工作台列表会越界崩溃（GuiModuleList 用 minorModules.length 分配数组、
 * 却按 names.length 循环）。
 *
 * <p>注意：{@code getMinorModules} 是 {@link IModularItem} 的默认方法（不在 ModularItem 里），
 * 所以这里对接口做 mixin，handler 必须是 default 且不能带字段。
 */
@Mixin(value = IModularItem.class, remap = false)
public interface ModularCellModulesMixin {

    @Inject(method = "getMinorModules", at = @At("RETURN"), cancellable = true, remap = false)
    default void ae2tp$alignMinorModules(ItemStack stack, CallbackInfoReturnable<ItemModule[]> cir) {
        if (!(this instanceof TetraModularCellItem) || stack == null) {
            return;
        }
        try {
            ItemModule[] modules = cir.getReturnValue();
            String[] keys = ((IModularItem) this).getMinorModuleKeys(stack);
            if (keys == null) {
                return;
            }
            if (modules != null && modules.length == keys.length) {
                return;
            }
            ItemModule[] next = Arrays.copyOf(modules == null ? new ItemModule[0] : modules, keys.length);
            cir.setReturnValue(next);
        } catch (Throwable ignored) {
            // 出错保持原值
        }
    }
}
