package shadows.apotheosis.adventure.boss;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;
import shadows.apotheosis.Apotheosis;
import shadows.placebo.codec.EnumCodec;
import shadows.placebo.codec.PlaceboCodecs;
import shadows.placebo.codec.PlaceboCodecs.CodecProvider;
import shadows.placebo.json.NBTAdapter;

public interface Exclusion extends CodecProvider<Exclusion> {

	public static final BiMap<ResourceLocation, Codec<? extends Exclusion>> CODECS = HashBiMap.create();

	public static final Codec<Exclusion> CODEC = PlaceboCodecs.mapBacked("Miniboss Exclusion", CODECS);

	public boolean isExcluded(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, @Nullable CompoundTag entityNbt);

	public boolean requiresNbtAccess();

	public static void initSerializers() {
		register("spawn_type", SpawnTypeExclusion.CODEC);
		register("nbt", NbtExclusion.CODEC);
		register("and", AndExclusion.CODEC);
	}

	private static void register(String id, Codec<? extends Exclusion> codec) {
		CODECS.put(Apotheosis.loc(id), codec);
	}

	public static record SpawnTypeExclusion(Set<MobSpawnType> types) implements Exclusion {

		//Formatter::off
		public static Codec<SpawnTypeExclusion> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(
				PlaceboCodecs.setCodec(new EnumCodec<>(MobSpawnType.class)).fieldOf("spawn_types").forGetter(SpawnTypeExclusion::types))
				.apply(inst, SpawnTypeExclusion::new)
			);
		//Formatter::on

		@Override
		public Codec<? extends Exclusion> getCodec() {
			return CODEC;
		}

		@Override
		public boolean isExcluded(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
			return this.types.contains(spawnType);
		}

		@Override
		public boolean requiresNbtAccess() {
			return false;
		}

	}

	/**
	 * An NBT Exclusion will exclude the entity if the entity has all of the nbt values specified.
	 * You can logical-or multiple tags by providing multiple exclusions.
	 */
	public static record NbtExclusion(CompoundTag nbt) implements Exclusion {

		//Formatter::off
		public static Codec<NbtExclusion> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(
				NBTAdapter.EITHER_CODEC.fieldOf("nbt").forGetter(NbtExclusion::nbt))
				.apply(inst, NbtExclusion::new)
			);
		//Formatter::on

		@Override
		public Codec<? extends Exclusion> getCodec() {
			return CODEC;
		}

		@Override
		public boolean isExcluded(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
			return NbtUtils.compareNbt(this.nbt, entityNbt, true);
		}

		@Override
		public boolean requiresNbtAccess() {
			return true;
		}

	}

	/**
	 * This class performs the logical-and of all child exclusions.
	 */
	public static record AndExclusion(List<Exclusion> exclusions) implements Exclusion {

		//Formatter::off
		public static Codec<AndExclusion> CODEC = RecordCodecBuilder.create(inst -> inst
			.group(
				Exclusion.CODEC.listOf().fieldOf("exclusions").forGetter(AndExclusion::exclusions))
				.apply(inst, AndExclusion::new)
			);
		//Formatter::on

		@Override
		public Codec<? extends Exclusion> getCodec() {
			return CODEC;
		}

		@Override
		public boolean isExcluded(Mob mob, ServerLevelAccessor level, MobSpawnType spawnType, CompoundTag entityNbt) {
			return exclusions.stream().allMatch(e -> e.isExcluded(mob, level, spawnType, entityNbt));
		}

		@Override
		public boolean requiresNbtAccess() {
			return exclusions.stream().anyMatch(Exclusion::requiresNbtAccess);
		}

	}

}
