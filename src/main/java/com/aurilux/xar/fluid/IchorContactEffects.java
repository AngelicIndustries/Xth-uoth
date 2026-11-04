package com.aurilux.xar.fluid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import com.aurilux.xar.Xthuoth;
import com.aurilux.xar.handler.ConfigHandler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** Gives creatures touching ichor the potion effects listed in the ichor_contact_effects config. */
public final class IchorContactEffects {

    /** Same limits as the vanilla /effect command. */
    static final int MAX_LEVEL = 256;
    static final double MAX_SECONDS = 1000000;

    private static volatile Cache cache = new Cache(
        new String[0],
        Collections.<Effect>emptyList(),
        Collections.<String>emptyList());
    /** The list whose errors were last sent to online players. Only used on the server thread. */
    private static Cache reported = cache;
    /** When each creature may next receive each instant effect. Only used on the server thread. */
    private static final Map<EntityLivingBase, Map<Effect, Long>> nextInstantTimes = new WeakHashMap<>();

    private IchorContactEffects() {}

    public static void apply(EntityLivingBase entity) {
        List<Effect> effects = getEffects();
        if (effects.isEmpty()) return;
        long now = entity.worldObj.getTotalWorldTime();
        for (Effect effect : effects) {
            if (effect.potion.isInstant()) {
                applyInstant(entity, effect, now);
                continue;
            }
            // Wait for the effect to wear off before reapplying it. Refreshing it every tick would resend it to
            // players constantly and keep resetting the timing of periodic effects such as Poison.
            PotionEffect active = entity.getActivePotionEffect(effect.potion);
            if (active == null || active.getAmplifier() < effect.amplifier) {
                entity.addPotionEffect(new PotionEffect(effect.potion.id, effect.ticks, effect.amplifier));
            }
        }
    }

    private static void applyInstant(EntityLivingBase entity, Effect effect, long now) {
        Map<Effect, Long> nextTimes = nextInstantTimes.get(entity);
        if (nextTimes == null) {
            nextTimes = new HashMap<>();
            nextInstantTimes.put(entity, nextTimes);
        }
        Long next = nextTimes.get(effect);
        if (next != null && now < next) return;
        nextTimes.put(effect, now + effect.ticks);
        // Apply it like a splash potion does: as a timed effect, it would repeat every tick for its whole duration.
        effect.potion.affectEntity(null, entity, effect.amplifier, 1.0D);
    }

    /** Returns the configured effects, parsing the list again and logging unusable entries whenever it changes. */
    public static List<Effect> getEffects() {
        return refresh().effects;
    }

    private static Cache refresh() {
        String[] entries = ConfigHandler.ICHOR_CONTACT_EFFECTS;
        Cache current = cache;
        if (!Arrays.equals(current.entries, entries)) {
            List<String> errors = new ArrayList<>();
            List<Effect> effects = parse(entries, Potion.potionTypes, errors);
            for (int i = 0; i < errors.size(); i++) {
                errors.set(i, "Ignoring ichor_contact_effects entry " + errors.get(i));
                Xthuoth.log.error(errors.get(i));
            }
            current = new Cache(entries, effects, Collections.unmodifiableList(errors));
            cache = current;
        }
        return current;
    }

    /** Tells online players about unusable entries if the list changed since they were last told. */
    private static boolean reportChanges(MinecraftServer server) {
        Cache current = refresh();
        if (current == reported) return false;
        reported = current;
        for (Object player : server.getConfigurationManager().playerEntityList) {
            sendErrors((EntityPlayerMP) player, current.errors);
        }
        return true;
    }

    private static void sendErrors(EntityPlayerMP player, List<String> errors) {
        if (errors.isEmpty() || !canEditConfig(player)) return;
        for (String error : errors) {
            player.addChatMessage(
                new ChatComponentText("[Xth'uoth] " + error)
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED)));
        }
    }

    /** Server operators, plus whoever hosts a singleplayer or LAN world, who isn't an operator without cheats. */
    private static boolean canEditConfig(EntityPlayerMP player) {
        MinecraftServer server = player.mcServer;
        return server.getConfigurationManager()
            .func_152596_g(player.getGameProfile())
            || player.getCommandSenderName()
                .equals(server.getServerOwner());
    }

    static List<Effect> parse(String[] entries, Potion[] registry, List<String> errors) {
        List<Effect> effects = new ArrayList<>();
        for (String entry : entries) {
            if (entry.trim()
                .isEmpty()) continue;
            try {
                effects.add(parseEntry(entry, registry));
            } catch (IllegalArgumentException e) {
                errors.add("\"" + entry.trim() + "\": " + e.getMessage());
            }
        }
        return Collections.unmodifiableList(effects);
    }

    private static Effect parseEntry(String entry, Potion[] registry) {
        String[] parts = entry.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("expected effect, level, seconds");
        }
        Potion potion = findPotion(parts[0].trim(), registry);

        int level;
        try {
            level = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            level = 0;
        }
        if (level < 1 || level > MAX_LEVEL) {
            throw new IllegalArgumentException("level must be a whole number from 1 to " + MAX_LEVEL);
        }

        double seconds;
        try {
            seconds = Double.parseDouble(parts[2].trim());
        } catch (NumberFormatException e) {
            seconds = Double.NaN;
        }
        if (!(seconds > 0 && seconds <= MAX_SECONDS)) {
            throw new IllegalArgumentException("seconds must be a number above 0 and at most " + (int) MAX_SECONDS);
        }

        return new Effect(potion, level - 1, Math.max(1, (int) Math.round(seconds * 20)));
    }

    private static Potion findPotion(String name, Potion[] registry) {
        if (name.matches("\\d+")) {
            int id = name.length() <= 9 ? Integer.parseInt(name) : Integer.MAX_VALUE;
            if (id < registry.length && registry[id] != null) return registry[id];
            throw new IllegalArgumentException("no effect has ID " + name);
        }
        // Prefer an exact name, then allow leaving off the "potion." prefix that most effect names share.
        Potion potion = findPotionNamed(name, registry);
        if (potion == null) potion = findPotionNamed("potion." + name, registry);
        if (potion == null) throw new IllegalArgumentException("no effect is named " + name);
        return potion;
    }

    private static Potion findPotionNamed(String name, Potion[] registry) {
        Potion found = null;
        for (Potion potion : registry) {
            if (potion == null || !name.equalsIgnoreCase(potion.getName())) continue;
            if (found != null && found != potion) {
                throw new IllegalArgumentException("more than one effect is named " + name + "; use its ID instead");
            }
            found = potion;
        }
        return found;
    }

    public static final class Effect {

        public final Potion potion;
        public final int amplifier;
        public final int ticks;

        Effect(Potion potion, int amplifier, int ticks) {
            this.potion = potion;
            this.amplifier = amplifier;
            this.ticks = ticks;
        }
    }

    private static final class Cache {

        final String[] entries;
        final List<Effect> effects;
        final List<String> errors;

        Cache(String[] entries, List<Effect> effects, List<String> errors) {
            this.entries = entries;
            this.effects = effects;
            this.errors = errors;
        }
    }

    /** Reports unusable entries in chat when a player joins, or to everyone online when the list changes. */
    public static final class ErrorReporter {

        @SubscribeEvent
        public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
            if (!(event.player instanceof EntityPlayerMP)) return;
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            // A changed list is reported to everyone online, this player included.
            if (!reportChanges(player.mcServer)) sendErrors(player, cache.errors);
        }

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) reportChanges(MinecraftServer.getServer());
        }
    }
}
