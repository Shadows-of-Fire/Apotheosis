package shadows.apotheosis.adventure.client;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;

public class GemModel implements BakedModel {
	private final BakedModel original;
	private final ItemOverrides itemHandler;

	@SuppressWarnings("deprecation")
	public GemModel(BakedModel original, ModelBakery loader) {
		this.original = original;
		BlockModel missing = (BlockModel) loader.getModel(ModelBakery.MISSING_MODEL_LOCATION);

		this.itemHandler = new ItemOverrides(loader, missing, id -> missing, Collections.emptyList()) {
			@Override
			public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
				return GemModel.this.resolve(original, stack, world, entity, seed);
			}
		};
	}

	public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
		Gem gem = GemItem.getGem(stack);
		if (gem != null) {
			return Minecraft.getInstance().getModelManager().getModel(new ResourceLocation(Apotheosis.MODID, "item/gems/" + gem.getId().getPath()));
		}
		return original;
	}

	@Override
	public ItemOverrides getOverrides() {
		return itemHandler;
	}

	@Override
	@Deprecated
	public List<BakedQuad> getQuads(BlockState pState, Direction pDirection, RandomSource pRandom) {
		return original.getQuads(pState, pDirection, pRandom);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return original.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return original.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return original.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return original.isCustomRenderer();
	}

	@Override
	@Deprecated
	public TextureAtlasSprite getParticleIcon() {
		return original.getParticleIcon();
	}

	@Override
	@Deprecated
	public ItemTransforms getTransforms() {
		return original.getTransforms();
	}
}
