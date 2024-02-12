package shadows.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.SlotItemHandler;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.advancements.EnchantedTrigger;
import shadows.apotheosis.util.ApothMiscUtil;
import shadows.apotheosis.util.FloatReferenceHolder;
import shadows.placebo.network.PacketDistro;
import shadows.placebo.util.EnchantmentUtils;

public class ApothEnchantmentMenu extends EnchantmentMenu {

    protected final FloatReferenceHolder eterna = new FloatReferenceHolder(0F, 0, EnchantingStatManager.getAbsoluteMaxEterna());
    protected final FloatReferenceHolder quanta = new FloatReferenceHolder(0F, 0, 100);
    protected final FloatReferenceHolder arcana = new FloatReferenceHolder(0F, 0, 100);
    protected final FloatReferenceHolder rectification = new FloatReferenceHolder(0F, -100, 100);
    protected final DataSlot clues = DataSlot.standalone();
    protected final Player player;

    public ApothEnchantmentMenu(int id, Inventory inv) {
        super(id, inv, ContainerLevelAccess.NULL);
        this.player = inv.player;
        this.slots.clear();
        this.addSecretSlot(new Slot(this.enchantSlots, 0, 15, 47){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSecretSlot(new Slot(this.enchantSlots, 1, 35, 47){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Tags.Items.ENCHANTING_FUELS);
            }
        });
        this.initCommon(inv);

    }

