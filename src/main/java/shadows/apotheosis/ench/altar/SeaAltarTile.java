package shadows.apotheosis.ench.altar;

import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.ench.EnchantmentInfo;
import shadows.apotheosis.util.ParticleMessage;
import shadows.placebo.recipe.VanillaPacketDispatcher;
import shadows.placebo.util.EnchantmentUtils;
import shadows.placebo.util.NetworkUtils;

public class SeaAltarTile extends BlockEntity implements TickableBlockEntity {

	private Random rand = new Random();

	public SeaAltarTile() {
		super(ApotheosisObjects.ALTAR_TYPE);
	}

	protected ItemStackHandler inv = new ItemStackHandler(5);
	protected float xpDrained = 0;
	protected ItemStack target = ItemStack.EMPTY;
	protected float targetXP = 0;
	int soundTick = 0;

	@Override
	public void tick() {
		if (this.level.isClientSide) return;
		if (!this.inv.getStackInSlot(4).isEmpty()) return;
		for (int i = 0; i < 4; i++) {
			if (this.inv.getStackInSlot(i).isEmpty()) {
				this.target = ItemStack.EMPTY;
				this.targetXP = 0;
				return;
			}
		}
		if (!this.target.isEmpty()) {
			this.drainXP();
			if (this.xpDrained >= this.targetXP) {
				this.inv.setStackInSlot(4, this.target);
				this.target = ItemStack.EMPTY;
				this.xpDrained = this.targetXP = 0;
				for (int i = 0; i < 4; i++)
					this.inv.setStackInSlot(i, ItemStack.EMPTY);
				this.markAndNotify();
				this.level.playSound(null, this.worldPosition, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1, 1);
			}
		} else {
			this.findTarget(this.calcProvidedEnchValue());
		}
	}

	double[][] offsets = { { 3 / 16D, 3 / 16D }, { 3 / 16D, 13 / 16D }, { 13 / 16D, 3 / 16D }, { 13 / 16D, 13 / 16D } };

	public int calcProvidedEnchValue() {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			value += EnchantmentHelper.getEnchantments(this.inv.getStackInSlot(i)).entrySet().stream().map(this::getValueForEnch).collect(IntCollector.INSTANCE);
		}
		return value;
	}

	public int getValueForEnch(Entry<Enchantment, Integer> ench) {
		if (ench.getKey() == null) return 0;
		EnchantmentInfo info = EnchModule.getEnchInfo(ench.getKey());
		double avg = (info.getMaxPower(ench.getValue()) + info.getMinPower(ench.getValue())) / 2.5D;
		return (int) Math.floor(avg / 4);
	}

	public void drainXP() {
		List<Player> nearby = this.level.getEntitiesOfClass(Player.class, new AABB(this.worldPosition).inflate(5, 5, 5));
		boolean removed = false;
		for (Player p : nearby) {
			int maxDrain = (int) Math.ceil(this.targetXP / 200);
			int removable = Math.min(1 + maxDrain, p.totalExperience);
			EnchantmentUtils.addPlayerXP(p, -removable);
			this.xpDrained += removable;
			if (removable > 0) {
				this.trySpawnParticles(p, removable);
				removed = true;
			}
		}
		if (this.soundTick++ % 50 == 0) this.level.playSound(null, this.worldPosition, ApotheosisObjects.ALTAR_SOUND, SoundSource.BLOCKS, 0.5F, 1);
		if (!removed && this.soundTick % 10 == 0) {
			for (int i = 0; i < 4; i++) {
				ParticleMessage msg = new ParticleMessage(ParticleTypes.WITCH, this.worldPosition.getX() + this.offsets[i][0], this.worldPosition.getY() + 0.8, this.worldPosition.getZ() + this.offsets[i][1], 0, 0.1, 0, 1);
				NetworkUtils.sendToTracking(Apotheosis.CHANNEL, msg, (ServerLevel) this.level, this.worldPosition);
			}
		}
	}

	public void findTarget(int value) {
		value = Math.min(value, 85);
		ItemStack book = new ItemStack(Items.BOOK);
		this.target = new ItemStack(Items.ENCHANTED_BOOK);
		this.targetXP = EnchantmentUtils.getExperienceForLevel(value / 2);
		long seed = 1831;
		for (int i = 0; i < 4; i++)
			for (Enchantment e : EnchantmentHelper.getEnchantments(this.inv.getStackInSlot(i)).keySet()) {
				seed ^= e.getRegistryName().hashCode();
			}
		this.rand.setSeed(seed);
		int half = value / 2;
		List<EnchantmentInstance> datas = EnchantmentHelper.selectEnchantment(this.rand, book, value, true);
		while (datas.isEmpty() && value >= half) {
			datas = EnchantmentHelper.selectEnchantment(this.rand, book, value -= 5, true);
		}
		if (!datas.isEmpty()) {
			for (EnchantmentInstance d : datas)
				EnchantedBookItem.addEnchantment(this.target, d);
			this.level.playSound(null, this.worldPosition, ApotheosisObjects.ALTAR_SOUND, SoundSource.BLOCKS, 0.5F, 1);
			this.soundTick = 0;
		} else {
			this.target = ItemStack.EMPTY;
			this.targetXP = 0;
		}
	}

	public void trySpawnParticles(Player player, int xpDrain) {
		Vec3 to = new Vec3(player.getX() - (this.worldPosition.getX() + 0.5), player.getY() - this.worldPosition.getY(), player.getZ() - (this.worldPosition.getZ() + 0.5));
		ParticleMessage msg = new ParticleMessage(ParticleTypes.ENCHANT, this.worldPosition.getX() + this.level.random.nextDouble(), this.worldPosition.getY() + 1 + this.level.random.nextDouble(), this.worldPosition.getZ() + this.level.random.nextDouble(), to.x, to.y, to.z, Math.min(5, xpDrain));
		NetworkUtils.sendToTracking(Apotheosis.CHANNEL, msg, (ServerLevel) this.level, this.worldPosition);
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		tag.put("inv", this.inv.serializeNBT());
		tag.putFloat("xp", this.xpDrained);
		tag.put("target", this.target.serializeNBT());
		tag.putFloat("targetXP", this.targetXP);
		return super.save(tag);
	}

	@Override
	public void load(BlockState state, CompoundTag tag) {
		super.load(state, tag);
		this.inv.deserializeNBT(tag.getCompound("inv"));
		this.xpDrained = tag.getFloat("xp");
		this.target = ItemStack.of(tag.getCompound("target"));
		this.targetXP = tag.getFloat("targetXP");
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		tag.put("inv", this.inv.serializeNBT());
		return tag;
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundTag tag) {
		super.handleUpdateTag(state, tag);
		this.inv.deserializeNBT(tag.getCompound("inv"));
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return new ClientboundBlockEntityDataPacket(this.worldPosition, -1, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		this.handleUpdateTag(this.getBlockState(), pkt.getTag());
	}

	public ItemStackHandler getInv() {
		return this.inv;
	}

	public void markAndNotify() {
		this.setChanged();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

}