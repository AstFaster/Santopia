package fr.astfaster.santopia.server.tablist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class TabListTeam {

    private final String name;
    private String prefix;
    private String suffix;

    private Team handle;
    private final Scoreboard scoreboard;

    public TabListTeam(String name, String prefix, String suffix) {
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;

        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        this.handle = this.scoreboard.getTeam(this.name);

        if (this.handle == null) {
            this.handle = this.scoreboard.registerNewTeam(this.name);
        }

        this.handle.setCanSeeFriendlyInvisibles(true);
        this.handle.setAllowFriendlyFire(true);
        this.handle.setPrefix(ChatColor.translateAlternateColorCodes('&', this.prefix));
        this.handle.setSuffix(ChatColor.translateAlternateColorCodes('&', this.suffix));
    }

    @SuppressWarnings("deprecation")
    public void add(Player... players) {
        for (Player player : players) {
            this.handle.addPlayer(player);

            player.setScoreboard(this.scoreboard);
        }
    }

    public void destroy() {
        this.handle.unregister();
    }

    public String name() {
        return this.name;
    }

    public String prefix() {
        return this.prefix;
    }

    public void prefix(String prefix) {
        this.prefix = prefix;
        this.handle.setPrefix(ChatColor.translateAlternateColorCodes('&', this.prefix));
    }

    public String suffix() {
        return this.suffix;
    }

    public void suffix(String suffix) {
        this.suffix = suffix;
        this.handle.setSuffix(ChatColor.translateAlternateColorCodes('&', this.suffix));
    }

}
