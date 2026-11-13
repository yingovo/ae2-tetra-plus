package com.skymirror.ae2tetraplus.mixin;

import net.minecraft.world.item.ItemStack;
import net.yiran.ae_tetra.ae.TetraModularCellItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.ModularItem;

import java.util.Arrays;

/**
 * AE Tetra 模块化磁盘的槽位调整（只对本模组的物品生效）：
 *
 * <ol>
 *   <li>追加 {@code cell/keytype} 槽（存储类型核心，必需）；</li>
 *   <li>把 {@code cell/keytype} 设为必需（不显示「移除组件」）。</li>
 * </ol>
 */
@Mixin(value = ModularItem.class, remap = false)
public abstract class ModularCellSlotsMixin {

    private static final String KEYTYPE_SLOT = "cell/keytype";


    @Inject(method = "getMinorModuleKeys", at = @At("RETURN"), cancellable = true, remap = false)
    private void ae2tp$appendExtraSlots(ItemStack stack, CallbackInfoReturnable<String[]> cir) {
        if (!((Object) this instanceof TetraModularCellItem)) {
            return;
        }
        String[] original = cir.getReturnValue();
        if (original == null) {
            original = new String[0];
        }
        String[] next = ae2tp$appendIfMissing(original, KEYTYPE_SLOT);
        if (next != original) {
            cir.setReturnValue(next);
        }
    }

    @Inject(method = "getRequiredModules", at = @At("RETURN"), cancellable = true, remap = false)
    private void ae2tp$requireKeyTypeSlot(ItemStack stack, CallbackInfoReturnable<String[]> cir) {
        if (!((Object) this instanceof TetraModularCellItem)) {
            return;
        }
        String[] original = cir.getReturnValue();
        if (original == null) {
            original = new String[0];
        }
        String[] next = ae2tp$appendIfMissing(original, KEYTYPE_SLOT);
        if (next != original) {
            cir.setReturnValue(next);
        }
    }

    private static String[] ae2tp$appendIfMissing(String[] array, String slot) {
        for (String key : array) {
            if (slot.equals(key)) {
                return array;
            }
        }
        String[] next = Arrays.copyOf(array, array.length + 1);
        next[next.length - 1] = slot;
        return next;
    }
}
