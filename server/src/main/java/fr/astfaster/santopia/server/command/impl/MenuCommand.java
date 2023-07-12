package fr.astfaster.santopia.server.command.impl;

import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.gui.impl.MainMenuGUI;
import org.bukkit.entity.Player;

public class MenuCommand extends SantopiaCommand {

    public MenuCommand() {
        super("menu");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        new MainMenuGUI(player).open();
    }

}
