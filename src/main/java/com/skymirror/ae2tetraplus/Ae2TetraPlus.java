package com.skymirror.ae2tetraplus;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AE Tetra Plus：给 AE Tetra 的模块化存储磁盘扩展「存储类型」核心。
 *
 * <p>功能全部通过 Mixin 挂接，启动阶段不注册任何东西。
 */
@Mod(Ae2TetraPlus.MODID)
public class Ae2TetraPlus {

    public static final String MODID = "ae2tetraplus";
    public static final Logger LOGGER = LoggerFactory.getLogger("AE2TetraPlus");

    public Ae2TetraPlus() {
        LOGGER.info("AE Tetra Plus loaded");
    }
}
