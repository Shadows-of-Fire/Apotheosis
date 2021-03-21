package shadows.apotheosis.ench.altar;

import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.ItemStackHandler;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.EnchModule;
import shadows.apotheosis.ench.EnchantmentInfo;
import shadows.apotheosis.util.ParticleMessage;
import shadows.placebo.recipe.VanillaPacketDispatcher;
import shadows.placebo.util.EnchantmentUtils;
import shadows.placebo.util.NetworkUtils;

public class SeaAltarTile extends TileEntity implements ITickableTileEntity {

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
		if (this.world.isRemote) return;
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
				this.world.playSound(null, this.pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1, 1);
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
		List<PlayerEntity> nearby = this.world.getEntitiesWithinAABB(PlayerEntity.class, new AxisAlignedBB(this.pos).grow(5, 5, 5));
		boolean removed = false;
		for (PlayerEntity p : nearby) {
			int maxDrain = (int) Math.ceil(this.targetXP / 200);
			int removable = Math.min(1 + maxDrain, p.experienceTotal);
			EnchantmentUtils.addPlayerXP(p, -removable);
			this.xpDrained += removable;
			if (removable > 0) {
				this.trySpawnParticles(p, removable);
				removed = true;
			}
		}
		if (this.soundTick++ % 50 == 0) this.world.playSound(null, this.pos, ApotheosisObjects.ALTAR_SOUND, SoundCategory.BLOCKS, 0.5F, 1);
		if (!removed && this.soundTick % 10 == 0) {
			for (int i = 0; i < 4; i++) {
				ParticleMessage msg = new ParticleMessage(ParticleTypes.WITCH, this.pos.getX() + this.offsets[i][0], this.pos.getY() + 0.8, this.pos.getZ() + this.offsets[i][1], 0, 0.1, 0, 1);
				NetworkUtils.sendToTracking(Apotheosis.CHANNEL, msg, (ServerWorld) this.world, this.pos);
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
		List<EnchantmentData> datas = EnchantmentHelper.buildEnchantmentList(this.rand, book, value, true);
		while (datas.isEmpty() && value >= half) {
			datas = EnchantmentHelper.buildEnchantmentList(this.rand, book, value -= 5, true);
		}
		if (!datas.isEmpty()) {
			for (EnchantmentData d : datas)
				EnchantedBookItem.addEnchantment(this.target, d);
			this.world.playSound(null, this.pos, ApotheosisObjects.ALTAR_SOUND, SoundCategory.BLOCKS, 0.5F, 1);
			this.soundTick = 0;
		} else {
			this.target = ItemStack.EMPTY;
			this.targetXP = 0;
		}
	}

	public void trySpawnParticles(PlayerEntity player, int xpDrain) {
		Vector3d to = new Vector3d(player.getPosX() - (this.pos.getX() + 0.5), player.getPosY() - this.pos.getY(), player.getPosZ() - (this.pos.getZ() + 0.5));
		ParticleMessage msg = new ParticleMessage(ParticleTypes.ENCHANT, this.pos.getX() + this.world.rand.nextDouble(), this.pos.getY() + 1 + this.world.rand.nextDouble(), this.pos.getZ() + this.world.rand.nextDouble(), to.x, to.y, to.z, Math.min(5, xpDrain));
		NetworkUtils.sendToTracking(Apotheosis.CHANNEL, msg, (ServerWorld) this.world, this.pos);
	}

	@Override
	public CompoundNBT write(CompoundNBT tag) {
		tag.put("inv", this.inv.serializeNBT());
		tag.putFloat("xp", this.xpDrained);
		tag.put("target", this.target.serializeNBT());
		tag.putFloat("targetXP", this.targetXP);
		return super.write(tag);
	}

	@Override
	public void read(BlockState state, CompoundNBT tag) {
		super.read(state, tag);
		this.inv.deserializeNBT(tag.getCompound("inv"));
		this.xpDrained = tag.getFloat("xp");
		this.target = ItemStack.read(tag.getCompound("target"));
		this.targetXP = tag.getFloat("targetXP");
	}

	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT tag = super.getUpdateTag();
		tag.put("inv", this.inv.serializeNBT());
		return tag;
	}

	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		super.handleUpdateTag(state, tag);
		this.inv.deserializeNBT(tag.getCompound("inv"));
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, -1, this.getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		this.handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
	}

	public ItemStackHandler getInv() {
		return this.inv;
	}

	public void markAndNotify() {
		this.markDirty();
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

}