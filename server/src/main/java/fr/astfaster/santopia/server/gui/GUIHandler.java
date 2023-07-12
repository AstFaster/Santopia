package fr.astfaster.santopia.server.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class GUIHandler implements Listener {

    private final Map<UUID, CustomGUI> playersGUI = new HashMap<>();

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getInventory().getHolder() instanceof final CustomGUI gui) {
            final int clickedSlot = event.getRawSlot();

            event.setCancelled(gui.isCancelClickEvent());

            gui.onClick(event);

            if (gui.getClickConsumers().containsKey(clickedSlot)) {
                gui.getClickConsumers().get(clickedSlot).accept(event);
            }
        }
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof final CustomGUI gui) {
            final CustomGUI.Update update = gui.getUpdate();

            gui.onOpen(event);

            if (update != null) {
                update.start();
            }

            this.playersGUI.put(event.getPlayer().getUniqueId(), gui);
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() != null && event.getInventory().getHolder() instanceof final CustomGUI gui) {
            final CustomGUI.Update update = gui.getUpdate();

            if (update != null) {
                update.cancel();
            }

            gui.onClose(event);

            this.playersGUI.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.playersGUI.remove(event.getPlayer().getUniqueId());
    }

    @SuppressWarnings("unchecked")
    public <T extends CustomGUI> T playerGUI(UUID playerId) {
        return (T) this.playersGUI.get(playerId);
    }

    public <T extends CustomGUI> T playerGUI(Player player) {
        return this.playerGUI(player.getUniqueId());
    }

    public <T extends CustomGUI> List<T> guis(Class<T> guisClass) {
        final List<T> result = new ArrayList<>();

        for (CustomGUI gui : this.playersGUI.values()) {
            if (!gui.getClass().equals(guisClass)) {
                continue;
            }

            result.add(guisClass.cast(gui));
        }
        return result;
    }

}
