package fr.astfaster.santopia.server.module.impl.survival;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.command.impl.WelcomeCommand;
import fr.astfaster.santopia.server.message.Message;
import fr.astfaster.santopia.server.player.PlayerHandler;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class SurvivalPlayerHandler extends PlayerHandler {

    private static final List<Vector> FIREWORKS_LOCATION = Arrays.asList(
            new Vector(1, 0, 0),
            new Vector(-1, 0, 0),
            new Vector(0, 0, 1),
            new Vector(0, 0, -1)
    );

    private final List<UUID> teleportationRequests = new CopyOnWriteArrayList<>();

    public SurvivalPlayerHandler(UUID playerId) {
        super(playerId);
    }

    @Override
    public void onJoin(boolean networkConnection) {
        super.onJoin(networkConnection);

        // Messages
        if (networkConnection) {
            this.player.sendMessage(Message.JOIN_MESSAGE.value().replace("%players%", String.valueOf(SantopiaAPI.instance().networkService().playersCounter())));
            this.player.sendTitle(Message.JOIN_TITLE.value(), Message.JOIN_SUBTITLE.value().replace("%player%", this.player.getName()), 20, 5 * 20, 20);
            this.player.playSound(this.player.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_1, 1.0f, 1.0f);
        }

        if (!this.player.hasPlayedBefore()) {
            for (Vector fireworkLocation : FIREWORKS_LOCATION) {
                final Firework firework = (Firework) this.player.getWorld().spawnEntity(this.player.getLocation().clone().add(fireworkLocation), EntityType.FIREWORK);
                final FireworkMeta fireworkMeta = firework.getFireworkMeta();
                final FireworkEffect effect = FireworkEffect.builder()
                        .withColor(Color.BLUE)
                        .withFade(Color.ORANGE)
                        .with(FireworkEffect.Type.BALL)
                        .build();

                fireworkMeta.addEffect(effect);
                firework.setFireworkMeta(fireworkMeta);
            }

            // Welcome message + command
            Bukkit.broadcastMessage(Message.WELCOME_MESSAGE.value().replace("%player%", this.player.getName()));

            ((WelcomeCommand) SantopiaPlugin.instance().commandRegistry().command("welcome")).newPlayer(new WelcomeCommand.WelcomePlayer(this.playerId, this.player.getName()));
        }
    }

    @Override
    public void onQuit() {
        super.onQuit();
    }

    public void addTeleportationRequest(UUID player) {
        this.teleportationRequests.add(player);

        // Expire after 1 minute
        Bukkit.getScheduler().runTaskLaterAsynchronously(SantopiaPlugin.instance(), () -> this.teleportationRequests.remove(player), 60 * 20L);
    }

    public void removeTeleportationRequest(UUID player) {
        this.teleportationRequests.remove(player);
    }

    public boolean hasTeleportationRequest(UUID player) {
        return this.teleportationRequests.contains(player);
    }

    public List<UUID> teleportationRequests() {
        return this.teleportationRequests;
    }

}
