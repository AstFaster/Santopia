package fr.astfaster.santopia.server.gui.impl;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.server.ServerType;
import fr.astfaster.santopia.server.gui.CustomGUI;
import fr.astfaster.santopia.server.item.ItemBuilder;
import fr.astfaster.santopia.server.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class MainMenuGUI extends CustomGUI {

    public MainMenuGUI(Player owner) {
        super(owner, "§f\uDAFF\uDFD0得", 5 * 9);

        // Survival
        this.fillArea(0, 29, this.serverItem(ServerType.SURVIVAL, ChatColor.YELLOW, ChatColor.GOLD), this.serverEvent(ServerType.SURVIVAL));
        // Creative
        this.fillArea(3, 32, this.serverItem(ServerType.CREATIVE, ChatColor.GREEN, ChatColor.DARK_GREEN), this.serverEvent(ServerType.CREATIVE));
        // Wonders
        this.fillArea(6, 35, this.serverItem(ServerType.WONDERS, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE), this.serverEvent(ServerType.WONDERS));

        // Discord
        this.fillArea(36, 38, null, this.commandEvent("discord"));
        // Twitch
        this.setItem(39, null, this.commandEvent("twitch"));
        // TikTok
        this.setItem(40, null, this.commandEvent("tiktok"));
        // Minecraft
        this.setItem(41, null, this.commandEvent("ip"));
    }

    private Consumer<InventoryClickEvent> commandEvent(String command) {
        return event -> {
            this.owner.closeInventory();
            this.owner.playSound(this.owner.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 2.0f);
            this.owner.performCommand(command);
        };
    }

    private ItemStack serverItem(ServerType server, ChatColor mainColor, ChatColor secondaryColor) {
        return new ItemBuilder(Material.PAPER)
                .withName(mainColor + server.display())
                .withCustomModelData(1598)
                .appendLore(StringUtil.splitInLines(server.description(), 35).toArray(new String[0]))
                .appendLore("",
                        "&7Joueurs: " + mainColor + SantopiaAPI.instance().serversService().server(server).players().size() + secondaryColor + "/" + server.slots(),
                        "",
                        secondaryColor + "Cliquer pour rejoindre")
                .build();
    }

    private Consumer<InventoryClickEvent> serverEvent(ServerType server) {
        return event -> {
            this.owner.closeInventory();
            this.owner.playSound(this.owner.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 2.0f);

            SantopiaAPI.instance().playersService().connectPlayer(this.owner.getUniqueId(), server);
        };
    }

}
