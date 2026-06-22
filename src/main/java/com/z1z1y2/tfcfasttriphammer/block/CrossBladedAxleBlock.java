package com.z1z1y2.tfcfasttriphammer.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.rotation.AxleBlock;
import net.dries007.tfc.common.blocks.rotation.BladedAxleBlock;
import com.z1z1y2.tfcfasttriphammer.rotation.CrossBladedAxleBlockEntity;

public class CrossBladedAxleBlock extends BladedAxleBlock
{
    public static Supplier<BlockEntityType<CrossBladedAxleBlockEntity>> BLOCK_ENTITY_TYPE;

    public CrossBladedAxleBlock(ExtendedProperties properties, Supplier<? extends AxleBlock> axle)
    {
        super(properties, axle);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        Supplier<BlockEntityType<CrossBladedAxleBlockEntity>> type = BLOCK_ENTITY_TYPE;
        if (type != null && type.get() != null)
        {
            return type.get().create(pos, state);
        }
        return super.newBlockEntity(pos, state);
    }
}
