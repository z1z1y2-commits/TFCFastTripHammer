package com.z1z1y2.tfcfasttriphammer.rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;

import java.util.EnumSet;

/**
 * A simple Node subclass that passes through the real Rotation without any folding or speed boosting.
 * The 4x detection frequency is now handled by the TripHammerBlockEntityMixin which checks
 * 4 crossing angles (0, 90, 180, 270) directly in serverTick.
 */
public class SpeedBoostedNode extends Node
{
    public SpeedBoostedNode(BlockPos pos, EnumSet<Direction> connections, float multiplier)
    {
        super(pos, connections);
    }

    @Override
    public Rotation rotation(Rotation value, Direction from, Direction to)
    {
        return value;
    }
}