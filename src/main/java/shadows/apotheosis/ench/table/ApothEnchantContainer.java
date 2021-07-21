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
		super(id, inv, IWorldPosCallable.NULL);
		this.slots.clear();
		this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return true;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		this.addSlot(new Slot(this.enchantSlots, 1, 35, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(stack.getItem());
			}
		});
		this.initCommon(inv);
	}

	public ApothEnchantContainer(int id, PlayerInventory inv, IWorldPosCallable wPos, ApothEnchantTile te) {
		super(id, inv, wPos);
		this.slots.clear();
		this.addSlot(new Slot(this.enchantSlots, 0, 15, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return true;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		this.addSlot(new SlotItemHandler(te.inv, 0, 35, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
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
		this.addDataSlots(this.eterna.getArray());
		this.addDataSlots(this.quanta.getArray());
		this.addDataSlots(this.arcana.getArray());
	}

	@Override
	public boolean clickMenuButton(PlayerEntity player, int id) {
		int level = this.costs[id];
		ItemStack toEnchant = this.enchantSlots.getItem(0);
		ItemStack lapis = this.getSlot(1).getItem();
		int i = id + 1;
		if ((lapis.isEmpty() || lapis.getCount() < i) && !player.abilities.instabuild) return false;

		if (this.costs[id] <= 0 || toEnchant.isEmpty() || (player.experienceLevel < i || player.experienceLevel < this.costs[id]) && !player.abilities.instabuild) return false;

		this.access.execute((world, pos) -> {
			ItemStack enchanted = toEnchant;
			List<EnchantmentData> list = this.getEnchantmentList(toEnchant, id, this.costs[id]);
			if (!list.isEmpty()) {
				player.onEnchantmentPerformed(toEnchant, i);
				boolean flag = toEnchant.getItem() == Items.BOOK || toEnchant.getItem() instanceof TomeItem;
				if (flag) {
					enchanted = new ItemStack(Items.ENCHANTED_BOOK);
					this.enchantSlots.setItem(0, enchanted);
				}

				for (int j = 0; j < list.size(); ++j) {
					EnchantmentData enchantmentdata = list.get(j);
					if (flag) {
						EnchantedBookItem.addEnchantment(enchanted, enchantmentdata);
					} else {
						enchanted.enchant(enchantmentdata.enchantment, enchantmentdata.level);
					}
				}

				if (!player.abilities.instabuild) {
					lapis.shrink(i);
					if (lapis.isEmpty()) {
						this.enchantSlots.setItem(1, ItemStack.EMPTY);
					}
				}

				player.awardStat(Stats.ENCHANT_ITEM);
				if (player instanceof ServerPlayerEntity) {
					((EnchantedTrigger) CriteriaTriggers.ENCHANTED_ITEM).trigger((ServerPlayerEntity) player, enchanted, level, this.eterna.get(), this.quanta.get(), this.arcana.get());
				}

				this.enchantSlots.setChanged();
				this.enchantmentSeed.set(player.getEnchantmentSeed());
				this.slotsChanged(this.enchantSlots);
				world.playSound((PlayerEntity) null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
			}

		});
		return true;

	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void slotsChanged(IInventory inventoryIn) {
		this.access.evaluate((world, pos) -> {
			if (inventoryIn == this.enchantSlots) {
				ItemStack itemstack = inventoryIn.getItem(0);
				if (itemstack.getCount() == 1 && itemstack.getItem().isEnchantable(itemstack) && this.isEnchantableEnough(itemstack)) {
					this.gatherStats();
					float power = this.eterna.get();
					if (power < 1.5) power = 1.5F;
					this.random.setSeed(this.enchantmentSeed.get());

					for (int num = 0; num < 3; ++num) {
						this.costs[num] = RealEnchantmentHelper.calcSlotLevel(this.random, num, power, itemstack);
						this.enchantClue[num] = -1;
						this.levelClue[num] = -1;

						if (this.costs[num] < num + 1) {
							this.costs[num] = 0;
						}
						this.costs[num] = ForgeEventFactory.onEnchantmentLevelSet(world, pos, num, (int) power, itemstack, this.costs[num]);
					}

					for (int j1 = 0; j1 < 3; ++j1) {
						if (this.costs[j1] > 0) {
							List<EnchantmentData> list = this.getEnchantmentList(itemstack, j1, this.costs[j1]);

							if (list != null && !list.isEmpty()) {
								EnchantmentData enchantmentdata = list.get(this.random.nextInt(list.size()));
								this.enchantClue[j1] = Registry.ENCHANTMENT.getId(enchantmentdata.enchantment);
								this.levelClue[j1] = enchantmentdata.level;
							}
						}
					}

					this.broadcastChanges();
				} else {
					for (int i = 0; i < 3; ++i) {
						this.costs[i] = 0;
						this.enchantClue[i] = -1;
						this.levelClue[i] = -1;
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
		this.random.setSeed(this.enchantmentSeed.get() + enchantSlot);
		List<EnchantmentData> list = RealEnchantmentHelper.buildEnchantmentList(this.random, stack, level, this.quanta.get(), this.arcana.get(), false);
		return list;
	}

	public void gatherStats() {
		this.access.evaluate((world, pos) -> {
			Float2FloatMap eternaMap = new Float2FloatOpenHashMap();
			float[] stats = { 0, 1, 0 };
			for (int j = -1; j <= 1; ++j) {
				for (int k = -1; k <= 1; ++k) {
					if ((j != 0 || k != 0) && world.isEmptyBlock(pos.offset(k, 0, j)) && world.isEmptyBlock(pos.offset(k, 1, j))) {
						this.gatherStats(eternaMap, stats, world, pos.offset(k * 2, 0, j * 2));
						this.gatherStats(eternaMap, stats, world, pos.offset(k * 2, 1, j * 2));
						if (k != 0 && j != 0) {
							this.gatherStats(eternaMap, stats, world, pos.offset(k * 2, 0, j));
							this.gatherStats(eternaMap, stats, world, pos.offset(k * 2, 1, j));
							this.gatherStats(eternaMap, stats, world, pos.offset(k, 0, j * 2));
							this.gatherStats(eternaMap, stats, world, pos.offset(k, 1, j * 2));
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