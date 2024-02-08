package dev.shadowsoffire.apotheosis.adventure.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants.Type;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.net.RadialStateChangeMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AdventureKeys {

    public static final KeyMapping TOGGLE_RADIAL = new KeyMapping("key." + Apotheosis.MODID + ".toggle_radial_mining", KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Type.KEYSYM, GLFW.GLFW_KEY_O,
        "key.categories." + Apotheosis.MODID);

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent e) {
        e.register(TOGGLE_RADIAL);
    }

    @SubscribeEvent
    public static void handleKeys(ClientTickEvent e) {
        if (e.phase == Phase.START) return;
        if (Minecraft.getInstance().player == null) return;

        while (TOGGLE_RADIAL.consumeClick() && TOGGLE_RADIAL.isConflictContextAndModifierActive()) {
            if (Minecraft.getInstance().screen == null) {
                Apotheosis.CHANNEL.sendToServer(new RadialStateChangeMessage());
            }
        }
    }
}
