package dev.shadowsoffire.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.advancements.EnchantedTrigger;
import dev.shadowsoffire.apotheosis.ench.Ench;
import dev.shadowsoffire.apotheosis.ench.api.IEnchantingBlock;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.placebo.network.PacketDistro;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import it.unimi.dsi.fastutil.floats.Float2FloatMap;
import it.unimi.dsi.fastutil.floats.Float2FloatOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
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

public class ApothEnchantmentMenu extends EnchantmentMenu {

    protected TableStats stats = TableStats.INVALID;
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
            float eterna = this.stats.eterna, quanta = this.stats.quanta, arcana = this.stats.arcana,
                rectification = this.stats.rectification;
            List<EnchantmentInstance> list = this.getEnchantmentList(toEnchant, slot, this.costs[slot]);
            if (!list.isEmpty()) {
                EnchantmentUtils.chargeExperience(player, ApothMiscUtil.getExpCostForSlot(level, slot));
                player.onEnchantmentPerformed(toEnchant, 0); // Pass zero here instead of the cost so no experience is taken, but the method is still called for tracking reasons.
                if (list.get(0).enchantment == Ench.Enchantments.INFUSION.get()) {
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
                    float eterna = this.stats.eterna();
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
                                this.enchantClue[slot] = BuiltInRegistries.ENCHANTMENT.getId(enchantmentdata.enchantment);
                                this.levelClue[slot] = enchantmentdata.level;
                                int clues = this.stats.clues();
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
                    this.stats = TableStats.INVALID;
                    PacketDistro.sendTo(Apotheosis.CHANNEL, new StatsMessage(this.stats), this.player);
                }
            }
            return this;
        });
    }

    private List<EnchantmentInstance> getEnchantmentList(ItemStack stack, int enchantSlot, int level) {
        this.random.setSeed(this.enchantmentSeed.get() + enchantSlot);
        List<EnchantmentInstance> list = RealEnchantmentHelper.selectEnchantment(this.random, stack, level, this.stats.quanta(), this.stats.arcana(), this.stats.rectification(), this.stats.treasure(), this.stats.blacklist());
        EnchantingRecipe match = this.access.evaluate((world, pos) -> Optional.ofNullable(EnchantingRecipe.findMatch(world, stack, this.stats.eterna(), this.stats.quanta(), this.stats.arcana()))).get().orElse(null);
        if (enchantSlot == 2 && match != null) {
            list.clear();
            list.add(new EnchantmentInstance(Ench.Enchantments.INFUSION.get(), 1));
        }
        return list;
    }

    public void gatherStats() {
        this.access.evaluate((world, pos) -> {
            this.stats = gatherStats(world, pos, this.getSlot(0).getItem().getEnchantmentValue());
            PacketDistro.sendTo(Apotheosis.CHANNEL, new StatsMessage(this.stats), this.player);
            return this;
        }).orElse(this);
    }

    /**
     * Gathers all enchanting stats for an enchantment table located at the specified position.
     *
     * @param level    The level.
     * @param pos      The position of the enchantment table.
     * @param itemEnch The enchantability of the item being enchanted.
     * @return The computed {@link TableStats}.
     */
    public static TableStats gatherStats(Level level, BlockPos pos, int itemEnch) {
        TableStats.Builder builder = new TableStats.Builder(itemEnch);
        for (BlockPos offset : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
            if (canReadStatsFrom(level, pos, offset)) {
                gatherStats(builder, level, pos.offset(offset));
            }
        }
        return builder.build();
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
        return level.getBlockState(tablePos.offset(offset.getX() / 2, offset.getY(), offset.getZ() / 2)).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);
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
    public static void gatherStats(TableStats.Builder builder, Level world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.isAir()) return;
        float eterna = EnchantingStatRegistry.getEterna(state, world, pos);
        float max = EnchantingStatRegistry.getMaxEterna(state, world, pos);
        builder.addEterna(eterna, max);
        builder.addQuanta(EnchantingStatRegistry.getQuanta(state, world, pos));
        builder.addArcana(EnchantingStatRegistry.getArcana(state, world, pos));
        builder.addRectification(EnchantingStatRegistry.getQuantaRectification(state, world, pos));
        builder.addClues(EnchantingStatRegistry.getBonusClues(state, world, pos));
        ((IEnchantingBlock) state.getBlock()).getBlacklistedEnchantments(state, world, pos).forEach(builder::blacklistEnchant);
        if (((IEnchantingBlock) state.getBlock()).allowsTreasure(state, world, pos)) {
            builder.setAllowsTreasure(true);
        }
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
    @SuppressWarnings("deprecation")
    public static record TableStats(float eterna, float quanta, float arcana, float rectification, int clues, Set<Enchantment> blacklist, boolean treasure) {

        public static TableStats INVALID = new TableStats(0, 0, 0, 0, 0, Collections.emptySet(), false);

        public TableStats(float eterna, float quanta, float arcana, float rectification, int clues, Set<Enchantment> blacklist, boolean treasure) {
            this.eterna = Mth.clamp(eterna, 0, EnchantingStatRegistry.getAbsoluteMaxEterna());
            this.quanta = Mth.clamp(quanta, 0, 100);
            this.arcana = Mth.clamp(arcana, 0, 100);
            this.rectification = Mth.clamp(rectification, 0, 100);
            this.clues = Math.max(clues, 0);
            this.blacklist = Collections.unmodifiableSet(blacklist);
            this.treasure = treasure;
        }

        public TableStats(float[] data, Set<Enchantment> blacklist, boolean treasure) {
            this(data[0], data[1], data[2], data[3], (int) data[4], blacklist, treasure);
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeFloat(this.eterna);
            buf.writeFloat(this.quanta);
            buf.writeFloat(this.arcana);
            buf.writeFloat(this.rectification);
            buf.writeByte(this.clues);
            buf.writeShort(this.blacklist.size());
            for (Enchantment e : this.blacklist) {
                buf.writeVarInt(BuiltInRegistries.ENCHANTMENT.getId(e));
            }
            buf.writeBoolean(this.treasure);
        }

        public static TableStats read(FriendlyByteBuf buf) {
            float[] data = { buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readByte() };
            int size = buf.readShort();
            Set<Enchantment> blacklist = new HashSet<>(size);
            for (int i = 0; i < size; i++) {
                blacklist.add(BuiltInRegistries.ENCHANTMENT.byId(buf.readVarInt()));
            }
            boolean treasure = buf.readBoolean();
            return new TableStats(data, blacklist, treasure);
        }

        public static class Builder {

            private final Float2FloatMap eternaMap = new Float2FloatOpenHashMap();

            private final Set<Enchantment> blacklist = new HashSet<>();

            private boolean allowsTreasure = false;

            private final float[] stats = new float[5];

            public Builder(int itemEnch) {
                this.addQuanta(15F);
                this.addArcana(itemEnch / 2F);
                this.addClues(1);
            }

            public void addEterna(float eterna, float max) {
                this.eternaMap.put(max, this.eternaMap.getOrDefault(max, 0) + eterna);
            }

            public void addQuanta(float quanta) {
                this.stats[1] += quanta;
            }

            public void addArcana(float arcana) {
                this.stats[2] += arcana;
            }

            public void addRectification(float rectification) {
                this.stats[3] += rectification;
            }

            public void addClues(int clues) {
                this.stats[4] += clues;
            }

            public void blacklistEnchant(Enchantment ench) {
                this.blacklist.add(ench);
            }

            public void setAllowsTreasure(boolean allowsTreasure) {
                this.allowsTreasure = allowsTreasure;
            }

            public TableStats build() {
                List<Float2FloatMap.Entry> entries = new ArrayList<>(this.eternaMap.float2FloatEntrySet());
                Collections.sort(entries, Comparator.comparing(Float2FloatMap.Entry::getFloatKey));

                for (Float2FloatMap.Entry e : entries) {
                    if (e.getFloatKey() > 0) this.stats[0] = Math.min(e.getFloatKey(), this.stats[0] + e.getFloatValue());
                    else this.stats[0] += e.getFloatValue();
                }

                return new TableStats(this.stats, this.blacklist, this.allowsTreasure);
            }
        }

    }
}
