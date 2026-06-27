package com.z1z1y2.tfcfasttriphammer.forge;

import net.dries007.tfc.common.component.forge.ForgeRule;
import net.dries007.tfc.common.component.forge.ForgeStep;
import net.dries007.tfc.common.recipes.AnvilRecipe;

import java.util.HashSet;

/**
 * Extracts the last-3-step constraints from an AnvilRecipe's ForgeRule list.
 * Ported from AnvilHelper's TerraFirmaCraftAnvilRecipeInfo (client-side) for server-side use.
 */
public final class AnvilRecipeInfo {
    private static final ForgeStep[] LAST3STEPS_INVALID = new ForgeStep[0];
    public final AnvilRecipe anvilRecipe;
    public final ForgeStep[] last3Steps;
    public final boolean invalid;

    private AnvilRecipeInfo(AnvilRecipe anvilRecipe, ForgeStep[] last3Steps, boolean invalid) {
        this.anvilRecipe = anvilRecipe;
        this.last3Steps = last3Steps;
        this.invalid = invalid;
    }

    public static AnvilRecipeInfo getRecipeInfo(AnvilRecipe anvilRecipe) {
        ForgeStep[] last3Steps = new ForgeStep[3];
        HashSet<ForgeStep> hitAny = new HashSet<>();
        HashSet<ForgeStep> hitNotLast = new HashSet<>();
        for (ForgeRule rule : anvilRecipe.getRules()) {
            ForgeStep forgeStep = getForgeStep(rule);
            switch (getOrder(rule)) {
                case ANY: hitAny.add(forgeStep); break;
                case LAST: last3Steps[2] = forgeStep; break;
                case NOT_LAST: hitNotLast.add(forgeStep); break;
                case THIRD_LAST: last3Steps[0] = forgeStep; break;
                case SECOND_LAST: last3Steps[1] = forgeStep; break;
            }
        }
        // Remove steps already assigned to specific positions from the ANY set
        for (ForgeStep forgeStep : last3Steps) {
            if (forgeStep != null) {
                hitAny.remove(forgeStep);
            }
        }
        // Fill remaining positions with ANY steps
        for (ForgeStep forgeStep : hitAny) {
            for (int i = 2; i >= 0; i--) {
                if (last3Steps[i] == null) {
                    last3Steps[i] = forgeStep;
                    break;
                }
                if (i == 0) {
                    return makeInvalid(anvilRecipe);
                }
            }
        }
        // Handle NOT_LAST rules: fill into second-last or third-last
        hitNotLast.remove(last3Steps[0]);
        hitNotLast.remove(last3Steps[1]);
        for (ForgeStep forgeStep : hitNotLast) {
            if (forgeStep != null &&
                    forgeStep != last3Steps[0] &&
                    forgeStep != last3Steps[1]) {
                if (last3Steps[1] == null) {
                    last3Steps[1] = forgeStep;
                } else if (last3Steps[0] == null) {
                    last3Steps[0] = forgeStep;
                } else {
                    return makeInvalid(anvilRecipe);
                }
            }
        }
        return new AnvilRecipeInfo(anvilRecipe, last3Steps, false);
    }

    private static AnvilRecipeInfo makeInvalid(AnvilRecipe anvilRecipe) {
        return new AnvilRecipeInfo(anvilRecipe, LAST3STEPS_INVALID, true);
    }

    /**
     * Determines the ForgeStep type from a ForgeRule's name prefix.
     * HIT_* -> HIT_LIGHT, DRAW_* -> DRAW, PUNCH_* -> PUNCH, etc.
     */
    static ForgeStep getForgeStep(ForgeRule rule) {
        String name = rule.name();
        if (name.startsWith("HIT_")) return ForgeStep.HIT_LIGHT;
        if (name.startsWith("DRAW_")) return ForgeStep.DRAW;
        if (name.startsWith("PUNCH_")) return ForgeStep.PUNCH;
        if (name.startsWith("BEND_")) return ForgeStep.BEND;
        if (name.startsWith("UPSET_")) return ForgeStep.UPSET;
        if (name.startsWith("SHRINK_")) return ForgeStep.SHRINK;
        throw new UnsupportedOperationException(name);
    }

    /**
     * Determines the Order from a ForgeRule's name suffix.
     * _ANY -> ANY, _LAST -> LAST, _NOT_LAST -> NOT_LAST, etc.
     */
    static Order getOrder(ForgeRule rule) {
        String name = rule.name();
        if (name.endsWith("_ANY")) return Order.ANY;
        if (name.endsWith("_NOT_LAST")) return Order.NOT_LAST;
        if (name.endsWith("_SECOND_LAST")) return Order.SECOND_LAST;
        if (name.endsWith("_THIRD_LAST")) return Order.THIRD_LAST;
        if (name.endsWith("_LAST")) return Order.LAST;
        throw new UnsupportedOperationException(name);
    }

    enum Order {
        ANY,
        LAST,
        NOT_LAST,
        SECOND_LAST,
        THIRD_LAST
    }
}
