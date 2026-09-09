package com.aurilux.xar.client.renderer.entity;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.aurilux.xar.entity.EntityRift;
import com.aurilux.xar.util.ResourceUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRift extends Render {

    private IIcon portalIcon;

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 0) {
            portalIcon = ResourceUtils.getIcon(event.map, "tile.portal");
        }
    }

    @Override
    public void doRender(Entity entity, double xPos, double yPos, double zPos, float width, float height) {
        this.doRenderRift((EntityRift) entity, xPos, yPos, zPos, width, height);
    }

    public void doRenderRift(EntityRift rift, double xPos, double yPos, double zPos, float width, float height) {
        GL11.glPushMatrix();
        GL11.glTranslatef((float) xPos, (float) yPos + 1, (float) zPos);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        this.bindEntityTexture(rift);

        // rotates the entity to always face the player across the y-axis
        // the first parameter determines the value(amount) of the rotation, while the other three are the value's
        // effect on the axis (x, y, and z respectively)
        GL11.glRotatef(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setNormal(0.0F, 0.0F, 1.0F);
        // Tile the square animation twice across the one-by-two-block rift without stretching it.
        for (int y = -1; y < 1; y++) {
            tessellator.addVertexWithUV(-0.5D, y, 0.0D, portalIcon.getMinU(), portalIcon.getMaxV());
            tessellator.addVertexWithUV(0.5D, y, 0.0D, portalIcon.getMaxU(), portalIcon.getMaxV());
            tessellator.addVertexWithUV(0.5D, y + 1, 0.0D, portalIcon.getMaxU(), portalIcon.getMinV());
            tessellator.addVertexWithUV(-0.5D, y + 1, 0.0D, portalIcon.getMinU(), portalIcon.getMinV());
        }
        tessellator.draw();
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity) {
        return TextureMap.locationBlocksTexture;
    }
}
