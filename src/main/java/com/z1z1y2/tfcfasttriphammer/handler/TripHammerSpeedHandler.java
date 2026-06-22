package com.z1z1y2.tfcfasttriphammer.handler;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;

import net.dries007.tfc.common.blockentities.rotation.TripHammerBlockEntity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

@EventBusSubscriber(modid = "tfcfasttriphammer")
public final class TripHammerSpeedHandler
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Field cooldownField;
    private static Method getChunksMethod;
    private static boolean initialized = false;

    @SuppressWarnings("unchecked")
    private static void initialize()
    {
        if (initialized) return;
        initialized = true;
        try
        {
            cooldownField = TripHammerBlockEntity.class.getDeclaredField("cooldownTicks");
            cooldownField.setAccessible(true);
            // Use reflection because NeoForge-patched methods are not on compile classpath
            Class<?> cacheClass = Class.forName("net.minecraft.server.level.ServerChunkCache");
            getChunksMethod = cacheClass.getMethod("getLoadedChunks");
            LOGGER.info("Trip Hammer speed hack initialized.");
        }
        catch (Exception e)
        {
            LOGGER.error("Init failed: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event)
    {
        if (!initialized) initialize();
        if (cooldownField == null || getChunksMethod == null) return;
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) return;

        try
        {
            List<LevelChunk> chunks = (List<LevelChunk>) getChunksMethod.invoke(serverLevel.getChunkSource());
            for (LevelChunk chunk : chunks)
            {
                for (BlockEntity be : chunk.getBlockEntities().values())
                {
                    if (be instanceof TripHammerBlockEntity hammer)
                    {
                        int cooldown = cooldownField.getInt(hammer);
                        if (cooldown > 0)
                        {
                            cooldownField.setInt(hammer, Math.max(0, cooldown / 4));
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Tick handler error: {}", e.getMessage());
        }
    }
}
