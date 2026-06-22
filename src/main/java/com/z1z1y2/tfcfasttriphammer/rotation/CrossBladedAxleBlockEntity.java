package com.z1z1y2.tfcfasttriphammer.rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.dries007.tfc.common.blockentities.rotation.BladedAxleBlockEntity;
import net.dries007.tfc.util.rotation.Node;
import java.util.EnumSet;

/**
 * Block entity for Cross Bladed Axle. Uses SpeedBoostedNode which now simply
 * passes through the real Rotation. The 4x Trip Hammer frequency is achieved
 * via the TripHammerBlockEntityMixin which detects crossings at 4 angles.
 */
public class CrossBladedAxleBlockEntity extends BladedAxleBlockEntity
{
    private final SpeedBoostedNode boostedNode;

    public CrossBladedAxleBlockEntity(BlockPos pos, BlockState state)
    {
        this((BlockEntityType<?>) null, pos, state);
    }

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
    public Node getRotationNode()
    {
        return boostedNode;
    }
}