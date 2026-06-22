package com.z1z1y2.tfcfasttriphammer.rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.dries007.tfc.util.rotation.Node;
import net.dries007.tfc.util.rotation.Rotation;

import java.util.EnumSet;

public class SpeedBoostedNode extends Node
{
    private Rotation currentRotation;
    private final float multiplier;

    public SpeedBoostedNode(BlockPos pos, EnumSet<Direction> connections, float multiplier)
    {
        super(pos, connections);
        this.multiplier = multiplier;
    }

    public boolean update(long networkId, Direction from, Rotation rot)
    {
        this.currentRotation = rot;
        return super.update(networkId, from, rot);
    }

    public void setRotationFromOutsideWorld()
    {
        this.currentRotation = Rotation.ofFake();
        super.setRotationFromOutsideWorld();
    }

    public Rotation rotation()
    {
        if (!isConnectedToNetwork() || currentRotation == null) return super.rotation();
        return new SpeedBoostedRotation(currentRotation, multiplier);
    }

    public float angle(float partialTick)
    {
        if (!isConnectedToNetwork() || currentRotation == null) return 0;
        return currentRotation.angle(partialTick);
    }

    public float speed()
    {
        if (!isConnectedToNetwork() || currentRotation == null) return 0;
        return currentRotation.speed();
    }

    public Direction direction()
    {
        if (!isConnectedToNetwork() || currentRotation == null) return Direction.NORTH;
        return currentRotation.direction();
    }

    public Rotation rotation(Rotation value, Direction from, Direction to)
    {
        return value;
    }
}
