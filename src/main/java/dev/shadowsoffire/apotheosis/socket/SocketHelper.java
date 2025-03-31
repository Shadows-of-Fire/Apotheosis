package dev.shadowsoffire.apotheosis.socket;

import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.event.GetItemSocketsEvent;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Utility class for the manipulation of Sockets on items.
 * <p>
 * Sockets may only be applied to items which are of a valid loot category.
 */
public class SocketHelper {

    public static final ResourceLocation GEMS_CACHED_OBJECT = Apotheosis.loc("gems");

    private static final ToIntFunction<ItemStack> SOCKET_DEPENDENT_COMPONENTS_HASHER = CachedObject.hashComponents(Components.GEM, Components.PURITY, Components.SOCKETED_GEMS);

    /**
     * Gets the number of sockets on an item.
     * By default, this equals the nbt-encoded socket count, but it may be modified by {@link GetItemSocketsEvent}.
     *
     * @param stack The stack being queried.
     * @return The number of sockets on the stack.
     */
    public static int getSockets(ItemStack stack) {
        int sockets = stack.getOrDefault(Components.SOCKETS, 0);
        var event = new GetItemSocketsEvent(stack, sockets);
        NeoForge.EVENT_BUS.post(event);
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
        stack.set(Components.SOCKETS, Mth.clamp(sockets, 0, 16));
    }

    /**
     * Gets the list of gems socketed into the item. Gems in the list may be unbound, invalid, or empty.
     *
     * @param stack The stack being queried
     * @return An immutable list of all gems socketed in this item. This list is cached.
     */
    public static SocketedGems getGems(ItemStack stack) {
        return CachedObjectSource.getOrCreate(stack, GEMS_CACHED_OBJECT, SocketHelper::getGemsImpl, SocketHelper::hashSockets);
    }

    /**
     * Computes the invalidation hash for the SocketedGems cache. The hash changes if the number of sockets changes, or the affix data changes.
     */
    private static int hashSockets(ItemStack stack) {
        return Objects.hash(SOCKET_DEPENDENT_COMPONENTS_HASHER.applyAsInt(stack), getSockets(stack));
    }

    /**
     * Implementation for {@link #getGems(ItemStack)}
     */
    private static SocketedGems getGemsImpl(ItemStack stack) {
        int size = getSockets(stack);
        if (size <= 0 || stack.isEmpty()) {
            return SocketedGems.EMPTY;
        }

        LootCategory cat = LootCategory.forItem(stack);
        if (cat.isNone()) {
            return SocketedGems.EMPTY;
        }

        NonNullList<GemInstance> list = NonNullList.withSize(size, GemInstance.EMPTY);
        ItemContainerContents socketedGems = stack.getOrDefault(Components.SOCKETED_GEMS, ItemContainerContents.EMPTY);

        for (int i = 0; i < Math.min(size, socketedGems.getSlots()); i++) {
            ItemStack gem = socketedGems.getStackInSlot(i);
            if (!gem.isEmpty()) {
                gem.setCount(1);
                GemInstance inst = GemInstance.socketed(stack, gem, i);
                list.set(i, inst);
            }
        }

        return new SocketedGems(list);
    }

    /**
     * Sets the gem list on the item to the provided list of gems.<br>
     * Setting more gems than there are sockets will cause the extra gems to be lost.
     *
     * @param stack The stack being modified.
     * @param gems  The list of socketed gems.
     */
    public static void setGems(ItemStack stack, SocketedGems gems) {
        var contents = ItemContainerContents.fromItems(gems.stream().map(GemInstance::gemStack).toList());
        stack.set(Components.SOCKETED_GEMS, contents);
    }

    /**
     * Checks if any of the sockets on the item are empty.
     *
     * @param stack The stack being queried.
     * @return True, if any sockets are empty, otherwise false.
     */
    public static boolean hasEmptySockets(ItemStack stack) {
        return getGems(stack).gems().stream().anyMatch(g -> !g.isValid());
    }

    /**
     * Computes the index of the first empty socket, used during socketing.
     *
     * @param stack The stack being queried.
     * @return The index of the first empty socket in the stack's gem list.
     * @see #getGems(ItemStack)
     */
    public static int getFirstEmptySocket(ItemStack stack) {
        SocketedGems gems = getGems(stack);
        for (int socket = 0; socket < gems.size(); socket++) {
            if (!gems.get(socket).isValid()) {
                return socket;
            }
        }
        return 0;
    }

    /**
     * Gets a stream of socketed gems that are valid for use by the arrow.
     *
     * @param arrow The arrow being queried.
     * @return A stream containing all valid gems in the arrow.
     * @see GemInstance#isValid()
     */
    public static Stream<GemInstance> getGemInstances(Projectile proj) {
        ItemStack stack = AffixHelper.getSourceWeapon(proj);
        return getGems(stack).stream().filter(GemInstance::isValid);
    }

}
