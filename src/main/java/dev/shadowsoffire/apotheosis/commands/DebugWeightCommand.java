package dev.shadowsoffire.apotheosis.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.serialization.JsonOps;

import dev.shadowsoffire.apotheosis.Apoth.BuiltInRegs;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.AffixType;
import dev.shadowsoffire.apotheosis.loot.AffixLootRegistry;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.EliteRegistry;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.tiers.Constraints.Constrained;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.TieredWeights.Weighted;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;

public class DebugWeightCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_AFFIX_TYPE = (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(AffixType.values()).map(StringRepresentable::getSerializedName), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_LOOT_CATEGORY = (ctx, builder) -> SharedSuggestionProvider.suggest(BuiltInRegs.LOOT_CATEGORY.keySet().stream().map(ResourceLocation::toString), builder);

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root, CommandBuildContext ctx) {
        LiteralArgumentBuilder<CommandSourceStack> weights = Commands.literal("weights");

        weights.then(Commands.literal("affix_loot_entries").executes(c -> dumpWeights(c, AffixLootRegistry.INSTANCE)));
        weights.then(Commands.literal("affixes")
            .then(Commands.argument("item", ItemArgument.item(ctx))
                .then(Commands.argument("type", StringArgumentType.word()).suggests(SUGGEST_AFFIX_TYPE)
                    .then(Commands.argument("rarity", ResourceLocationArgument.id()).suggests(RarityCommand.SUGGEST_RARITY)
                        .executes(c -> dumpAffixWeights(c, ItemArgument.getItem(c, "item"), StringArgumentType.getString(c, "type"), ResourceLocationArgument.getId(c, "rarity")))))));
        weights.then(Commands.literal("elites").executes(c -> dumpWeights(c, EliteRegistry.INSTANCE)));
        weights.then(Commands.literal("gems").executes(c -> dumpWeights(c, GemRegistry.INSTANCE)));
        weights.then(Commands.literal("invaders").executes(c -> dumpWeights(c, InvaderRegistry.INSTANCE)));
        weights.then(Commands.literal("rarities").executes(c -> dumpWeights(c, RarityRegistry.INSTANCE)));

        root.then(weights);
    }

    public static <T extends CodecProvider<? super T> & Weighted> void dumpWeightsFor(GenContext ctx, DynamicRegistry<T> registry) {
        dumpWeightsFor(ctx, registry, Predicates.alwaysTrue());
    }

    /**
     * Dumps the weights for all objects in the target registry.
     * <p>
     * If the registry objects are {@link Constrainted}, objects that fail their constraint check will be treated as having zero weight.
     */
    public static <T extends CodecProvider<? super T> & Weighted> void dumpWeightsFor(GenContext ctx, DynamicRegistry<T> registry, Predicate<T> filter) {
        Collection<T> values = registry.getValues();
        List<Wrapper<T>> list = new ArrayList<>(values.size());

        values.stream().filter(filter).map(t -> wrapWithConstraints(ctx, t)).forEach(list::add);

        float total = WeightedRandom.getTotalWeight(list);

        Apotheosis.LOGGER.info("Starting dump of all {} weights...", registry.getPath());
        Apotheosis.LOGGER.info("Current GenContext: {}", ctx);
        Comparator<Wrapper<T>> comparator = Comparator.comparing(w -> -w.weight().asInt());
        comparator = comparator.thenComparing(Comparator.comparing(w -> registry.getKey(w.data()).toString()));
        list.sort(comparator);
        for (Wrapper<T> entry : list) {
            ResourceLocation key = registry.getKey(entry.data());
            float chance = entry.weight().asInt() / total;
            Apotheosis.LOGGER.info("{} : {}% ({} / {}}", key, Affix.fmt(chance * 100), entry.weight().asInt(), (int) total);
        }
    }

    public static <T extends CodecProvider<? super T> & Weighted> int dumpWeights(CommandContext<CommandSourceStack> c, DynamicRegistry<T> registry) throws CommandSyntaxException {
        GenContext ctx = GenContext.forPlayer(c.getSource().getPlayerOrException());
        dumpWeightsFor(ctx, registry);
        c.getSource().sendSuccess(() -> Component.literal("Weight values have been dumped to the log file."), true);
        return 0;
    }

    private static <T extends Weighted> Wrapper<T> wrapWithConstraints(GenContext ctx, T t) {
        if (t instanceof Constrained c && !c.constraints().test(ctx)) {
            return new WeightedEntry.Wrapper<>(t, Weighted.SAFE_ZERO);
        }
        return t.<T>wrap(ctx.tier(), ctx.luck());
    }

    private static final DynamicCommandExceptionType UNKNOWN_RARITY = new DynamicCommandExceptionType(str -> () -> "Unknown Rarity: " + str);

    private static final DynamicCommandExceptionType UNKNOWN_AFFIX_TYPE = new DynamicCommandExceptionType(str -> () -> "Unknown Affix Type: " + str);

    private static int dumpAffixWeights(CommandContext<CommandSourceStack> c, ItemInput item, String typeStr, ResourceLocation rarityId) throws CommandSyntaxException {
        LootRarity rarity = RarityRegistry.INSTANCE.getValue(rarityId);
        if (rarity == null) {
            throw UNKNOWN_RARITY.create(rarityId);
        }

        AffixType type = AffixType.CODEC.decode(JsonOps.INSTANCE, new JsonPrimitive(typeStr)).getOrThrow(s -> UNKNOWN_AFFIX_TYPE.create(typeStr)).getFirst();

        ItemStack stack = item.createItemStack(1, false);
        LootCategory cat = LootCategory.forItem(stack);
        Apotheosis.LOGGER.info("Affix weight dump target item: " + stack.toString());

        GenContext ctx = GenContext.forPlayer(c.getSource().getPlayerOrException());
        dumpWeightsFor(ctx, AffixRegistry.INSTANCE, afx -> afx.canApplyTo(stack, cat, rarity) && afx.definition().type() == type);
        c.getSource().sendSuccess(() -> Component.literal("Weight values have been dumped to the log file."), true);
        return 0;
    }

}
