package dev.shadowsoffire.apotheosis.adventure.affix.socket;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import dev.shadowsoffire.apotheosis.Apoth.Affixes;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.adventure.event.GetItemSocketsEvent;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

/**
 * Utility class for the manipulation of Sockets on items.
 * <p>
 * Sockets may only be applied to items which are of a valid loot category.
 */
public class SocketHelper {

    public static final String AFFIX_DATA = AffixHelper.AFFIX_DATA;
    public static final String GEMS = "gems";
    public static final String SOCKETS = "sockets";

    /**
     * Gets the list of gems socketed into the item.<br>
     * Does not validate that the gems are valid in the item.
     *
     * @param stack The stack being queried
     * @return A list of all gems socketed in this item.
     */
    public static List<ItemStack> getGems(ItemStack stack) {
        return getGems(stack, getSockets(stack));
    }

    /**
     * Implementation for {@link #getGems(ItemStack)}
     */
    private static List<ItemStack> getGems(ItemStack stack, int size) {
        List<ItemStack> gems = NonNullList.withSize(size, ItemStack.EMPTY);
        if (size == 0 || stack.isEmpty()) return gems;
        int i = 0;
        CompoundTag afxData = stack.getTagElement(AffixHelper.AFFIX_DATA);
        if (afxData != null && afxData.contains(GEMS)) {
            ListTag gemData = afxData.getList(GEMS, Tag.TAG_COMPOUND);
            for (Tag tag : gemData) {
                gems.set(i++, ItemStack.of((CompoundTag) tag));
                if (i >= size) break;
            }
        }
        return gems;
    }

    /**
     * Gets a stream of socketed gems that are valid for use in the item.
     *
     * @param stack The stack being queried.
     * @return A stream containing all valid gems in the item.
     * @see GemInstance#isValid()
     */
    public static Stream<GemInstance> getGemInstances(ItemStack stack) {
        return getGems(stack).stream().map(gemStack -> new GemInstance(stack, gemStack)).filter(GemInstance::isValid);
    }

    /**
     * Gets the list of Gem objects that are active in
     *
     * @param stack
     * @return
     */
    // public static List<Gem> getActiveGems(ItemStack stack) {
    // return getGems(stack).stream().map(GemItem::getGem).filter(Objects::nonNull).toList();
    // }

    /**
     * Sets the gem list on the item to the provided list of gems.<br>
     * Setting more gems than there are sockets will cause the extra gems to be lost.
     *
     * @param stack The stack being modified.
     * @param gems  The list of socketed gems.
     */
    public static void setGems(ItemStack stack, List<ItemStack> gems) {
        CompoundTag afxData = stack.getOrCreateTagElement(AffixHelper.AFFIX_DATA);
        ListTag gemData = new ListTag();
        for (ItemStack s : gems) {
            gemData.add(s.save(new CompoundTag()));
        }
        afxData.put(GEMS, gemData);
    }

    /**
     * Gets the number of sockets on an item.<br>
     * By default, this equals the nbt-encoded socket count, but it may be modified by {@link GetItemSocketsEvent}.
     *
     * @param stack The stack being queried.
     * @return The number of sockets on the stack.
     */
    public static int getSockets(ItemStack stack) {
        int sockets = stack.getOrCreateTagElement(AFFIX_DATA).getInt(SOCKETS);
        var event = new GetItemSocketsEvent(stack, sockets);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getSockets();
    }

    /**
     * Sets the number of sockets on the item to the specified value.
     * <p>
     * The value set here is not necessarily the value that will be returned by {@link #getSockets(ItemStack)} due to {@link GetItemSocketsEvent}.
     *
     * @param stack   The stack being modified.
     * @param sockets The number of sockets.
     */
    public static void setSockets(ItemStack stack, int sockets) {
        stack.getOrCreateTagElement(AFFIX_DATA).putInt(SOCKETS, sockets);
    }

    /**
     * Checks if any of the sockets on the item are empty.
     *
     * @param stack The stack being queried.
     * @return True, if any sockets are empty, otherwise false.
     */
    public static boolean hasEmptySockets(ItemStack stack) {
        return getGems(stack).stream().map(GemItem::getGem).map(DynamicHolder::getOptional).anyMatch(Optional::isEmpty);
    }

    /**
     * Computes the index of the first empty socket, used during socketing.
     *
     * @param stack The stack being queried.
     * @return The index of the first empty socket in the stack's gem list.
     * @see #getGems(ItemStack)
     */
    public static int getFirstEmptySocket(ItemStack stack) {
        List<ItemStack> gems = getGems(stack);
        for (int socket = 0; socket < gems.size(); socket++) {
            DynamicHolder<Gem> gem = GemItem.getGem(gems.get(socket));
            if (!gem.isBound()) return socket;
        }
        return 0;
    }

    public static void loadSocketAffix(ItemStack stack, Map<DynamicHolder<? extends Affix>, AffixInstance> affixes) {
        int sockets = getSockets(stack);
        if (sockets > 0) {
            // The rarity is irrelevant for the socket affix, so we always pass the min rarity to the fake affix instance.
            affixes.put(Affixes.SOCKET, new AffixInstance(Affixes.SOCKET, stack, RarityRegistry.getMinRarity(), sockets));
        }
    }

}
