package com.skymirror.ae2tetraplus.mixin;

import com.skymirror.ae2tetraplus.Lang;
import net.minecraft.client.resources.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 兜底方案：拦截原版 I18n 的翻译入口（srg 名 m_118938_），
 * 只对我们自己添加的几个 Tetra 键返回文字（按客户端语言中/英切换）。
 *
 * <p>为什么用这招：Tetra 的槽位名 / 模块变体名 / 蓝图名有多条取值路径，
 * 逐个补都可能有漏；但它们最终都会走 {@code I18n.get}，在这里拦一次即可全覆盖。
 * 其它键一律原样返回，无副作用。
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
            cir.setReturnValue(Lang.slotStorageType());
            return;
        }
        if (key.startsWith("tetra.variant.cell/keytype") || key.startsWith("tetra/schematic/cell/keytype")) {
            if (key.contains("all")) {
                cir.setReturnValue(Lang.coreAll());
            } else if (key.contains("mana")) {
                cir.setReturnValue(Lang.coreMana());
            } else if (key.contains("fluid")) {
                cir.setReturnValue(Lang.coreFluid());
            } else if (key.contains("air")) {
                cir.setReturnValue(Lang.coreAir());
            } else if (key.contains("fe")) {
                cir.setReturnValue(Lang.coreFe());
            } else {
                cir.setReturnValue(Lang.coreItem());
            }
            return;
        }
        if (key.contains("cell/storage/empty_core") || key.contains("cell_storage/empty")) {
            cir.setReturnValue(Lang.coreEmptyStorage());
        }
    }
}
