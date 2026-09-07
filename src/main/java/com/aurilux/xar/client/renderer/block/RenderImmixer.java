package com.aurilux.xar.client.renderer.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import com.aurilux.xar.client.model.block.ModelImmixer;
import com.aurilux.xar.handler.ConfigHandler;
import com.aurilux.xar.tileentity.TileEntityImmixer;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderImmixer implements ISimpleBlockRenderingHandler {

    private final int renderId;
    private final ModelImmixer model = new ModelImmixer();

    public RenderImmixer(int renderId) {
        this.renderId = renderId;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
            Tessellator.instance.startDrawingQuads();
            model.render(
                block.getIcon(0, metadata),
                getStoneIcon(),
                0,
                0,
                0,
                false,
                false,
                0,
                ConfigHandler.IMMIXER_3D_BOTTLES);
            Tessellator.instance.draw();
        } finally {
            GL11.glPopMatrix();
        }
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        boolean breaking = renderer.hasOverrideBlockTexture();
        IIcon icon = breaking ? renderer.overrideBlockTexture : block.getIcon(0, world.getBlockMetadata(x, y, z));
        Tessellator.instance.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        TileEntity tile = world.getTileEntity(x, y, z);
        int filledPotionSlots = tile instanceof TileEntityImmixer ? ((TileEntityImmixer) tile).getFilledPotionSlots()
            : 0;
        model.render(
            icon,
            breaking ? icon : getStoneIcon(),
            x,
            y,
            z,
            true,
            breaking,
            filledPotionSlots,
            ConfigHandler.IMMIXER_3D_BOTTLES);
        return true;
    }

    private IIcon getStoneIcon() {
        return ((BlockBrewingStand) Blocks.brewing_stand).getIconBrewingStandBase();
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public int getRenderId() {
        return renderId;
    }
}
