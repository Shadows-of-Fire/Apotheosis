package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.util.ExtraCodecs;

public enum GemVariant {
	PARITY("parity", 0),
	ARCANE("arcane", 1),
	SPLENDOR("splendor", 2),
	BREACH("breach", 3),
	GUARDIAN("guardian", 4),
	CHAOTIC("chaotic", 5),
	NECROTIC("necrotic", 6),
	MIRROR("mirror", 7),
	GEOMETRIC("geometric", 8),
	VALENCE("valence", 9),
	ENDERSURGE("endersurge", 10);

	public static final Map<String, GemVariant> BY_ID = Arrays.stream(GemVariant.values()).collect(Collectors.toMap(GemVariant::key, Function.identity()));
	public static final Codec<GemVariant> CODEC = ExtraCodecs.stringResolverCodec(GemVariant::key, GemVariant::byId);

	private final String key;
	private final int id;

	private GemVariant(String key, int id) {
		this.key = key;
		this.id = id;
	}

	public String key() {
		return this.key;
	}

	public int id() {
		return this.id;
	}

	@Nullable
	public static GemVariant byId(String id) {
		return BY_ID.get(id);
	}
}
