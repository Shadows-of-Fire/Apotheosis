package dev.shadowsoffire.apotheosis.adventure.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.Adventure;
import dev.shadowsoffire.apotheosis.adventure.Adventure.Menus;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureModule;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.AugmentingScreen;
import dev.shadowsoffire.apotheosis.adventure.affix.augmenting.AugmentingTableTileRenderer;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingScreen;
import dev.shadowsoffire.apotheosis.adventure.affix.reforging.ReforgingTableTileRenderer;
import dev.shadowsoffire.apotheosis.adventure.affix.salvaging.SalvagingScreen;
import dev.shadowsoffire.apotheosis.adventure.client.BossSpawnMessage.BossSpawnData;
import dev.shadowsoffire.apotheosis.adventure.client.SocketTooltipRenderer.SocketComponent;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.cutting.GemCuttingScreen;
import dev.shadowsoffire.attributeslib.api.client.AddAttributeTooltipsEvent;
import dev.shadowsoffire.attributeslib.api.client.GatherSkippedAttributeTooltipsEvent;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
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
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent.Stage;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class AdventureModuleClient {

    public static List<BossSpawnData> BOSS_SPAWNS = new ArrayList<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.register(AdventureModuleClient.class);
        MenuScreens.register(Menus.REFORGING.get(), ReforgingScreen::new);
        MenuScreens.register(Menus.SALVAGE.get(), SalvagingScreen::new);
        MenuScreens.register(Menus.GEM_CUTTING.get(), GemCuttingScreen::new);
        MenuScreens.register(Menus.AUGMENTING.get(), AugmentingScreen::new);
        BlockEntityRenderers.register(Apoth.Tiles.REFORGING_TABLE.get(), k -> new ReforgingTableTileRenderer());
        BlockEntityRenderers.register(Apoth.Tiles.AUGMENTING_TABLE.get(), k -> new AugmentingTableTileRenderer());
        MinecraftForge.EVENT_BUS.register(AdventureKeys.class);
    }

    public static void onBossSpawn(BlockPos pos, float[] color) {
        BOSS_SPAWNS.add(new BossSpawnData(pos, color, new MutableInt()));
        SoundManager sm = Minecraft.getInstance().getSoundManager();
        for (SoundEvent soundEvent : AdventureConfig.bossAnnounceSounds) {
            sm.play(new SimpleSoundInstance(soundEvent, SoundSource.HOSTILE, AdventureConfig.bossAnnounceVolume, 1.25F, Minecraft.getInstance().player.getRandom(), Minecraft.getInstance().player.blockPosition()));
        }
    }

    @EventBusSubscriber(modid = Apotheosis.MODID, value = Dist.CLIENT, bus = Bus.MOD)
    public static class ModBusSub {
        @SubscribeEvent
        public static void models(ModelEvent.RegisterAdditional e) {
            e.register(ReforgingTableTileRenderer.HAMMER);
            e.register(AugmentingTableTileRenderer.STAR_CUBE);
        }

        @SubscribeEvent
        public static void tooltipComps(RegisterClientTooltipComponentFactoriesEvent e) {
            e.register(SocketComponent.class, SocketTooltipRenderer::new);
        }

        @SubscribeEvent
        public static void addGemModels(ModelEvent.RegisterAdditional e) {
            Set<ResourceLocation> locs = Minecraft.getInstance().getResourceManager().listResources("models", loc -> Apotheosis.MODID.equals(loc.getNamespace()) && loc.getPath().contains("/gems/") && loc.getPath().endsWith(".json"))
                .keySet();
            for (ResourceLocation s : locs) {
                String path = s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());
                e.register(new ResourceLocation(Apotheosis.MODID, path));
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
            event.registerShader(new ShaderInstance(event.getResourceProvider(), new ResourceLocation("apotheosis:gray"), DefaultVertexFormat.NEW_ENTITY), shaderInstance -> {
                CustomRenderTypes.grayShader = shaderInstance;
            });
        }

        @SubscribeEvent
        public static void keys(RegisterKeyMappingsEvent e) {
            e.register(AdventureKeys.TOGGLE_RADIAL);
            e.register(AdventureKeys.WORLD_TIERS_ARENT_REAL);
        }

        @SubscribeEvent
        public static void client(FMLClientSetupEvent e) {
            if (Apotheosis.enableAdventure) {
                e.enqueueWork(() -> {
                    ItemProperties.register(Adventure.Items.GEM.get(), Apotheosis.loc("rarity"), (stack, level, entity, tint) -> {
                        DynamicHolder<Gem> gem = GemItem.getGem(stack);
                        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(stack);
                        return gem.isBound() && rarity.isBound() ? rarity.get().ordinal() : Float.NEGATIVE_INFINITY;
                    });
                });
            }
        }
    }

    @SubscribeEvent
    public static void render(RenderLevelStageEvent e) {
        if (e.getStage() != Stage.AFTER_TRIPWIRE_BLOCKS) return;
        PoseStack stack = e.getPoseStack();
        MultiBufferSource.BufferSource buf = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        Player p = Minecraft.getInstance().player;
        for (int i = 0; i < BOSS_SPAWNS.size(); i++) {
            BossSpawnData data = BOSS_SPAWNS.get(i);
            stack.pushPose();
            float partials = e.getPartialTick();
            Vec3 vec = Minecraft.getInstance().getCameraEntity().getEyePosition(partials);
            stack.translate(-vec.x, -vec.y, -vec.z);
            stack.translate(data.pos().getX(), data.pos().getY(), data.pos().getZ());
            BeaconRenderer.renderBeaconBeam(stack, buf, BeaconRenderer.BEAM_LOCATION, partials, 1, p.level().getGameTime(), 0, 64, data.color(), 0.166F, 0.33F);
            stack.popPose();
        }
        buf.endBatch();
    }

    @SubscribeEvent
    public static void time(ClientTickEvent e) {
        if (e.phase != Phase.END) return;
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
        ListIterator<Component> it = e.getAttributeTooltipIterator();
        int sockets = SocketHelper.getSockets(stack);
        if (sockets > 0) it.add(Component.literal("APOTH_REMOVE_MARKER"));
    }

    @SubscribeEvent
    public static void ignoreSocketUUIDS(GatherSkippedAttributeTooltipsEvent e) {
        ItemStack stack = e.getStack();
        for (GemInstance gem : SocketHelper.getGems(stack)) {
            gem.getUUIDs().forEach(e::skipUUID);
        }
    }

    @SubscribeEvent
    public static void comps(RenderTooltipEvent.GatherComponents e) {
        int sockets = SocketHelper.getSockets(e.getItemStack());
        if (sockets == 0) return;
        List<Either<FormattedText, TooltipComponent>> list = e.getTooltipElements();
        int rmvIdx = -1;
        for (int i = 0; i < list.size(); i++) {
            Optional<FormattedText> o = list.get(i).left();
            if (o.isPresent() && o.get() instanceof Component comp && comp.getContents() instanceof LiteralContents tc) {
                if ("APOTH_REMOVE_MARKER".equals(tc.text())) {
                    rmvIdx = i;
                    list.remove(i);
                    break;
                }
            }
        }
        if (rmvIdx == -1) return;
        e.getTooltipElements().add(rmvIdx, Either.right(new SocketComponent(e.getItemStack(), SocketHelper.getGems(e.getItemStack()))));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void affixTooltips(ItemTooltipEvent e) {
        ItemStack stack = e.getItemStack();
        if (stack.hasTag()) {
            Map<DynamicHolder<? extends Affix>, AffixInstance> affixes = AffixHelper.getAffixes(stack);
            List<Component> components = new ArrayList<>();
            Consumer<Component> dotPrefixer = afxComp -> {
                components.add(Component.translatable("text.apotheosis.dot_prefix", afxComp).withStyle(ChatFormatting.YELLOW));
            };

            affixes.values().stream()
                .sorted(Comparator.comparingInt(a -> a.affix().get().getType().ordinal()))
                .map(AffixInstance::getDescription)
                .filter(c -> c.getContents() != ComponentContents.EMPTY)
                .forEach(dotPrefixer);

            e.getToolTip().addAll(1, components);
        }
    }

    // Accessor functon, ensures that you don't use the raw methods below unintentionally.
    public static RenderType gray(ResourceLocation texture) {
        return CustomRenderTypes.GRAY.apply(texture);
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

    public static void checkAffixLangKeys() {
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
        if (any) AdventureModule.LOGGER.error(sb.toString());
    }

}
