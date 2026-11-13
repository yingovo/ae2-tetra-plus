package com.skymirror.ae2tetraplus;

/**
 * 简单的双语字符串选择器。
 *
 * <p>本模组的界面文字需要经过 Tetra 的 {@code I18n.get}（返回 String，不能用翻译组件），
 * 而语言文件在部分整合包里读不到，所以在代码里直接按客户端语言挑一份。
 */
public final class Lang {

    private Lang() {
    }

    /** 客户端语言以 zh 开头时返回中文，否则返回英文。 */
    public static String pick(String zh, String en) {
        try {
            if (!net.minecraftforge.fml.loading.FMLLoader.getDist().isClient()) {
                return en;
            }
            String code = net.minecraft.client.Minecraft.getInstance()
                    .getLanguageManager().getSelected();
            return code != null && code.toLowerCase(java.util.Locale.ROOT).startsWith("zh") ? zh : en;
        } catch (Throwable ignored) {
            return zh;
        }
    }

    public static String slotStorageType() {
        return pick("存储类型", "Storage Type");
    }

    public static String coreItem() {
        return pick("物品核心", "Item Core");
    }

    public static String coreFluid() {
        return pick("流体核心", "Fluid Core");
    }

    public static String coreMana() {
        return pick("魔力核心", "Mana Core");
    }

    public static String coreFe() {
        return pick("能量核心", "FE Core");
    }

    public static String coreAir() {
        return pick("空气核心", "Air Core");
    }

    public static String coreAll() {
        return pick("全类型核心", "Universal Core");
    }

    public static String coreEmptyStorage() {
        return pick("空存储核心", "Empty Storage Core");
    }
}
