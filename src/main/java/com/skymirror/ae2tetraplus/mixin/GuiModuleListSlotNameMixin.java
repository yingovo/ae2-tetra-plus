package com.skymirror.ae2tetraplus.mixin;

import com.skymirror.ae2tetraplus.Lang;

import com.skymirror.ae2tetraplus.Ae2TetraPlus;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import se.mickelus.tetra.blocks.workbench.gui.GuiModuleList;
import se.mickelus.tetra.items.modular.IModularItem;

/**
 * 世界里加工台方块的模块列表（{@code GuiModuleList}）也取槽位名，
 * 这里同样把 {@code tetra.slot.cell/keytype} 换成「存储类型」，并打日志确认命中。
 */
@Mixin(value = GuiModuleList.class, remap = false)
public abstract class GuiModuleListSlotNameMixin {

    @Redirect(
            method = "updateMajorModules",
            at = @At(
                    value = "INVOKE",
                    target = "Lse/mickelus/tetra/items/modular/IModularItem;getMajorModuleNames(Lnet/minecraft/world/item/ItemStack;)[Ljava/lang/String;"
            ),
            remap = false,
            require = 0
    )
    private String[] ae2tp$majorNames(IModularItem item, ItemStack stack) {
        String[] names = item.getMajorModuleNames(stack);
        StringBuilder sb = new StringBuilder();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                sb.append('[').append(names[i]).append(']');
                if ("tetra.slot.cell/keytype".equals(names[i])) {
                    names[i] = Lang.slotStorageType();
                }
            }
        }
        Ae2TetraPlus.LOGGER.info("[AE2TP] GuiModuleList majorNames={}", sb);
        return names;
    }

    @Redirect(
            method = "updateMajorModules",
            at = @At(
                    value = "INVOKE",
                    target = "Lse/mickelus/tetra/items/modular/IModularItem;getMinorModuleNames(Lnet/minecraft/world/item/ItemStack;)[Ljava/lang/String;"
            ),
            remap = false,
            require = 0
    )
    private String[] ae2tp$minorNames(IModularItem item, ItemStack stack) {
        String[] names = item.getMinorModuleNames(stack);
        StringBuilder sb = new StringBuilder();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                sb.append('[').append(names[i]).append(']');
                if ("tetra.slot.cell/keytype".equals(names[i])) {
                    names[i] = Lang.slotStorageType();
                }
            }
        }
        Ae2TetraPlus.LOGGER.info("[AE2TP] GuiModuleList minorNames={}", sb);
        return names;
    }
}
