package dev.shadowsoffire.apotheosis.affix.reforging;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Items;
import dev.shadowsoffire.apotheosis.Apoth.RecipeTypes;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.cap.InternalItemHandler;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class ReforgingTableTile extends BlockEntity implements TickingBlockEntity {

    public int time = 0;
    public boolean step1 = true;

    protected InternalItemHandler inv = new InternalItemHandler(2){
        @Override
        public boolean isValid(int index, ItemResource resource) {
            if (index == 0) {
                return ReforgingTableTile.this.isValidRarityMat(resource.toStack());
            }
            return resource.is(Items.SIGIL_OF_REBIRTH);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            ReforgingTableTile.this.setChanged();
        }
    };

    public ReforgingTableTile(BlockPos pWorldPosition, BlockState pBlockState) {
        super(Apoth.Tiles.REFORGING_TABLE, pWorldPosition, pBlockState);
    }

    public boolean isValidRarityMat(ItemStack stack) {
        DynamicHolder<LootRarity> rarity = RarityRegistry.getMaterialRarity(stack.getItem());
        return rarity.isBound() && this.getRecipeFor(rarity.get()) != null;
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public ReforgingRecipe getRecipeFor(LootRarity rarity) {
        if (this.level == null) {
            return null;
        }
        Collection<RecipeHolder<ReforgingRecipe>> recipes;
        if (this.level.isClientSide()) {
            recipes = ReforgingRecipeCache.all();
        }
        else {
            recipes = this.level.getServer().getRecipeManager().recipeMap().byType(RecipeTypes.REFORGING);
        }
        return recipes.stream()
            .map(RecipeHolder::value)
            .filter(r -> r.rarity().get() == rarity && r.tables().contains(this.getBlockState().getBlock().builtInRegistryHolder()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public void clientTick(Level pLevel, BlockPos pPos, BlockState pState) {
        Player player = pLevel.getNearestPlayer(pPos.getX() + 0.5D, pPos.getY() + 0.5D, pPos.getZ() + 0.5D, 4, false);

        if (player != null) {
            this.time++;
        }
        else {
            if (this.time == 0 && this.step1) {
                return;
            }
            else {
                this.time++;
            }
        }

        if (this.step1 && this.time == 59) {
            this.step1 = false;
            this.time = 0;
        }
        else if (this.time == 4 && !this.step1) {
            RandomSource rand = pLevel.getRandom();
            for (int i = 0; i < 6; i++) {
                pLevel.addParticle(ParticleTypes.CRIT, pPos.getX() + 0.5 - 0.1 * rand.nextDouble(), pPos.getY() + 13 / 16D, pPos.getZ() + 0.5 + 0.1 * rand.nextDouble(), 0, 0, 0);
            }
            pLevel.playLocalSound(pPos.getX(), pPos.getY(), pPos.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.03F, 1.7F + rand.nextFloat() * 0.2F, true);
            this.step1 = true;
            this.time = 0;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putChild("inventory", this.inv);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.readChild("inventory", this.inv);
    }

    public ResourceHandler<ItemResource> getInventory() {
        return this.inv;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level == null) {
            return;
        }
        for (int i = 0; i < this.inv.size(); i++) {
            ItemResource res = this.inv.getResource(i);
            int amount = this.inv.getAmountAsInt(i);
            if (!res.isEmpty() && amount > 0) {
                Block.popResource(this.level, pos, res.toStack(amount));
            }
        }
    }

}
