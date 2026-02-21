package dev.shadowsoffire.apotheosis.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;

import dev.shadowsoffire.apotheosis.AdventureConfig;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apoth.Menus;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.affix.AttributeProvidingAffix;
import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingScreen;
import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingTableTileRenderer;
import dev.shadowsoffire.apotheosis.affix.effect.StoneformingAffix;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingScreen;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingTableTileRenderer;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingScreen;
import dev.shadowsoffire.apotheosis.client.SocketTooltipRenderer.SocketComponent;
import dev.shadowsoffire.apotheosis.client.StoneformingTooltipRenderer.StoneformingComponent;
import dev.shadowsoffire.apotheosis.item.PotionCharmItem;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.mixin.client.GuiGraphicsAccessor;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload.BossSpawnData;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingScreen;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseScreen;
import dev.shadowsoffire.apotheosis.socket.gem.storage.GemCaseTileRenderer;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apotheosis.util.EquipmentComparePositioner;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Apotheosis.MODID, value = Dist.CLIENT)
public class AdventureModuleClient {

    public static final int COMPARE_PADDING = 18;

    private static final List<BossSpawnData> BOSS_SPAWNS = new ArrayList<>();
    private static final Component GEM_SOCKET_MARKER = Component.literal("APOTH_SOCKET_MARKER");

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            BlockEntityRenderers.register(Apoth.Tiles.REFORGING_TABLE, k -> new ReforgingTableTileRenderer());
            BlockEntityRenderers.register(Apoth.Tiles.AUGMENTING_TABLE, k -> new AugmentingTableTileRenderer());
            BlockEntityRenderers.register(Apoth.Tiles.GEM_CASE, k -> new GemCaseTileRenderer());
            BlockEntityRenderers.register(Apoth.Tiles.ENDER_GEM_CASE, k -> new GemCaseTileRenderer());

            ItemProperties.register(Apoth.Items.GEM.value(), Apotheosis.loc("purity"), (stack, level, entity, tint) -> {
                DynamicHolder<Gem> gem = GemItem.getGem(stack);
                Purity purity = GemItem.getPurity(stack);
                return gem.isBound() ? purity.ordinal() : Float.NEGATIVE_INFINITY;
            });

