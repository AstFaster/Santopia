package fr.astfaster.santopia.server.command.impl;

import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.message.Message;
import fr.astfaster.santopia.server.module.impl.survival.SurvivalPlayerHandler;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class TeleportationCommand extends SantopiaCommand implements Listener {

    public TeleportationCommand() {
        super("tpa", "Teleportation command", "/tpa <joueur>", Arrays.asList("tpaccept", "tpdeny"));
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (label.equalsIgnoreCase("tpa")) {
            this.request(player, args);
        } else if (label.equalsIgnoreCase("tpaccept")) {
            this.teleport(player, args);
        } else if (label.equalsIgnoreCase("tpdeny")) {
            this.deny(player, args);
        }
    }

    private void request(Player player, String[] args) {
        if (args.length != 1) {
            player.sendMessage(Message.INVALID_COMMAND.value().replace("%command%", this.usageMessage));
            return;
        }

        final Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            player.sendMessage(Message.PLAYER_NOT_CONNECTED.value().replace("%player%", args[0]));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        final SurvivalPlayerHandler targetHandler = (SurvivalPlayerHandler) SantopiaPlugin.instance().playerHandler(target.getUniqueId());

        if (targetHandler.hasTeleportationRequest(player.getUniqueId())) {
            player.sendMessage(Message.TELEPORTATION_REQUEST_ALREADY_SENT.value().replace("%player%", target.getName()));
            return;
        }

        targetHandler.addTeleportationRequest(player.getUniqueId());

        player.sendMessage(Message.TELEPORTATION_REQUEST_SENT.value().replace("%player%", target.getName()));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        target.sendMessage(Message.TELEPORTATION_REQUEST_RECEIVED.value().replace("%player%", player.getName()));
        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
    }

    private void teleport(Player player, String[] args) {
        final SurvivalPlayerHandler playerHandler = (SurvivalPlayerHandler) SantopiaPlugin.instance().playerHandler(player.getUniqueId());

        this.commonChecks(player, args, (requesterId, requester) -> {
            if (requester == null || !requester.isOnline()) {
                playerHandler.removeTeleportationRequest(requesterId);

                player.sendMessage(Message.TELEPORTATION_REQUESTER_NOT_ONLINE.value());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                return;
            }

            requester.teleport(player.getLocation());

            requester.sendMessage(Message.TELEPORTATION_REQUEST_ACCEPTED.value().replace("%player%", player.getName()));
            requester.playSound(requester.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);

            player.sendMessage(Message.TELEPORTATION_REQUEST_ACCEPTED_TARGET.value().replace("%player%", requester.getName()));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
        });
    }

    private void deny(Player player, String[] args) {
        final SurvivalPlayerHandler playerHandler = (SurvivalPlayerHandler) SantopiaPlugin.instance().playerHandler(player.getUniqueId());

        this.commonChecks(player, args, (requesterId, requester) -> {
            playerHandler.removeTeleportationRequest(requesterId);

            if (requester != null && requester.isOnline()) {
                requester.sendMessage(Message.TELEPORTATION_REQUEST_DENIED.value().replace("%player%", player.getName()));
                requester.playSound(requester.getLocation(), Sound.ENTITY_VILLAGER_YES, 1.0F, 1.0F);
            }

            player.sendMessage(Message.TELEPORTATION_REQUEST_DENIED_TARGET.value().replace("%player%", requester.getName()));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 1.0F);
        });
    }

    private void commonChecks(Player player, String[] args, BiConsumer<UUID, Player> postChecksExecution) {
        final SurvivalPlayerHandler playerHandler = (SurvivalPlayerHandler) SantopiaPlugin.instance().playerHandler(player.getUniqueId());
        final List<UUID> requests = playerHandler.teleportationRequests();

        if (requests.size() == 0) {
            player.sendMessage(Message.TELEPORTATION_NO_REQUEST.value());
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
            return;
        }

        UUID requesterId;
        Player requester;
        if (args.length > 0) { // Precise requester
            requester = Bukkit.getPlayerExact(args[0]);

            if (requester == null) {
                player.sendMessage(Message.PLAYER_NOT_CONNECTED.value());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                return;
            }

            requesterId = requester.getUniqueId();

            if (!playerHandler.hasTeleportationRequest(requesterId)) {
                player.sendMessage(Message.TELEPORTATION_NO_REQUEST_FROM_PLAYER.value());
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0F, 1.0F);
                return;
            }
        } else { // Last requester
            requesterId = requests.get(requests.size() - 1);
            requester = Bukkit.getPlayer(requesterId);
        }

        postChecksExecution.accept(requesterId, requester);
    }

}
