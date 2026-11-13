package com.skymirror.ae2tetraplus.mixin;

import net.minecraft.world.item.ItemStack;
import net.yiran.ae_tetra.ae.TetraModularCellItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.gui.GuiModuleOffsets;
import se.mickelus.tetra.items.modular.IModularItem;

/**
 * Tetra 原版 {@code getMinorGuiOffsets} 用固定长度布局表（按槽位数索引），
 * 槽位数超出表长会越界。这里在 HEAD 接管，自己构造坐标。
 *
 * <p>数据格式：每槽 2 个 int（x, y），alignment 由 x &gt; 0 推导。
 * 原版槽位原样保留；新增槽固定在<b>右侧</b>（x = 20），从原版最右槽的 y 下方开始排。
 *
 * <p>接口 mixin 不能有字段，所以数值全部写字面量。
 */
@Mixin(value = IModularItem.class, remap = false)
public interface ModularCellOffsetsMixin {

    @Inject(method = "getMinorGuiOffsets", at = @At("HEAD"), cancellable = true, remap = false)
    default void ae2tp$padMinorOffsets(ItemStack stack, CallbackInfoReturnable<GuiModuleOffsets> cir) {
        if (!(this instanceof TetraModularCellItem) || stack == null) {
            return;
        }
        try {
            int count = ((IModularItem) this).getNumMinorModules(stack);
            GuiModuleOffsets[] table = ae2tp$defaultTable();
            if (table == null || count < table.length) {
                return;
            }
            GuiModuleOffsets base = table[table.length - 1];
            int baseCount = base.size();
            int[] data = new int[count * 2];
            for (int i = 0; i < baseCount && i < count; i++) {
                data[i * 2] = base.getX(i);
                data[i * 2 + 1] = base.getY(i);
            }
            // 原版最右侧槽的 y 作为起点；x 一律用正数，保证排到右边
            int startY = 0;
            int maxX = Integer.MIN_VALUE;
            for (int i = 0; i < baseCount; i++) {
                if (base.getX(i) > maxX) {
                    maxX = base.getX(i);
                    startY = base.getY(i);
                }
            }
            for (int i = baseCount; i < count; i++) {
                data[i * 2] = 20;
                data[i * 2 + 1] = startY + 20 * (i - baseCount + 1);
            }
            cir.setReturnValue(new GuiModuleOffsets(data));
        } catch (Throwable ignored) {
            // 出错交给原版
        }
    }

    private static GuiModuleOffsets[] ae2tp$defaultTable() {
        try {
            java.lang.reflect.Field f = IModularItem.class.getDeclaredField("defaultMinorOffsets");
            f.setAccessible(true);
            Object value = f.get(null);
            if (value instanceof GuiModuleOffsets[] arr) {
                return arr;
            }
        } catch (Throwable ignored) {
            // 取不到就算了
        }
        return null;
    }
}
