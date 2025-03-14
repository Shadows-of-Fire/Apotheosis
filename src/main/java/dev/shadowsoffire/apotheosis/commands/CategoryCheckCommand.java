package dev.shadowsoffire.apotheosis.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apothic_attributes.modifiers.EntitySlotGroup;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CategoryCheckCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("loot_category").requires(c -> c.hasPermission(2)).executes(c -> {
            Player p = c.getSource().getPlayerOrException();
            ItemStack stack = p.getMainHandItem();
            LootCategory cat = LootCategory.forItem(stack);
            EntitySlotGroup slots = cat == null ? null : cat.getSlots();
            p.sendSystemMessage(Component.literal("Loot Category - " + (cat == null ? "null" : cat.getKey())));
            p.sendSystemMessage(Component.literal("Equipment Slot - " + (slots == null ? "null" : slots.id())));
            return 0;
        }));
    }

}
