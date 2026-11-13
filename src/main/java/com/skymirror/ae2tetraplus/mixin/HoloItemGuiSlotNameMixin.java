package com.skymirror.ae2tetraplus.mixin;

import com.skymirror.ae2tetraplus.Ae2TetraPlus;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.holo.gui.craft.HoloItemGui;

/**
 * Tetra 工作台槽位名本地化。
 *
 * <p>带调试日志：如果这条链路没生效，日志里不会出现 [AE2TP]；如果出现了但名字对不上，
 * 日志会打印真实字符串，便于定位。
 */
@Mixin(value = HoloItemGui.class, remap = false)
public abstract class HoloItemGuiSlotNameMixin {

    @Redirect(
            method = "setupSlots",
            at = @At(
                    value = "INVOKE",
                    target = "Lse/mickelus/tetra/items/modular/IModularItem;getMinorModuleNames(Lnet/minecraft/world/item/ItemStack;)[Ljava/lang/String;"
            ),
            remap = false,
            require = 0
    )
    private String[] ae2tp$localizeMinorSlotNames(IModularItem item, ItemStack stack) {
        String[] names = item.getMinorModuleNames(stack);
        if (names == null) {
            Ae2TetraPlus.LOGGER.info("[AE2TP] setupSlots minor names = null");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++) {
            sb.append('[').append(names[i]).append(']');
            if ("tetra.slot.cell/keytype".equals(names[i])) {
                names[i] = "存储类型";
            }
        }
        Ae2TetraPlus.LOGGER.info("[AE2TP] setupSlots minor names = {}", sb);
        return names;
    }
}
