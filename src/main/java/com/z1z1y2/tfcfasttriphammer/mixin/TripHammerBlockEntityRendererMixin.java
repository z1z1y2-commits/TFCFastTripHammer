package com.z1z1y2.tfcfasttriphammer.mixin;

import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.dries007.tfc.client.render.blockentity.TripHammerBlockEntityRenderer;

@Mixin(TripHammerBlockEntityRenderer.class)
public abstract class TripHammerBlockEntityRendererMixin
{
    private static final float[] CROSS_TARGETS = {0f, 90f, 180f, 270f};

    /**
     * Overwrites getPivotAngle to check 4 target angles (0, 90, 180, 270) instead of just one.
     * This makes the Trip Hammer animation swing 4 times per rotation, matching the 4x detection
     * in the serverTick mixin. For regular Bladed Axles, the hammer will visually swing near
     * all 4 angles, but work only triggers at 180 (original behavior preserved in serverTick).
     * @author z1z1y2
     * @reason Sync Trip Hammer animation with 4x work frequency for Cross Bladed Axle
     */
    @Overwrite(remap = false)
    private static float getPivotAngle(boolean isNegative, float angle,
                                        float windowSize, float target, float narrowOffset)
    {
        // Check all 4 target angles, return the first non-zero result
        for (float t : CROSS_TARGETS)
        {
            float result = computePivotAngle(isNegative, angle, windowSize, t, narrowOffset);
            if (result != 0f) return result;
        }
        return 0f;
    }

    /**
     * Faithful reproduction of the original getPivotAngle logic for a single target angle.
     */
    private static float computePivotAngle(boolean isNegative, float angle,
                                           float windowSize, float target, float narrowOffset)
    {
        // Rotation.angle() is normalized to [0, 360), so target 0 crosses the
        // wrap boundary. Compare the shortest signed distance instead of using
        // raw intervals, which would only render the 0..narrowOffset tail.
        float relative = angle - target;
        while (relative >= 180f) relative -= 360f;
        while (relative < -180f) relative += 360f;

        if (isNegative)
        {
            if (relative > 0f && relative < windowSize)
            {
                return 45f - Mth.map(relative, 0f, windowSize, 0f, 45f);
            }
            if (relative > -narrowOffset && relative < 0f)
            {
                return Mth.map(relative, -narrowOffset, 0f, 0f, 45f);
            }
        }
        else
        {
            if (relative > -windowSize && relative < 0f)
            {
                return Mth.map(relative, -windowSize, 0f, 0f, 45f);
            }
            if (relative > 0f && relative < narrowOffset)
            {
                return 45f - Mth.map(relative, 0f, narrowOffset, 0f, 45f);
            }
        }
        return 0f;
    }
}
