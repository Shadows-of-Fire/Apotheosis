package dev.shadowsoffire.apotheosis.adventure.compat;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.placebo.color.GradientColor;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.capabilities.CapabilityList;
import twilightforest.entity.monster.Redcap;

public class AdventureTwilightCompat {

    protected static final RegistryObject<Item> ORE_MAGNET = RegistryObject.create(new ResourceLocation("twilightforest", "ore_magnet"), Registries.ITEM, Apotheosis.MODID);
    protected static final RegistryObject<EntityType<Redcap>> REDCAP = RegistryObject.create(new ResourceLocation("twilightforest", "redcap"), Registries.ENTITY_TYPE, Apotheosis.MODID);

    public static void register() {
        GemBonus.CODEC.register(Apotheosis.loc("twilight_ore_magnet"), OreMagnetBonus.CODEC);
        GemBonus.CODEC.register(Apotheosis.loc("twilight_treasure_goblin"), TreasureGoblinBonus.CODEC);
        GemBonus.CODEC.register(Apotheosis.loc("twilight_fortification"), FortificationBonus.CODEC);
        MinecraftForge.EVENT_BUS.addListener(AdventureTwilightCompat::doGoblins);
    }

    public static class OreMagnetBonus extends GemBonus {

        public static final Codec<OreMagnetBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                gemClass(),
                GemBonus.VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
            .apply(inst, OreMagnetBonus::new));

        protected final Map<LootRarity, StepFunction> values;

        public OreMagnetBonus(GemClass gemClass, Map<LootRarity, StepFunction> values) {
            super(Apotheosis.loc("twilight_ore_magnet"), gemClass);
            this.values = values;
        }

        @Override
        public InteractionResult onItemUse(ItemStack gem, LootRarity rarity, UseOnContext ctx) {
            BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
            if (state.isAir()) return null;
            Level level = ctx.getLevel();
            Player player = ctx.getPlayer();
            player.startUsingItem(ctx.getHand());
            // The ore magnet only checks that the use duration (72000 - param) is > 10
            // https://github.com/TeamTwilight/twilightforest/blob/1.20.x/src/main/java/twilightforest/item/OreMagnetItem.java#L77
            ORE_MAGNET.get().releaseUsing(gem, level, player, 0);
            player.stopUsingItem();
            int cost = this.values.get(rarity).getInt(0);
            ctx.getItemInHand().hurtAndBreak(cost, player, user -> user.broadcastBreakEvent(ctx.getHand()));
            return super.onItemUse(gem, rarity, ctx);
        }

        @Override
        public Codec<? extends GemBonus> getCodec() {
            return CODEC;
        }

        @Override
        public GemBonus validate() {
            Preconditions.checkArgument(!this.values.isEmpty(), "No values provided!");
            return this;
        }

        @Override
        public boolean supports(LootRarity rarity) {
            return this.values.containsKey(rarity);
        }

        @Override
        public int getNumberOfUUIDs() {
            return 0;
        }

