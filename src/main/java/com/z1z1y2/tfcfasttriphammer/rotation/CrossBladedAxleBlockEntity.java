package com.z1z1y2.tfcfasttriphammer.rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.dries007.tfc.common.blockentities.rotation.BladedAxleBlockEntity;
import net.dries007.tfc.util.rotation.Node;
import java.util.EnumSet;

public class CrossBladedAxleBlockEntity extends BladedAxleBlockEntity
{
    private final SpeedBoostedNode boostedNode;

    // 2-param constructor for BlockEntityType.BlockEntitySupplier
    public CrossBladedAxleBlockEntity(BlockPos pos, BlockState state)
    {
        this((BlockEntityType<?>) null, pos, state);
    }

    // 3-param constructor for internal use
    public CrossBladedAxleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type != null ? type : com.z1z1y2.tfcfasttriphammer.ModRegistry.CROSS_BLADED_AXLE_BE.get(), pos, state);
        Node parentNode = super.getRotationNode();
        this.boostedNode = new SpeedBoostedNode(
            worldPosition,
            EnumSet.copyOf(parentNode.connections()),
            4.0f
        );
    }

    @Override
    public float getRotationAngle(float partialTick)
    {
        // Return REAL angle for renderer (not folded 4x angle from SpeedBoostedRotation)
        return boostedNode.angle(partialTick);
    }
    public Node getRotationNode()
    {
        if (level != null && level.isClientSide)
        {
            boostedNode.setFolding(false);
        }
        return boostedNode;
    }
}





