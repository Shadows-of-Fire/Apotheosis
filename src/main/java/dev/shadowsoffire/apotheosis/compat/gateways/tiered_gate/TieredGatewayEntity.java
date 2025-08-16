package dev.shadowsoffire.apotheosis.compat.gateways.tiered_gate;

import com.google.common.base.Preconditions;

import dev.shadowsoffire.apotheosis.compat.gateways.GatewaysCompat;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.Wave;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TieredGatewayEntity extends GatewayEntity {

    public TieredGatewayEntity(Level level, Player placer, DynamicHolder<Gateway> gate) {
        super(GatewaysCompat.TIERED_GATEWAY.get(), level, placer, gate);
        this.summonerId = placer.getUUID();
        this.gate = gate;
        Preconditions.checkArgument(gate.isBound(), "A gateway may not be constructed for an unbound holder.");
        this.setCustomName(Component.translatable(gate.getId().toString().replace(':', '.')).withStyle(Style.EMPTY.withColor(gate.get().color())));
        this.bossEvent = this.createBossEvent();
        this.refreshDimensions();
    }

    public TieredGatewayEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public boolean isLastWave() {
        return this.getWave() >= this.getGateway().getNumWaves();
    }

    @Override
    public Wave getCurrentWave() {
        return this.getGateway().getWave(Math.min(this.getGateway().getNumWaves() - 1, this.getWave()));
    }

    @Override
    protected boolean canStartNextWave() {
        return super.canStartNextWave() && !this.isLastWave();
    }

    @Override
    public boolean isCompleted() {
        return this.undroppedItems.isEmpty() && this.isLastWave();
    }

    @Override
    protected void completeWave() {
        Player player = this.summonerOrClosest();
        this.undroppedItems.addAll(this.getCurrentWave().spawnRewards((ServerLevel) this.level(), this, player));
    }

    @Override
    protected void completeGateway() {
        super.completeGateway();
        Player player = this.summonerOrClosest();
        this.getGateway().rewards().forEach(r -> {
            r.generateLoot((ServerLevel) this.level(), this, player, this::spawnCompletionItem);
        });
    }

    @Override
    public TieredGateway getGateway() {
        return (TieredGateway) super.getGateway();
    }

}
