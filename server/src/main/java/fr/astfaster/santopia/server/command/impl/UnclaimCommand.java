package fr.astfaster.santopia.server.command.impl;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.guild.claim.ClaimsService;
import fr.astfaster.santopia.api.player.Rank;
import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.message.Message;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class UnclaimCommand extends SantopiaCommand {

    public UnclaimCommand() {
        super("unclaim");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        final Rank rank = SantopiaAPI.instance().playersService().playerRank(player.getUniqueId());

        if (rank.less(Rank.MODERATOR)) {
            player.sendMessage(Message.INVALID_PERMISSION.value());
            return;
        }

        final Location location = player.getLocation();
        final String world = location.getWorld().getName();
        final int x = location.getBlockX() >> 4;
        final int z = location.getBlockZ() >> 4;
        final ClaimsService claimsService = SantopiaAPI.instance().claimsService();

        claimsService.loadClaim(world, x, z).thenAcceptAsync(claim -> {
            if (claim == null) {
                player.sendMessage(Message.GUILD_NOT_CLAIMED.value());
                return;
            }

            claimsService.deleteClaim(claim).join();

            SantopiaPlugin.instance().chunksService().disableClaim(world, x, z);

            player.sendMessage(Message.GUILD_UNCLAIMED.value());
        });
    }

}
