package dev.shadowsoffire.apotheosis.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.net.RadialStatePayload;
import dev.shadowsoffire.apotheosis.util.ItemLinking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;

public class AdventureKeys {

    public static final KeyMapping TOGGLE_RADIAL = new KeyMapping(
        Apotheosis.langKey("key", "toggle_radial_mining"),
        KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Type.KEYSYM,
        GLFW.GLFW_KEY_O, "key.categories." + Apotheosis.MODID);

    public static final KeyMapping OPEN_WORLD_TIER_SELECT = new KeyMapping(
        Apotheosis.langKey("key", "open_world_tier_select"),
        KeyConflictContext.IN_GAME, KeyModifier.CONTROL, Type.KEYSYM,
        GLFW.GLFW_KEY_T, "key.categories." + Apotheosis.MODID);

    public static final KeyMapping LINK_ITEM_TO_CHAT = new KeyMapping(
        Apotheosis.langKey("key", "link_item_to_chat"),
        KeyConflictContext.GUI, KeyModifier.SHIFT, Type.KEYSYM,
        GLFW.GLFW_KEY_T, "key.categories." + Apotheosis.MODID);

    public static final KeyMapping COMPARE_EQUIPMENT = new KeyMapping(
        Apotheosis.langKey("key", "compare_equipment"),
        KeyConflictContext.GUI, KeyModifier.NONE, Type.KEYSYM,
        GLFW.GLFW_KEY_LEFT_SHIFT, "key.categories." + Apotheosis.MODID);

    @SubscribeEvent
    public static void handleKeys(ClientTickEvent.Post e) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        while (TOGGLE_RADIAL.consumeClick() && TOGGLE_RADIAL.isConflictContextAndModifierActive()) {
            if (Minecraft.getInstance().screen == null) {
                PacketDistributor.sendToServer(new RadialStatePayload());
            }
        }

        while (OPEN_WORLD_TIER_SELECT.consumeClick() && OPEN_WORLD_TIER_SELECT.isConflictContextAndModifierActive()) {
            if (Minecraft.getInstance().screen == null) {
                Minecraft.getInstance().setScreen(new WorldTierSelectScreen());
            }
        }

    }

    @SubscribeEvent
    public static void handleGuiKeys(InputEvent.Key e) {
        if (e.getAction() == GLFW.GLFW_PRESS) {
            if (LINK_ITEM_TO_CHAT.isActiveAndMatches(InputConstants.getKey(e.getKey(), e.getScanCode()))) {
                ItemLinking.sendHoveredItem();
            }
        }
    }
}
