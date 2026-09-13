package com.aurilux.xar.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.entity.EntityLiving;

import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GLContext;

import com.aurilux.xar.Xthuoth;
import com.aurilux.xar.handler.ConfigHandler;
import com.aurilux.xar.util.ResourceUtils;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** Loads the WTFPL Shoggoth shaders with resource-pack reloads and a texture-only fallback. */
@SideOnly(Side.CLIENT)
public final class AberrantSlimeShader implements IResourceManagerReloadListener {

    private static int animationTicks;
    private int program;
    private int timeUniform;
    private int painUniform;
    private boolean attempted;

    public AberrantSlimeShader() {
        ((IReloadableResourceManager) Minecraft.getMinecraft()
            .getResourceManager()).registerReloadListener(this);
    }

    /** Returns the program to restore afterwards, or -1 when the effect is disabled/unavailable. */
    public int begin(EntityLiving entity) {
        if (!ConfigHandler.ABERRANT_SLIME_SHADERS || !GLContext.getCapabilities().OpenGL20) return -1;
        int previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        if (!attempted) loadProgram();
        if (program == 0) return -1;

        GL20.glUseProgram(program);
        GL20.glUniform1i(timeUniform, animationTicks);
        GL20.glUniform1f(painUniform, entity.hurtResistantTime);
        return previousProgram;
    }

    public void end(int previousProgram) {
        GL20.glUseProgram(previousProgram);
    }

    public static final class AnimationClock {

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            // Upstream advances on both tick phases, independently of any particular mob's age.
            animationTicks++;
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        if (program != 0) GL20.glDeleteProgram(program);
        program = 0;
        attempted = false;
    }

    private void loadProgram() {
        attempted = true;
        int previousProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int vertex = 0;
        int fragment = 0;
        try {
            vertex = compile("aberrant_slime.vert", GL20.GL_VERTEX_SHADER);
            fragment = compile("aberrant_slime.frag", GL20.GL_FRAGMENT_SHADER);
            program = GL20.glCreateProgram();
            if (program == 0) throw new IOException("Unable to allocate a shader program");
            GL20.glAttachShader(program, vertex);
            GL20.glAttachShader(program, fragment);
            GL20.glLinkProgram(program);
            if (GL20.glGetProgrami(program, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                throw new IOException(GL20.glGetProgramInfoLog(program, 4096));
            }

            timeUniform = GL20.glGetUniformLocation(program, "time");
            painUniform = GL20.glGetUniformLocation(program, "pain");
            GL20.glUseProgram(program);
            try {
                GL20.glUniform1i(GL20.glGetUniformLocation(program, "bgl_RenderedTexture"), 0);
                GL20.glUniform1f(GL20.glGetUniformLocation(program, "disfiguration"), 0.045F);
                GL20.glUniform1f(GL20.glGetUniformLocation(program, "grainIntensity"), 0.55F);
            } finally {
                GL20.glUseProgram(previousProgram);
            }
        } catch (IOException | RuntimeException e) {
            if (program != 0) GL20.glDeleteProgram(program);
            program = 0;
            Xthuoth.log.warn("Unable to load Aberrant Slime shaders; using the slime texture", e);
        } finally {
            if (vertex != 0) GL20.glDeleteShader(vertex);
            if (fragment != 0) GL20.glDeleteShader(fragment);
        }
    }

    private int compile(String name, int type) throws IOException {
        String source;
        try (InputStream stream = Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(ResourceUtils.getResource("shaders/" + name))
            .getInputStream()) {
            source = IOUtils.toString(stream, StandardCharsets.UTF_8);
        }
        int shader = GL20.glCreateShader(type);
        if (shader == 0) throw new IOException("Unable to allocate shader " + name);
        try {
            GL20.glShaderSource(shader, source);
            GL20.glCompileShader(shader);
            if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                throw new IOException(name + ": " + GL20.glGetShaderInfoLog(shader, 4096));
            }
            return shader;
        } catch (IOException | RuntimeException e) {
            GL20.glDeleteShader(shader);
            throw e;
        }
    }
}
