package shadows.apotheosis.adventure.compat;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import shadows.apotheosis.adventure.boss.BossItem;
import shadows.apotheosis.adventure.boss.BossItemManager;
import shadows.apotheosis.adventure.loot.AffixLootManager;
import shadows.apotheosis.adventure.loot.LootController;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.gateways.entity.GatewayEntity;
import shadows.gateways.gate.Reward;
import shadows.gateways.gate.WaveEntity;
import shadows.placebo.json.PSerializer;

@SuppressWarnings("removal")
public class GatewaysCompat {

	public static void register() {
		WaveEntity.SERIALIZERS.put(new ResourceLocation("apotheosis:boss"), BossWaveEntity.SERIALIZER);
		Reward.SERIALIZERS.put("apotheosis:affix", PSerializer.autoRegister("Rarity Affix Reward", RarityAffixItemReward.class).build(true));
	}

	public static class BossWaveEntity implements WaveEntity {

		static final PSerializer<WaveEntity> SERIALIZER = PSerializer.<WaveEntity>autoRegister("Boss Wave Entity", BossWaveEntity.class).build(true);

		private final @Nullable BossItem boss;

		public BossWaveEntity(@Nullable BossItem boss) {
			this.boss = boss;
		}

		@Override
		public LivingEntity createEntity(Level level) {
			BossItem realBoss = this.boss == null ? BossItemManager.INSTANCE.getRandomItem(level.random) : this.boss;
			if (realBoss == null) return null; // error condition
			return realBoss.createBoss((ServerLevelAccessor) level, BlockPos.ZERO, level.random, 0);
		}

		@Override
		public Component getDescription() {
			return Component.translatable("misc.apotheosis.boss", Component.translatable(this.boss == null ? "misc.apotheosis.random" : boss.getEntity().getDescriptionId()));
		}

		@Override
		public AABB getAABB(double x, double y, double z) {
			return this.boss == null ? new AABB(0, 0, 0, 2, 2, 2).move(x, y, z) : this.boss.getSize();
		}

		@Override
		public boolean shouldFinalizeSpawn() {
			return false;
		}

		@Override
		public PSerializer<WaveEntity> getSerializer() {
			return SERIALIZER;
		}

		public JsonObject write() {
			JsonObject entityData = new JsonObject();
			if (this.boss != null) entityData.addProperty("boss", boss.getId().toString());
			return entityData;
		}

		public static WaveEntity read(JsonObject obj) {
			BossItem boss = obj.has("boss") ? BossItemManager.INSTANCE.getValue(new ResourceLocation(obj.get("boss").getAsString())) : null;
			return new BossWaveEntity(boss);
		}

		public void write(FriendlyByteBuf buf) {
			buf.writeResourceLocation(this.boss == null ? new ResourceLocation("null", "null") : this.boss.getId());
		}

		public static WaveEntity read(FriendlyByteBuf buf) {
			BossItem boss = BossItemManager.INSTANCE.getValue(buf.readResourceLocation());
			return new BossWaveEntity(boss);
		}
	}

	/**
	 * Provides a random affix item as a reward.
	 */
	public static record RarityAffixItemReward(LootRarity rarity) implements Reward {

		@Override
		public void generateLoot(ServerLevel level, GatewayEntity gate, Player summoner, Consumer<ItemStack> list) {
			list.accept(LootController.createLootItem(AffixLootManager.INSTANCE.getRandomItem(level.random, summoner.getLuck()).getStack(), rarity, level.random));
		}

		@Override
		public JsonObject write() {
			JsonObject obj = Reward.super.write();
			obj.addProperty("rarity", rarity.id());
			return obj;
		}

		public static RarityAffixItemReward read(JsonObject obj) {
			return new RarityAffixItemReward(LootRarity.byId(obj.get("rarity").getAsString()));
		}

		@Override
		public void write(FriendlyByteBuf buf) {
			Reward.super.write(buf);
			buf.writeUtf(this.rarity.id());
		}

		public static RarityAffixItemReward read(FriendlyByteBuf buf) {
			return new RarityAffixItemReward(LootRarity.byId(buf.readUtf()));
		}

		@Override
		public String getName() {
			return "apotheosis:affix";
		}

		@Override
		public void appendHoverText(Consumer<Component> list) {
			list.accept(Component.translatable("reward.apotheosis.affix", this.rarity.toComponent()));
		}
	}
}
