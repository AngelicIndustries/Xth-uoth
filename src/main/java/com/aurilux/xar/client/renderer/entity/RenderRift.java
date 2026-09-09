package com.aurilux.xar.client.renderer.entity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.aurilux.xar.Xthuoth;
import com.aurilux.xar.entity.EntityRift;
import com.aurilux.xar.util.ResourceUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderRift extends Render {

    private IIcon portalIcon;
    private final List<RiftStrip> shape = new ArrayList<RiftStrip>();

    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 0) {
            portalIcon = ResourceUtils.getIcon(event.map, "tile.portal");
            loadShape();
        }
    }

    private void loadShape() {
        shape.clear();
        try (InputStream stream = Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(ResourceUtils.RIFT_TEXTURE)
            .getInputStream()) {
            BufferedImage mask = ImageIO.read(stream);
            if (mask == null) throw new IOException("Invalid rift silhouette image");

            // Use the original rift's opaque outline to clip the animated atlas sprite. Each run of visible
            // pixels becomes one strip, retaining resource-pack silhouettes without rendering their static art.
            for (int y = 0; y < mask.getHeight(); y++) {
                int x = 0;
                while (x < mask.getWidth()) {
                    while (x < mask.getWidth() && (mask.getRGB(x, y) >>> 24) == 0) x++;
                    int left = x;
                    while (x < mask.getWidth() && (mask.getRGB(x, y) >>> 24) != 0) x++;
                    if (left < x) {
                        addStrip(
                            left / (double) mask.getWidth(),
                            x / (double) mask.getWidth(),
                            y / (double) mask.getHeight(),
                            (y + 1) / (double) mask.getHeight());
                    }
                }
            }
        } catch (IOException e) {
            Xthuoth.log.warn("Unable to load the rift silhouette; using an oval outline", e);
            for (int y = 0; y < 32; y++) {
                double distance = (y + 0.5D) / 16.0D - 1.0D;
                double halfWidth = Math.sqrt(1.0D - distance * distance) * 0.5D;
                addStrip(0.5D - halfWidth, 0.5D + halfWidth, y / 32.0D, (y + 1) / 32.0D);
            }
        }
    }

    private void addStrip(double left, double right, double top, double bottom) {
        // Keep the two square animation tiles unstretched, including masks with an odd number of rows.
        if (top < 0.5D && bottom > 0.5D) {
            addStrip(left, right, top, 0.5D);
            addStrip(left, right, 0.5D, bottom);
        } else {
            shape.add(new RiftStrip(left, right, top, bottom));
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
        for (RiftStrip strip : shape) {
            double minU = portalIcon.getInterpolatedU(strip.left * 16.0D);
            double maxU = portalIcon.getInterpolatedU(strip.right * 16.0D);
            double tileTop = strip.top < 0.5D ? 0.0D : 0.5D;
            double minV = portalIcon.getInterpolatedV((strip.top - tileTop) * 32.0D);
            double maxV = portalIcon.getInterpolatedV((strip.bottom - tileTop) * 32.0D);
            double left = strip.left - 0.5D;
            double right = strip.right - 0.5D;
            double top = 1.0D - strip.top * 2.0D;
            double bottom = 1.0D - strip.bottom * 2.0D;
            tessellator.addVertexWithUV(left, bottom, 0.0D, minU, maxV);
            tessellator.addVertexWithUV(right, bottom, 0.0D, maxU, maxV);
            tessellator.addVertexWithUV(right, top, 0.0D, maxU, minV);
            tessellator.addVertexWithUV(left, top, 0.0D, minU, minV);
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

    private static class RiftStrip {

        private final double left;
        private final double right;
        private final double top;
        private final double bottom;

        private RiftStrip(double left, double right, double top, double bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }
    }
}