    public ApothEnchantmentMenu(int id, Inventory inv, ContainerLevelAccess wPos, ApothEnchantTile te) {
        super(id, inv, wPos);
        this.player = inv.player;
        this.slots.clear();
        this.addSecretSlot(new Slot(this.enchantSlots, 0, 15, 47){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSecretSlot(new SlotItemHandler(te.inv, 0, 35, 47){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Tags.Items.ENCHANTING_FUELS);
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
        this.clues.set(1);
        this.addDataSlot(this.clues);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        int slot = id;
        int level = this.costs[slot];
        ItemStack toEnchant = this.enchantSlots.getItem(0);
        ItemStack lapis = this.getSlot(1).getItem();
        int cost = slot + 1;
        if ((lapis.isEmpty() || lapis.getCount() < cost) && !player.getAbilities().instabuild) return false;

        if (this.costs[slot] <= 0 || toEnchant.isEmpty() || (player.experienceLevel < cost || player.experienceLevel < this.costs[slot]) && !player.getAbilities().instabuild) return false;

        this.access.execute((world, pos) -> {
            ItemStack enchanted = toEnchant;
            float eterna = this.eterna.get(), quanta = this.quanta.get(), arcana = this.arcana.get(),
                rectification = this.rectification.get();
            List<EnchantmentInstance> list = this.getEnchantmentList(toEnchant, slot, this.costs[slot]);
            if (!list.isEmpty() && EnchantmentUtils.chargeExperience(player, ApothMiscUtil.getExpCostForSlot(level, slot))) {
                player.onEnchantmentPerformed(toEnchant, 0); // Pass zero here instead of the cost so no experience is taken, but the method is still called for tracking reasons.
                if (list.get(0).enchantment == Apoth.Enchantments.INFUSION.get()) {
                    EnchantingRecipe match = EnchantingRecipe.findMatch(world, toEnchant, eterna, quanta, arcana);
                    if (match != null) this.enchantSlots.setItem(0, match.assemble(toEnchant, eterna, quanta, arcana));
                    else return;
                }
                else this.enchantSlots.setItem(0, ((IEnchantableItem) toEnchant.getItem()).onEnchantment(toEnchant, list));

                if (!player.getAbilities().instabuild) {
                    lapis.shrink(cost);
                    if (lapis.isEmpty()) {
                        this.enchantSlots.setItem(1, ItemStack.EMPTY);
                    }
                }

                player.awardStat(Stats.ENCHANT_ITEM);
                if (player instanceof ServerPlayer) {
                    ((EnchantedTrigger) CriteriaTriggers.ENCHANTED_ITEM).trigger((ServerPlayer) player, enchanted, level, eterna, quanta, arcana, rectification);
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
                EnchantingRecipe match = EnchantingRecipe.findItemMatch(world, toEnchant);
                if (toEnchant.getCount() == 1 && (match != null || toEnchant.getItem().isEnchantable(toEnchant) && isEnchantableEnough(toEnchant))) {
                    float eterna = this.eterna.get();
                    if (eterna < 1.5) eterna = 1.5F; // Allow for enchanting with no bookshelves as vanilla does
                    this.random.setSeed(this.enchantmentSeed.get());

                    for (int slot = 0; slot < 3; ++slot) {
                        this.costs[slot] = RealEnchantmentHelper.getEnchantmentCost(this.random, slot, eterna, toEnchant);
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
                                int clues = this.clues.get();
                                List<EnchantmentInstance> clueList = new ArrayList<>();
                                if (clues-- > 0) clueList.add(enchantmentdata);
                                while (clues-- > 0 && !list.isEmpty()) {
                                    clueList.add(list.remove(this.random.nextInt(list.size())));
                                }
                                PacketDistro.sendTo(Apotheosis.CHANNEL, new ClueMessage(slot, clueList, list.isEmpty()), this.player);
                            }
                        }
                    }

                    this.broadcastChanges();
                }
                else {
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
        List<EnchantmentInstance> list = RealEnchantmentHelper.selectEnchantment(this.random, stack, level, this.quanta.get(), this.arcana.get(), this.rectification.get(), false);
        EnchantingRecipe match = this.access.evaluate((world, pos) -> Optional.ofNullable(EnchantingRecipe.findMatch(world, stack, this.eterna.get(), this.quanta.get(), this.arcana.get()))).get().orElse(null);
        if (enchantSlot == 2 && match != null) {
            list.clear();
            list.add(new EnchantmentInstance(Apoth.Enchantments.INFUSION.get(), 1));
        }
        return list;
    }

    public void gatherStats() {
        this.access.evaluate((world, pos) -> {
            TableStats stats = gatherStats(world, pos);
            this.eterna.set(stats.eterna());
            this.quanta.set(stats.quanta());
            this.arcana.set(stats.arcana() + this.getSlot(0).getItem().getEnchantmentValue() / 2F);
            this.rectification.set(stats.rectification());
            this.clues.set(stats.clues());
            return this;
        }).orElse(this);
    }

    /**
     * Gathers all enchanting stats for an enchantment table located at the specified position.
     * 
     * @param level The level.
     * @param pos   The position of the enchantment table.
     * @return The computed {@link TableStats}.
     */
    public static TableStats gatherStats(Level level, BlockPos pos) {
        Float2FloatMap eternaMap = new Float2FloatOpenHashMap();
        // Base Stats are 15% Quanta and 1 Clue, but 0 of everything else.
        float[] stats = { 0, 15F, 0, 0, 1 };
        for (BlockPos offset : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
            if (canReadStatsFrom(level, pos, offset)) {
                gatherStats(eternaMap, stats, level, pos.offset(offset));
            }
        }
        List<Float2FloatMap.Entry> entries = new ArrayList<>(eternaMap.float2FloatEntrySet());
        Collections.sort(entries, Comparator.comparing(Float2FloatMap.Entry::getFloatKey));
        for (Float2FloatMap.Entry e : entries) {
            if (e.getFloatKey() > 0) stats[0] = Math.min(e.getFloatKey(), stats[0] + e.getFloatValue());
            else stats[0] += e.getFloatValue();
        }
        return new TableStats(stats);
    }

    /**
     * Checks if stats can be read from a block at a particular offset.
     * 
     * @param level    The level.
     * @param tablePos The position of the enchanting table.
     * @param offset   The offset being checked.
     * @return True if the block between the table and the offset is {@link BlockTags#ENCHANTMENT_POWER_TRANSMITTER}, false otherwise.
     */
    public static boolean canReadStatsFrom(Level level, BlockPos tablePos, BlockPos offset) {
        return level.getBlockState(tablePos.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2)).getMaterial().isReplaceable();
    }

    /**
     * Collects enchanting stats from a particular shelf spot into the stat array.<br>
     * If you are collecting all stats, you should use {@link #gatherStats(Level, BlockPos)} instead.
     * 
     * @param eternaMap A map of max eterna contributions to eterna contributions for that max.
     * @param stats     The stat array, with order {eterna, quanta, arcana, rectification, clues}.
     * @param world     The world.
     * @param pos       The position of the stat-providing block.
     */
    public static void gatherStats(Float2FloatMap eternaMap, float[] stats, Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return;
        float max = EnchantingStatManager.getMaxEterna(state, world, pos);
        float eterna = EnchantingStatManager.getEterna(state, world, pos);
        eternaMap.put(max, eternaMap.getOrDefault(max, 0) + eterna);

        stats[1] += EnchantingStatManager.getQuanta(state, world, pos);
        stats[2] += EnchantingStatManager.getArcana(state, world, pos);
        stats[3] += EnchantingStatManager.getQuantaRectification(state, world, pos);
        stats[4] += EnchantingStatManager.getBonusClues(state, world, pos);
    }

    @Override
    public MenuType<?> getType() {
        return Apoth.Menus.ENCHANTING_TABLE.get();
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

    /**
     * Holder for the computed stat values of an enchantment table.
     */
    public static record TableStats(float eterna, float quanta, float arcana, float rectification, int clues) {

        public TableStats(float[] data) {
            this(data[0], data[1], data[2], data[3], (int) data[4]);
        }

    }
}
