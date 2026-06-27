package com.z1z1y2.tfcfasttriphammer.forge;

import net.dries007.tfc.common.component.forge.ForgeStep;
import net.dries007.tfc.common.component.forge.Forging;
import net.dries007.tfc.common.recipes.AnvilRecipe;

import java.util.List;

/**
 * A solved sequence of ForgeSteps that takes a Forging from its current work to the target.
 * Ported from AnvilHelper's TerraFirmaCraftAnvilSolution for server-side use.
 * @param recipeInfo the recipe info this solution was computed for
 * @param target the target work value
 * @param forgeSteps the ordered array of steps to execute
 * @param forgeIndexes the work value expected before each step is executed
 */
public record AnvilSolution(AnvilRecipeInfo recipeInfo, int target,
                            ForgeStep[] forgeSteps, int[] forgeIndexes) {

    public static final AnvilSolution UNDEFINED =
        new AnvilSolution(null, -1, new ForgeStep[0], new int[0]);

    public AnvilSolution(AnvilRecipeInfo recipeInfo, int target,
                         ForgeStep[] forgeSteps, int[] forgeIndexes) {
        this.recipeInfo = recipeInfo;
        this.target = target;
        this.forgeSteps = forgeSteps;
        this.forgeIndexes = forgeIndexes;
        if (forgeSteps.length != forgeIndexes.length) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Returns the index of the next step to execute for the given forging state,
     * or -1 if no step matches (e.g. the forging has diverged from the solution).
     */
    public int getStepForForging(Forging forging) {
        AnvilRecipe anvilRecipe = forging.getRecipe();
        final int currentTarget = forging.target();
        if (this.recipeInfo == null ||
            this.recipeInfo.anvilRecipe != anvilRecipe ||
            this.target != currentTarget) {
            return -1;
        }
        final int currentWork = forging.work();
        final int len = this.forgeIndexes.length;
        if (currentWork == currentTarget) {
            // Check for fully completed solution
            List<ForgeStep> lastSteps = forging.lastSteps();
            ForgeStep[] last3Steps = this.recipeInfo.last3Steps;
            boolean valid = true;
            for (int i2 = 3; i2 > 0; i2--) {
                if (last3Steps[3 - i2] != null &&
                    last3Steps[3 - i2] != getStep(lastSteps, i2)) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                return len;
            }
        }
        int i = 1;
        if (len > 3) {
            // Strict check for the last 3 steps
            List<ForgeStep> lastSteps = forging.lastSteps();
            ForgeStep[] last3Steps = this.recipeInfo.last3Steps;
            for (; i <= 3; i++) {
                int expectedWork = this.forgeIndexes[len - i];
                if (expectedWork == currentWork) {
                    boolean valid = true;
                    for (int i2 = 3 - i; i2 > 0; i2--) {
                        int lastStepsIndex = 3 - (i + i2);
                        if (last3Steps[lastStepsIndex] != null &&
                            last3Steps[lastStepsIndex] != getStep(lastSteps, i2)) {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) {
                        return len - i;
                    }
                }
            }
        }
        // Loose check for all other steps
        for (; i <= len; i++) {
            int expectedWork = this.forgeIndexes[len - i];
            if (expectedWork == currentWork) {
                return len - i;
            }
        }
        return -1;
    }

    private static ForgeStep getStep(List<ForgeStep> list, int fromEnd) {
        int index = list.size() - fromEnd;
        return index < 0 ? null : list.get(index);
    }
}
