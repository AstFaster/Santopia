package fr.astfaster.santopia.server.tablist;

import fr.astfaster.santopia.server.SantopiaPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TabListHandler {

    private final Map<String, TabListTeam> teams = new HashMap<>();

    public TabListTeam createPlayerTeam(Player player) {
        final TabListTeam team = new TabListTeam(player.getName(), SantopiaPlugin.instance().playerHandler(player.getUniqueId()).prefix().join(), "");

        team.add(player);

        this.teams.put(player.getName(), team);

        return team;
    }

    public void removePlayerTeam(Player player) {
        Optional.ofNullable(this.teams.remove(player.getName())).ifPresent(TabListTeam::destroy);
    }

    public TabListTeam team(String name) {
        return this.teams.get(name);
    }

}
