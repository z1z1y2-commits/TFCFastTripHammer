package com.z1z1y2.tfcfasttriphammer.mixin;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.dries007.tfc.common.blockentities.rotation.TripHammerBlockEntity;

@Mixin(TripHammerBlockEntity.class)
public interface TripHammerBlockEntityAccessor
{
    @Accessor("cooldownTicks")
    int getCooldownTicks();

    @Accessor("cooldownTicks")
    void setCooldownTicks(int value);

    @Accessor("lastAngle")
    float getLastAngle();

    @Accessor("lastAngle")
    void setLastAngle(float value);
}
