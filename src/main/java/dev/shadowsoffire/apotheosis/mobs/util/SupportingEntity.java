package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.Optional;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.json.NBTAdapter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

// TODO: Remove offsets. Maybe setup a spawn range and use Gateways' inward spiral?
// TODO: Allow providing certain entity bonuses via normal routes, instead of raw NBT.
public class SupportingEntity {

    public static Codec<SupportingEntity> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity").forGetter(t -> t.entity),
            NBTAdapter.EITHER_CODEC.optionalFieldOf("nbt").forGetter(t -> Optional.ofNullable(t.nbt)),
            Codec.DOUBLE.optionalFieldOf("x", 0D).forGetter(t -> t.x),
            Codec.DOUBLE.optionalFieldOf("y", 0D).forGetter(t -> t.y),
            Codec.DOUBLE.optionalFieldOf("z", 0D).forGetter(t -> t.z))
        .apply(inst, SupportingEntity::new));

    public final EntityType<?> entity;
    protected final CompoundTag nbt;
    protected final double x, y, z;

    public SupportingEntity(EntityType<?> entity, Optional<CompoundTag> nbt, double x, double y, double z) {
        this.entity = entity;
        this.nbt = nbt.orElse(null);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Nullable
    public Mob create(Level level, double x, double y, double z) {
        Mob ent = (Mob) this.entity.create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (ent != null && this.nbt != null) {
            try (net.minecraft.util.ProblemReporter.ScopedCollector reporter = new net.minecraft.util.ProblemReporter.ScopedCollector(ent.problemPath(), dev.shadowsoffire.apotheosis.Apotheosis.LOGGER)) {
                ent.load(net.minecraft.world.level.storage.TagValueInput.create(reporter, level.registryAccess(), this.nbt));
            }
        }
        if (ent != null) {
            ent.setPos(this.x + x, this.y + y, this.z + z);
        }
        return ent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private EntityType<? extends Mob> entity;
        private CompoundTag nbt;
        private double x = 0.0;
        private double y = 0.0;
        private double z = 0.0;

        public Builder entity(EntityType<? extends Mob> entity) {
            this.entity = entity;
            return this;
        }

        public Builder nbt(CompoundTag nbt) {
            this.nbt = nbt;
            return this;
        }

        public Builder nbt(Consumer<CompoundTag> nbt) {
            CompoundTag current = this.nbt == null ? new CompoundTag() : this.nbt;
            nbt.accept(current);
            this.nbt = current;
            return this;
        }

        public Builder x(double x) {
            this.x = x;
            return this;
        }

        public Builder y(double y) {
            this.y = y;
            return this;
        }

        public Builder z(double z) {
            this.z = z;
            return this;
        }

        public Builder position(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public SupportingEntity build() {
            if (entity == null) {
                throw new IllegalStateException("Entity type must be set");
            }
            return new SupportingEntity(entity, Optional.ofNullable(nbt), x, y, z);
        }
    }

}
