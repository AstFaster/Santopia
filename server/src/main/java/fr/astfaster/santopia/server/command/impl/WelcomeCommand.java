package fr.astfaster.santopia.server.command.impl;

import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class WelcomeCommand extends SantopiaCommand {

    private BukkitTask invalidateTask;
    private WelcomePlayer newPlayer;

    public WelcomeCommand() {
        super("welcome", "Welcome command", "/bienvenue", Arrays.asList("bienvenue", "bvn"));
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (this.newPlayer == null) {
            player.sendMessage(Message.WELCOME_NO_PLAYER.value());
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (this.newPlayer.uuid().equals(player.getUniqueId())) {
            return;
        }

        if (this.newPlayer.welcomes().contains(player.getUniqueId())) {
            player.sendMessage(Message.WELCOME_ALREADY_WELCOMED.value());
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        Bukkit.broadcastMessage(Message.WELCOME_PLAYER_WELCOMED.value()
                .replace("%player%", player.getName())
                .replace("%new_player%", this.newPlayer.name()));

        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0f, 1.0f);

        this.newPlayer.addWelcome(player.getUniqueId());
    }

    public void newPlayer(WelcomePlayer newPlayer) {
        this.newPlayer = newPlayer;

        if (this.invalidateTask != null) {
            this.invalidateTask.cancel();
            this.invalidateTask = null;
        }

        // Invalidate welcomes after 1 minute
        this.invalidateTask = Bukkit.getScheduler().runTaskLaterAsynchronously(SantopiaPlugin.instance(), () -> this.newPlayer = null, 60 * 20L);
    }

    public static class WelcomePlayer {

        private final UUID uuid;
        private final String name;
        private final List<UUID> welcomes = new CopyOnWriteArrayList<>();

        public WelcomePlayer(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public UUID uuid() {
            return this.uuid;
        }

        public String name() {
            return this.name;
        }

        public void addWelcome(UUID welcome) {
            this.welcomes.add(welcome);
        }

        public List<UUID> welcomes() {
            return this.welcomes;
        }

    }

}
