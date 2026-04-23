package dev.shadowsoffire.apotheosis.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
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
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingRecipeCache;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingScreen;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingTableTileRenderer;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipeCache;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingScreen;
import dev.shadowsoffire.apotheosis.client.SocketTooltipRenderer.SocketComponent;
import dev.shadowsoffire.apotheosis.client.StoneformingTooltipRenderer.StoneformingComponent;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload.BossSpawnData;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipeCache;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import net.neoforged.neoforge.client.pipeline.RegisterPipelineModifiersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = Apotheosis.MODID, value = Dist.CLIENT)
public class AdventureModuleClient {

    public static final int COMPARE_PADDING = 18;

    public static final StandaloneModelKey<BlockStateModel> HAMMER_MODEL = new StandaloneModelKey<>(() -> "apotheosis:hammer");
    public static final StandaloneModelKey<BlockStateModel> STAR_CUBE_MODEL = new StandaloneModelKey<>(() -> "apotheosis:star_cube");

    private static final Identifier COMPARE_TOOLTIP_SPRITES = Apotheosis.loc("compare");

    public static final int[] GHOST_ALPHA_TIERS = {
        0x40, 0x48, 0x50, 0x58, 0x60, 0x68, 0x70, 0x78, 0x80, 0x88,
        0x90, 0x98, 0xA0, 0xA8, 0xB0, 0xB8, 0xC0, 0xC8, 0xD0, 0xD8, 0xE0
    };

    @SuppressWarnings("unchecked")
    public static final ResourceKey<PipelineModifier>[] GHOST_ITEM_TIERS = new ResourceKey[GHOST_ALPHA_TIERS.length];
    static {
        for (int i = 0; i < GHOST_ALPHA_TIERS.length; i++) {
            GHOST_ITEM_TIERS[i] = ResourceKey.create(PipelineModifier.MODIFIERS_KEY, Apotheosis.loc("ghost_item_" + Integer.toHexString(GHOST_ALPHA_TIERS[i])));
        }
    }

    public static final ResourceKey<PipelineModifier> GRAY_ITEM = ResourceKey.create(PipelineModifier.MODIFIERS_KEY, Apotheosis.loc("gray_item"));

    private static final List<BossSpawnData> BOSS_SPAWNS = new ArrayList<>();
    private static final Component GEM_SOCKET_MARKER = Component.literal("APOTH_SOCKET_MARKER");

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            BlockEntityRenderers.register(Apoth.Tiles.REFORGING_TABLE, ReforgingTableTileRenderer::new);
            BlockEntityRenderers.register(Apoth.Tiles.AUGMENTING_TABLE, AugmentingTableTileRenderer::new);
            BlockEntityRenderers.register(Apoth.Tiles.GEM_CASE, GemCaseTileRenderer::new);
            BlockEntityRenderers.register(Apoth.Tiles.ENDER_GEM_CASE, GemCaseTileRenderer::new);
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
    public static void registerStandaloneModels(ModelEvent.RegisterStandalone e) {
        e.register(HAMMER_MODEL, SimpleUnbakedStandaloneModel.blockStateModel(Apotheosis.loc("item/hammer")));
        e.register(STAR_CUBE_MODEL, SimpleUnbakedStandaloneModel.blockStateModel(Apotheosis.loc("item/star_cube")));
        addGemModels(e);
    }

