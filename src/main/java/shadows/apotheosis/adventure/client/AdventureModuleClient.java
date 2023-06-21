package shadows.apotheosis.adventure.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.lang3.mutable.MutableInt;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Either;
import com.mojang.math.Vector3f;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
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
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apoth.Affixes;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.reforging.ReforgingScreen;
import shadows.apotheosis.adventure.affix.reforging.ReforgingTableTileRenderer;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingScreen;
import shadows.apotheosis.adventure.affix.socket.SocketHelper;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.affix.socket.gem.cutting.GemCuttingScreen;
import shadows.apotheosis.adventure.client.BossSpawnMessage.BossSpawnData;
import shadows.apotheosis.adventure.client.SocketTooltipRenderer.SocketComponent;
import shadows.apotheosis.core.attributeslib.api.AddAttributeTooltipsEvent;
import shadows.apotheosis.core.attributeslib.api.GatherSkippedAttributeTooltipsEvent;

public class AdventureModuleClient {

	public static List<BossSpawnData> BOSS_SPAWNS = new ArrayList<>();

	public static void init() {
		MinecraftForge.EVENT_BUS.register(AdventureModuleClient.class);
		MenuScreens.register(Apoth.Menus.REFORGING.get(), ReforgingScreen::new);
		MenuScreens.register(Apoth.Menus.SALVAGE.get(), SalvagingScreen::new);
		MenuScreens.register(Apoth.Menus.GEM_CUTTING.get(), GemCuttingScreen::new);
		BlockEntityRenderers.register(Apoth.Tiles.REFORGING_TABLE.get(), k -> new ReforgingTableTileRenderer());
	}

