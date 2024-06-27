package dev.shadowsoffire.apotheosis.adventure.commands;

import java.util.Arrays;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class GemCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_OP = (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(Operation.values()).map(Operation::name), builder);
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_ATTRIB = (ctx, builder) -> SharedSuggestionProvider.suggest(ForgeRegistries.ATTRIBUTES.getKeys().stream().map(ResourceLocation::toString), builder);
    public static final SuggestionProvider<CommandSourceStack> SUGGEST_GEM = (ctx, builder) -> SharedSuggestionProvider.suggest(GemRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

    @SuppressWarnings("removal")
    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("gem").requires(c -> c.hasPermission(2)).then(Commands.literal("fromPreset").then(Commands.argument("gem", ResourceLocationArgument.id()).suggests(SUGGEST_GEM).executes(c -> {
            Gem gem = GemRegistry.INSTANCE.getValue(ResourceLocationArgument.getId(c, "gem"));
            Player p = c.getSource().getPlayerOrException();
            ItemStack stack = GemRegistry.createGemStack(gem, LootRarity.random(p.random, p.getLuck()));
            p.addItem(stack);
            return 0;
        }))).then(Commands.literal("random").executes(c -> {
            Player p = c.getSource().getPlayerOrException();
            ItemStack gem = GemRegistry.createRandomGemStack(p.random, c.getSource().getLevel(), p.getLuck(), IDimensional.matches(p.level()), IStaged.matches(p));
            p.addItem(gem);
            return 0;
        })));
    }

}
