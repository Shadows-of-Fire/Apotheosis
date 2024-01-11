package dev.shadowsoffire.apotheosis.spawn.compat;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.spawn.modifiers.SpawnerStats;
import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerBlock;
import dev.shadowsoffire.apotheosis.spawn.spawner.ApothSpawnerTile;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class SpawnerHwylaPlugin implements IWailaPlugin, IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    private static final ApothSpawnerTile tooltipTile = new ApothSpawnerTile(BlockPos.ZERO, Blocks.AIR.defaultBlockState());

    @Override
    public void register(IWailaCommonRegistration reg) {
        if (Apotheosis.enableSpawner) reg.registerBlockDataProvider(this, ApothSpawnerTile.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration reg) {
        if (Apotheosis.enableSpawner) reg.registerBlockComponent(this, ApothSpawnerBlock.class);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (Screen.hasControlDown()) {
            tooltipTile.load(accessor.getServerData());
            SpawnerStats.generateTooltip(tooltipTile, tooltip::add);
        }
        else tooltip.add(Component.translatable("misc.apotheosis.ctrl_stats"));
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor access) {
        if (access.getBlockEntity() instanceof ApothSpawnerTile spw) {
            spw.saveAdditional(tag);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return Apotheosis.loc("spawner");
    }

}
