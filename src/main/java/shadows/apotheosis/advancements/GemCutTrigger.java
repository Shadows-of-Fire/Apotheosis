package shadows.apotheosis.advancements;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apotheosis;

public class GemCutTrigger implements CriterionTrigger<GemCutTrigger.Instance> {
    private static final ResourceLocation ID = new ResourceLocation(Apotheosis.MODID, "gem_cutting");
    private final Map<PlayerAdvancements, GemCutTrigger.Listeners> listeners = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements playerAdvancementsIn, CriterionTrigger.Listener<GemCutTrigger.Instance> listener) {
        GemCutTrigger.Listeners ModifierTrigger$listeners = this.listeners.get(playerAdvancementsIn);
        if (ModifierTrigger$listeners == null) {
            ModifierTrigger$listeners = new GemCutTrigger.Listeners(playerAdvancementsIn);
            this.listeners.put(playerAdvancementsIn, ModifierTrigger$listeners);
        }

        ModifierTrigger$listeners.add(listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements playerAdvancementsIn, CriterionTrigger.Listener<GemCutTrigger.Instance> listener) {
        GemCutTrigger.Listeners ModifierTrigger$listeners = this.listeners.get(playerAdvancementsIn);
        if (ModifierTrigger$listeners != null) {
            ModifierTrigger$listeners.remove(listener);
            if (ModifierTrigger$listeners.isEmpty()) {
                this.listeners.remove(playerAdvancementsIn);
            }
        }

    }

    @Override
    public void removePlayerListeners(PlayerAdvancements playerAdvancementsIn) {
        this.listeners.remove(playerAdvancementsIn);
    }

    @Override
    public GemCutTrigger.Instance createInstance(JsonObject json, DeserializationContext conditionsParser) {
        json = json.getAsJsonObject("conditions");
        ItemPredicate item = ItemPredicate.fromJson(json.get("item"));
        String rarity = GsonHelper.getAsString(json, "rarity", "");
        return new GemCutTrigger.Instance(item, rarity);
    }

    public void trigger(ServerPlayer player, ItemStack stack, String rarity) {
        GemCutTrigger.Listeners ModifierTrigger$listeners = this.listeners.get(player.getAdvancements());
        if (ModifierTrigger$listeners != null) {
            ModifierTrigger$listeners.trigger(stack, rarity);
        }

    }

    public static class Instance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate gem;
        private final String rarity;

        public Instance(ItemPredicate gem, String rarity) {
            super(GemCutTrigger.ID, EntityPredicate.Composite.ANY);
            this.gem = gem;
            this.rarity = rarity;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            return new JsonObject();
        }

        public boolean test(ItemStack stack, String rarity) {
            return this.gem.matches(stack) && (this.rarity.isEmpty() || this.rarity.equals(rarity));
        }
    }

    static class Listeners {
        private final PlayerAdvancements playerAdvancements;
        private final Set<CriterionTrigger.Listener<GemCutTrigger.Instance>> listeners = Sets.newHashSet();

        public Listeners(PlayerAdvancements playerAdvancementsIn) {
            this.playerAdvancements = playerAdvancementsIn;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void add(CriterionTrigger.Listener<GemCutTrigger.Instance> listener) {
            this.listeners.add(listener);
        }

        public void remove(CriterionTrigger.Listener<GemCutTrigger.Instance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(ItemStack stack, String rarity) {
            List<CriterionTrigger.Listener<GemCutTrigger.Instance>> list = null;

            for (CriterionTrigger.Listener<GemCutTrigger.Instance> listener : this.listeners) {
                if (listener.getTriggerInstance().test(stack, rarity)) {
                    if (list == null) {
                        list = Lists.newArrayList();
                    }

                    list.add(listener);
                }
            }

            if (list != null) {
                for (CriterionTrigger.Listener<GemCutTrigger.Instance> listener1 : list) {
                    listener1.run(this.playerAdvancements);
                }
            }

        }
    }
}
