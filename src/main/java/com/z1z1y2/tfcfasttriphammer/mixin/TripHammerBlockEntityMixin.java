package com.z1z1y2.tfcfasttriphammer.mixin;

import net.minecraft.core.BlockPos;
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
import net.dries007.tfc.common.component.forge.Forging;
import net.dries007.tfc.common.component.forge.ForgingBonus;
import net.dries007.tfc.common.component.forge.ForgingBonusComponent;
import net.dries007.tfc.common.component.forge.ForgingCapability;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.rotation.Rotation;

import com.z1z1y2.tfcfasttriphammer.Config;
import com.z1z1y2.tfcfasttriphammer.TFCFastTripHammer;
import com.z1z1y2.tfcfasttriphammer.forge.AnvilRecipeInfo;
import com.z1z1y2.tfcfasttriphammer.forge.AnvilSolution;
import com.z1z1y2.tfcfasttriphammer.forge.AnvilSolver;
import com.z1z1y2.tfcfasttriphammer.rotation.CrossBladedAxleBlockEntity;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@Mixin(TripHammerBlockEntity.class)
public abstract class TripHammerBlockEntityMixin
{
    @Shadow(remap = false) private int cooldownTicks;
    @Shadow(remap = false) private float lastAngle;

    @Shadow(remap = false) public abstract Rotation getRotation();
    @Shadow(remap = false) public abstract float getRealRotationDegrees(Rotation rotation, float partialTick);

    private static final float[] TARGET_ANGLES = {0f, 90f, 180f, 270f};
    private static final float ORIGINAL_TARGET = 180f;

    // Cache AnvilRecipeInfo by AnvilRecipe reference to avoid recomputing on every hammer hit
    private static final Map<AnvilRecipe, AnvilRecipeInfo> RECIPE_INFO_CACHE = new WeakHashMap<>();

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

                boolean worked;
                if (Config.autoForge())
                {
                    worked = doAutoForge(level, anvil, stack);
                }
                else
                {
                    worked = anvil.workRemotely(ForgeStep.HIT_LIGHT, 12, true);
                }

                if (worked)
                {
                    Helpers.damageItem(stack, level);
                    hammer.markForSync();
                    anvil.markForSync();
                }
                if (stack.isEmpty())
                {
                    level.playSound(null, pos, SoundEvents.ITEM_BREAK, SoundSource.BLOCKS);
                }
                int cooldown = Mth.ceil(5.0265484f / rotation.positiveSpeed());
                if (isCrossBlade) cooldown = (cooldown + 3) / 4;
                accessor.setCooldownTicks(cooldown);
            }
        }
    }

    /**
     * Auto-forge logic: solve the recipe and execute the correct next ForgeStep.
     * Returns true if the hammer hit was applied successfully.
     * After recipe completion, sets ForgingBonus on the output if the recipe allows it.
     */
    private static boolean doAutoForge(Level level, AnvilBlockEntity anvil, ItemStack hammerStack)
    {
        var inventory = (net.dries007.tfc.common.blockentities.AnvilBlockEntity.AnvilInventory) anvil.getInventory();
        ItemStack inputStack = inventory.getStackInSlot(AnvilBlockEntity.SLOT_INPUT_MAIN);
        if (inputStack.isEmpty()) return false;

        Forging forging = ForgingCapability.get(inputStack);
        if (forging == null) return false;

        // If no recipe is selected, try to auto-match one
        AnvilRecipe recipe = forging.getRecipe();
        if (recipe == null)
        {
            int tier = anvil.getTier();
            List<net.minecraft.world.item.crafting.RecipeHolder<AnvilRecipe>> recipes =
                AnvilRecipe.getAll(level, inputStack, tier);
            if (recipes.isEmpty()) return false;
            net.minecraft.world.item.crafting.RecipeHolder<AnvilRecipe> holder = recipes.get(0);
            forging.setRecipe(holder, inventory);
            recipe = holder.value();
        }

        // Get or cache the recipe info
        AnvilRecipeInfo recipeInfo = RECIPE_INFO_CACHE.get(recipe);
        if (recipeInfo == null)
        {
            recipeInfo = AnvilRecipeInfo.getRecipeInfo(recipe);
            RECIPE_INFO_CACHE.put(recipe, recipeInfo);
        }

        // Solve the step sequence
        AnvilSolution solution = AnvilSolver.solveFor(recipeInfo, forging);
        if (solution == AnvilSolution.UNDEFINED)
        {
            // Can't solve this recipe, fall back to HIT_LIGHT
            return anvil.workRemotely(ForgeStep.HIT_LIGHT, 12, true);
        }

        // Find the next step to execute
        int stepIndex = solution.getStepForForging(forging);
        if (stepIndex < 0 || stepIndex >= solution.forgeSteps().length)
        {
            // No matching step found, fall back to HIT_LIGHT
            return anvil.workRemotely(ForgeStep.HIT_LIGHT, 12, true);
        }

       ForgeStep nextStep = solution.forgeSteps()[stepIndex];
       int force = nextStep.step();

       // Record input before work to detect recipe completion
       ItemStack inputBefore = inputStack.copy();

       // forceToTarget=false: the solver already computes steps that land exactly on target.
       // Using true would clamp/negate forces when work temporarily exceeds target (needed
       // for rule-constrained steps like DRAW_LAST), breaking the solved sequence.
       boolean worked = anvil.workRemotely(nextStep, force, false);

       // Check if the recipe completed (item in slot changed)
        if (worked)
        {
            ItemStack outputNow = inventory.getStackInSlot(AnvilBlockEntity.SLOT_INPUT_MAIN);
            if (!outputNow.isEmpty() && !ItemStack.isSameItemSameComponents(inputBefore, outputNow))
            {
                // Recipe completed — apply ForgingBonus if the recipe allows it
                applyForgingBonusIfNeeded(recipe, outputNow);
                anvil.markForSync();
            }
        }

        return worked;
    }

    /**
     * Sets the configured ForgingBonus on the output stack, but only if the recipe
     * originally supports a forging bonus. Items that can't get a bonus stay without one.
     */
    private static void applyForgingBonusIfNeeded(AnvilRecipe recipe, ItemStack output)
    {
        if (!recipe.shouldApplyForgingBonus()) return;

        int bonusLevel = Config.forgingBonus();
        if (bonusLevel <= 0) return;

        ForgingBonus[] bonuses = ForgingBonus.values();
        if (bonusLevel >= bonuses.length) bonusLevel = bonuses.length - 1;

        ForgingBonus targetBonus = bonuses[bonusLevel];
        if (targetBonus != ForgingBonus.NONE)
        {
            ForgingBonusComponent.set(output, targetBonus);
        }
    }
}
