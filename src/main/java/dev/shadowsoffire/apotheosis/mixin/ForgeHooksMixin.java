package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import dev.shadowsoffire.apotheosis.Apoth.LootTables;
import dev.shadowsoffire.placebo.Placebo;
import dev.shadowsoffire.placebo.reload.PlaceboJsonReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;

@Mixin(ForgeHooks.class)
public class ForgeHooksMixin {

    @Inject(at = @At("HEAD"), method = "loadLootTable(Lcom/google/gson/Gson;Lnet/minecraft/resources/ResourceLocation;Lcom/google/gson/JsonElement;ZLnet/minecraft/world/level/storage/loot/LootTables;)Lnet/minecraft/world/level/storage/loot/LootTable;", cancellable = true, require = 1, remap = false)
    private static void apoth_lootTableConditions(Gson gson, ResourceLocation name, JsonElement data, boolean custom, LootTables mgr, CallbackInfoReturnable<LootTable> cir) {
        if (!PlaceboJsonReloadListener.checkConditions(data, name, "loot_table", Placebo.LOGGER, IContext.EMPTY)) {
            cir.setReturnValue(LootTable.EMPTY);
        }
    }

}
