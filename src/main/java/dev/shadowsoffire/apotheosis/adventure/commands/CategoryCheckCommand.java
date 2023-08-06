package dev.shadowsoffire.apotheosis.adventure.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class CategoryCheckCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("loot_category").requires(c -> c.hasPermission(2)).executes(c -> {
            Player p = c.getSource().getPlayerOrException();
            ItemStack stack = p.getMainHandItem();
            LootCategory cat = LootCategory.forItem(stack);
            EquipmentSlot[] slots = cat == null ? null : cat.getSlots();
            p.sendSystemMessage(Component.literal("Loot Category - " + (cat == null ? "null" : cat.getName())));
            p.sendSystemMessage(Component.literal("Equipment Slot - " + (slots == null ? "null" : toStr(slots))));
            return 0;
        }));
    }

    static String toStr(EquipmentSlot[] slots) {
        StringBuilder b = new StringBuilder();
        b.append('{');
        for (int i = 0; i < slots.length; i++) {
            b.append(slots[i].name().toLowerCase());
            if (i == slots.length - 1) {
                b.append('}');
            }
            else b.append(", ");
        }
        return b.toString();
    }

}
