package dev.shadowsoffire.apotheosis.commands;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootController;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.apothic_attributes.api.AttributeHelper;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class AffixCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_AFFIX = (ctx, builder) -> SharedSuggestionProvider.suggest(AffixRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_APPLICABLE_AFFIX = (ctx, builder) -> {
        Entity entity = ctx.getSource().getEntity();
        if (entity instanceof LivingEntity living) {
            ItemStack held = living.getMainHandItem();
            if (!held.isEmpty()) {
                LootCategory cat = LootCategory.forItem(held);
                DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(held);
                if (!cat.isNone() && rarity.isBound()) {
                    Stream<String> suggestions = AffixRegistry.INSTANCE.getValues().stream().filter(a -> a.canApplyTo(held, cat, rarity.get())).map(AffixRegistry.INSTANCE::getKey).map(ResourceLocation::toString);
                    return SharedSuggestionProvider.suggest(suggestions, builder);
                }
            }
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    };

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_AFFIX_ON_ITEM = (ctx, builder) -> {
        Entity entity = ctx.getSource().getEntity();
        if (entity instanceof LivingEntity living) {
            ItemStack held = living.getMainHandItem();
            if (!held.isEmpty()) {
                Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(held);
                return SharedSuggestionProvider.suggest(affixes.keySet().stream().map(DynamicHolder::getId).map(ResourceLocation::toString), builder);
            }
        }
        return SharedSuggestionProvider.suggest(Collections.emptyList(), builder);
    };

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("affix").requires(c -> c.hasPermission(2));

        builder.then(
            Commands.literal("apply")
                .then(Commands.argument("affix", ResourceLocationArgument.id()).suggests(SUGGEST_APPLICABLE_AFFIX)
                    .then(Commands.argument("level", FloatArgumentType.floatArg(0, Affix.MAX_LEVEL))
                        .executes(c -> applyAffix(c, ResourceLocationArgument.getId(c, "affix"), FloatArgumentType.getFloat(c, "level"))))
                    .executes(c -> applyAffix(c, ResourceLocationArgument.getId(c, "affix"), c.getSource().getLevel().random.nextFloat()))));

        builder.then(
            Commands.literal("list")
                .executes(AffixCommand::listAffixes));

        builder.then(
            Commands.literal("list_alternatives")
                .then(Commands.argument("affix", ResourceLocationArgument.id()).suggests(SUGGEST_AFFIX_ON_ITEM)
                    .executes(c -> listAlternatives(c, ResourceLocationArgument.getId(c, "affix")))));

        root.then(builder);
    }

    public static int applyAffix(CommandContext<CommandSourceStack> c, ResourceLocation affixId, float level) {
        DynamicHolder<Affix> afx = AffixRegistry.INSTANCE.holder(affixId);
        if (!afx.isBound()) {
            return fail(c, "Unknown affix: " + affixId, -1);
        }

        Entity entity = c.getSource().getEntity();
        if (entity instanceof LivingEntity living) {
            ItemStack held = living.getMainHandItem();
            if (held.isEmpty()) {
                return fail(c, "The target entity must have an item in their main hand.", -2);
            }

            DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(held);
            if (!rarity.isBound()) {
                return fail(c, "The target item must have a set rarity.", -3);
            }

            LootCategory cat = LootCategory.forItem(held);
            if (cat.isNone()) {
                return fail(c, "The target item must have a valid loot category", -4);
            }

            if (!afx.get().canApplyTo(held, cat, rarity.get())) {
                return fail(c, "The selected affix cannot be applied to the target item.", -5);
            }

            AffixHelper.applyAffix(held, new AffixInstance(afx, level, rarity, held));
            c.getSource().sendSuccess(() -> Component.translatable("Successfully applied affix %s with level %s to %s", affixId.toString(), level, held.getDisplayName()), true);
            return 0;
        }
        else {
            return fail(c, "/apoth affix must be executed by a living entity.", -10);
        }
    }

    public static int listAffixes(CommandContext<CommandSourceStack> c) {
        Entity entity = c.getSource().getEntity();
        if (entity instanceof LivingEntity living) {
            ItemStack held = living.getMainHandItem();
            if (held.isEmpty()) {
                return fail(c, "The target entity must have an item in their main hand.", -2);
            }

            DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(held);
            if (!rarity.isBound()) {
                return fail(c, "The target item must have a set rarity.", -3);
            }

            Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(held);
            AttributeTooltipContext ctx = AttributeTooltipContext.of(living instanceof Player p ? p : null, TooltipContext.of(c.getSource().getLevel()), ApothicAttributes.getTooltipFlag());

            c.getSource().sendSystemMessage(Component.translatable("Affixes present on %s:", held.getDisplayName()));
            affixes.forEach((afx, inst) -> {
                MutableComponent name = Component.translatable("[%s]", inst.getName(true));
                name.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, inst.getAugmentingText(ctx))));
                c.getSource().sendSystemMessage(Component.translatable("%s - %s%%", name, Affix.fmt(100 * inst.level())));
            });

            return 0;
        }
        else {
            return fail(c, "/apoth affix must be executed by a living entity.", -10);
        }
    }

    public static int listAlternatives(CommandContext<CommandSourceStack> c, ResourceLocation affixId) throws CommandSyntaxException {
        DynamicHolder<Affix> afx = AffixRegistry.INSTANCE.holder(affixId);
        if (!afx.isBound()) {
            return fail(c, "Unknown affix: " + affixId, -1);
        }

        Entity entity = c.getSource().getEntity();
        if (entity instanceof LivingEntity living) {
            ItemStack held = living.getMainHandItem();
            if (held.isEmpty()) {
                return fail(c, "The target entity must have an item in their main hand.", -2);
            }

            DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(held);
            if (!rarity.isBound()) {
                return fail(c, "The target item must have a set rarity.", -3);
            }

            Map<DynamicHolder<Affix>, AffixInstance> affixes = AffixHelper.getAffixes(held);
            if (!affixes.containsKey(afx)) {
                return fail(c, "The target item does not contain the selected affix.", -4);
            }

            Stream<DynamicHolder<Affix>> alternatives = LootController.getAlternativeAffixes(c.getSource().getPlayerOrException(), held, rarity.get(), afx);
            c.getSource().sendSystemMessage(Component.translatable("Possible alternatives to %s:", afx.get().getName(true)));
            AttributeTooltipContext ctx = AttributeTooltipContext.of(living instanceof Player p ? p : null, TooltipContext.of(c.getSource().getLevel()), ApothicAttributes.getTooltipFlag());

            alternatives.forEach(a -> {
                MutableComponent name = Component.translatable("[%s]", a.get().getName(true));
                AffixInstance inst = new AffixInstance(a, 0.5F, rarity, held);
                Component augTxt = inst.getAugmentingText(ctx);
                name.setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, augTxt)));
                c.getSource().sendSystemMessage(AttributeHelper.list().append(name));
            });

            return 0;
        }
        else {
            return fail(c, "/apoth affix must be executed by a living entity.", -10);
        }
    }

    public static int fail(CommandContext<CommandSourceStack> c, String msg, int code) {
        c.getSource().sendFailure(Component.translatable(msg));
        return code;
    }
}