        @Override
        public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
            return Component.translatable("bonus." + this.getId() + ".desc", this.values.get(rarity).getInt(0)).withStyle(ChatFormatting.YELLOW);
        }

    }

    public static class TreasureGoblinBonus extends GemBonus {

        public static final Codec<TreasureGoblinBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                gemClass(),
                LootRarity.mapCodec(Data.CODEC).fieldOf("values").forGetter(a -> a.values))
            .apply(inst, TreasureGoblinBonus::new));

        protected final Map<LootRarity, Data> values;

        public TreasureGoblinBonus(GemClass gemClass, Map<LootRarity, Data> values) {
            super(Apotheosis.loc("twilight_treasure_goblin"), gemClass);
            this.values = values;
        }

        @Override
        public void doPostAttack(ItemStack gem, LootRarity rarity, LivingEntity user, Entity target) {
            Data d = this.values.get(rarity);
            if (Affix.isOnCooldown(this.getCooldownId(gem), d.cooldown, user)) return;
            if (user.random.nextFloat() <= d.chance) {
                Redcap goblin = REDCAP.get().create(user.level());
                CompoundTag tag = new CompoundTag();
                tag.putString("DeathLootTable", "apotheosis:entity/treasure_goblin");
                goblin.readAdditionalSaveData(tag);
                goblin.getPersistentData().putBoolean("apoth.treasure_goblin", true);
                goblin.setCustomName(Component.translatable("name.apotheosis.treasure_goblin").withStyle(s -> s.withColor(GradientColor.RAINBOW)));
                goblin.setCustomNameVisible(true);
                goblin.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(new AttributeModifier("apoth.very_fast", 0.2, Operation.ADDITION));
                goblin.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("apoth.healmth", 60, Operation.ADDITION));
                goblin.setHealth(goblin.getMaxHealth());
                for (int i = 0; i < 8; i++) {
                    int x = Mth.nextInt(goblin.random, -5, 5);
                    int y = Mth.nextInt(goblin.random, -1, 1);
                    int z = Mth.nextInt(goblin.random, -5, 5);
                    goblin.setPos(target.position().add(x, y, z));
                    if (user.level().noCollision(goblin)) break;
                    if (i == 7) goblin.setPos(target.position());
                }
                goblin.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                user.level().addFreshEntity(goblin);
                Affix.startCooldown(this.getCooldownId(gem), user);
            }
        }

        @Override
        public Codec<? extends GemBonus> getCodec() {
            return CODEC;
        }

        @Override
        public GemBonus validate() {
            Preconditions.checkArgument(!this.values.isEmpty(), "No values provided!");
            return this;
        }

        @Override
        public boolean supports(LootRarity rarity) {
            return this.values.containsKey(rarity);
        }

        @Override
        public int getNumberOfUUIDs() {
            return 0;
        }

        @Override
        public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
            Data d = this.values.get(rarity);
            Component cooldown = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(d.cooldown));
            return Component.translatable("bonus." + this.getId() + ".desc", Affix.fmt(d.chance * 100), cooldown).withStyle(ChatFormatting.YELLOW);
        }

        protected static record Data(float chance, int cooldown) {

            public static final Codec<Data> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                    Codec.FLOAT.fieldOf("chance").forGetter(Data::chance),
                    Codec.INT.fieldOf("cooldown").forGetter(Data::cooldown))
                .apply(inst, Data::new));

        }

    }

    @SubscribeEvent
    public static void doGoblins(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof Redcap r && r.getPersistentData().contains("apoth.treasure_goblin")) {
            r.targetSelector.removeAllGoals(Predicates.alwaysTrue());
            r.goalSelector.removeAllGoals(Predicates.alwaysTrue());
            r.goalSelector.addGoal(10, new AvoidEntityGoal<>(r, Player.class, 6, 1, 1.25));
        }
    }

    public static class FortificationBonus extends GemBonus {

        public static final Codec<FortificationBonus> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                gemClass(),
                LootRarity.mapCodec(Data.CODEC).fieldOf("values").forGetter(a -> a.values))
            .apply(inst, FortificationBonus::new));

        protected final Map<LootRarity, Data> values;

        public FortificationBonus(GemClass gemClass, Map<LootRarity, Data> values) {
            super(Apotheosis.loc("twilight_fortification"), gemClass);
            this.values = values;
        }

        @Override
        public void doPostHurt(ItemStack gem, LootRarity rarity, LivingEntity user, Entity attacker) {
            Data d = this.values.get(rarity);
            if (Affix.isOnCooldown(this.getCooldownId(gem), d.cooldown, user)) return;
            if (user.random.nextFloat() <= d.chance) {
                user.getCapability(CapabilityList.SHIELDS).ifPresent(cap -> {
                    cap.replenishShields();
                });
                Affix.startCooldown(this.getCooldownId(gem), user);
            }
        }

        @Override
        public Codec<? extends GemBonus> getCodec() {
            return CODEC;
        }

        @Override
        public GemBonus validate() {
            Preconditions.checkArgument(!this.values.isEmpty(), "No values provided!");
            return this;
        }

        @Override
        public boolean supports(LootRarity rarity) {
            return this.values.containsKey(rarity);
        }

        @Override
        public int getNumberOfUUIDs() {
            return 0;
        }

        @Override
        public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
            Data d = this.values.get(rarity);
            Component cooldown = Component.translatable("affix.apotheosis.cooldown", StringUtil.formatTickDuration(d.cooldown));
            return Component.translatable("bonus." + this.getId() + ".desc", Affix.fmt(d.chance * 100), cooldown).withStyle(ChatFormatting.YELLOW);
        }

        protected static record Data(float chance, int cooldown) {

            public static final Codec<Data> CODEC = RecordCodecBuilder.create(inst -> inst
                .group(
                    Codec.FLOAT.fieldOf("chance").forGetter(Data::chance),
                    Codec.INT.fieldOf("cooldown").forGetter(Data::cooldown))
                .apply(inst, Data::new));

        }

    }

}
