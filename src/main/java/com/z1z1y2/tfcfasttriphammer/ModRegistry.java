package com.z1z1y2.tfcfasttriphammer;

import com.z1z1y2.tfcfasttriphammer.block.CrossBladedAxleBlock;
import com.z1z1y2.tfcfasttriphammer.rotation.CrossBladedAxleBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredRegister;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.rotation.AxleBlock;
import net.dries007.tfc.common.blocks.wood.Wood;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Supplier;

public final class ModRegistry
{
    static final String MOD_ID = "tfcfasttriphammer";

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Map<Wood, Supplier<Block>> CROSS_BLADED_AXLES = new HashMap<>();
    public static Supplier<BlockEntityType<CrossBladedAxleBlockEntity>> CROSS_BLADED_AXLE_BE;
    public static Supplier<CreativeModeTab> TAB;

    private static boolean initialized = false;

    public static void init()
    {
        if (initialized) return;
        initialized = true;

        List<java.util.function.Supplier<Block>> blockSuppliers = new java.util.ArrayList<>();

        for (Wood wood : Wood.VALUES)
        {
            String woodName = wood.getSerializedName();
            String blockId = "cross_bladed_axle/" + woodName;

            ExtendedProperties ep = ExtendedProperties.of(BlockBehaviour.Properties.of()
                .mapColor(wood.woodColor())
                .sound(SoundType.WOOD)
                .strength(3.0F)
                .noOcclusion()
                .requiresCorrectToolForDrops()
            ).noOcclusion()
                .blockEntity(() -> ModRegistry.CROSS_BLADED_AXLE_BE.get());

            Supplier<Block> regBlock = BLOCKS.register(blockId, () ->
            {
                Supplier<? extends AxleBlock> axleSupplier = () -> {
                    try {
                        var axleType = Wood.BlockType.valueOf("AXLE");
                        Block b = wood.getBlock(axleType).get();
                        if (b instanceof AxleBlock ax) return ax;
                    } catch (Exception ignored) {}
                    return null;
                };
                return new CrossBladedAxleBlock(ep, axleSupplier);
            });

            ITEMS.register(blockId, () -> new BlockItem(regBlock.get(), new Item.Properties()));
            blockSuppliers.add(regBlock);
            CROSS_BLADED_AXLES.put(wood, regBlock);
        }

        CROSS_BLADED_AXLE_BE = BLOCK_ENTITIES.register("cross_bladed_axle",
            () -> BlockEntityType.Builder.of(
                CrossBladedAxleBlockEntity::new,
                blockSuppliers.stream().map(java.util.function.Supplier::get).toArray(Block[]::new)
            ).build(null)
        );

        CrossBladedAxleBlock.BLOCK_ENTITY_TYPE = (Supplier) CROSS_BLADED_AXLE_BE;

        TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.tfcfasttriphammer"))
                .icon(() -> {
                    for (Supplier<Block> b : CROSS_BLADED_AXLES.values()) {
                        Block blk = b.get();
                        if (blk != null) return new ItemStack(blk);
                    }
                    return ItemStack.EMPTY;
                })
                .displayItems((params, output) -> {
                    for (Supplier<Block> b : CROSS_BLADED_AXLES.values()) {
                        Block blk = b.get();
                        if (blk != null) output.accept(new ItemStack(blk));
                    }
                })
                .build()
        );
    }
}




