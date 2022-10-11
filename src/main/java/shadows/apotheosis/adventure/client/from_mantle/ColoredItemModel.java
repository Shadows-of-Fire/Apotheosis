package shadows.apotheosis.adventure.client.from_mantle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.CompositeModelState;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;
import net.minecraftforge.client.model.pipeline.IVertexConsumer;
import net.minecraftforge.client.model.pipeline.TRSRTransformer;

/**
 * Clone of {@link ItemLayerModel} to propagate a hardcoded color in, allows reducing rendering time by bypassing item colors for a static color.
 * Also supports luminosity, and when used as a model loader supports telling a layer to not use a tint index
 */
public class ColoredItemModel implements IModelGeometry<ColoredItemModel> {
	/** Model loader instance */
	public static final Loader LOADER = new Loader();

	private static final Direction[] HORIZONTALS = { Direction.UP, Direction.DOWN };
	private static final Direction[] VERTICALS = { Direction.WEST, Direction.EAST };

	/** Layers in the model */
	private final List<LayerData> layers;
	/** Textures fetched during baking */
	private List<Material> textures = Collections.emptyList();

	public ColoredItemModel(List<LayerData> layers, List<Material> textures) {
		this.layers = layers;
		this.textures = textures;
	}

	/** Gets the layer at the given index */
	private LayerData getLayer(int index) {
		if (index < 0 || index >= layers.size()) { return LayerData.DEFAULT; }
		return layers.get(index);
	}

