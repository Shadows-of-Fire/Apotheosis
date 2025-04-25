package dev.shadowsoffire.apotheosis.adventure.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants.Type;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.net.RadialStateChangeMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AdventureKeys {

    private static int lastNotificationTime = 1000;

    public static final KeyMapping TOGGLE_RADIAL = new KeyMapping("key." + Apotheosis.MODID + ".toggle_radial_mining", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Type.KEYSYM, GLFW.GLFW_KEY_O,
        "key.categories." + Apotheosis.MODID);

    public static final KeyMapping WORLD_TIERS_ARENT_REAL = new KeyMapping(
        "key.apotheosis.world_tiers_arent_real",
        KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Type.KEYSYM,
        GLFW.GLFW_KEY_T, "key.categories." + Apotheosis.MODID);

    @SubscribeEvent
    public static void handleKeys(ClientTickEvent e) {
        if (e.phase == Phase.END) return;
        if (Minecraft.getInstance().player == null) return;

        while (TOGGLE_RADIAL.consumeClick() && TOGGLE_RADIAL.isConflictContextAndModifierActive()) {
            if (Minecraft.getInstance().screen == null) {
                Apotheosis.CHANNEL.sendToServer(new RadialStateChangeMessage());
            }
        }

        while (WORLD_TIERS_ARENT_REAL.consumeClick() && WORLD_TIERS_ARENT_REAL.isConflictContextAndModifierActive()) {
            if (Minecraft.getInstance().screen == null && lastNotificationTime + 1000 < Minecraft.getInstance().level.getGameTime()) {
                Minecraft.getInstance().player.sendSystemMessage(Component.translatable("message.apotheosis.world_tiers_arent_real"));
                lastNotificationTime = (int) Minecraft.getInstance().level.getGameTime();
            }
        }
    }
}
