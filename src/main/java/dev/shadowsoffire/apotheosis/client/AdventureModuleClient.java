package dev.shadowsoffire.apotheosis.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
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
import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingScreen;
import dev.shadowsoffire.apotheosis.affix.augmenting.AugmentingTableTileRenderer;
import dev.shadowsoffire.apotheosis.affix.effect.StoneformingAffix;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingScreen;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingTableTileRenderer;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingScreen;
import dev.shadowsoffire.apotheosis.client.SocketTooltipRenderer.SocketComponent;
import dev.shadowsoffire.apotheosis.client.StoneformingTooltipRenderer.StoneformingComponent;
import dev.shadowsoffire.apotheosis.item.PotionCharmItem;
import dev.shadowsoffire.apotheosis.net.BossSpawnPayload.BossSpawnData;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingScreen;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.apothic_attributes.ApothicAttributes;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
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
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
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
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.GatherSkippedAttributeTooltipsEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(bus = Bus.MOD, modid = Apotheosis.MODID, value = Dist.CLIENT)
public class AdventureModuleClient {

    public static List<BossSpawnData> BOSS_SPAWNS = new ArrayList<>();

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent e) {
        e.enqueueWork(() -> {
            BlockEntityRenderers.register(Apoth.Tiles.REFORGING_TABLE, k -> new ReforgingTableTileRenderer());
            BlockEntityRenderers.register(Apoth.Tiles.AUGMENTING_TABLE, k -> new AugmentingTableTileRenderer());

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
        NeoForge.EVENT_BUS.register(GameBusEvents.class);
    }

    @SubscribeEvent
    public static void screens(RegisterMenuScreensEvent e) {
        e.register(Menus.REFORGING, ReforgingScreen::new);
        e.register(Menus.SALVAGE, SalvagingScreen::new);
        e.register(Menus.GEM_CUTTING, GemCuttingScreen::new);
        e.register(Menus.AUGMENTING, AugmentingScreen::new);
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
        ModelResourceLocation key = new ModelResourceLocation(Apotheosis.loc("gem"), "inventory");
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
    }

    @SubscribeEvent
    public static void colors(RegisterColorHandlersEvent.Item e) {
        e.register(
            (stack, tint) -> tint == 0 ? -1 : FastColor.ARGB32.opaque(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor()),
            Apoth.Items.POTION_CHARM.value());
    }

    public static void onBossSpawn(BlockPos pos, int color) {
        BOSS_SPAWNS.add(new BossSpawnData(pos, color, new MutableInt()));
        Minecraft.getInstance().getSoundManager()
            .play(new SimpleSoundInstance(SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, AdventureConfig.bossAnnounceVolume, 1.25F, Minecraft.getInstance().player.getRandom(), Minecraft.getInstance().player.blockPosition()));
    }

    public static void checkAffixLangKeys() {
        if (DatagenModLoader.isRunningDataGen()) return; // TODO: Load the lang file, somehow

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

    public static class GameBusEvents {

        private static MultiBufferSource.BufferSource buf = MultiBufferSource.immediate(new ByteBufferBuilder(512));

        @SubscribeEvent
        public static void render(RenderLevelStageEvent e) {
            if (e.getStage() != Stage.AFTER_TRIPWIRE_BLOCKS) return;
            PoseStack stack = e.getPoseStack();
            Player p = Minecraft.getInstance().player;
            for (int i = 0; i < BOSS_SPAWNS.size(); i++) {
                BossSpawnData data = BOSS_SPAWNS.get(i);
                stack.pushPose();
                float partials = e.getPartialTick().getGameTimeDeltaPartialTick(false);
                Vec3 vec = Minecraft.getInstance().getCameraEntity().getEyePosition(partials);
                stack.translate(-vec.x, -vec.y, -vec.z);
                stack.translate(data.pos().getX(), data.pos().getY(), data.pos().getZ());
                BeaconRenderer.renderBeaconBeam(stack, buf, BeaconRenderer.BEAM_LOCATION, partials, 1, p.level().getGameTime(), 0, 64, data.color(), 0.166F, 0.33F);
                stack.popPose();
            }
            buf.endBatch();
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

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void tooltips(AddAttributeTooltipsEvent e) {
            ItemStack stack = e.getStack();
            int sockets = SocketHelper.getSockets(stack);
            if (sockets > 0) {
                e.addTooltipLines(Component.literal("APOTH_REMOVE_MARKER"));
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
        }

        @SubscribeEvent
        public static void comps(RenderTooltipEvent.GatherComponents e) {
            List<Either<FormattedText, TooltipComponent>> list = e.getTooltipElements();
            for (int i = 0; i < list.size(); i++) {
                var entry = list.get(i);
                if (containsText(entry, "APOTH_REMOVE_MARKER")) {
                    list.remove(i);
                    list.add(i, Either.right(new SocketComponent(e.getItemStack(), SocketHelper.getGems(e.getItemStack()))));
                }
                else if (containsAffixMarker(entry, StoneformingAffix.TOOLTIP_MARKER)) {
                    list.remove(i);
                    AffixInstance inst = AffixHelper.streamAffixes(e.getItemStack()).filter(a -> a.getAffix() instanceof StoneformingAffix).findFirst().orElse(null);
                    if (inst != null) {
                        list.add(i, Either.right(new StoneformingComponent(inst)));
                    }
                }
            }
        }

        private static boolean containsText(Either<FormattedText, TooltipComponent> entry, String text) {
            Optional<FormattedText> o = entry.left();
            return o.isPresent() && o.get() instanceof Component comp && comp.contains(Component.literal(text));
        }

        private static boolean containsAffixMarker(Either<FormattedText, TooltipComponent> entry, Component marker) {
            Optional<FormattedText> o = entry.left();
            return o.isPresent() && o.get() instanceof Component comp && comp.contains(marker);
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void affixTooltips(ItemTooltipEvent e) {
            ItemStack stack = e.getItemStack();
            List<Component> components = new ArrayList<>();

            if (stack.has(Components.AFFIXES)) {
                AttributeTooltipContext ctx = AttributeTooltipContext.of(Minecraft.getInstance().player, e.getContext(), e.getFlags());
                AffixHelper.streamAffixes(stack)
                    .sorted(Comparator.comparingInt(a -> a.getAffix().definition().type().ordinal()))
                    .forEach(inst -> {
                        Component desc = inst.getDescription(ctx);
                        if (desc.getContents() != PlainTextContents.EMPTY) {
                            if (inst.level() > Affix.STANDARD_MAX_LEVEL) {
                                components.add(ApothMiscUtil.starPrefix(desc));
                            }
                            else {
                                components.add(ApothMiscUtil.dotPrefix(desc));
                            }
                        }
                    });
            }

            if (stack.has(Components.DURABILITY_BONUS) && !stack.has(DataComponents.UNBREAKABLE)) {
                components.add(ApothMiscUtil.dotPrefix(Component.translatable("affix.apotheosis:durable.desc", Math.round(100 * stack.get(Components.DURABILITY_BONUS)))));
            }

            if (!components.isEmpty()) {
                e.getToolTip().addAll(1, components);
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
