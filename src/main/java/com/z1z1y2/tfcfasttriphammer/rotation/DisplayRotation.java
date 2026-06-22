package com.z1z1y2.tfcfasttriphammer.rotation;

import net.minecraft.core.Direction;
import net.dries007.tfc.util.rotation.Rotation;

public record DisplayRotation(Rotation delegate, float multiplier) implements Rotation
{
    private static final float TWO_PI = (float) (2 * Math.PI);

    @Override
    public float angle(float partialTick)
    {
        // Folded 4x angle for Trip Hammer animation synchronization
        float original = delegate.angle(partialTick);
        float normalized = original / TWO_PI;
        float folded = (normalized * multiplier) % 1.0f;
        return folded * TWO_PI + 0.001f;
    }

    @Override
    public float speed()
    {
        // Real speed for Jade display
        return delegate.speed();
    }

    @Override
    public Direction direction()
    {
        return delegate.direction();
    }
}
