package fr.astfaster.santopia.server.packet;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.messaging.PacketsHandler;
import fr.astfaster.santopia.api.messaging.SantopiaPacket;
import fr.astfaster.santopia.api.messaging.impl.guild.*;
import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.message.Message;
import fr.astfaster.santopia.server.player.PlayerHandler;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;

public class GuildsPacketsHandler implements PacketsHandler {

    @Override
    public void handle(SantopiaPacket packet) {
        if (packet instanceof final GuildPlayerJoinedPacket join) {
            final ObjectId guildId = join.guildId();

            SantopiaAPI.instance().playersService().account(join.playerId()).thenAcceptAsync(account -> {
                for (PlayerHandler playerHandler : SantopiaPlugin.instance().playersHandlers()) {
                    if (!Objects.equals(playerHandler.guildId(), guildId)) {
                        continue;
                    }

                    playerHandler.player().sendMessage(Message.GUILD_PLAYER_JOINED.value().replace("%player%", account.name()));
                }
            });
        } else if (packet instanceof final GuildPlayerLeftPacket left) {
            final ObjectId guildId = left.guildId();

            SantopiaAPI.instance().playersService().account(left.playerId()).thenAcceptAsync(account -> {
                for (PlayerHandler playerHandler : SantopiaPlugin.instance().playersHandlers()) {
                    if (!Objects.equals(playerHandler.guildId(), guildId)) {
                        continue;
                    }

                    playerHandler.player().sendMessage(Message.GUILD_PLAYER_LEFT.value().replace("%player%", account.name()));
                }
            });
        } else if (packet instanceof final GuildPlayerKickPacket kick) {
            final ObjectId guildId = kick.guildId();

            SantopiaAPI.instance().playersService().account(kick.playerId()).thenAcceptAsync(account -> {
                for (PlayerHandler playerHandler : SantopiaPlugin.instance().playersHandlers()) {
                    if (!Objects.equals(playerHandler.guildId(), guildId)) {
                        continue;
                    }

                    final Player player = playerHandler.player();

                    if (player.getUniqueId().equals(kick.playerId())) {
                        playerHandler.guildId(null);
                        playerHandler.updateDisplayName();
                        playerHandler.refreshTabList();

                        player.sendMessage(Message.GUILD_KICKED.value());
                    } else {
                        player.sendMessage(Message.GUILD_PLAYER_KICKED.value().replace("%player%", account.name()));
                    }
                }
            });
        } else if (packet instanceof final GuildDisbandPacket disband) {
            final ObjectId guildId = disband.guildId();

            SantopiaPlugin.instance().chunksService().clearClaims(guildId);

            for (PlayerHandler playerHandler : SantopiaPlugin.instance().playersHandlers()) {
                if (!Objects.equals(playerHandler.guildId(), guildId)) {
                    continue;
                }

                playerHandler.player().sendMessage(Message.GUILD_DISBAND.value());
                playerHandler.guildId(null);
                playerHandler.updateDisplayName();
                playerHandler.refreshTabList();
            }
        } else if (packet instanceof final GuildRenamePacket rename) {
            final ObjectId guildId = rename.guildId();

            for (PlayerHandler playerHandler : SantopiaPlugin.instance().playersHandlers()) {
                if (!Objects.equals(playerHandler.guildId(), guildId)) {
                    continue;
                }

                playerHandler.player().sendMessage(Message.GUILD_RENAMED.value());
                playerHandler.updateDisplayName();
                playerHandler.refreshTabList();
            }
        } else if (packet instanceof final GuildPrefixChangePacket prefixChange) {
            final ObjectId guildId = prefixChange.guildId();

            for (PlayerHandler playerHandler : SantopiaPlugin.instance().playersHandlers()) {
                if (!Objects.equals(playerHandler.guildId(), guildId)) {
                    continue;
                }

                playerHandler.player().sendMessage(Message.GUILD_PREFIX_CHANGED.value());
                playerHandler.updateDisplayName();
                playerHandler.refreshTabList();
            }
        } else if (packet instanceof final GuildPrefixColorChangePacket colorChange) {
            final ObjectId guildId = colorChange.guildId();

            for (PlayerHandler playerHandler : SantopiaPlugin.instance().playersHandlers()) {
                if (!Objects.equals(playerHandler.guildId(), guildId)) {
                    continue;
                }

                playerHandler.player().sendMessage(Message.GUILD_PREFIX_COLOR_CHANGED.value());
                playerHandler.updateDisplayName();
                playerHandler.refreshTabList();
            }
        } else if (packet instanceof final GuildRequestPacket request) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getUniqueId().equals(request.targetId())) {
                    continue;
                }

                SantopiaAPI.instance().guildsService().guild(request.guildId())
                        .thenAcceptAsync(guild -> SantopiaAPI.instance()
                        .playersService()
                        .account(request.senderId())
                        .thenAcceptAsync(account -> player.sendMessage(Message.GUILD_REQUEST_RECEIVED.value()
                                .replace("%player%", account.name())
                                .replace("%guild%", guild.name())
                                .replaceAll("%player%", account.name()))));
            }
        }
    }

}
