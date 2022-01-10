package shadows.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.SlotItemHandler;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.advancements.EnchantedTrigger;
import shadows.apotheosis.util.FloatReferenceHolder;
import shadows.placebo.network.PacketDistro;

public class ApothEnchantContainer extends EnchantmentMenu {

	protected final FloatReferenceHolder eterna = new FloatReferenceHolder(0F, 0, EnchantingStatManager.getAbsoluteMaxEterna());
	protected final FloatReferenceHolder quanta = new FloatReferenceHolder(0F, 0, 100);
	protected final FloatReferenceHolder arcana = new FloatReferenceHolder(0F, 0, 100);
	protected final FloatReferenceHolder rectification = new FloatReferenceHolder(0F, -100, 100);
	protected final DataSlot clues = DataSlot.standalone();
	protected final Player player;

	public ApothEnchantContainer(int id, Inventory inv) {
		super(id, inv, ContainerLevelAccess.NULL);
		this.player = inv.player;
		this.slots.clear();
		this.addSecretSlot(new Slot(this.enchantSlots, 0, 15, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return true;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		this.addSecretSlot(new Slot(this.enchantSlots, 1, 35, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(stack.getItem());
			}
		});
		this.initCommon(inv);

	}

	public ApothEnchantContainer(int id, Inventory inv, ContainerLevelAccess wPos, ApothEnchantTile te) {
		super(id, inv, wPos);
		this.player = inv.player;
		this.slots.clear();
		this.addSecretSlot(new Slot(this.enchantSlots, 0, 15, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return true;
			}

			@Override
			public int getMaxStackSize() {
				return 1;
			}
		});
		this.addSecretSlot(new SlotItemHandler(te.inv, 0, 35, 47) {
			@Override
			public boolean mayPlace(ItemStack stack) {
				return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(stack.getItem());
			}
		});
		this.initCommon(inv);
	}

	protected Slot addSecretSlot(Slot pSlot) {
		pSlot.index = this.slots.size();
		this.slots.add(pSlot);
		return pSlot;
	}

