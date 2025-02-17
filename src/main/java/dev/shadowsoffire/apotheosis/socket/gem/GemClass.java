package dev.shadowsoffire.apotheosis.socket.gem;

import java.util.Set;
import java.util.function.Function;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;

/**
 * A Gem Class is the set of types of items it may be applied to.
 * This comes in the form of a named group of LootCategories.
 */
public record GemClass(String key, Set<LootCategory> types) {

    public static Codec<GemClass> EXPLICIT_CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.STRING.fieldOf("key").forGetter(GemClass::key),
        LootCategory.SET_CODEC.fieldOf("types").forGetter(GemClass::types))
        .apply(inst, GemClass::new));

    public static Codec<GemClass> CODEC = Codec.either(EXPLICIT_CODEC, LootCategory.CODEC)
        .xmap(e -> e.map(Function.identity(), GemClass::new), GemClass::toEither);

    public GemClass(LootCategory category) {
        this(category.getKey().getPath(), category);
    }

    public GemClass(String key, LootCategory... types) {
        this(key, ApothMiscUtil.linkedSet(types));
    }

    public GemClass(String key, Set<LootCategory> types) {
        this.key = key;
        this.types = types;
        Preconditions.checkArgument(!Strings.isNullOrEmpty(this.key), "Invalid GemClass with null key");
        Preconditions.checkArgument(this.types != null && !this.types.isEmpty(), "Invalid GemClass with null or empty types");
    }

    private static Either<GemClass, LootCategory> toEither(GemClass gc) {
        if (gc.types.size() == 1) {
            return Either.right(gc.types.iterator().next());
        }
        return Either.left(gc);
    }
}
