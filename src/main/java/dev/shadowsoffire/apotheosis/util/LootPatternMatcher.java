package dev.shadowsoffire.apotheosis.util;

import java.util.Optional;
import java.util.regex.Pattern;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public record LootPatternMatcher(Optional<String> domain, Pattern pathRegex) implements LootItemCondition {

    public static final MapCodec<LootPatternMatcher> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Codec.STRING.optionalFieldOf("domain").forGetter(LootPatternMatcher::domain),
        Codec.STRING.xmap(Pattern::compile, Pattern::toString).fieldOf("path_regex").forGetter(LootPatternMatcher::pathRegex))
        .apply(inst, LootPatternMatcher::new));

    public static LootPatternMatcher of(String domain, String regex) {
        Preconditions.checkArgument(!domain.isBlank());
        return new LootPatternMatcher(Optional.of(domain), Pattern.compile(regex));
    }

    public static LootPatternMatcher of(String regex) {
        return new LootPatternMatcher(Optional.empty(), Pattern.compile(regex));
    }

    public boolean matches(ResourceLocation id) {
        return (this.domain.isEmpty() || this.domain.get().equals(id.getNamespace())) && this.pathRegex.matcher(id.getPath()).matches();
    }

    @Override
    public boolean test(LootContext t) {
        return this.matches(t.getQueriedLootTableId());
    }

    @Override
    public LootItemConditionType getType() {
        return Apoth.LootConditions.LOOT_TABLE_PATTERN_MATCHER;
    }

    public static LootItemCondition.Builder matchesTables(String domain, String regex) {
        return () -> new LootPatternMatcher(domain.isBlank() ? Optional.empty() : Optional.of(domain), Pattern.compile(regex));
    }
}
