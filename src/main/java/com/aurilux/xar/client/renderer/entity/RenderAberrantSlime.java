package com.aurilux.xar.client.renderer.entity;

import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.entity.RenderSlime;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;

import com.aurilux.xar.client.AberrantSlimeShader;
import com.aurilux.xar.util.ResourceUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** RidiculousWorld's Shoggoth appearance uses the vanilla inner and outer slime models. */
@SideOnly(Side.CLIENT)
public class RenderAberrantSlime extends RenderSlime {

    private static final ResourceLocation TEXTURE = ResourceUtils.getResource("textures/entity/aberrant_slime.png");
    private final AberrantSlimeShader shader = new AberrantSlimeShader();

    public RenderAberrantSlime() {
        super(new ModelSlime(16), new ModelSlime(0), 0.25F);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntitySlime slime) {
        return TEXTURE;
    }

    @Override
    public void doRender(EntityLiving entity, double x, double y, double z, float yaw, float partialTick) {
        int previousProgram = shader.begin(entity);
        try {
            super.doRender(entity, x, y, z, yaw, partialTick);
        } finally {
            if (previousProgram >= 0) shader.end(previousProgram);
        }
    }
}
