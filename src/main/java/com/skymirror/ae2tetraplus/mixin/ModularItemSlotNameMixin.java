package com.skymirror.ae2tetraplus.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.IModularItem;

/**
 * 把 {@code getMinorModuleNames / getMajorModuleNames} 返回值里的
 * {@code tetra.slot.cell/keytype} 换成「存储类型」。
 *
 * <p>说明：Tetra 自己用 {@code I18n.get("tetra.slot." + key)} 取名，取不到就原样返回 key；
 * 本模组的语言键在部分整合包里读不到，所以在方法层面兜底替换（见 {@code I18nSlotNameMixin}
 * 与工作台 GUI 的补丁，三处共同保证显示为中文）。
 *
 * <p>接口 mixin 不能带普通字段，字符串全部内联。
 */
@Mixin(value = IModularItem.class, remap = false)
public interface ModularItemSlotNameMixin {

    @Inject(method = "getMinorModuleNames", at = @At("RETURN"), cancellable = true, remap = false)
    default void ae2tp$localizeMinorSlotNames(ItemStack stack, CallbackInfoReturnable<String[]> cir) {
        String[] names = cir.getReturnValue();
        if (names == null) {
            return;
        }
        boolean changed = false;
        for (int i = 0; i < names.length; i++) {
            if ("tetra.slot.cell/keytype".equals(names[i])) {
                names[i] = "存储类型";
                changed = true;
            }
        }
        if (changed) {
            cir.setReturnValue(names);
        }
    }

    @Inject(method = "getMajorModuleNames", at = @At("RETURN"), cancellable = true, remap = false)
    default void ae2tp$localizeMajorSlotNames(ItemStack stack, CallbackInfoReturnable<String[]> cir) {
        String[] names = cir.getReturnValue();
        if (names == null) {
            return;
        }
        boolean changed = false;
        for (int i = 0; i < names.length; i++) {
            if ("tetra.slot.cell/keytype".equals(names[i])) {
                names[i] = "存储类型";
                changed = true;
            }
        }
        if (changed) {
            cir.setReturnValue(names);
        }
    }
}
