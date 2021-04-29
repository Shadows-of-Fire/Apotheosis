package shadows.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.SlotItemHandler;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.advancements.EnchantedTrigger;
import shadows.apotheosis.ench.objects.TomeItem;
import shadows.apotheosis.util.FloatReferenceHolder;

public class ApothEnchantContainer extends EnchantmentContainer {

	protected FloatReferenceHolder eterna = new FloatReferenceHolder(0F, 0, EnchantingStatManager.getAbsoluteMaxEterna());
	protected FloatReferenceHolder quanta = new FloatReferenceHolder(0F, 0, 10);
	protected FloatReferenceHolder arcana = new FloatReferenceHolder(0F, 0, 10);

	public ApothEnchantContainer(int id, PlayerInventory inv) {
		super(id, inv, IWorldPosCallable.DUMMY);
		this.inventorySlots.clear();
		this.addSlot(new Slot(this.tableInventory, 0, 15, 47) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return true;
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});
		this.addSlot(new Slot(this.tableInventory, 1, 35, 47) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(stack.getItem());
			}
		});
		this.initCommon(inv);
	}

	public ApothEnchantContainer(int id, PlayerInventory inv, IWorldPosCallable wPos, ApothEnchantTile te) {
		super(id, inv, wPos);
		this.inventorySlots.clear();
		this.addSlot(new Slot(this.tableInventory, 0, 15, 47) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return true;
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});
		this.addSlot(new SlotItemHandler(te.inv, 0, 35, 47) {
			@Override
			public boolean isItemValid(ItemStack stack) {
				return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(stack.getItem());
			}
		});
		this.initCommon(inv);
	}

	private void initCommon(PlayerInventory inv) {
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 31));
			}
		}
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(inv, k, 8 + k * 18, 142 + 31));
		}
		this.trackIntArray(this.eterna.getArray());
		this.trackIntArray(this.quanta.getArray());
		this.trackIntArray(this.arcana.getArray());
	}

	@Override
	public boolean enchantItem(PlayerEntity player, int id) {
		int level = this.enchantLevels[id];
		ItemStack toEnchant = this.tableInventory.getStackInSlot(0);
		ItemStack lapis = this.getSlot(1).getStack();
		int i = id + 1;
		if ((lapis.isEmpty() || lapis.getCount() < i) && !player.abilities.isCreativeMode) return false;

		if (this.enchantLevels[id] <= 0 || toEnchant.isEmpty() || (player.experienceLevel < i || player.experienceLevel < this.enchantLevels[id]) && !player.abilities.isCreativeMode) return false;

		this.worldPosCallable.consume((world, pos) -> {
			ItemStack enchanted = toEnchant;
			List<EnchantmentData> list = this.getEnchantmentList(toEnchant, id, this.enchantLevels[id]);
			if (!list.isEmpty()) {
				player.onEnchant(toEnchant, i);
				boolean flag = toEnchant.getItem() == Items.BOOK || toEnchant.getItem() instanceof TomeItem;
				if (flag) {
					enchanted = new ItemStack(Items.ENCHANTED_BOOK);
					this.tableInventory.setInventorySlotContents(0, enchanted);
				}

				for (int j = 0; j < list.size(); ++j) {
					EnchantmentData enchantmentdata = list.get(j);
					if (flag) {
						EnchantedBookItem.addEnchantment(enchanted, enchantmentdata);
					} else {
						enchanted.addEnchantment(enchantmentdata.enchantment, enchantmentdata.enchantmentLevel);
					}
				}

				if (!player.abilities.isCreativeMode) {
					lapis.shrink(i);
					if (lapis.isEmpty()) {
						this.tableInventory.setInventorySlotContents(1, ItemStack.EMPTY);
					}
				}

				player.addStat(Stats.ENCHANT_ITEM);
				if (player instanceof ServerPlayerEntity) {
					((EnchantedTrigger) CriteriaTriggers.ENCHANTED_ITEM).trigger((ServerPlayerEntity) player, enchanted, level, this.eterna.get(), this.quanta.get(), this.arcana.get());
				}

				this.tableInventory.markDirty();
				this.xpSeed.set(player.getXPSeed());
				this.onCraftMatrixChanged(this.tableInventory);
				world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() * 0.1F + 0.9F);
			}

		});
		return true;

	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		this.worldPosCallable.apply((world, pos) -> {
			if (inventoryIn == this.tableInventory) {
				ItemStack itemstack = inventoryIn.getStackInSlot(0);
				if (itemstack.getCount() == 1 && itemstack.getItem().isEnchantable(itemstack) && this.isEnchantableEnough(itemstack)) {
					this.gatherStats();
					float power = this.eterna.get();
					if (power < 1.5) power = 1.5F;
					this.rand.setSeed(this.xpSeed.get());

					for (int num = 0; num < 3; ++num) {
						this.enchantLevels[num] = RealEnchantmentHelper.calcSlotLevel(this.rand, num, power, itemstack);
						this.enchantClue[num] = -1;
						this.worldClue[num] = -1;

						if (this.enchantLevels[num] < num + 1) {
							this.enchantLevels[num] = 0;
						}
						this.enchantLevels[num] = ForgeEventFactory.onEnchantmentLevelSet(world, pos, num, (int) power, itemstack, this.enchantLevels[num]);
					}

					for (int j1 = 0; j1 < 3; ++j1) {
						if (this.enchantLevels[j1] > 0) {
							List<EnchantmentData> list = this.getEnchantmentList(itemstack, j1, this.enchantLevels[j1]);

							if (list != null && !list.isEmpty()) {
								EnchantmentData enchantmentdata = list.get(this.rand.nextInt(list.size()));
								this.enchantClue[j1] = Registry.ENCHANTMENT.getId(enchantmentdata.enchantment);
								this.worldClue[j1] = enchantmentdata.enchantmentLevel;
							}
						}
					}

					this.detectAndSendChanges();
				} else {
					for (int i = 0; i < 3; ++i) {
						this.enchantLevels[i] = 0;
						this.enchantClue[i] = -1;
						this.worldClue[i] = -1;
					}
					this.eterna.set(0);
					this.quanta.set(0);
					this.arcana.set(0);
				}
			}
			return this;
		});
	}

	private List<EnchantmentData> getEnchantmentList(ItemStack stack, int enchantSlot, int level) {
		this.rand.setSeed(this.xpSeed.get() + enchantSlot);
		List<EnchantmentData> list = RealEnchantmentHelper.buildEnchantmentList(this.rand, stack, level, this.quanta.get(), this.arcana.get(), false);
		return list;
	}

	public void gatherStats() {
		this.worldPosCallable.apply((world, pos) -> {
			Float2FloatMap eternaMap = new Float2FloatOpenHashMap();
			float[] stats = { 0, 1, 0 };
			for (int j = -1; j <= 1; ++j) {
				for (int k = -1; k <= 1; ++k) {
					if ((j != 0 || k != 0) && world.isAirBlock(pos.add(k, 0, j)) && world.isAirBlock(pos.add(k, 1, j))) {
						this.gatherStats(eternaMap, stats, world, pos.add(k * 2, 0, j * 2));
						this.gatherStats(eternaMap, stats, world, pos.add(k * 2, 1, j * 2));
						if (k != 0 && j != 0) {
							this.gatherStats(eternaMap, stats, world, pos.add(k * 2, 0, j));
							this.gatherStats(eternaMap, stats, world, pos.add(k * 2, 1, j));
							this.gatherStats(eternaMap, stats, world, pos.add(k, 0, j * 2));
							this.gatherStats(eternaMap, stats, world, pos.add(k, 1, j * 2));
						}
					}
				}
			}
			List<Float2FloatMap.Entry> entries = new ArrayList<>(eternaMap.float2FloatEntrySet());
			Collections.sort(entries, Comparator.comparing(Float2FloatMap.Entry::getFloatKey));
			for (Float2FloatMap.Entry e : entries) {
				if (e.getFloatKey() > 0) stats[0] = Math.min(e.getFloatKey(), stats[0] + e.getFloatValue());
				else stats[0] += e.getFloatValue();
			}
			this.eterna.set(stats[0]);
			this.quanta.set(stats[1]);
			this.arcana.set(stats[2]);
			return this;
		}).orElse(this);
	}

	public void gatherStats(Float2FloatMap eternaMap, float[] stats, World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock().isAir(state, world, pos)) return;
		float max = EnchantingStatManager.getMaxEterna(state, world, pos);
		float eterna = EnchantingStatManager.getEterna(state, world, pos);
		eternaMap.put(max, eternaMap.getOrDefault(max, 0) + eterna);
		float quanta = EnchantingStatManager.getQuanta(state, world, pos);
		stats[1] += quanta;
		float arcana = EnchantingStatManager.getArcana(state, world, pos);
		stats[2] += arcana;
	}

	@Override
	public ContainerType<?> getType() {
		return ApotheosisObjects.ENCHANTING;
	}

	public boolean isEnchantableEnough(ItemStack stack) {
		if (!stack.isEnchanted()) return true;
		else return EnchantmentHelper.getEnchantments(stack).keySet().stream().allMatch(Enchantment::isCurse);
	}

	/**
	 * Arcana Tiers, each represents a new rarity set.
	 */
	public static enum Arcana {
		EMPTY(0F, 10, 5, 2, 1),
		LITTLE(1F, 8, 5, 3, 1),
		FEW(2F, 7, 5, 4, 2),
		SOME(3F, 5, 5, 4, 2),
		LESS(4F, 5, 5, 4, 3),
		MEDIUM(5F, 5, 5, 5, 5),
		MORE(6F, 3, 4, 5, 5),
		VALUE(7F, 2, 4, 5, 5),
		EXTRA(8F, 2, 4, 5, 7),
		ALMOST(9F, 1, 3, 5, 8),
		MAX(9.9F, 1, 2, 5, 10);

		final float threshold;
		final int[] rarities;

		Arcana(float threshold, int... rarities) {
			this.threshold = threshold;
			this.rarities = rarities;
		}

		static Arcana[] VALUES = values();

		public int[] getRarities() {
			return this.rarities;
		}

		public static Arcana getForThreshold(float threshold) {
			for (int i = VALUES.length - 1; i >= 0; i--) {
				if (threshold >= VALUES[i].threshold) return VALUES[i];
			}
			return EMPTY;
		}

	}
}