	@Override
	public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		ImmutableList.Builder<Material> builder = ImmutableList.builder();
		for (int i = 0; owner.isTexturePresent("layer" + i); i++) {
			builder.add(owner.resolveTexture("layer" + i));
		}
		textures = builder.build();
		return textures;
	}

	@Override
	public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
		// determine particle texture
		TextureAtlasSprite particle = spriteGetter.apply(owner.isTexturePresent("particle") ? owner.resolveTexture("particle") : textures.get(0));
		// bake in special properties
		List<BakedQuad> builder = new ArrayList<>();
		// skip the pixel tracking if using a single texture only
		ItemLayerPixels pixels = textures.size() == 1 ? null : new ItemLayerPixels();
		Transformation transform = modelTransform.getRotation();
		for (int i = textures.size() - 1; i >= 0; i--) {
			TextureAtlasSprite sprite = spriteGetter.apply(textures.get(i));
			LayerData data = getLayer(i);
			builder.addAll(getQuadsForSprite(data.color(), data.noTint() ? -1 : i, sprite, transform, data.luminosity(), pixels));
		}
		// transform data
		ImmutableMap<ItemTransforms.TransformType, Transformation> transformMap = PerspectiveMapWrapper.getTransforms(new CompositeModelState(owner.getCombinedTransform(), modelTransform));
		Collections.reverse(builder);
		return new BakedItemModel(ImmutableList.copyOf(builder), particle, Maps.immutableEnumMap(transformMap), overrides, true, owner.isSideLit());
	}

	/**
	 * Gets all quads for an item layer for the given sprite
	 * @param color       Color for the sprite in AARRGGBB format.
	 * @param tint        Tint index for {@link net.minecraft.client.color.block.BlockColors} and {@link net.minecraft.client.color.item.ItemColors}. Generally unused
	 * @param sprite      Sprite to convert into quads
	 * @param transform   Transforms to apply
	 * @param luminosity  Extra light to add to the quad from 0-15, makes it appear to glow a bit
	 * @return  List of baked quads
	 */
	public static ImmutableList<BakedQuad> getQuadsForSprite(int color, int tint, TextureAtlasSprite sprite, Transformation transform, int luminosity) {
		return getQuadsForSprite(color, tint, sprite, transform, luminosity, null);
	}

	public static int getRealFrame(TextureAtlasSprite sprite, int frameIdx) {
		var ticker = sprite.getAnimationTicker();
		if (ticker != null) {
			TextureAtlasSprite.AnimatedTexture at = (TextureAtlasSprite.AnimatedTexture) ticker;
			return at.frames.get(frameIdx).index;
		}
		return 0;
	}

	/**
	 * Gets all quads for an item layer for the given sprite
	 * @param color       Color for the sprite in AARRGGBB format.
	 * @param tint        Tint index for {@link net.minecraft.client.color.block.BlockColors} and {@link net.minecraft.client.color.item.ItemColors}. Generally unused
	 * @param sprite      Sprite to convert into quads
	 * @param transform   Transforms to apply
	 * @param luminosity  Extra light to add to the quad from 0-15, makes it appear to glow a bit
	 * @param pixels      Object to keep track of used pixels across multiple layers to help prevent z-fighting. To effective use, sprites must be built in reverse order. Use null to skip this logic
	 * @return  List of baked quads
	 */
	public static ImmutableList<BakedQuad> getQuadsForSprite(int color, int tint, TextureAtlasSprite sprite, Transformation transform, int luminosity, @Nullable ItemLayerPixels pixels) {
		ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

		int uMax = sprite.getWidth();
		int vMax = sprite.getHeight();
		FaceData faceData = new FaceData(uMax, vMax);
		boolean translucent = false;

		for (int f = 0; f < sprite.getFrameCount(); f++) {
			boolean ptu;
			boolean[] ptv = new boolean[uMax];
			Arrays.fill(ptv, true);
			for (int v = 0; v < vMax; v++) {
				ptu = true;
				for (int u = 0; u < uMax; u++) {
					int alpha = sprite.getPixelRGBA(getRealFrame(sprite, f), u, vMax - v - 1) >> 24 & 0xFF;
					boolean t = alpha / 255f <= 0.1f;

					if (!t && alpha < 255) {
						translucent = true;
					}

					if (ptu && !t) { // left - transparent, right - opaque
						faceData.set(Direction.WEST, u, v);
					}
					if (!ptu && t) { // left - opaque, right - transparent
						faceData.set(Direction.EAST, u - 1, v);
					}
					if (ptv[u] && !t) { // up - transparent, down - opaque
						faceData.set(Direction.UP, u, v);
					}
					if (!ptv[u] && t) { // up - opaque, down - transparent
						faceData.set(Direction.DOWN, u, v - 1);
					}

					ptu = t;
					ptv[u] = t;
				}
				if (!ptu) { // last - opaque
					faceData.set(Direction.EAST, uMax - 1, v);
				}
			}
			// last line
			for (int u = 0; u < uMax; u++) {
				if (!ptv[u]) {
					faceData.set(Direction.DOWN, u, vMax - 1);
				}
			}
		}

		// horizontal quads
		for (Direction facing : HORIZONTALS) {
			for (int v = 0; v < vMax; v++) {
				int uStart = 0, uEnd = uMax;
				boolean building = false;
				for (int u = 0; u < uMax; u++) {
					boolean canDraw = pixels == null || !pixels.get(u, v, uMax, vMax);
					boolean face = canDraw && faceData.get(facing, u, v);
					// set the end for translucent to draw right after this pixel
					if (face) {
						uEnd = u + 1;
						// if not currently building and we have data, start new quad
						if (!building) {
							building = true;
							uStart = u;
						}
					}
					// make quad [uStart, u]
					else if (building) {
						// finish current quad if translucent (minimize overdraw) or we are forbidden from touching this pixel (previous layer drew here)
						if (!canDraw || translucent) {
							int off = facing == Direction.DOWN ? 1 : 0;
							builder.add(buildSideQuad(transform, facing, color, tint, sprite, uStart, v + off, uEnd - uStart, luminosity));
							building = false;
						}
					}
				}
				if (building) { // build remaining quad
					// make quad [uStart, uEnd]
					int off = facing == Direction.DOWN ? 1 : 0;
					builder.add(buildSideQuad(transform, facing, color, tint, sprite, uStart, v + off, uEnd - uStart, luminosity));
				}
			}
		}

		// vertical quads
		for (Direction facing : VERTICALS) {
			for (int u = 0; u < uMax; u++) {
				int vStart = 0, vEnd = vMax;
				boolean building = false;
				for (int v = 0; v < vMax; v++) {
					boolean canDraw = pixels == null || !pixels.get(u, v, uMax, vMax);
					boolean face = canDraw && faceData.get(facing, u, v);
					// set the end for translucent to draw right after this pixel
					if (face) {
						vEnd = v + 1;
						// if not currently building and we have data, start new quad
						if (!building) {
							building = true;
							vStart = v;
						}
					}
					// make quad [vStart, v]
					else if (building) {
						// finish current quad if translucent (minimize overdraw) or we are forbidden from touching this pixel (future layer drew here)
						if (!canDraw || translucent) {
							int off = facing == Direction.EAST ? 1 : 0;
							builder.add(buildSideQuad(transform, facing, color, tint, sprite, u + off, vStart, vEnd - vStart, luminosity));
							building = false;
						}
					}
				}
				if (building) { // build remaining quad
					// make quad [vStart, vEnd]
					int off = facing == Direction.EAST ? 1 : 0;
					builder.add(buildSideQuad(transform, facing, color, tint, sprite, u + off, vStart, vEnd - vStart, luminosity));
				}
			}
		}

		// front
		builder.add(buildQuad(transform, Direction.NORTH, sprite, color, tint, luminosity, 0, 0, 7.5f / 16f, sprite.getU0(), sprite.getV1(), 0, 1, 7.5f / 16f, sprite.getU0(), sprite.getV0(), 1, 1, 7.5f / 16f, sprite.getU1(), sprite.getV0(), 1, 0, 7.5f / 16f, sprite.getU1(), sprite.getV1()));
		// back
		builder.add(buildQuad(transform, Direction.SOUTH, sprite, color, tint, luminosity, 0, 0, 8.5f / 16f, sprite.getU0(), sprite.getV1(), 1, 0, 8.5f / 16f, sprite.getU1(), sprite.getV1(), 1, 1, 8.5f / 16f, sprite.getU1(), sprite.getV0(), 0, 1, 8.5f / 16f, sprite.getU0(), sprite.getV0()));

		// fill in the pixel map with new pixels from the sprite
		if (pixels != null) {
			// animated textures are tricky, as we have three choices:
			//  1. if a pixel is only potentially there, don't draw lower layers - leads to gaps
			//  2. if a pixel is only potentially there, always draw lower layers - leads to z-fighting
			//  3. only use the first frame
			// of these, 2 would give the most accurate result. However, its also the hardest to calculate
			// of the remaining methods, 3 is both more accurate and easier to calculate than 1, so I opted for that approach
			if (sprite.getFrameCount() > 0) {
				for (int v = 0; v < vMax; v++) {
					for (int u = 0; u < uMax; u++) {
						int alpha = sprite.getPixelRGBA(0, u, vMax - v - 1) >> 24 & 0xFF;
						if (alpha / 255f > 0.1f) {
							pixels.set(u, v, uMax, vMax);
						}
					}
				}
			}
		}

		return builder.build();
	}

	/**
	 * Builds a single quad on the side of the sprite
	 * @param transform  Transforms to apply
	 * @param side       Side to build
	 * @param color      Color for the sprite
	 * @param tint       Tint index for {@link net.minecraft.client.color.block.BlockColors} and {@link net.minecraft.client.color.item.ItemColors}
	 * @param sprite     Sprite to render
	 * @param u          Sprite U
	 * @param v          Sprite V
	 * @param size       Size of the quad in the correct direction (depth is always 1 pixel)
	 * @param luminosity Extra light to add to the quad between 0 and 15
	 * @return  Baked quad
	 */
	private static BakedQuad buildSideQuad(Transformation transform, Direction side, int color, int tint, TextureAtlasSprite sprite, int u, int v, int size, int luminosity) {
		final float eps = 1e-2f;
		int width = sprite.getWidth();
		int height = sprite.getHeight();
		float x0 = (float) u / width;
		float y0 = (float) v / height;
		float x1 = x0, y1 = y0;
		float z0 = 7.5f / 16f, z1 = 8.5f / 16f;
		switch (side) {
		case WEST:
			z0 = 8.5f / 16f;
			z1 = 7.5f / 16f;
			// continue into EAST
		case EAST:
			y1 = (float) (v + size) / height;
			break;
		case DOWN:
			z0 = 8.5f / 16f;
			z1 = 7.5f / 16f;
			// continue into UP
		case UP:
			x1 = (float) (u + size) / width;
			break;
		default:
			throw new IllegalArgumentException("can't handle z-oriented side");
		}

		// for the side, Y axis's use of getOpposite is related to the swapping of V direction
		float dx = side.getNormal().getX() * eps / width;
		float dy = side.getNormal().getY() * eps / height;
		float u0 = 16f * (x0 - dx);
		float u1 = 16f * (x1 - dx);
		float v0 = 16f * (1f - y0 - dy);
		float v1 = 16f * (1f - y1 - dy);
		return buildQuad(transform, (side.getAxis() == Axis.Y ? side.getOpposite() : side), sprite, color, tint, luminosity, x0, y0, z0, sprite.getU(u0), sprite.getV(v0), x1, y1, z0, sprite.getU(u1), sprite.getV(v1), x1, y1, z1, sprite.getU(u1), sprite.getV(v1), x0, y0, z1, sprite.getU(u0), sprite.getV(v0));
	}

	/**
	 * Builds a single quad in the model, based on the method in {@link ItemLayerModel} but with color added
	 * @param transform    Model transforms
	 * @param side         Quad side
	 * @param sprite       Sprite to use in the quad
	 * @param color        Color for the sprite in AARRGGBB format
	 * @param tint         Tint index for {@link net.minecraft.client.color.block.BlockColors} and {@link net.minecraft.client.color.item.ItemColors}
	 * @param luminosity Extra light to add to the quad between 0 and 15
	 * @return  Final quad
	 */
	protected static BakedQuad buildQuad(Transformation transform, Direction side, TextureAtlasSprite sprite, int color, int tint, int luminosity, float x0, float y0, float z0, float u0, float v0, float x1, float y1, float z1, float u1, float v1, float x2, float y2, float z2, float u2, float v2, float x3, float y3, float z3, float u3, float v3) {
		BakedQuadBuilder builder = new BakedQuadBuilder(sprite);
		builder.setQuadTint(tint);
		builder.setQuadOrientation(side);
		builder.setApplyDiffuseLighting(false); // TODO: luminosity == 0?

		boolean hasTransform = !transform.isIdentity();
		IVertexConsumer consumer = hasTransform ? new TRSRTransformer(builder, transform) : builder;

		putVertex(consumer, side, x0, y0, z0, u0, v0, color, luminosity);
		putVertex(consumer, side, x1, y1, z1, u1, v1, color, luminosity);
		putVertex(consumer, side, x2, y2, z2, u2, v2, color, luminosity);
		putVertex(consumer, side, x3, y3, z3, u3, v3, color, luminosity);
		return builder.build();
	}

	/**
	 * Clone of the method in {@link ItemLayerModel} with the color parameter added
	 * @param consumer   Vertex consumer
	 * @param side       Side for the quad
	 * @param x          Quad X position
	 * @param y          Quad Y position
	 * @param z          Quad Z position
	 * @param u          Quad texture U
	 * @param v          Quad texture V
	 * @param color      Quad color in AARRGGBB format
	 * @param luminosity Extra light to add to the quad between 0 and 15
	 */
	private static void putVertex(IVertexConsumer consumer, Direction side, float x, float y, float z, float u, float v, int color, int luminosity) {
		VertexFormat format = consumer.getVertexFormat();
		ImmutableList<VertexFormatElement> elements = format.getElements();
		int size = elements.size();
		for (int e = 0; e < size; e++) {
			VertexFormatElement element = elements.get(e);
			outer: switch (element.getUsage()) {
			case POSITION:
				consumer.put(e, x, y, z, 1f);
				break;
			case COLOR:
				float r = ((color >> 16) & 0xFF) / 255f;
				float g = ((color >> 8) & 0xFF) / 255f;
				float b = ((color) & 0xFF) / 255f;
				float a = ((color >> 24) & 0xFF) / 255f;
				consumer.put(e, r, g, b, a);
				break;
			case NORMAL:
				float offX = (float) side.getStepX();
				float offY = (float) side.getStepY();
				float offZ = (float) side.getStepZ();
				consumer.put(e, offX, offY, offZ, 0f);
				break;
			case UV:
				switch (element.getIndex()) {
				case 0:
					consumer.put(e, u, v, 0f, 1f);
					break outer;
				case 2:
					float light = (luminosity << 4) / 32768f;
					consumer.put(e, light, light, 0, 1);
					break outer;
				}
				// else fallthrough to default
			default:
				consumer.put(e);
				break;
			}
		}
	}

	/** Cloned from {@link ItemLayerModel}'s FaceData subclass */
	private static class FaceData {
		private final EnumMap<Direction, BitSet> data = new EnumMap<>(Direction.class);
		private final int vMax;

		FaceData(int uMax, int vMax) {
			this.vMax = vMax;

			data.put(Direction.WEST, new BitSet(uMax * vMax));
			data.put(Direction.EAST, new BitSet(uMax * vMax));
			data.put(Direction.UP, new BitSet(uMax * vMax));
			data.put(Direction.DOWN, new BitSet(uMax * vMax));
		}

		public void set(Direction facing, int u, int v) {
			data.get(facing).set(getIndex(u, v));
		}

		public boolean get(Direction facing, int u, int v) {
			return data.get(facing).get(getIndex(u, v));
		}

		private int getIndex(int u, int v) {
			return v * vMax + u;
		}
	}

	/**
	 * Class holding details about a single layer in the model
	 */
	private record LayerData(int color, int luminosity, boolean noTint) {

		private static final LayerData DEFAULT = new LayerData(-1, 0, false);

		/**
		 * Parses the layer data from JSON
		 */
		public static LayerData fromJson(JsonObject json) {
			int color = ColoredBlockModel.parseColor(GsonHelper.getAsString(json, "color", ""));
			int luminosity = GsonHelper.getAsInt(json, "luminosity");
			boolean noTint = GsonHelper.getAsBoolean(json, "no_tint", false);
			return new LayerData(color, luminosity, noTint);
		}
	}

	private static class Loader implements IModelLoader<ColoredItemModel> {
		@Override
		public void onResourceManagerReload(ResourceManager resourceManager) {
		}

		@Override
		public ColoredItemModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
			List<LayerData> layers = new ArrayList<>();
			GsonHelper.getAsJsonArray(modelContents, "colors").forEach(e -> layers.add(LayerData.fromJson(e.getAsJsonObject())));
			return new ColoredItemModel(layers, Collections.emptyList());
		}
	}
}