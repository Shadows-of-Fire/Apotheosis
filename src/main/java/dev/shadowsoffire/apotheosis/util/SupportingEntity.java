package dev.shadowsoffire.apotheosis.util;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import dev.shadowsoffire.placebo.json.NBTAdapter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class SupportingEntity {

    public static Codec<SupportingEntity> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity").forGetter(t -> t.entity),
            PlaceboCodecs.nullableField(NBTAdapter.EITHER_CODEC, "nbt").forGetter(t -> Optional.ofNullable(t.nbt)),
            PlaceboCodecs.nullableField(Codec.DOUBLE, "x", 0D).forGetter(t -> t.x),
            PlaceboCodecs.nullableField(Codec.DOUBLE, "y", 0D).forGetter(t -> t.y),
            PlaceboCodecs.nullableField(Codec.DOUBLE, "z", 0D).forGetter(t -> t.z))
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

    public Mob create(Level level, double x, double y, double z) {
        Mob ent = (Mob) this.entity.create(level);
        if (this.nbt != null) ent.deserializeNBT(this.nbt);
        ent.setPos(this.x + x, this.y + y, this.z + z);
        return ent;
    }
}