    private static void addGemModels(ModelEvent.RegisterStandalone e) {
        GemModel.GEM_MODEL_KEYS.clear();
        Minecraft.getInstance().getResourceManager()
            .listResources("models/item/gems", loc -> loc.getPath().endsWith(".json"))
            .keySet()
            .forEach(loc -> {
                String fullPath = loc.getPath();
                String modelPath = fullPath.substring("models/".length(), fullPath.length() - ".json".length());
                Identifier cuboidModelId = Identifier.fromNamespaceAndPath(loc.getNamespace(), modelPath);
                String gemName = modelPath.substring(modelPath.indexOf("item/gems/") + "item/gems/".length());
                DynamicHolder<Gem> gemId = GemRegistry.INSTANCE.holder(Identifier.fromNamespaceAndPath(loc.getNamespace(), gemName));
                StandaloneModelKey<ItemModel> key = new StandaloneModelKey<>(cuboidModelId::toString);
                GemModel.GEM_MODEL_KEYS.put(gemId, key);
                e.register(key, new SimpleUnbakedStandaloneModel<ItemModel>(cuboidModelId, (resolvedModel, baker, name) -> {
                    TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
                    QuadCollection quads = resolvedModel.bakeTopGeometry(textureSlots, baker, BlockModelRotation.IDENTITY);
                    ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedModel, textureSlots);
                    return new CuboidItemModelWrapper(List.of(), quads, properties, new Matrix4f());
                }));
            });
    }

    @SubscribeEvent
    public static void itemModels(RegisterItemModelsEvent e) {
        e.register(Apotheosis.loc("gem_dispatch"), GemModel.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void tooltipComps(RegisterClientTooltipComponentFactoriesEvent e) {
        e.register(SocketComponent.class, SocketTooltipRenderer::new);
        e.register(StoneformingComponent.class, StoneformingTooltipRenderer::new);
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        e.registerCategory(AdventureKeys.CATEGORY);
        e.register(AdventureKeys.TOGGLE_RADIAL);
        e.register(AdventureKeys.OPEN_WORLD_TIER_SELECT);
        e.register(AdventureKeys.LINK_ITEM_TO_CHAT);
        e.register(AdventureKeys.COMPARE_EQUIPMENT);
    }

    @SubscribeEvent
    public static void factories(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(Apoth.Particles.RARITY_GLOW, RarityParticle.Provider::new);
    }

    @SubscribeEvent
    public static void modifiers(RegisterPipelineModifiersEvent e) {
        for (int i = 0; i < GHOST_ALPHA_TIERS.length; i++) {
            final float alpha = GHOST_ALPHA_TIERS[i] / 255f;
            e.register(GHOST_ITEM_TIERS[i], (pipeline, name) -> {
                Identifier loc = pipeline.getLocation();
                if (loc.equals(RenderPipelines.ITEM_CUTOUT.getLocation()) || loc.equals(RenderPipelines.ITEM_TRANSLUCENT.getLocation())) {
                    return pipeline.toBuilder()
                        .withLocation(name)
                        .withFragmentShader(Apotheosis.loc("core/ghost"))
                        .withShaderDefine("APOTH_GHOST_ALPHA", alpha)
                        .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                        .build();
                }
                return pipeline;
            });
        }
        e.register(GRAY_ITEM, (pipeline, name) -> {
            Identifier loc = pipeline.getLocation();
            if (loc.equals(RenderPipelines.ITEM_CUTOUT.getLocation()) || loc.equals(RenderPipelines.ITEM_TRANSLUCENT.getLocation())) {
                return pipeline.toBuilder()
                    .withLocation(name)
                    .withFragmentShader(Apotheosis.loc("core/gray"))
                    .build();
            }
            return pipeline;
        });
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
            Identifier id = AffixRegistry.INSTANCE.getKey(a);
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
        return AttributeTooltipContext.of(Minecraft.getInstance().player, TooltipContext.of(Minecraft.getInstance().level), net.minecraft.world.item.component.TooltipDisplay.DEFAULT, ApothicAttributes.getTooltipFlag());
    }

    @SubscribeEvent
    public static void renderBossBeams(net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent e) {
        if (BOSS_SPAWNS.isEmpty()) {
            return;
        }

        net.minecraft.world.phys.Vec3 camPos = e.getLevelRenderState().cameraRenderState.pos;
        com.mojang.blaze3d.vertex.PoseStack poseStack = e.getPoseStack();
        net.minecraft.client.renderer.SubmitNodeCollector collector = e.getSubmitNodeCollector();
        float animTime = e.getLevelRenderState().gameTime;

        for (BossSpawnData data : BOSS_SPAWNS) {
            BlockPos pos = data.pos();
            int color = 0xFF000000 | data.rarity().color().getValue();

            poseStack.pushPose();
            poseStack.translate(pos.getX() - camPos.x, pos.getY() - camPos.y, pos.getZ() - camPos.z);
            net.minecraft.client.renderer.blockentity.BeaconRenderer.submitBeaconBeam(
                poseStack, collector,
                net.minecraft.client.renderer.blockentity.BeaconRenderer.BEAM_LOCATION,
                1.0F, animTime, 0, 1024, color, 0.2F, 0.25F);
            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void login(ClientPlayerNetworkEvent.LoggingIn e) {
        // Since we use the stats to determine if the world tier tutorial is active, we need to send the request here.
        e.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut e) {
        SalvagingRecipeCache.clear();
        GemCuttingRecipeCache.clear();
        ReforgingRecipeCache.clear();
    }

    @SubscribeEvent
    public static void recipesReceived(RecipesReceivedEvent e) {
        if (e.getRecipeTypes().contains(Apoth.RecipeTypes.SALVAGING)) {
            SalvagingRecipeCache.rebuildFromMap(e.getRecipeMap());
        }
        if (e.getRecipeTypes().contains(Apoth.RecipeTypes.GEM_CUTTING)) {
            GemCuttingRecipeCache.rebuildFromMap(e.getRecipeMap());
        }
        if (e.getRecipeTypes().contains(Apoth.RecipeTypes.REFORGING)) {
            ReforgingRecipeCache.rebuildFromMap(e.getRecipeMap());
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
    public static void comps(net.neoforged.neoforge.client.event.RenderTooltipEvent.GatherComponents e) {
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
        AttributeTooltipContext ctx = AttributeTooltipContext.of(Minecraft.getInstance().player, e.getContext(),
            stack.getOrDefault(net.minecraft.core.component.DataComponents.TOOLTIP_DISPLAY, net.minecraft.world.item.component.TooltipDisplay.DEFAULT), e.getFlags());

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

            if (!dev.shadowsoffire.apotheosis.item.PotionCharmItem.isValidPotion(potion)) {
                e.getToolTip().add(Component.translatable("misc.apotheosis.blacklisted_potion").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }
        }
    }

    @SubscribeEvent
    public static void renderCanSocketTooltip(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post e) {
        if (!(e.getScreen() instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> screen)) {
            return;
        }

        ItemStack carried = screen.getMenu().getCarried();
        net.minecraft.world.inventory.Slot slot = screen.getHoveredSlot();
        if (slot == null) {
            return;
        }

        ItemStack hover = slot.getItem();
        if (!carried.is(Apoth.Items.GEM) || !SocketHelper.canSocketGemInItem(hover, carried)) {
            return;
        }

        Component itemName = Component.translatable("%s", hover.getHoverName()).withStyle(ChatFormatting.WHITE);
        Component line = Apotheosis.lang("misc", "right_click_to_socket", carried.getHoverName(), itemName).withStyle(ChatFormatting.GRAY);

        Font font = Minecraft.getInstance().font;
        List<ClientTooltipComponent> comps = List.of(
            ClientTooltipComponent.create(line.getVisualOrderText()));
        e.getGuiGraphics().tooltip(font, comps, e.getMouseX(), e.getMouseY(),
            DefaultTooltipPositioner.INSTANCE, null);
    }

    private static boolean inComparisonRender = false;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void compareItems(net.neoforged.neoforge.client.event.RenderTooltipEvent.Pre e) {
        if (inComparisonRender || !AdventureConfig.enableEquipmentCompare) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (!ApothMiscUtil.ClientInternal.isKeyReallyDown(AdventureKeys.COMPARE_EQUIPMENT) || !(mc.screen instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        Slot slot = screen.getHoveredSlot();
        if (slot == null || !slot.hasItem() || slot.getItem() != e.getItemStack()) {
            return;
        }

        ItemStack stack = e.getItemStack();
        LootCategory cat = LootCategory.forItem(stack);
        if (cat.isNone()) {
            return;
        }

        Player player = mc.player;
        ItemStack equipped = ItemStack.EMPTY;

        // If the item is an equipable, find it's slot and do the comparison there.
        // TODO: Update this whole block to some kind of iterate-over-slots and check categories thingy.
        // I think that will automatically pick up curios? Not sure.
        Equippable equip = stack.get(DataComponents.EQUIPPABLE);
        if (equip != null) {
            ItemStack candidate = player.getItemBySlot(equip.slot());
            if (!candidate.isEmpty() && stack != candidate) {
                equipped = candidate;
            }
        }
        else {
            // Otherwise... well, the item lives in a hand, though we don't know which one necessarily.
            // So we need to look at both, and find one with the same loot category to do the comparison.
            if (cat.getSlots().test(ALObjects.EquipmentSlots.MAINHAND)) {
                ItemStack candidate = player.getMainHandItem();
                if (LootCategory.forItem(candidate) == cat && stack != candidate) {
                    equipped = candidate;
                }
            }
            if (equipped.isEmpty() && cat.getSlots().test(ALObjects.EquipmentSlots.OFFHAND)) {
                ItemStack candidate = player.getOffhandItem();
                if (LootCategory.forItem(candidate) == cat && stack != candidate) {
                    equipped = candidate;
                }
            }
        }

        if (equipped.isEmpty()) {
            return;
        }

        tryRenderComparison(e, mc, equipped);
    }

    private static void tryRenderComparison(net.neoforged.neoforge.client.event.RenderTooltipEvent.Pre e, Minecraft mc, ItemStack equipped) {
        Font font = e.getFont();
        GuiGraphicsExtractor gfx = e.getGraphics();
        ClientTooltipPositioner positioner = e.getTooltipPositioner();

        int scnWidth = e.getScreenWidth();
        int scnHeight = e.getScreenHeight();

        List<ClientTooltipComponent> compList = e.getComponents();
        int compWidth = -1;
        int compHeight = 0;
        for (var comp : compList) {
            compWidth = Math.max(compWidth, comp.getWidth(font));
            compHeight += comp.getHeight(font);
        }

        // Ask the positioner for the default position of the original item.
        // In an ideal case, the original item fits, and the new tooltip fits to the left of it without any change.
        Vector2ic compPos = positioner.positionTooltip(scnWidth, scnHeight, e.getX(), e.getY(), compWidth, compHeight);

        // Lie about the x pos (0) and GUI width (width - compWidth) here to get the "best" line wrapping.
        // This combo allows the components to be split in a way that has the highest likelihood the two tooltips will fit on the screen.
        List<Component> equipLines = Screen.getTooltipFromItem(mc, equipped);
        List<ClientTooltipComponent> equipList = ClientHooks.gatherTooltipComponents(equipped, equipLines, equipped.getTooltipImage(), 0, gfx.guiWidth() - compWidth - COMPARE_PADDING * 2, gfx.guiHeight(), font);

        int equipWidth = -1;
        int equipHeight = 0;
        for (var comp : equipList) {
            equipWidth = Math.max(equipWidth, comp.getWidth(font));
            equipHeight += comp.getHeight(font);
        }

        // Compute the default position for the equipped item hover.
        // We try to put it to the left, at the same Y-level. The 12 px are padding to ensure the mouse doesn't overlap the tooltip.
        Vector2ic equipPos = new Vector2i(compPos.x() - COMPARE_PADDING - equipWidth, compPos.y());

        EquipmentComparePositioner realPositioner = new EquipmentComparePositioner(scnWidth, scnHeight);
        boolean canRender = realPositioner.position(equipPos, equipWidth + 6, equipHeight + 6, compPos, compWidth + 6, compHeight + 6);

        if (!canRender) {
            return;
        }

        e.setCanceled(true);

        inComparisonRender = true;
        try {
            final Vector2ic finalCompPos = realPositioner.getComparePos();
            final Vector2ic finalEquipPos = realPositioner.getEquippedPos();

            // Fixed-position positioners force the tooltips to our computed positions rather than letting vanilla re-align to mouse.
            ClientTooltipPositioner compFixed = (sw, sh, mx, my, w, h) -> finalCompPos;
            ClientTooltipPositioner equipFixed = (sw, sh, mx, my, w, h) -> finalEquipPos;

            // Draw the hovered item's tooltip at its computed position.
            gfx.tooltip(font, compList, finalCompPos.x(), finalCompPos.y(), compFixed, null, e.getItemStack());

            // Draw the equipped item's tooltip at the paired position.
            gfx.tooltip(font, equipList, finalEquipPos.x(), finalEquipPos.y(), equipFixed, null, equipped);

            // Draw the "Currently Equipped" label above the equipped tooltip with a vanilla-style background.
            Component equippedTxt = Apotheosis.lang("text", "equipped");
            int txtWidth = font.width(equippedTxt);
            int txtX = (finalEquipPos.x() + (equipWidth / 2)) - txtWidth / 2;
            int txtY = finalEquipPos.y() - font.lineHeight - 10;

            TooltipRenderUtil.extractTooltipBackground(gfx, txtX, txtY, txtWidth, font.lineHeight, COMPARE_TOOLTIP_SPRITES);
            gfx.text(font, equippedTxt, txtX, txtY, 0xFFFFFFFF);
        }
        finally {
            inComparisonRender = false;
        }
    }

}
