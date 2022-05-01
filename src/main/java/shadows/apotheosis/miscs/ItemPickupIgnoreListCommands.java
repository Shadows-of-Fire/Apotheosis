package shadows.apotheosis.miscs;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.NoSuchElementException;

public class ItemPickupIgnoreListCommands {

    private static int printList(CommandSourceStack source, Player player){
        try {
            var ignoreList = player.getCapability(MiscCapability.ITEM_PICKUP_IGNORE_LIST_CAPABILITY)
                    .resolve().get().getIgnoreList();
            var msg = "Your ignore list: \n    " + (ignoreList.isEmpty() ?
                        "is empty" :
                        String.join(",\n    ", ignoreList.stream().map(ResourceLocation::toString).toList()));
            source.sendSuccess(new TextComponent(msg), false);
        } catch (NoSuchElementException ex) {
            source.sendFailure(new TextComponent("ipiList is not available"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int clearList(CommandSourceStack source, Player player){
        try {
            player.getCapability(MiscCapability.ITEM_PICKUP_IGNORE_LIST_CAPABILITY).resolve().get().reset();
            var msg = "Cleared ipiList";
            source.sendSuccess(new TextComponent(msg), false);
        } catch (NoSuchElementException ex) {
            source.sendFailure(new TextComponent("ipiList is not available"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int addItem(CommandSourceStack source, Player player, ItemInput item){
        try {
            var resLoc = item.getItem().getRegistryName();
            player.getCapability(MiscCapability.ITEM_PICKUP_IGNORE_LIST_CAPABILITY)
                    .resolve().get().ignoreItem(resLoc);
            var msg = "Added " + resLoc + " to ignore list";
            source.sendSuccess(new TextComponent(msg), false);
        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
            source.sendFailure(new TextComponent("ipiList is not available"));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int removeItem(CommandSourceStack source, Player player, ItemInput item){
        try {
            var resLoc = item.getItem().getRegistryName();
            player.getCapability(MiscCapability.ITEM_PICKUP_IGNORE_LIST_CAPABILITY)
                    .resolve().get().unignoreItem(resLoc);
            var msg = "Removed " + resLoc + " from ignore list";
            source.sendSuccess(new TextComponent(msg), false);
        } catch (NoSuchElementException ex) {
            source.sendFailure(new TextComponent("ipiList is not available"));
        }
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> ipiCommand = Commands.literal("ipiList")
                .requires(p -> p.hasPermission(Commands.LEVEL_ALL));

        ipiCommand.then(Commands.literal("add").then(
                Commands.argument("item", ItemArgument.item()).executes(
                        ctx -> addItem(ctx.getSource(), ctx.getSource().getPlayerOrException(), ItemArgument.getItem(ctx, "item"))
                )
        ));
        ipiCommand.then(Commands.literal("remove").then(
                Commands.argument("item", ItemArgument.item()).executes(
                        ctx -> removeItem(ctx.getSource(), ctx.getSource().getPlayerOrException(), ItemArgument.getItem(ctx, "item"))
                )
        ));
        ipiCommand.then(Commands.literal("clear").executes(
                ctx -> clearList(ctx.getSource(), ctx.getSource().getPlayerOrException())
        ));
        ipiCommand.then(Commands.literal("list").executes(
                ctx -> printList(ctx.getSource(), ctx.getSource().getPlayerOrException())
        ));

        pDispatcher.register(ipiCommand);
    }
}
