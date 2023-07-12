package fr.astfaster.santopia.server.gui.impl;

import fr.astfaster.santopia.server.gui.CustomGUI;
import fr.astfaster.santopia.server.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GuildColorGUI extends CustomGUI {

    private static final Map<ChatColor, Material> COLORS_TO_WOOL = new HashMap<>(){{
        put(ChatColor.DARK_BLUE, Material.BLUE_WOOL);
        put(ChatColor.BLUE, Material.LIGHT_BLUE_WOOL);
        put(ChatColor.AQUA, Material.CYAN_WOOL);
        put(ChatColor.GREEN, Material.LIME_WOOL);
        put(ChatColor.DARK_GREEN, Material.GREEN_WOOL);
        put(ChatColor.YELLOW, Material.YELLOW_WOOL);
        put(ChatColor.GOLD, Material.ORANGE_WOOL);
        put(ChatColor.DARK_PURPLE, Material.PURPLE_WOOL);
        put(ChatColor.LIGHT_PURPLE, Material.PINK_WOOL);
        put(ChatColor.RED, Material.RED_WOOL);
        put(ChatColor.BLACK, Material.BLACK_WOOL);
        put(ChatColor.DARK_GRAY, Material.GRAY_WOOL);
        put(ChatColor.GRAY, Material.LIGHT_GRAY_WOOL);
        put(ChatColor.WHITE, Material.WHITE_WOOL);
    }};

    public GuildColorGUI(Player owner, String guildPrefix, Consumer<ChatColor> onChose) {
        super(owner, "Choisis une couleur", dynamicSize(COLORS_TO_WOOL.size()));

        for (Map.Entry<ChatColor, Material> entry : COLORS_TO_WOOL.entrySet()) {
            this.addItem(new ItemBuilder(entry.getValue())
                    .withName(entry.getKey() + guildPrefix)
                    .build(),
                    event -> {
                        this.owner.closeInventory();

                        onChose.accept(entry.getKey());
                    });
        }
    }

}
