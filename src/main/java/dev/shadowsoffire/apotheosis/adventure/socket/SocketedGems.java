package dev.shadowsoffire.apotheosis.adventure.socket;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemInstance;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;

/**
 * Live instance of socketed gems on an item. The size of the list is equal to the number of sockets on the object.
 * Additionally, this list may contain empty or invalid gem instances.
 */
public record SocketedGems(ImmutableList<GemInstance> gems) implements List<GemInstance> {

    public static final SocketedGems EMPTY = new SocketedGems(ImmutableList.of());

    public SocketedGems(List<GemInstance> gems) {
        this(ImmutableList.copyOf(gems));
    }

    public void addModifiers(LootCategory cat, EquipmentSlot type, BiConsumer<Attribute, AttributeModifier> map) {
        this.streamValidGems().forEach(inst -> inst.addModifiers(type, map));
    }

    public int getDamageProtection(DamageSource source) {
        return this.streamValidGems().map(inst -> inst.getDamageProtection(source)).reduce(0, Integer::sum);
    }

    public float getDamageBonus(MobType creatureType) {
        return this.streamValidGems().map(inst -> inst.getDamageBonus(creatureType)).reduce(Float::sum).orElse(0F);
    }

    public void doPostAttack(LivingEntity user, Entity target) {
        this.streamValidGems().forEach(inst -> {
            int old = target.invulnerableTime;
            target.invulnerableTime = 0;
            inst.doPostAttack(user, target);
            target.invulnerableTime = old;
        });
    }

    public void doPostHurt(LivingEntity user, Entity attacker) {
        this.streamValidGems().forEach(inst -> inst.doPostHurt(user, attacker));
    }

    public void onArrowFired(LivingEntity user, AbstractArrow arrow) {
        this.streamValidGems().forEach(inst -> inst.onArrowFired(user, arrow));
    }

    @Nullable
    public InteractionResult onItemUse(UseOnContext useinst) {
        return this.streamValidGems().map(inst -> inst.onItemUse(useinst)).filter(Predicates.notNull()).findFirst().orElse(null);
    }

    public float onShieldBlock(LivingEntity entity, DamageSource source, float amount) {
        for (GemInstance inst : this.gems) {
            if (inst.isValid()) {
                amount = inst.onShieldBlock(entity, source, amount);
            }
        }
        return amount;
    }

    public void onBlockBreak(Player player, LevelAccessor world, BlockPos pos, BlockState state) {
        this.streamValidGems().forEach(inst -> inst.onBlockBreak(player, world, pos, state));
    }

    public DoubleStream getDurabilityBonusPercentage(ServerPlayer user) {
        return this.streamValidGems().mapToDouble(inst -> inst.getDurabilityBonusPercentage(user));
    }

    public float onHurt(DamageSource src, LivingEntity ent, float amount) {
        for (GemInstance inst : this.gems) {
            if (inst.isValid()) {
                amount = inst.onHurt(src, ent, amount);
            }
        }
        return amount;
    }

    public void getEnchantmentLevels(Map<Enchantment, Integer> enchantments) {
        this.streamValidGems().forEach(inst -> inst.getEnchantmentLevels(enchantments));
    }

    public void modifyLoot(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        this.streamValidGems().forEach(inst -> inst.modifyLoot(loot, ctx));
    }

    /**
     * Returns a stream of all socketed gem instances that are {@link GemInstance#isValid()}.
     */
    public Stream<GemInstance> streamValidGems() {
        return this.gems.stream().filter(GemInstance::isValid);
    }

    // List interface methods below this line

    @Override
    public int size() {
        return this.gems.size();
    }

    @Override
    public boolean isEmpty() {
        return this.gems.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.gems.contains(o);
    }

    @Override
    public Iterator<GemInstance> iterator() {
        return this.gems.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.gems.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.gems.toArray(a);
    }

    @Override
    @Deprecated
    public boolean add(GemInstance e) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.gems.containsAll(c);
    }

    @Override
    @Deprecated
    public boolean addAll(Collection<? extends GemInstance> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean addAll(int index, Collection<? extends GemInstance> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    @Deprecated
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GemInstance get(int index) {
        return this.gems.get(index);
    }

    @Override
    @Deprecated
    public GemInstance set(int index, GemInstance element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void add(int index, GemInstance element) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public GemInstance remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return this.gems.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.gems.lastIndexOf(o);
    }

    @Override
    public ListIterator<GemInstance> listIterator() {
        return this.gems.listIterator();
    }

    @Override
    public ListIterator<GemInstance> listIterator(int index) {
        return this.gems.listIterator(index);
    }

    @Override
    public List<GemInstance> subList(int fromIndex, int toIndex) {
        return this.gems.subList(fromIndex, toIndex);
    }

}