	private void initCommon(Inventory inv) {
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSecretSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 31));
			}
		}
		for (int k = 0; k < 9; ++k) {
			this.addSecretSlot(new Slot(inv, k, 8 + k * 18, 142 + 31));
		}
		this.addDataSlots(this.eterna.getArray());
		this.addDataSlots(this.quanta.getArray());
		this.addDataSlots(this.arcana.getArray());
		this.addDataSlots(this.rectification.getArray());
		clues.set(0);
		this.addDataSlot(clues);
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		int level = this.costs[id];
		ItemStack toEnchant = this.enchantSlots.getItem(0);
		ItemStack lapis = this.getSlot(1).getItem();
		int cost = id + 1;
		if ((lapis.isEmpty() || lapis.getCount() < cost) && !player.getAbilities().instabuild) return false;

		if (this.costs[id] <= 0 || toEnchant.isEmpty() || (player.experienceLevel < cost || player.experienceLevel < this.costs[id]) && !player.getAbilities().instabuild) return false;

		this.access.execute((world, pos) -> {
			ItemStack enchanted = toEnchant;
			List<EnchantmentInstance> list = this.getEnchantmentList(toEnchant, id, this.costs[id]);
			if (!list.isEmpty()) {
				player.onEnchantmentPerformed(toEnchant, cost);
				if (list.get(0).enchantment == Apoth.Enchantments.INFUSION) {
					List<EnchantingRecipe> recipes = world.getServer().getRecipeManager().getAllRecipesFor(EnchantingRecipe.TYPE);
					EnchantingRecipe match = recipes.stream().filter(r -> r.matches(toEnchant, this.eterna.get(), this.quanta.get(), this.arcana.get())).findFirst().orElse(null);
					if (match != null) this.enchantSlots.setItem(0, match.getResultItem().copy());
					else return;
				} else this.enchantSlots.setItem(0, ((IEnchantableItem) toEnchant.getItem()).onEnchantment(toEnchant, list));

				if (!player.getAbilities().instabuild) {
					lapis.shrink(cost);
					if (lapis.isEmpty()) {
						this.enchantSlots.setItem(1, ItemStack.EMPTY);
					}
				}

				player.awardStat(Stats.ENCHANT_ITEM);
				if (player instanceof ServerPlayer) {
					((EnchantedTrigger) CriteriaTriggers.ENCHANTED_ITEM).trigger((ServerPlayer) player, enchanted, level, this.eterna.get(), this.quanta.get(), this.arcana.get());
				}

				this.enchantSlots.setChanged();
				this.enchantmentSeed.set(player.getEnchantmentSeed());
				this.slotsChanged(this.enchantSlots);
				world.playSound((Player) null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
			}

		});
		return true;

	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void slotsChanged(Container inventoryIn) {
		this.access.evaluate((world, pos) -> {
			if (inventoryIn == this.enchantSlots) {
				ItemStack toEnchant = inventoryIn.getItem(0);
				this.gatherStats();
				EnchantingRecipe match = world.getRecipeManager().getAllRecipesFor(EnchantingRecipe.TYPE).stream().filter(r -> r.matches(toEnchant, this.eterna.get(), this.quanta.get(), this.arcana.get())).findFirst().orElse(null);
				if (toEnchant.getCount() == 1 && (match != null || (toEnchant.getItem().isEnchantable(toEnchant) && isEnchantableEnough(toEnchant)))) {
					float eterna = this.eterna.get();
					if (eterna < 1.5) eterna = 1.5F; // Allow for enchanting with no bookshelves as vanilla does
					this.random.setSeed(this.enchantmentSeed.get());

					for (int slot = 0; slot < 3; ++slot) {
						this.costs[slot] = RealEnchantmentHelper.calcSlotLevel(this.random, slot, eterna, toEnchant);
						this.enchantClue[slot] = -1;
						this.levelClue[slot] = -1;

						if (this.costs[slot] < slot + 1) {
							this.costs[slot]++;
						}
						this.costs[slot] = ForgeEventFactory.onEnchantmentLevelSet(world, pos, slot, Math.round(eterna), toEnchant, this.costs[slot]);
					}

					for (int slot = 0; slot < 3; ++slot) {
						if (this.costs[slot] > 0) {
							List<EnchantmentInstance> list = this.getEnchantmentList(toEnchant, slot, this.costs[slot]);

							if (list != null && !list.isEmpty()) {
								EnchantmentInstance enchantmentdata = list.remove(this.random.nextInt(list.size()));
								this.enchantClue[slot] = Registry.ENCHANTMENT.getId(enchantmentdata.enchantment);
								this.levelClue[slot] = enchantmentdata.level;
								int clues = 1 + this.clues.get();
								List<EnchantmentInstance> clueList = new ArrayList<>();
								if (clues-- > 0) clueList.add(enchantmentdata);
								while (clues-- > 0 && !list.isEmpty()) {
									clueList.add(list.remove(this.random.nextInt(list.size())));
								}
								PacketDistro.sendTo(Apotheosis.CHANNEL, new ClueMessage(slot, clueList, list.isEmpty()), player);
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

	private List<EnchantmentInstance> getEnchantmentList(ItemStack stack, int enchantSlot, int level) {
		this.random.setSeed(this.enchantmentSeed.get() + enchantSlot);
		List<EnchantmentInstance> list = RealEnchantmentHelper.buildEnchantmentList(this.random, stack, level, this.quanta.get(), this.arcana.get(), this.rectification.get(), false);
		EnchantingRecipe match = this.access.evaluate((world, pos) -> {
			List<EnchantingRecipe> recipes = world.getServer().getRecipeManager().getAllRecipesFor(EnchantingRecipe.TYPE);
			return recipes.stream().filter(r -> r.matches(stack, this.eterna.get(), this.quanta.get(), this.arcana.get())).findFirst();
		}).get().orElse(null);
		if (enchantSlot == 2 && match != null) {
			list.clear();
			list.add(new EnchantmentInstance(Apoth.Enchantments.INFUSION, match.displayLevel));
		}
		return list;
	}

	public void gatherStats() {
		this.access.evaluate((world, pos) -> {
			Float2FloatMap eternaMap = new Float2FloatOpenHashMap();
			float[] stats = { 0, 15F, 0, 0, 0 };
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
			this.arcana.set(stats[2] + this.getSlot(0).getItem().getItemEnchantability() / 2F);
			this.rectification.set(stats[3]);
			this.clues.set((int) stats[4]);
			return this;
		}).orElse(this);
	}

	public void gatherStats(Float2FloatMap eternaMap, float[] stats, Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.isAir()) return;
		float max = EnchantingStatManager.getMaxEterna(state, world, pos);
		float eterna = EnchantingStatManager.getEterna(state, world, pos);
		eternaMap.put(max, eternaMap.getOrDefault(max, 0) + eterna);
		float quanta = EnchantingStatManager.getQuanta(state, world, pos);
		stats[1] += quanta;
		float arcana = EnchantingStatManager.getArcana(state, world, pos);
		stats[2] += arcana;
		float quantaRec = EnchantingStatManager.getQuantaRectification(state, world, pos);
		stats[3] += quantaRec;
		int clues = EnchantingStatManager.getBonusClues(state, world, pos);
		stats[4] += clues;
	}

	@Override
	public MenuType<?> getType() {
		return Apoth.Menus.ENCHANTING_TABLE;
	}

	/**
	 * An item can be enchanted if it is not enchanted, or all the enchantments on it are curses.
	 */
	public static boolean isEnchantableEnough(ItemStack stack) {
		if (!stack.isEnchanted()) return true;
		else return EnchantmentHelper.getEnchantments(stack).keySet().stream().allMatch(Enchantment::isCurse);
	}

	/**
	 * Arcana Tiers, each represents a new rarity set.
	 */
	public static enum Arcana {
		EMPTY(0, 10, 5, 2, 1),
		LITTLE(10, 8, 5, 3, 1),
		FEW(20, 7, 5, 4, 2),
		SOME(30, 5, 5, 4, 2),
		LESS(40, 5, 5, 4, 3),
		MEDIUM(50, 5, 5, 5, 5),
		MORE(60, 3, 4, 5, 5),
		VALUE(70, 2, 4, 5, 5),
		EXTRA(80, 2, 4, 5, 7),
		ALMOST(90, 1, 3, 5, 8),
		MAX(99, 1, 2, 5, 10);

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