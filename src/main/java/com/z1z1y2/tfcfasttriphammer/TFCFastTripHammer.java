package com.z1z1y2.tfcfasttriphammer;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slf4j.Logger;

import com.z1z1y2.tfcfasttriphammer.client.CrossBladedAxleRenderer;

@Mod(TFCFastTripHammer.MOD_ID)
public final class TFCFastTripHammer
{
    public static final String MOD_ID = "tfcfasttriphammer";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TFCFastTripHammer(IEventBus modEventBus)
    {
        // Initialize all registries
        ModRegistry.init();
        ModRegistry.BLOCKS.register(modEventBus);
        ModRegistry.ITEMS.register(modEventBus);
        ModRegistry.BLOCK_ENTITIES.register(modEventBus);
        ModRegistry.CREATIVE_TABS.register(modEventBus);

        // Register renderer event
        modEventBus.addListener(this::onRegisterRenderers);

        LOGGER.info("{} loaded.", MOD_ID);
    }

    private void onRegisterRenderers(final EntityRenderersEvent.RegisterRenderers event)
    {
        if (ModRegistry.CROSS_BLADED_AXLE_BE != null && ModRegistry.CROSS_BLADED_AXLE_BE.get() != null)
        {
            event.registerBlockEntityRenderer(ModRegistry.CROSS_BLADED_AXLE_BE.get(), CrossBladedAxleRenderer::new);
            LOGGER.info("Registered cross-blade renderer.");
        }
    }
}
