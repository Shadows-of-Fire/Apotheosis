package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.LootCategories;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.attachments.AttributeToggles;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.net.AttributeTogglesPayload;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Boots ability affix that lets the wearer toggle all bonuses to one or more attributes on or off with a keybind.
 * <p>
 * All attributes of a single affix are toggled together: if any of them is currently suppressed, the key re-enables all of them,
 * otherwise it suppresses all of them.
 * <p>
 * While suppressed, {@link LivingEntity#getAttributeValue(Holder)} clamps the attribute for the wearer down to its base value,
 * carving out the vanilla sprint boost. Net penalties still pass through (see {@link AttributeToggles#getCappedValue}).
 * <p>
 * The player's choice is stored in {@link Apoth.Attachments#ATTRIBUTE_TOGGLES} and is cleared as soon as the boots granting it
 * are removed (see {@link #prune(ServerPlayer)}).
 */
public class AttributeToggleAffix extends Affix {

    public static final String TOGGLE_KEY = Apotheosis.langKey("key", "toggle_attribute_bonuses");

    public static final Codec<AttributeToggleAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            PlaceboCodecs.setOf(BuiltInRegistries.ATTRIBUTE.holderByNameCodec()).fieldOf("attributes").forGetter(a -> a.attributes),
            PlaceboCodecs.setOf(LootRarity.CODEC).fieldOf("rarities").forGetter(a -> a.rarities))
        .apply(inst, AttributeToggleAffix::new));

    protected final Set<Holder<Attribute>> attributes;
    protected final Set<LootRarity> rarities;

    public AttributeToggleAffix(AffixDefinition def, Set<Holder<Attribute>> attributes, Set<LootRarity> rarities) {
        super(def);
        this.attributes = attributes;
        this.rarities = rarities;
    }

    public Set<Holder<Attribute>> getAttributes() {
        return this.attributes;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat == LootCategories.BOOTS && this.rarities.contains(rarity);
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent desc = Component.translatable("affix." + this.id() + ".desc", Component.keybind(TOGGLE_KEY));
        Player player = ctx.player();
        if (player != null) {
            AttributeToggles toggles = getToggles(player);
            boolean suppressed = this.attributes.stream().allMatch(toggles::isSuppressed);
            MutableComponent state = Apotheosis.lang("misc", "attribute_toggle." + (suppressed ? "off" : "on"))
                .withStyle(suppressed ? ChatFormatting.RED : ChatFormatting.GREEN);
            desc.append(CommonComponents.SPACE).append(Apotheosis.lang("misc", "attribute_toggle.state", state));
        }
        return desc;
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return true;
    }

    // Static helpers

    public static AttributeToggles getToggles(Player player) {
        return player.getData(Apoth.Attachments.ATTRIBUTE_TOGGLES);
    }

    public static void setToggles(Player player, AttributeToggles toggles) {
        player.setData(Apoth.Attachments.ATTRIBUTE_TOGGLES, toggles);
    }

    /**
     * Streams every toggle affix present on the player's currently-worn boots.
     */
    public static Stream<AttributeToggleAffix> streamWornAffixes(Player player) {
        return AffixHelper.streamAffixes(player.getItemBySlot(EquipmentSlot.FEET))
            .map(AffixInstance::getAffix)
            .filter(AttributeToggleAffix.class::isInstance)
            .map(AttributeToggleAffix.class::cast);
    }

    /**
     * Collects every attribute that the player's worn boots allow toggling.
     */
    public static Set<Holder<Attribute>> getWornAttributes(Player player) {
        return streamWornAffixes(player).flatMap(a -> a.attributes.stream()).collect(Collectors.toSet());
    }

    /**
     * Drops any suppressed attributes that are no longer provided by the player's worn boots, syncing to the client if anything changed.
     * <p>
     * Called when the boots slot changes and when the player joins a level.
     */
    public static void prune(ServerPlayer player) {
        AttributeToggles toggles = getToggles(player);
        if (toggles.isEmpty()) {
            return;
        }
        AttributeToggles pruned = toggles.retain(getWornAttributes(player));
        if (pruned != toggles) {
            setToggles(player, pruned);
            sync(player);
        }
    }

    /**
     * Server-side handler for the toggle keybind. If any attribute provided by the worn boots is suppressed, all of them are
     * re-enabled; otherwise all of them are suppressed. The result is synced to the client.
     */
    public static void handleToggle(ServerPlayer player) {
        Set<Holder<Attribute>> attrs = getWornAttributes(player);
        if (attrs.isEmpty()) {
            player.sendSystemMessage(Apotheosis.sysMessageHeader().append(Apotheosis.lang("misc", "attribute_toggle.none").withStyle(ChatFormatting.YELLOW)));
            return;
        }

        AttributeToggles toggles = getToggles(player);
        boolean suppress = attrs.stream().noneMatch(toggles::isSuppressed);
        setToggles(player, toggles.withSuppressed(attrs, suppress));
        sync(player);

        MutableComponent state = Apotheosis.lang("misc", "attribute_toggle." + (suppress ? "off" : "on"))
            .withStyle(suppress ? ChatFormatting.RED : ChatFormatting.GREEN);
        for (Holder<Attribute> attr : attrs) {
            Component attrName = Component.translatable(attr.value().getDescriptionId());
            player.sendSystemMessage(Apotheosis.sysMessageHeader().append(Apotheosis.lang("misc", "attribute_toggle.updated", attrName, state).withStyle(ChatFormatting.YELLOW)));
        }
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new AttributeTogglesPayload(getToggles(player)));
    }

}
