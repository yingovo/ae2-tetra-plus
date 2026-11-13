package com.skymirror.ae2tetraplus.mixin;

import com.skymirror.ae2tetraplus.KeyTypeCores;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.SchematicRegistry;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.ArrayList;
import java.util.List;

/**
 * 按扩展模组是否存在，隐藏对应的存储类型核心选项：
 * <ul>
 *   <li>没装 AppliedBotanics → 隐藏「魔力核心」</li>
 *   <li>没装 AppliedFlux → 隐藏「能量核心」</li>
 *   <li>没装 AppliedPneumatics → 隐藏「空气核心」</li>
 * </ul>
 * 物品/流体核心是 AE2 原生能力，永远保留。
 *
 * <p>目标方法是 static，所以 handler 也必须是 static。
 */
@Mixin(value = SchematicRegistry.class, remap = false)
public abstract class ManaSchematicHideMixin {

    @Inject(
            method = "getSchematics(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)[Lse/mickelus/tetra/module/schematic/UpgradeSchematic;",
            at = @At("RETURN"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private static void ae2tp$hideUnavailableCores(ItemStack stack, String slot,
                                                   CallbackInfoReturnable<UpgradeSchematic[]> cir) {
        try {
            boolean hideMana = KeyTypeCores.manaType() == null;
            boolean hideFe = KeyTypeCores.feType() == null;
            boolean hideAir = KeyTypeCores.airType() == null;
            if (!hideMana && !hideFe && !hideAir) {
                return;
            }
            UpgradeSchematic[] all = cir.getReturnValue();
            if (all == null || all.length == 0) {
                return;
            }
            List<UpgradeSchematic> kept = new ArrayList<>(all.length);
            boolean removed = false;
            for (UpgradeSchematic schematic : all) {
                String key = schematic == null ? null : schematic.getKey();
                if (key != null) {
                    if (hideMana && key.contains("mana")) {
                        removed = true;
                        continue;
                    }
                    if (hideFe && key.contains("fe")) {
                        removed = true;
                        continue;
                    }
                    if (hideAir && key.contains("air")) {
                        removed = true;
                        continue;
                    }
                }
                kept.add(schematic);
            }
            if (removed) {
                cir.setReturnValue(kept.toArray(new UpgradeSchematic[0]));
            }
        } catch (Throwable ignored) {
            // 出错保留原列表
        }
    }
}
