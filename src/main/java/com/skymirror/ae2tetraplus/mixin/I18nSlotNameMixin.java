package com.skymirror.ae2tetraplus.mixin;

import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 兜底方案：拦截原版 I18n 的翻译入口（srg 名 m_118938_），
 * 只对我们自己添加的几个 Tetra 键返回中文。
 */
@Mixin(value = I18n.class, remap = false)
public class I18nSlotNameMixin {

    @Inject(method = "m_118938_", at = @At("HEAD"), cancellable = true, remap = false)
    private static void ae2tp$localizeKeyType(String key, Object[] args,
                                              CallbackInfoReturnable<String> cir) {
        if (key == null) {
            return;
        }
        if ("tetra.slot.cell/keytype".equals(key)) {
            cir.setReturnValue("存储类型");
            return;
        }
        if (key.startsWith("tetra.variant.cell/keytype") || key.startsWith("tetra/schematic/cell/keytype")) {
            cir.setReturnValue(coreName(key));
            return;
        }
        if (key.contains("cell/storage/empty_core") || key.contains("cell_storage/empty")) {
            cir.setReturnValue("空存储核心");
        }
    }

    private static String coreName(String key) {
        if (key.contains("all")) {
            return "全类型核心";
        }
        if (key.contains("mana")) {
            return "魔力核心";
        }
        if (key.contains("fluid")) {
            return "流体核心";
        }
        if (key.contains("air")) {
            return "空气核心";
        }
        if (key.contains("fe")) {
            return "能量核心";
        }
        return "物品核心";
    }
}
