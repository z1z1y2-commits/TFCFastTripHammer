package com.z1z1y2.tfcfasttriphammer.api;

import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.blockentities.rotation.TripHammerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.dries007.tfc.util.rotation.Rotation;

/** Immutable context for one positive-direction trip-hammer hit. */
public record TripHammerContext(Level level, BlockPos hammerPos, BlockState hammerState,
                                TripHammerBlockEntity hammer, Rotation rotation,
                                AnvilBlockEntity anvil, ItemStack input, boolean crossBladedAxle) {
    public TripHammerContext {
        if (level == null || hammerPos == null || hammerState == null || hammer == null
            || rotation == null || anvil == null || input == null) {
            throw new IllegalArgumentException("Trip hammer context cannot contain null values");
        }
        // Controllers are observers/decision makers. Keep the live inventory
        // stack owned by FastTripHammer so an API consumer cannot mutate it
        // outside the normal anvil work path.
        input = input.copy();
    }
}
