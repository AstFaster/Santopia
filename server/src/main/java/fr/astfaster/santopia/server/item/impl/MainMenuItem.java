package fr.astfaster.santopia.server.item.impl;

import fr.astfaster.santopia.server.gui.impl.MainMenuGUI;
import fr.astfaster.santopia.server.item.CustomItem;
import fr.astfaster.santopia.server.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

public class MainMenuItem extends CustomItem {

    public MainMenuItem() {
        super("main_menu", () -> new ItemBuilder(Material.COMPASS)
                .withName("&6Menu Principal &7(Clic-droit)")
                .withLore("&7Permet de choisir sur quel serveur tu", "&7souhaites aller.")
                .build());
    }

    @Override
    protected void onRightClick(Player player, PlayerInteractEvent event) {
        new MainMenuGUI(player).open();
    }

}
