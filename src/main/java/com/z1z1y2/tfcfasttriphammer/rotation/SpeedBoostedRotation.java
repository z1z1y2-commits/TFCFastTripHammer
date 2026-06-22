package com.z1z1y2.tfcfasttriphammer.rotation;

import net.minecraft.core.Direction;
import net.dries007.tfc.util.rotation.Rotation;

public record SpeedBoostedRotation(Rotation delegate, float multiplier) implements Rotation
{
    private static final float TWO_PI = (float) (2 * Math.PI);

    @Override
    public float angle(float partialTick)
    {
        // Fold angle 4 times per rotation ??180? crossing fires 4x
        float original = delegate.angle(partialTick);
        float normalized = original / TWO_PI;
        float folded = (normalized * multiplier) % 1.0f;
        return folded * TWO_PI + 0.001f;
    }

    @Override
    public float speed()
    {
        return delegate.speed(); // NO boost - original cooldown
    }

    @Override
    public Direction direction()
    {
        return delegate.direction();
    }
}

