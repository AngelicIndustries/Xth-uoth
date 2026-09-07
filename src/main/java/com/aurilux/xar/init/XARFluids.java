package com.aurilux.xar.init;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import com.aurilux.xar.fluid.FluidIchor;

public class XARFluids {

    public static Fluid ichor;

    public static void init() {
        ichor = new FluidIchor("ichor");

        FluidRegistry.registerFluid(ichor);
    }
}
