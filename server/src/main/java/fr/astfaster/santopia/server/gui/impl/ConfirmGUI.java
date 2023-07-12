package fr.astfaster.santopia.server.gui.impl;

import fr.astfaster.santopia.server.gui.CustomGUI;
import fr.astfaster.santopia.server.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.function.Consumer;

public class ConfirmGUI extends CustomGUI {

    private Consumer<InventoryClickEvent> confirm;
    private Consumer<InventoryClickEvent> cancel;

    public ConfirmGUI(Player owner) {
        super(owner, "Confirmer", 3 * 9);
    }

    public ConfirmGUI confirm(Consumer<InventoryClickEvent> confirm) {
        this.confirm = confirm;
        return this;
    }

    public ConfirmGUI cancel(Consumer<InventoryClickEvent> cancel) {
        this.cancel = cancel;
        return this;
    }

    @Override
    public void open() {
        this.setItem(12, new ItemBuilder(Material.LIME_STAINED_GLASS)
                .withName("&aConfirmer")
                .build(),
                event -> {
                    this.owner.closeInventory();
                    this.confirm.accept(event);
                });

        this.setItem(14, new ItemBuilder(Material.RED_STAINED_GLASS)
                .withName("&cAnnuler")
                .build(),
                event -> {
                    this.owner.closeInventory();
                    this.cancel.accept(event);
                });

        super.open();
    }

}
