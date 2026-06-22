package com.z1z1y2.tfcfasttriphammer.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.render.blockentity.AxleBlockEntityRenderer;
import net.dries007.tfc.common.blockentities.rotation.BladedAxleBlockEntity;
import net.dries007.tfc.common.blocks.rotation.BladedAxleBlock;
import net.dries007.tfc.util.Helpers;

/**
 * Renders the Bladed Axle with 4 symmetric blades forming a + cross shape.
 * The original renderer renders 1 blade in +Y; we render 4 blades (+Y,-Y,+X,-X)
 * which appear as a symmetric cross after applyRotation().
 */
public class CrossBladedAxleRenderer implements BlockEntityRenderer<BladedAxleBlockEntity>
{
    private static final ResourceLocation BLADE_TEXTURE = Helpers.identifier("block/metal/block/steel");

    public CrossBladedAxleRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public void render(BladedAxleBlockEntity axle, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight, int packedOverlay)
    {
        BlockState state = axle.getBlockState();
        Level level = axle.getLevel();

        if (!(state.getBlock() instanceof BladedAxleBlock axleBlock) || level == null) return;

        Direction.Axis axis = state.getValue(BladedAxleBlock.AXIS);
        float rotationAngle = -axle.getRotationAngle(partialTick);

        // Render the axle shaft
        AxleBlockEntityRenderer.renderAxle(stack, bufferSource, axleBlock, axis, packedLight, packedOverlay, rotationAngle);

        // Render 4 symmetric cross-shaped blades
        renderCrossBlades(stack, bufferSource, axis, packedLight, packedOverlay, rotationAngle);
    }

    private static void renderCrossBlades(PoseStack stack, MultiBufferSource bufferSource, Direction.Axis axis, int packedLight, int packedOverlay, float rotationAngle)
    {
        TextureAtlasSprite sprite = RenderHelpers.blockTexture(BLADE_TEXTURE);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());

        stack.pushPose();
        AxleBlockEntityRenderer.applyRotation(stack, axis, rotationAngle);

        // Original blade (+Y direction)
        RenderHelpers.renderTexturedCuboid(stack, buffer, sprite, packedLight, packedOverlay,
            7f/16f, 10f/16f, 6f/16f, 9f/16f, 17.5f/16f, 10f/16f, false);

        // Mirror blade (-Y direction)
        RenderHelpers.renderTexturedCuboid(stack, buffer, sprite, packedLight, packedOverlay,
            7f/16f, -1.5f/16f, 6f/16f, 9f/16f, 6f/16f, 10f/16f, false);

        // Cross blade (+X direction)
        RenderHelpers.renderTexturedCuboid(stack, buffer, sprite, packedLight, packedOverlay,
            10f/16f, 7f/16f, 6f/16f, 17.5f/16f, 9f/16f, 10f/16f, false);

        // Cross mirror blade (-X direction)
        RenderHelpers.renderTexturedCuboid(stack, buffer, sprite, packedLight, packedOverlay,
            -1.5f/16f, 7f/16f, 6f/16f, 6f/16f, 9f/16f, 10f/16f, false);

        stack.popPose();
    }
}
