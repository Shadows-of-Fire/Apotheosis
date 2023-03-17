package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import shadows.apotheosis.adventure.loot.LootCategory;

/**
 * A Gem Class is the set of types of items it may be applied to.
 * This comes in the form of a named group of LootCategories.
 */
public record GemClass(String key, Set<LootCategory> types) {

	//Formatter::off
	public static Codec<GemClass> CODEC = RecordCodecBuilder.create(inst -> 
		inst.group(
			Codec.STRING.fieldOf("key").forGetter(GemClass::key),
			LootCategory.SET_CODEC.fieldOf("types").forGetter(GemClass::types))
			.apply(inst, GemClass::new)
		);
	//Formatter::on

	public GemClass(String key, Set<LootCategory> types) {
		this.key = key;
		this.types = types;
		Preconditions.checkArgument(!Strings.isNullOrEmpty(this.key), "Invalid GemClass with null key");
		Preconditions.checkArgument(this.types != null && !this.types.isEmpty(), "Invalid GemClass with null or empty types");
	}

	public void write(FriendlyByteBuf buf) {
		buf.writeUtf(this.key);
		buf.writeByte(this.types.size());
		types.forEach(c -> buf.writeEnum(c));
	}

	public static GemClass read(FriendlyByteBuf buf) {
		String key = buf.readUtf();
		int size = buf.readByte();
		List<LootCategory> list = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			list.add(buf.readEnum(LootCategory.class));
		}
		return new GemClass(key, ImmutableSet.copyOf(list));
	}
}