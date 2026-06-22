package com.z1z1y2.tfcfasttriphammer;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slf4j.Logger;

import com.z1z1y2.tfcfasttriphammer.client.CrossBladedAxleRenderer;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;

@Mod(TFCFastTripHammer.MOD_ID)
public final class TFCFastTripHammer
{
    public static final String MOD_ID = "tfcfasttriphammer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TFCFastTripHammer(IEventBus modEventBus)
    {
        LOGGER.info("{} loaded - Trip Hammer 4x speed (reflection) and Bladed Axle cross-blade model active.", MOD_ID);

        // Register custom BlockEntityRenderer for Bladed Axle (cross-shaped blades)
        modEventBus.addListener(this::onRegisterRenderers);
    }

    private void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(TFCBlockEntities.BLADED_AXLE.get(), CrossBladedAxleRenderer::new);
        LOGGER.info("Registered cross-blade renderer for Bladed Axle.");
    }
}
