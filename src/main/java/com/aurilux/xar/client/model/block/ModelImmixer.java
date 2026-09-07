package com.aurilux.xar.client.model.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Extrudes the column, pipes and flasks from the original Immixer texture, on vanilla brewing-stand stone feet.
 * Geometry is measured in sixteenths of a block.
 */
@SideOnly(Side.CLIENT)
public class ModelImmixer {

    private static final UV METAL = new UV(5, 2, 6, 12);
    private static final UV UPPER_BAR = new UV(3, 0, 13, 2);
    private static final UV FRAME = new UV(6, 2, 7, 3);
    private static final UV GOLD = new UV(7, 2, 9, 12);
    private static final UV GLASS = new UV(1, 11, 4, 12);
    private static final UV FLASK_BODY = new UV(0, 8, 5, 11);
    private static final UV FLASK_SHOULDER = new UV(1, 5, 4, 8);
    // The sprite's pipe bends left on row 4; (3, 4) is green background, not pipe.
    private static final UV PIPE = new UV(3, 2, 4, 4);
    private static final UV FULL_TEXTURE = new UV(0, 0, 16, 16);

    private final List<Box> parts = new ArrayList<>();
    private final List<Box> stoneParts = new ArrayList<>();
    private final List<Box> holderParts = new ArrayList<>();

    public ModelImmixer() {
        // A small central stone support and low metal frame link the six separate bottle feet.
        stoneBox(6, 0, 6, 10, 2, 10);
        box(3, 0.5, 7.5, 13, 1.5, 8.5, FRAME, FRAME);
        for (int x : new int[] { 3, 13 }) {
            box(x - 0.5, 0.5, 3, x + 0.5, 1.5, 13, FRAME, FRAME);
            for (int z : new int[] { 3, 8, 13 }) {
                stoneBox(x - 2, 0, z - 2, x + 2, 1, z + 2);
                stoneBox(x - 1.5, 1, z - 1.5, x + 1.5, 2, z + 1.5);
            }
        }

        // Dark metal collar around the gold-colored central column.
        box(6, 2, 6, 10, 3, 10, METAL, METAL);
        box(7, 3, 7, 9, 14, 9, GOLD, GOLD);
        box(6, 13.5, 6, 10, 15, 10, METAL, METAL);

        // The crossbar from the sprite supports a rack on each side of the column.
        holderParts.add(new Box(2.5, 13, 7.5, 13.5, 14, 8.5, METAL, METAL, 0));
        int potionBit = 1;
        for (int x : new int[] { 3, 13 }) {
            holderParts.add(new Box(x - 0.5, 12, 2.5, x + 0.5, 13, 13.5, METAL, METAL, 0));
            for (int z : new int[] { 3, 8, 13 }) {
                // Pipes stay attached to the rack even when the potion slot below is empty.
                holderParts.add(new Box(x - 0.5, 9, z - 0.5, x + 0.5, 12, z + 0.5, PIPE, PIPE, 0));
                // Each removable flask corresponds to one potion input slot.
                box(x - 1.2, 2, z - 1.2, x + 1.2, 3, z + 1.2, GLASS, GLASS, potionBit);
                box(x - 2, 3, z - 2, x + 2, 6, z + 2, FLASK_BODY, GLASS, potionBit);
                box(x - 1.2, 6, z - 1.2, x + 1.2, 9, z + 1.2, FLASK_SHOULDER, GLASS, potionBit);
                potionBit <<= 1;
            }
        }
    }

    private void stoneBox(double x0, double y0, double z0, double x1, double y1, double z1) {
        // All stone supports have square footprints. Crop one texture pixel per model unit on every face.
        UV sides = new UV(x0, 16 - y1, x1, 16 - y0);
        UV caps = new UV(x0, z0, x1, z1);
        stoneParts.add(new Box(x0, y0, z0, x1, y1, z1, sides, caps, 0));
    }

    private void box(double x0, double y0, double z0, double x1, double y1, double z1, UV sides, UV caps) {
        box(x0, y0, z0, x1, y1, z1, sides, caps, 0);
    }

    private void box(double x0, double y0, double z0, double x1, double y1, double z1, UV sides, UV caps,
        int potionBit) {
        parts.add(new Box(x0, y0, z0, x1, y1, z1, sides, caps, potionBit));
    }

    public void render(IIcon icon, IIcon stoneIcon, double x, double y, double z, boolean worldLighting,
        boolean breaking, int filledPotionSlots, boolean bottles3D) {
        renderParts(stoneParts, stoneIcon, x, y, z, worldLighting, breaking, 0);
        renderParts(parts, icon, x, y, z, worldLighting, breaking, bottles3D ? filledPotionSlots : 0);
        if (bottles3D) {
            renderParts(holderParts, icon, x, y, z, worldLighting, breaking, 0);
        } else {
            renderFlatBottlesAndHolders(icon, x, y, z, worldLighting, breaking, filledPotionSlots);
        }
    }