	public static void onBossSpawn(BlockPos pos, float[] color) {
		BOSS_SPAWNS.add(new BossSpawnData(pos, color, new MutableInt()));
		Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, AdventureConfig.bossAnnounceVolume, 1.25F, Minecraft.getInstance().player.random, Minecraft.getInstance().player.blockPosition()));
	}

	@EventBusSubscriber(modid = Apotheosis.MODID, value = Dist.CLIENT, bus = Bus.MOD)
	public static class ModBusSub {
		@SubscribeEvent
		public static void models(ModelEvent.RegisterAdditional e) {
			e.register(new ResourceLocation(Apotheosis.MODID, "item/hammer"));
		}

		@SubscribeEvent
		public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders e) {
			e.register("item_layers", FItemLayerModel.Loader.INSTANCE);
		}

		@SubscribeEvent
		public static void tooltipComps(RegisterClientTooltipComponentFactoriesEvent e) {
			e.register(SocketComponent.class, SocketTooltipRenderer::new);
		}

		@SubscribeEvent
		public static void addGemModels(ModelEvent.RegisterAdditional e) {
			Set<ResourceLocation> locs = Minecraft.getInstance().getResourceManager().listResources("models", loc -> loc.getNamespace().equals(Apotheosis.MODID) && loc.getPath().contains("/gems/") && loc.getPath().endsWith(".json")).keySet();
			for (ResourceLocation s : locs) {
				String path = s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());
				e.register(new ResourceLocation(Apotheosis.MODID, path));
			}
		}

		@SubscribeEvent
		public static void replaceGemModel(ModelEvent.BakingCompleted e) {
			ModelResourceLocation key = new ModelResourceLocation(Apotheosis.loc("gem"), "inventory");
			BakedModel oldModel = e.getModels().get(key);
			if (oldModel != null) {
				e.getModels().put(key, new GemModel(oldModel, e.getModelBakery()));
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
			BeaconRenderer.renderBeaconBeam(stack, buf, BeaconRenderer.BEAM_LOCATION, partials, 1, p.level.getGameTime(), 0, 64, data.color(), 0.166F, 0.33F);
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
		if (AffixHelper.getAffixes(stack).containsKey(Affixes.SOCKET.get())) it.add(Component.literal("APOTH_REMOVE_MARKER"));
	}

	@SubscribeEvent
	public static void ignoreSocketUUIDS(GatherSkippedAttributeTooltipsEvent e) {
		ItemStack stack = e.getStack();
		int sockets = SocketHelper.getSockets(stack);
		if (sockets > 0) {
			for (ItemStack gem : SocketHelper.getGems(stack, sockets)) {
				GemItem.getUUIDs(gem).forEach(e::skipUUID);
			}
		}
	}

	@SubscribeEvent
	public static void comps(RenderTooltipEvent.GatherComponents e) {
		AffixInstance socket = AffixHelper.getAffixes(e.getItemStack()).get(Affixes.SOCKET.get());
		if (socket == null) return;

		List<Either<FormattedText, TooltipComponent>> list = e.getTooltipElements();
		int rmvIdx = -1;
		for (int i = 0; i < list.size(); i++) {
			Optional<FormattedText> o = list.get(i).left();
			if (o.isPresent() && o.get() instanceof Component comp && comp.getContents() instanceof LiteralContents tc) {
				if (tc.text().equals("APOTH_REMOVE_MARKER")) {
					rmvIdx = i;
					list.remove(i);
					break;
				}
			}
		}
		if (rmvIdx == -1) return;
		int size = (int) socket.level();
		e.getTooltipElements().add(rmvIdx, Either.right(new SocketComponent(e.getItemStack(), SocketHelper.getGems(e.getItemStack(), size))));
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void affixTooltips(ItemTooltipEvent e) {
		ItemStack stack = e.getItemStack();
		if (stack.hasTag()) {
			Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
			List<Component> components = new ArrayList<>();
			affixes.values().stream().sorted(Comparator.comparingInt(a -> a.affix().getType().ordinal())).forEach(inst -> inst.addInformation(components::add));
			e.getToolTip().addAll(1, components);
		}
	}

	// Unused, doesn't actually work to render beacons without depth.
	private static abstract class CustomBeacon extends RenderStateShard {

		public CustomBeacon(String pName, Runnable pSetupState, Runnable pClearState) {
			super(pName, pSetupState, pClearState);
		}

		//Formatter::off
		static final BiFunction<ResourceLocation, Boolean, RenderType> BEACON_BEAM = Util.memoize((p_173224_, p_173225_) -> {
			RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
				.setShaderState(RENDERTYPE_BEACON_BEAM_SHADER)
				.setTextureState(new RenderStateShard.TextureStateShard(p_173224_, false, false))
				.setTransparencyState(p_173225_ ? TRANSLUCENT_TRANSPARENCY : NO_TRANSPARENCY)
				.setWriteMaskState(p_173225_ ? COLOR_WRITE : COLOR_WRITE)
				.setDepthTestState(NO_DEPTH_TEST)
				.setCullState(NO_CULL)
				.createCompositeState(false);
			return RenderType.create("custom_beacon_beam", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, false, true, rendertype$compositestate);
		});
		//Formatter::on
	}

	static final RenderType beaconBeam(ResourceLocation tex, boolean color) {
		return CustomBeacon.BEACON_BEAM.apply(tex, color);
	}

	public static void renderBeaconBeam(PoseStack pPoseStack, MultiBufferSource pBufferSource, ResourceLocation pBeamLocation, float pPartialTick, float pTextureScale, long pGameTime, int pYOffset, int pHeight, float[] pColors, float pBeamRadius, float pGlowRadius) {
		int i = pYOffset + pHeight;
		pPoseStack.pushPose();
		pPoseStack.translate(0.5D, 0.0D, 0.5D);
		float f = (float) Math.floorMod(pGameTime, 40) + pPartialTick;
		float f1 = pHeight < 0 ? f : -f;
		float f2 = Mth.frac(f1 * 0.2F - (float) Mth.floor(f1 * 0.1F));
		float f3 = pColors[0];
		float f4 = pColors[1];
		float f5 = pColors[2];
		pPoseStack.pushPose();
		pPoseStack.mulPose(Vector3f.YP.rotationDegrees(f * 2.25F - 45.0F));
		float f6 = 0.0F;
		float f8 = 0.0F;
		float f9 = -pBeamRadius;
		float f12 = -pBeamRadius;
		float f15 = -1.0F + f2;
		float f16 = (float) pHeight * pTextureScale * (0.5F / pBeamRadius) + f15;
		BeaconRenderer.renderPart(pPoseStack, pBufferSource.getBuffer(beaconBeam(pBeamLocation, false)), f3, f4, f5, 1.0F, pYOffset, i, 0.0F, pBeamRadius, pBeamRadius, 0.0F, f9, 0.0F, 0.0F, f12, 0.0F, 1.0F, f16, f15);
		pPoseStack.popPose();
		f6 = -pGlowRadius;
		float f7 = -pGlowRadius;
		f8 = -pGlowRadius;
		f9 = -pGlowRadius;
		f15 = -1.0F + f2;
		f16 = (float) pHeight * pTextureScale + f15;
		BeaconRenderer.renderPart(pPoseStack, pBufferSource.getBuffer(beaconBeam(pBeamLocation, true)), f3, f4, f5, 0.125F, pYOffset, i, f6, f7, pGlowRadius, f8, f9, pGlowRadius, pGlowRadius, pGlowRadius, 0.0F, 1.0F, f16, f15);
		pPoseStack.popPose();
	}

}