            ItemProperties.register(Apoth.Items.POTION_CHARM.value(), Apotheosis.loc("enabled"), (stack, level, entity, tint) -> {
                return stack.getOrDefault(Components.CHARM_ENABLED, false) ? 1 : 0;
            });
        });
        NeoForge.EVENT_BUS.register(AdventureKeys.class);
        NeoForge.EVENT_BUS.register(RadialProgressTracker.class);
    }

    @SubscribeEvent
    public static void screens(RegisterMenuScreensEvent e) {
        e.register(Menus.REFORGING, ReforgingScreen::new);
        e.register(Menus.SALVAGE, SalvagingScreen::new);
        e.register(Menus.GEM_CUTTING, GemCuttingScreen::new);
        e.register(Menus.AUGMENTING, AugmentingScreen::new);
        e.register(Menus.GEM_CASE, GemCaseScreen::new);
    }

    @SubscribeEvent
    public static void models(ModelEvent.RegisterAdditional e) {
        e.register(ReforgingTableTileRenderer.HAMMER);
        e.register(AugmentingTableTileRenderer.STAR_CUBE);
    }

    @SubscribeEvent
    public static void tooltipComps(RegisterClientTooltipComponentFactoriesEvent e) {
        e.register(SocketComponent.class, SocketTooltipRenderer::new);
        e.register(StoneformingComponent.class, StoneformingTooltipRenderer::new);
    }

    @SubscribeEvent
    public static void addGemModels(ModelEvent.RegisterAdditional e) {
        Set<ResourceLocation> locs = Minecraft.getInstance().getResourceManager().listResources("models", loc -> Apotheosis.MODID.equals(loc.getNamespace()) && loc.getPath().contains("/gems/") && loc.getPath().endsWith(".json"))
            .keySet();
        for (ResourceLocation s : locs) {
            String path = s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());
            e.register(ModelResourceLocation.standalone(Apotheosis.loc(path)));
        }
    }

    @SubscribeEvent
    public static void replaceGemModel(ModelEvent.ModifyBakingResult e) {
        ModelResourceLocation key = ModelResourceLocation.inventory(Apotheosis.loc("gem"));
        BakedModel oldModel = e.getModels().get(key);
        if (oldModel != null) {
            e.getModels().put(key, new GemModel(oldModel, e.getModelBakery()));
        }
    }

    @SubscribeEvent
    public static void shaderRegistry(RegisterShadersEvent event) throws IOException {
        // Adds a shader to the list, the callback runs when loading is complete.
        event.registerShader(new ShaderInstance(event.getResourceProvider(), Apotheosis.loc("gray"), DefaultVertexFormat.NEW_ENTITY), shaderInstance -> {
            CustomRenderTypes.grayShader = shaderInstance;
        });
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(AdventureKeys.TOGGLE_RADIAL);
        e.register(AdventureKeys.OPEN_WORLD_TIER_SELECT);
        e.register(AdventureKeys.LINK_ITEM_TO_CHAT);
        e.register(AdventureKeys.COMPARE_EQUIPMENT);
    }

    @SubscribeEvent
    public static void colors(RegisterColorHandlersEvent.Item e) {
        e.register(
            (stack, tint) -> tint == 0 ? -1 : FastColor.ARGB32.opaque(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor()),
            Apoth.Items.POTION_CHARM.value());
    }

    @SubscribeEvent
    public static void factories(RegisterParticleProvidersEvent e) {
        e.registerSprite(Apoth.Particles.RARITY_GLOW, RarityParticle::new);
    }

    public static void onBossSpawn(BlockPos pos, DynamicHolder<LootRarity> rarityHolder) {
        if (rarityHolder.isBound()) {
            LootRarity rarity = rarityHolder.get();
            BOSS_SPAWNS.add(new BossSpawnData(pos, rarity, new MutableInt()));
            Minecraft.getInstance().getSoundManager()
                .play(new SimpleSoundInstance(rarity.invaderSound(), SoundSource.HOSTILE, AdventureConfig.bossAnnounceRange / 16F, 1.0F, Minecraft.getInstance().player.getRandom(), pos));
        }
    }

    public static void checkAffixLangKeys() {
        if (DatagenModLoader.isRunningDataGen()) {
            return; // TODO: Load the lang file, somehow
        }

        StringBuilder sb = new StringBuilder("Missing Affix Lang Keys:\n");
        boolean any = false;
        String json = "\"%s\": \"\",";
        for (Affix a : AffixRegistry.INSTANCE.getValues()) {
            ResourceLocation id = AffixRegistry.INSTANCE.getKey(a);
            if (!I18n.exists("affix." + id)) {
                sb.append(json.formatted("affix." + id) + "\n");
                any = true;
            }
            if (!I18n.exists("affix." + id + ".suffix")) {
                sb.append(json.formatted("affix." + id + ".suffix") + "\n");
                any = true;
            }
        }
        if (any) {
            Apotheosis.LOGGER.error(sb.toString());
        }
    }

    public static AttributeTooltipContext tooltipCtx() {
        return AttributeTooltipContext.of(Minecraft.getInstance().player, TooltipContext.of(Minecraft.getInstance().level), ApothicAttributes.getTooltipFlag());
    }

    // Accessor functon, ensures that you don't use the raw methods below unintentionally.
    public static RenderType gray(ResourceLocation texture) {
        return CustomRenderTypes.GRAY.apply(texture);
    }

    @SubscribeEvent
    public static void login(ClientPlayerNetworkEvent.LoggingIn e) {
        // Since we use the stats to determine if the world tier tutorial is active, we need to send the request here.
        e.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent e) {
        if (e.getStage() != Stage.AFTER_TRIPWIRE_BLOCKS) {
            return;
        }

        PoseStack stack = e.getPoseStack();
        Player p = Minecraft.getInstance().player;
        BufferSource buf = Minecraft.getInstance().renderBuffers().bufferSource();

        for (BossSpawnData data : BOSS_SPAWNS) {
            stack.pushPose();
            float partials = e.getPartialTick().getGameTimeDeltaPartialTick(false);
            Vec3 vec = e.getCamera().getPosition();
            stack.translate(-vec.x, -vec.y, -vec.z);
            stack.translate(data.pos().getX(), data.pos().getY(), data.pos().getZ());
            BeaconRenderer.renderBeaconBeam(stack, buf, BeaconRenderer.BEAM_LOCATION, partials, 1, p.level().getGameTime(), 0, 64, data.rarity().color().getValue(), 0.166F, 0.33F);
            stack.popPose();
        }
    }

    @SubscribeEvent
    public static void time(ClientTickEvent.Post e) {
        for (int i = 0; i < BOSS_SPAWNS.size(); i++) {
            BossSpawnData data = BOSS_SPAWNS.get(i);
            if (data.ticks().getAndIncrement() > 400) {
                BOSS_SPAWNS.remove(i--);
            }
        }
    }

    @SubscribeEvent
    public static void tooltips(AddAttributeTooltipsEvent e) {
        ItemStack stack = e.getStack();
        int sockets = SocketHelper.getSockets(stack);
        if (sockets > 0 && !WorldTier.isTutorialActive(Minecraft.getInstance().player)) {
            e.addTooltipLines(GEM_SOCKET_MARKER.copy());
        }
    }

    @SubscribeEvent
    public static void ignoreSocketUUIDS(GatherSkippedAttributeTooltipsEvent e) {
        ItemStack stack = e.getStack();
        for (GemInstance gem : SocketHelper.getGems(stack)) {
            if (gem.isValid()) {
                gem.skipModifierIds(e::skipId);
            }
        }
        AffixHelper.streamAffixes(stack).forEach(inst -> {
            if (inst.getAffix() instanceof AttributeProvidingAffix afx) {
                afx.skipModifierIds(inst, e.getContext(), e::skipId);
            }
        });
    }

    @SubscribeEvent
    public static void comps(RenderTooltipEvent.GatherComponents e) {
        List<Either<FormattedText, TooltipComponent>> list = e.getTooltipElements();
        for (int i = 0; i < list.size(); i++) {
            var entry = list.get(i);
            if (containsMarker(entry, GEM_SOCKET_MARKER)) {
                list.remove(i);
                list.add(i, Either.right(new SocketComponent(e.getItemStack(), SocketHelper.getGems(e.getItemStack()))));
            }
            else if (containsMarker(entry, StoneformingAffix.TOOLTIP_MARKER)) {
                list.remove(i);
                AffixInstance inst = AffixHelper.streamAffixes(e.getItemStack()).filter(a -> a.getAffix() instanceof StoneformingAffix).findFirst().orElse(null);
                if (inst != null) {
                    list.add(i, Either.right(new StoneformingComponent(inst)));
                }
            }
        }
    }

    private static boolean containsMarker(Either<FormattedText, TooltipComponent> entry, Component marker) {
        Optional<FormattedText> o = entry.left();
        return o.isPresent() && o.get() instanceof Component comp && comp.contains(marker);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void affixTooltips(ItemTooltipEvent e) {
        ItemStack stack = e.getItemStack();
        List<Component> components = new ArrayList<>();
        AttributeTooltipContext ctx = AttributeTooltipContext.of(Minecraft.getInstance().player, e.getContext(), e.getFlags());

        if (e.getEntity() != null && WorldTier.isTutorialActive(e.getEntity())) {
            if (stack.has(Components.AFFIXES) || stack.has(Components.SOCKETS) || stack.has(Components.RARITY)) {
                e.getToolTip().add(1, Apotheosis.lang("text", "world_tier_tutorial").withStyle(ChatFormatting.YELLOW));
                e.getToolTip().add(2, Apotheosis.lang("text", "world_tier_tutorial.2", AdventureKeys.OPEN_WORLD_TIER_SELECT.getTranslatedKeyMessage()).withStyle(ChatFormatting.YELLOW));
            }
            return;
        }

        if (stack.has(Components.AFFIXES)) {
            AffixHelper.streamAffixes(stack)
                .sorted(Comparator.comparingInt(a -> a.getAffix().definition().type().ordinal()))
                .forEach(inst -> {
                    Component desc = inst.getDescription(ctx);
                    if (desc.getContents() != PlainTextContents.EMPTY) {
                        if (inst.level() > Affix.STANDARD_MAX_LEVEL) {
                            components.add(ApothMiscUtil.starPrefix(desc).withStyle(ChatFormatting.YELLOW));
                        }
                        else {
                            components.add(ApothMiscUtil.dotPrefix(desc).withStyle(ChatFormatting.YELLOW));
                        }
                    }
                });
        }

        if (stack.has(Components.DURABILITY_BONUS) && !stack.has(DataComponents.UNBREAKABLE)) {
            Component desc = Component.translatable("affix.apotheosis:durable.desc", Math.round(100 * stack.get(Components.DURABILITY_BONUS)));
            components.add(ApothMiscUtil.dotPrefix(desc).withStyle(ChatFormatting.YELLOW));
        }

        if (stack.getOrDefault(Components.MALICE_MARKER, false)) {
            Component desc = Apotheosis.lang("text", "malice_marker").withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE);
            components.add(desc);
        }

        if (stack.getOrDefault(Components.TOUCHED_BY_MALICE, false)) {
            Component desc = Apotheosis.lang("text", "touched_by_malice");
            components.add(ApothMiscUtil.dotPrefix(desc).withStyle(ChatFormatting.RED));
        }

        if (!components.isEmpty()) {
            e.getToolTip().addAll(1, components);
        }

        // We want attribute modifiers that are being supplied by over-max affixes to reflect that in the tooltip.
        // However, there's not really any way to know which attribute modifiers are from affixes.
        // So to fix that, we have to ask all over-max affixes for their modifier tooltips, and search for them in the tooltip.
        // If we find them, we add a star prefix to them.
        Set<Component> special = new HashSet<>();
        AffixHelper.streamAffixes(stack)
            .filter(inst -> inst.level() > Affix.STANDARD_MAX_LEVEL)
            .filter(inst -> inst.getAffix() instanceof AttributeProvidingAffix)
            .forEach(inst -> ((AttributeProvidingAffix) inst.getAffix()).gatherModifierTooltips(inst, ctx, special::add));

        List<Component> tooltips = e.getToolTip();

        Component listHeader = Component.literal(" \u2507 ").withStyle(ChatFormatting.GRAY);

        if (!special.isEmpty()) {
            for (int i = 0; i < tooltips.size(); i++) {
                Component comp = tooltips.get(i);
                if (special.contains(comp)) {
                    tooltips.remove(i);
                    tooltips.add(i, ApothMiscUtil.starPrefix(comp).withStyle(comp.getStyle()));
                }
                // Try to find tooltips nested in a list header to apply the star to support merged tooltips.
                else if (comp.getContents().equals(listHeader.getContents()) && comp.getSiblings().size() == 1) {
                    Component child = comp.getSiblings().get(0);
                    if (special.contains(child)) {
                        tooltips.remove(i);
                        MutableComponent replacement = listHeader.copy();
                        replacement.append(ApothMiscUtil.starPrefix(child).withStyle(child.getStyle()));
                        for (int j = 1; j < comp.getSiblings().size(); j++) {
                            replacement.append(comp.getSiblings().get(j));
                        }
                        tooltips.add(i, replacement);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void showBlacklistedPotions(ItemTooltipEvent e) {
        if (e.getItemStack().getItem() == Items.POTION) {
            Holder<Potion> potion = e.getItemStack().getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(Potions.WATER);

            if (!PotionCharmItem.isValidPotion(potion)) {
                e.getToolTip().add(Component.translatable("misc.apotheosis.blacklisted_potion").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        }
    }

    @SubscribeEvent
    public static void renderCanSocketTooltip(ScreenEvent.Render.Post e) {
        if (e.getScreen() instanceof AbstractContainerScreen<?> screen) {
            ItemStack carried = screen.getMenu().getCarried();
            ItemStack hover = screen.getSlotUnderMouse() == null ? ItemStack.EMPTY : screen.getSlotUnderMouse().getItem();
            if (carried.is(Apoth.Items.GEM) && SocketHelper.canSocketGemInItem(hover, carried)) {
                GuiGraphics gfx = e.getGuiGraphics();
                List<Component> tooltip = new ArrayList<>();
                // We want the hovered item's name to be white by default, so we need to wrap it in a component specifying white.
                Component itemName = Component.translatable("%s", hover.getHoverName()).withStyle(ChatFormatting.WHITE);
                tooltip.add(Apotheosis.lang("misc", "right_click_to_socket", carried.getHoverName(), itemName).withStyle(ChatFormatting.GRAY));
                gfx.pose().pushPose();
                gfx.pose().translate(0, 0, 400);
                e.getGuiGraphics().renderComponentTooltip(Minecraft.getInstance().font, tooltip, e.getMouseX(), e.getMouseY());
                gfx.pose().popPose();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void compareItems(RenderTooltipEvent.Pre e) {
        if (!AdventureConfig.enableEquipmentCompare) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (!ApothMiscUtil.ClientInternal.isKeyReallyDown(AdventureKeys.COMPARE_EQUIPMENT) || !(mc.screen instanceof AbstractContainerScreen)) {
            return;
        }

        Slot slot = ((AbstractContainerScreen<?>) mc.screen).getSlotUnderMouse();
        if (slot == null || !slot.hasItem() || slot.getItem() != e.getItemStack()) {
            return;
        }

        ItemStack stack = e.getItemStack();
        LootCategory cat = LootCategory.forItem(stack);
        if (cat.isNone()) {
            return;
        }

        Player player = mc.player;
        // If the item is an equipable, find it's slot and do the comparison there.
        // TODO: Update this whole block to some kind of iterate-over-slots and check categories thingy.
        // I think that will automatically pick up curios? Not sure.
        if (stack.getItem() instanceof Equipable equip) {
            EquipmentSlot equipmentSlot = equip.getEquipmentSlot();
            ItemStack equipped = player.getItemBySlot(equipmentSlot);
            if (!equipped.isEmpty() && stack != equipped) {
                tryRenderComparison(e, mc, equipped);
            }
        }
        else {
            // Otherwise... well, the item lives in a hand, though we don't know which one necessarily.
            // So we need to look at both, and find one with the same loot category to do the comparison.
            if (cat.getSlots().test(ALObjects.EquipmentSlots.MAINHAND)) {
                ItemStack equipped = player.getMainHandItem();
                LootCategory equippedCat = LootCategory.forItem(equipped);
                if (equippedCat == cat && stack != equipped) {
                    tryRenderComparison(e, mc, equipped);
                    return;
                }
            }

            if (cat.getSlots().test(ALObjects.EquipmentSlots.OFFHAND)) {
                ItemStack equipped = player.getOffhandItem();
                LootCategory equippedCat = LootCategory.forItem(equipped);
                if (equippedCat == cat && stack != equipped) {
                    tryRenderComparison(e, mc, equipped);
                    return;
                }
            }

            // And other than that it lives somewhere else entirely. Maybe we should fire an event here to lookup the comparison item?
            // Could be useful for addons extending the EntityEquipmentSlot API.
        }
    }

    private static boolean tryRenderComparison(RenderTooltipEvent.Pre e, Minecraft mc, ItemStack equipped) {
        Font font = e.getFont();
        GuiGraphics gfx = e.getGraphics();
        GuiGraphicsAccessor acc = (GuiGraphicsAccessor) gfx;

        ClientTooltipPositioner positioner = e.getTooltipPositioner();
        List<Component> equipLines = Screen.getTooltipFromItem(mc, equipped);

        int scnWidth = e.getScreenWidth();
        int scnHeight = e.getScreenHeight();

        List<ClientTooltipComponent> compList = e.getComponents();
        int compWidth = -1;
        int compHeight = 0;
        for (var comp : compList) {
            compWidth = Math.max(compWidth, comp.getWidth(font));
            compHeight += comp.getHeight();
        }

        // Ask the positioner for the default position of the original item.
        // In an ideal case, the original item fits, and the new tooltip fits to the left of it without any change.
        Vector2ic compPos = positioner.positionTooltip(scnWidth, scnHeight, e.getX(), e.getY(), compWidth, compHeight);

        // Lie about the x pos (0) and GUI width (width - compWidth) here to get the "best" line wrapping.
        // This combo allows the components to be split in a way that has the highest likelihood the two tooltips will fit on the screen.
        List<ClientTooltipComponent> equipList = ClientHooks.gatherTooltipComponents(equipped, equipLines, equipped.getTooltipImage(), 0, gfx.guiWidth() - compWidth - COMPARE_PADDING * 2, gfx.guiHeight(), font);
        int equipWidth = -1;
        int equipHeight = 0;
        for (var comp : equipList) {
            equipWidth = Math.max(equipWidth, comp.getWidth(font));
            equipHeight += comp.getHeight();
        }

        // Compute the default position for the equipped item hover.
        // We try to put it to the left, at the same Y-level. The 12 px are padding to ensure the mouse doesn't overlap the tooltip.
        Vector2ic equipPos = new Vector2i(compPos.x() - COMPARE_PADDING - equipWidth, compPos.y());

        EquipmentComparePositioner realPositioner = new EquipmentComparePositioner(scnWidth, scnHeight);

        boolean canRender = realPositioner.position(equipPos, equipWidth + 6, equipHeight + 6, compPos, compWidth + 6, compHeight + 6);

        if (canRender) {
            try {
                e.setCanceled(true);

                // Draw the hovered item's tooltip
                renderTooltipInternalNoEvent(gfx, font, compList, realPositioner.getComparePos());

                // Draw the equipped item's tooltip
                Vector2ic newEquipPos = realPositioner.getEquippedPos();
                acc.setTooltipStack(equipped);
                renderTooltipInternalNoEvent(gfx, font, equipList, newEquipPos);

                // Reset the tooltip stack and draw the "equipped" text above the equipped item.
                // We (optimistically) hope it fits, and don't check if it does or not.
                acc.setTooltipStack(ItemStack.EMPTY);

                Component equippedTxt = Apotheosis.lang("text", "equipped");
                int txtWidth = font.width(equippedTxt);
                int txtX = (newEquipPos.x() + (equipWidth / 2)) - txtWidth / 2;
                int txtY = newEquipPos.y() - font.lineHeight - 10;

                PoseStack pose = gfx.pose();
                pose.pushPose();

                // Colors
                final int bgColor = 0xFF14141E;
                final int borderTop = 0xEF5E5D89;
                final int borderBot = 0xEF393954;

                TooltipRenderUtil.renderTooltipBackground(gfx, txtX - 15, txtY, font.width(equippedTxt) + 30, font.lineHeight, 400, bgColor, bgColor, borderTop, borderBot);
                pose.translate(0, 0, 400);
                gfx.drawString(font, equippedTxt, txtX, txtY, 0xFFFFFF);

                pose.popPose();
            }
            finally {
                acc.setTooltipStack(e.getItemStack());
            }
            return true;
        }
        else {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private static void renderTooltipInternalNoEvent(GuiGraphics gfx, Font font, List<ClientTooltipComponent> components, Vector2ic pos) {
        if (!components.isEmpty()) {
            GuiGraphicsAccessor acc = (GuiGraphicsAccessor) gfx;

            int i = 0;
            int j = components.size() == 1 ? -2 : 0;

            for (ClientTooltipComponent clienttooltipcomponent : components) {
                int k = clienttooltipcomponent.getWidth(font);
                if (k > i) {
                    i = k;
                }

                j += clienttooltipcomponent.getHeight();
            }

            int i2 = i;
            int j2 = j;
            Vector2ic vector2ic = pos;
            int l = vector2ic.x();
            int i1 = vector2ic.y();
            gfx.pose().pushPose();
            RenderTooltipEvent.Color colorEvent = ClientHooks.onRenderTooltipColor(acc.getTooltipStack(), gfx, l, i1, font, components);
            gfx.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(gfx, l, i1, i2, j2, 400, colorEvent.getBackgroundStart(), colorEvent.getBackgroundEnd(), colorEvent.getBorderStart(), colorEvent.getBorderEnd()));
            gfx.pose().translate(0.0F, 0.0F, 400.0F);
            int k1 = i1;

            for (int l1 = 0; l1 < components.size(); l1++) {
                ClientTooltipComponent clienttooltipcomponent1 = components.get(l1);
                clienttooltipcomponent1.renderText(font, l, k1, gfx.pose().last().pose(), gfx.bufferSource());
                k1 += clienttooltipcomponent1.getHeight() + (l1 == 0 ? 2 : 0);
            }

            k1 = i1;

            for (int k2 = 0; k2 < components.size(); k2++) {
                ClientTooltipComponent clienttooltipcomponent2 = components.get(k2);
                clienttooltipcomponent2.renderImage(font, l, k1, gfx);
                k1 += clienttooltipcomponent2.getHeight() + (k2 == 0 ? 2 : 0);
            }

            gfx.pose().popPose();
        }
    }

    // Keep private because this stuff isn't meant to be public
    private static class CustomRenderTypes extends RenderType {
        // Holds the object loaded via RegisterShadersEvent
        private static ShaderInstance grayShader;

        // Shader state for use in the render type, the supplier ensures it updates automatically with resource reloads
        private static final ShaderStateShard RENDER_TYPE_GRAY = new ShaderStateShard(() -> grayShader);

        // The memoize caches the output value for each input, meaning the expensive registration process doesn't have to rerun
        public static Function<ResourceLocation, RenderType> GRAY = Util.memoize(CustomRenderTypes::gray);

        // Defines the RenderType. Make sure the name is unique by including your MODID in the name.
        private static RenderType gray(ResourceLocation loc) {

            RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                .setShaderState(RENDER_TYPE_GRAY)
                .setTextureState(new RenderStateShard.TextureStateShard(loc, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .createCompositeState(true);
            return create("gray", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$state);

        }

        // Dummy constructor needed to make java happy
        private CustomRenderTypes(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b2, Runnable r, Runnable r2) {
            super(s, v, m, i, b, b2, r, r2);
            throw new IllegalStateException("This class is not meant to be constructed!");
        }
    }

}
