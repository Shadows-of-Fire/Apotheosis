package dev.shadowsoffire.apotheosis.gen;

import java.util.Set;

import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureEntityInfo;

public class ItemFrameGemsProcessor extends StructureProcessor {

    public static final MapCodec<ItemFrameGemsProcessor> CODEC = PlaceboCodecs.setOf(Purity.CODEC).optionalFieldOf("purities", Set.of())
        .xmap(ItemFrameGemsProcessor::new, i -> i.purities);

    protected final Set<Purity> purities;

    public ItemFrameGemsProcessor(Set<Purity> purities) {
        this.purities = purities;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return Apoth.Features.ITEM_FRAME_GEMS;
    }

    @Override
    public StructureEntityInfo processEntity(LevelReader world, BlockPos seedPos, StructureEntityInfo rawEntityInfo, StructureEntityInfo entityInfo, StructurePlaceSettings placementSettings, StructureTemplate template) {
        CompoundTag entityNBT = entityInfo.nbt;

        String id = entityNBT.getString("id"); // entity type ID
        if (world instanceof ServerLevelAccessor sla && "minecraft:item_frame".equals(id)) {
            this.writeEntityNBT(sla.getLevel(), entityInfo.blockPos, placementSettings.getRandom(entityInfo.blockPos), entityNBT, placementSettings);
        }

        return entityInfo;
    }

    protected void writeEntityNBT(ServerLevel level, BlockPos pos, RandomSource rand, CompoundTag nbt, StructurePlaceSettings settings) {
        Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), -1, false);
        GenContext ctx = player != null ? GenContext.forPlayerAtPos(rand, player, pos) : GenContext.standalone(rand, WorldTier.HAVEN, 0, level, pos);
        Gem gem = GemRegistry.INSTANCE.getRandomItem(ctx);
        if (gem != null) {
            Purity purity = Purity.random(ctx, this.purities);
            ItemStack stack = gem.toStack(purity);
            nbt.put("Item", stack.save(level.registryAccess()));
        }
        nbt.putInt("TileX", pos.getX());
        nbt.putInt("TileY", pos.getY());
        nbt.putInt("TileZ", pos.getZ());
    }
}
