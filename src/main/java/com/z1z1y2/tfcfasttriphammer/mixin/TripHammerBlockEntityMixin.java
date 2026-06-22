package com.z1z1y2.tfcfasttriphammer.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.blockentities.rotation.TripHammerBlockEntity;
import net.dries007.tfc.common.blocks.TripHammerBlock;
import net.dries007.tfc.common.blocks.devices.AnvilBlock;
import net.dries007.tfc.common.component.forge.ForgeStep;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.rotation.Rotation;

import com.z1z1y2.tfcfasttriphammer.rotation.CrossBladedAxleBlockEntity;

@Mixin(TripHammerBlockEntity.class)
public abstract class TripHammerBlockEntityMixin
{
    @Shadow(remap = false) private int cooldownTicks;
    @Shadow(remap = false) private float lastAngle;

    @Shadow(remap = false) public abstract Rotation getRotation();
    @Shadow(remap = false) public abstract float getRealRotationDegrees(Rotation rotation, float partialTick);

    private static final float[] TARGET_ANGLES = {0f, 90f, 180f, 270f};
    private static final float ORIGINAL_TARGET = 180f;

    /**
     * Replaces the original serverTick to detect blade crossings at 4 angles (0, 90, 180, 270)
     * when a Cross Bladed Axle is above, achieving 4x work frequency.
     * Cooldown is also divided by 4 for cross blades to allow 4 triggers per full rotation.
     * @author z1z1y2
     * @reason Enable 4x Trip Hammer work frequency with Cross Bladed Axle
     */
    @Overwrite(remap = false)
    public static void serverTick(Level level, BlockPos pos, BlockState state, TripHammerBlockEntity hammer)
    {
        @SuppressWarnings("unchecked")
        var accessor = (TripHammerBlockEntityAccessor) (Object) hammer;

        accessor.setCooldownTicks(accessor.getCooldownTicks() - 1);
        int newCooldown = accessor.getCooldownTicks();

        Rotation rotation = hammer.getRotation();
        if (rotation == null)
        {
            accessor.setLastAngle(Float.NEGATIVE_INFINITY);
            return;
        }

        float angle = hammer.getRealRotationDegrees(rotation, 1.0f);
        ItemStack stack = ((ItemStackHandler) hammer.getInventory()).getStackInSlot(0);

        if (newCooldown > 0 || stack.isEmpty())
        {
            accessor.setLastAngle(angle);
            return;
        }

        float lastAngle = accessor.getLastAngle();

        boolean isCrossBlade = isCrossBladedAxle(level, pos);
        float[] targets = isCrossBlade ? TARGET_ANGLES : new float[]{ORIGINAL_TARGET};

        for (float target : targets)
        {
            if (crossesTarget(angle, lastAngle, target))
            {
                triggerWork(level, pos, state, hammer, rotation, stack, accessor, isCrossBlade);
                accessor.setLastAngle(angle);
                hammer.checkForLastTickSync();
                return;
            }
        }

        accessor.setLastAngle(angle);
    }

    private static boolean isCrossBladedAxle(Level level, BlockPos pos)
    {
        BlockEntity above = level.getBlockEntity(pos.above());
        return above instanceof CrossBladedAxleBlockEntity;
    }

    private static boolean crossesTarget(float angle, float lastAngle, float target)
    {
        float a = normalizeAngle(angle);
        float l = normalizeAngle(lastAngle);

        float diff = angleDiff(a, l);
        if (diff == 0f) return false;

        if (diff > 0)
        {
            float targetDiff = angleDiff(target, l);
            return targetDiff > 0 && targetDiff <= diff;
        }
        else
        {
            float targetDiff = angleDiff(target, l);
            return targetDiff < 0 && targetDiff >= diff;
        }
    }

    private static float angleDiff(float b, float a)
    {
        float diff = b - a;
        while (diff > 180f) diff -= 360f;
        while (diff <= -180f) diff += 360f;
        return diff;
    }

    private static float normalizeAngle(float angle)
    {
        angle %= 360f;
        if (angle < 0f) angle += 360f;
        return angle;
    }

    private static void triggerWork(Level level, BlockPos pos, BlockState state,
                                     TripHammerBlockEntity hammer, Rotation rotation,
                                     ItemStack stack, TripHammerBlockEntityAccessor accessor,
                                     boolean isCrossBlade)
    {
        boolean isPositive = rotation.positiveDirection() == state.getValue(TripHammerBlock.FACING).getClockWise();

        if (!isPositive)
        {
            ItemStackHandler inventory = (ItemStackHandler) hammer.getInventory();
            ItemStack extracted = inventory.extractItem(0, 1, false);
            if (extracted.isDamageableItem())
            {
                extracted.hurtAndBreak(extracted.getMaxDamage() / 4 + 1,
                    (ServerLevel) level, null, item -> {});
            }
            if (!extracted.isEmpty())
            {
                Helpers.spawnItem(level, pos, extracted);
            }
            else
            {
                level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS);
            }
            level.playSound(null, pos, SoundEvents.VAULT_BREAK, SoundSource.BLOCKS);
        }
        else
        {
            BlockPos anvilPos = pos.relative(state.getValue(TripHammerBlock.FACING));
            BlockEntity be = level.getBlockEntity(anvilPos);
            if (be instanceof AnvilBlockEntity anvil
                && level.getBlockState(anvilPos).getBlock() instanceof AnvilBlock)
            {
                level.playSound(null, pos, TFCSounds.ANVIL_HIT.get(),
                    SoundSource.BLOCKS, 0.4f, 0.2f);
                if (anvil.workRemotely(ForgeStep.HIT_LIGHT, 12, true))
                {
                    Helpers.damageItem(stack, level);
                    hammer.markForSync();
                    anvil.markForSync();
                }
                if (stack.isEmpty())
                {
                    level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS);
                }
                // For cross blades, divide cooldown by 4 so all 4 targets can trigger per rotation
                int cooldown = Mth.ceil(5.0265484f / rotation.positiveSpeed());
                if (isCrossBlade) cooldown = (cooldown + 3) / 4; // ceiling division by 4
                accessor.setCooldownTicks(cooldown);
            }
        }
    }
}