package dev.shadowsoffire.apotheosis.compat.twilight;

// TODO(26.1): Restore Twilight Forest compat once a 26.1 build of twilightforest is published.
/*
import java.util.function.Supplier;

import com.google.common.base.Predicates;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import twilightforest.TwilightForestMod;
import twilightforest.entity.monster.Redcap;

public class AdventureTwilightCompat {

    protected static final Holder<Item> ORE_MAGNET = DeferredHolder.create(Registries.ITEM, TwilightForestMod.prefix("ore_magnet"));
    protected static final Supplier<EntityType<Redcap>> REDCAP = DeferredHolder.create(Registries.ENTITY_TYPE, TwilightForestMod.prefix("redcap"));

    public static void register() {
        GemBonus.CODEC.register(Apotheosis.loc("twilight_ore_magnet"), OreMagnetBonus.CODEC);
        GemBonus.CODEC.register(Apotheosis.loc("twilight_treasure_goblin"), TreasureGoblinBonus.CODEC);
        GemBonus.CODEC.register(Apotheosis.loc("twilight_fortification"), FortificationBonus.CODEC);
        NeoForge.EVENT_BUS.addListener(AdventureTwilightCompat::doGoblins);
    }

    @SubscribeEvent
    public static void doGoblins(EntityJoinLevelEvent e) {
        if (e.getEntity() instanceof Redcap r && r.getPersistentData().contains("apoth.treasure_goblin")) {
            r.targetSelector.removeAllGoals(Predicates.alwaysTrue());
            r.goalSelector.removeAllGoals(Predicates.alwaysTrue());
            r.goalSelector.addGoal(10, new AvoidEntityGoal<>(r, Player.class, 6, 1, 1.25));
        }
    }

}
*/
