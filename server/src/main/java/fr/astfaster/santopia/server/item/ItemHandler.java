package fr.astfaster.santopia.server.item;

import fr.astfaster.santopia.server.SantopiaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemHandler implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) {
            return;
        }

        final ItemStack itemStack = event.getItem();
        final ItemNBT nbt = new ItemNBT(itemStack);

        if (!nbt.hasTag(ItemRegistry.ITEM_NBT)) {
            return;
        }

        final String itemId = nbt.getString(ItemRegistry.ITEM_NBT);
        final CustomItem item = SantopiaPlugin.instance().itemRegistry().item(itemId);

        if (item == null) {
            return;
        }

        final Player player = event.getPlayer();
        final Action action = event.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            item.onLeftClick(player, event);
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            item.onRightClick(player, event);
        }
    }

}
