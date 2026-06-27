package com.z1z1y2.tfcfasttriphammer;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config
{
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue AUTO_FORGE;
    private static final ModConfigSpec.IntValue FORGING_BONUS;

    static
    {
        var builder = new ModConfigSpec.Builder();
        builder.push("trip_hammer");

        AUTO_FORGE = builder
            .comment("When true, the Trip Hammer automatically solves and executes the correct",
                "ForgeStep sequence to complete any anvil recipe.",
                "When false, the Trip Hammer keeps its original behavior (only HIT_LIGHT).")
            .define("autoForge", true);

        FORGING_BONUS = builder
            .comment("Target ForgingBonus level for auto-forged products (0=None, 1=Modest,",
                "2=Well, 3=Expert, 4=Perfect). Only applied to recipes that can receive a bonus.",
                "Set to 0 for no bonus.")
            .defineInRange("forgingBonus", 4, 0, 4);

        builder.pop();
        SPEC = builder.build();
    }

    private Config() {}

    public static boolean autoForge()
    {
        return AUTO_FORGE.get();
    }

    public static int forgingBonus()
    {
        return FORGING_BONUS.get();
    }
}