    private static void renderFlatBottlesAndHolders(IIcon icon, double x, double y, double z, boolean worldLighting,
        boolean breaking, int filledPotionSlots) {
        // Two-pixel beams retain the sprite's gray highlight and dark underside even when the stand is empty.
        flatSection(icon, UPPER_BAR, x + 0.5, y, z + 0.5, 1, 0, 5.5, 13, 15, worldLighting, breaking);
        int potionBit = 1;
        for (int bottleX : new int[] { 3, 13 }) {
            flatSection(icon, UPPER_BAR, x + bottleX / 16.0, y, z + 0.5, 0, 1, 5.5, 12, 14, worldLighting, breaking);
            for (int bottleZ : new int[] { 3, 8, 13 }) {
                // Each row of pipes and bottles shares its side rail's plane, parallel to the other row.
                double centerX = x + bottleX / 16.0;
                double centerZ = z + bottleZ / 16.0;
                flatSection(icon, PIPE, centerX, y, centerZ, 0, 1, 0.5, 9, 12, worldLighting, breaking);
                if ((filledPotionSlots & potionBit) != 0) {
                    flatSection(icon, GLASS, centerX, y, centerZ, 0, 1, 1.2, 2, 3, worldLighting, breaking);
                    flatSection(icon, FLASK_BODY, centerX, y, centerZ, 0, 1, 2, 3, 6, worldLighting, breaking);
                    flatSection(icon, FLASK_SHOULDER, centerX, y, centerZ, 0, 1, 1.2, 6, 9, worldLighting, breaking);
                }
                potionBit <<= 1;
            }
        }
    }

    private static void flatSection(IIcon icon, UV uv, double x, double y, double z, double dx, double dz,
        double halfWidth, double bottom, double top, boolean worldLighting, boolean breaking) {
        double x0 = x - dx * halfWidth / 16;
        double z0 = z - dz * halfWidth / 16;
        double x1 = x + dx * halfWidth / 16;
        double z1 = z + dz * halfWidth / 16;
        double y0 = y + bottom / 16;
        double y1 = y + top / 16;
        UV texture = breaking ? FULL_TEXTURE : uv;
        face(
            icon,
            texture,
            worldLighting,
            (float) -dz,
            0,
            (float) dx,
            1,
            x0,
            y1,
            z0,
            x0,
            y0,
            z0,
            x1,
            y0,
            z1,
            x1,
            y1,
            z1);
        face(
            icon,
            texture,
            worldLighting,
            (float) dz,
            0,
            (float) -dx,
            1,
            x1,
            y1,
            z1,
            x1,
            y0,
            z1,
            x0,
            y0,
            z0,
            x0,
            y1,
            z0);
    }

    private static void renderParts(List<Box> parts, IIcon icon, double x, double y, double z, boolean worldLighting,
        boolean breaking, int filledPotionSlots) {
        for (Box part : parts) {
            if (part.potionBit != 0 && (part.potionBit & filledPotionSlots) == 0) continue;
            double x0 = x + part.x0;
            double y0 = y + part.y0;
            double z0 = z + part.z0;
            double x1 = x + part.x1;
            double y1 = y + part.y1;
            double z1 = z + part.z1;
            UV sides = breaking ? FULL_TEXTURE : part.sides;
            UV caps = breaking ? FULL_TEXTURE : part.caps;

            face(icon, caps, worldLighting, 0, -1, 0, 0.5F, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1);
            face(icon, caps, worldLighting, 0, 1, 0, 1.0F, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
            face(icon, sides, worldLighting, 0, 0, -1, 0.8F, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0);
            face(icon, sides, worldLighting, 0, 0, 1, 0.8F, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1);
            face(icon, sides, worldLighting, -1, 0, 0, 0.6F, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1);
            face(icon, sides, worldLighting, 1, 0, 0, 0.6F, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0);
        }
    }

    private static void face(IIcon icon, UV uv, boolean worldLighting, float nx, float ny, float nz, float shade,
        double x0, double y0, double z0, double x1, double y1, double z1, double x2, double y2, double z2, double x3,
        double y3, double z3) {
        Tessellator tessellator = Tessellator.instance;
        float brightness = worldLighting ? shade : 1.0F;
        tessellator.setColorOpaque_F(brightness, brightness, brightness);
        tessellator.setNormal(nx, ny, nz);
        // Inset the UV edges slightly so adjacent pixels/atlas sprites cannot bleed onto narrow model parts.
        double u0 = icon.getInterpolatedU(uv.u0 + 0.001);
        double v0 = icon.getInterpolatedV(uv.v0 + 0.001);
        double u1 = icon.getInterpolatedU(uv.u1 - 0.001);
        double v1 = icon.getInterpolatedV(uv.v1 - 0.001);
        tessellator.addVertexWithUV(x0, y0, z0, u0, v0);
        tessellator.addVertexWithUV(x1, y1, z1, u0, v1);
        tessellator.addVertexWithUV(x2, y2, z2, u1, v1);
        tessellator.addVertexWithUV(x3, y3, z3, u1, v0);
    }

    private static class UV {

        private final double u0, v0, u1, v1;

        private UV(double u0, double v0, double u1, double v1) {
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
        }
    }

    private static class Box {

        private final double x0, y0, z0, x1, y1, z1;
        private final UV sides, caps;
        private final int potionBit;

        private Box(double x0, double y0, double z0, double x1, double y1, double z1, UV sides, UV caps,
            int potionBit) {
            this.x0 = x0 / 16;
            this.y0 = y0 / 16;
            this.z0 = z0 / 16;
            this.x1 = x1 / 16;
            this.y1 = y1 / 16;
            this.z1 = z1 / 16;
            this.sides = sides;
            this.caps = caps;
            this.potionBit = potionBit;
        }
    }
}
