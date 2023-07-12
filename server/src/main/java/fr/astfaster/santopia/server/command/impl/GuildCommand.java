package fr.astfaster.santopia.server.command.impl;

import com.mojang.authlib.GameProfile;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.guild.*;
import fr.astfaster.santopia.api.guild.claim.ClaimsService;
import fr.astfaster.santopia.api.guild.claim.SantopiaClaim;
import fr.astfaster.santopia.api.messaging.PacketsChannel;
import fr.astfaster.santopia.api.messaging.impl.guild.*;
import fr.astfaster.santopia.api.player.PlayersService;
import fr.astfaster.santopia.api.player.SantopiaAccount;
import fr.astfaster.santopia.api.server.ServerType;
import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.gui.impl.ConfirmGUI;
import fr.astfaster.santopia.server.gui.impl.GuildColorGUI;
import fr.astfaster.santopia.server.message.Message;
import fr.astfaster.santopia.server.player.PlayerHandler;
import org.bson.types.ObjectId;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.regex.Pattern;

public class GuildCommand extends SantopiaCommand {

    private static final String DASH_LINE = "§2§m" + SantopiaPlugin.DASH_LINE;

    public GuildCommand() {
        super("guild", "Guilds command", "/guild help", Arrays.asList("guilde", "g", "guilds", "guildes"));
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (args.length > 0) {
            final String firstArg = args[0];

            if (args.length == 1) {
                switch (firstArg) {
                    case "create" -> this.create(player);
                    case "info" -> this.info(player, null);
                    case "disband" -> this.disband(player);
                    case "leave" -> this.leave(player);
                    case "claim" -> this.claim(player);
                    case "unclaim" -> this.unclaim(player);
                    case "rename" -> this.rename(player);
                    case "prefix" -> this.changePrefix(player);
                    case "color" -> this.color(player);
                    default -> this.help(label, player);
                }
            } else if (args.length == 2) {
                final String secondArg = args[1];

                switch (firstArg) {
                    case "info" -> this.info(player, secondArg);
                    case "invite" -> this.invite(player, secondArg);
                    case "accept" -> this.accept(player, secondArg);
                    case "deny" -> this.deny(player, secondArg);
                    case "kick" -> this.kick(player, secondArg);
                    case "promote" -> this.editRole(player, secondArg, RoleEdit.PROMOTE);
                    case "demote" -> this.editRole(player, secondArg, RoleEdit.DEMOTE);
                    case "lead" -> this.lead(player, secondArg);
                    default -> this.help(label, player);
                }
            } else {
                this.help(label, player);
            }
        } else {
            this.help(label, player);
        }
    }

    private void create(Player player) {
        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);

        if (playerHandler.guildId() != null) {
            player.sendMessage(Message.GUILD_ALREADY_IN_ONE.value());
            return;
        }

        final ConversationFactory conversationFactory = new ConversationFactory(SantopiaPlugin.instance());
        final Conversation conversation = conversationFactory
                .withFirstPrompt(new CreationPrompt((name, prefix) -> new ConfirmGUI(player).confirm(event -> {
                    final SantopiaGuild guild = SantopiaAPI.instance().guildsService().createGuild(name, prefix, playerId).join();

                    playerHandler.account().thenAcceptAsync(account -> {
                        account.guild(guild.id());
                        account.update();

                        playerHandler.guildId(guild.id());
                        playerHandler.updateDisplayName();
                        playerHandler.refreshTabList();

                        player.sendMessage(Message.GUILD_CREATED.value());
                    });
                }).cancel(event -> player.sendMessage(Message.ACTION_CANCELLED.value())).open()))
                .withLocalEcho(false)
                .buildConversation(player);

        conversation.begin();
    }

    private void invite(Player player, String targetName) {
        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);
        final ObjectId guildId = playerHandler.guildId();

        if (guildId == null) {
            player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
            return;
        }

        playerHandler.guild().thenAcceptAsync(guild -> {
            if (!GuildPermission.INVITE.has(guild.member(playerId))) {
                player.sendMessage(Message.INVALID_PERMISSION.value());
                return;
            }

            SantopiaPlugin.instance().gameProfilesService().loadProfile(targetName).thenAcceptAsync(profile -> {
                final UUID targetId = profile.getId();

                if (playerId.equals(targetId)) {
                    player.sendMessage(Message.CANT_PERFORM_ON_YOURSELF.value());
                    return;
                }

                final GuildsService guildsService = SantopiaAPI.instance().guildsService();

                guildsService.hasRequest(playerId, targetId).thenAcceptAsync(request -> {
                    if (request) {
                        player.sendMessage(Message.GUILD_ALREADY_REQUESTED.value());
                        return;
                    }

                    SantopiaAPI.instance().playersService().account(targetId).thenAcceptAsync(account -> {
                        if (account == null) {
                            player.sendMessage(Message.PLAYER_NOT_CONNECTED.value().replace("%player%", targetName));
                            return;
                        }

                        SantopiaAPI.instance().guildsService().sendRequest(playerId, targetId, guildId);

                        player.sendMessage(Message.GUILD_REQUEST_SENT.value().replace("%player%", account.name()));
                    });
                });
            });
        });
    }

    private void accept(Player player, String targetName) {
        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);

        if (playerHandler.guildId() != null) {
            player.sendMessage(Message.GUILD_ALREADY_IN_ONE.value());
            return;
        }

        final GuildsService guildsService = SantopiaAPI.instance().guildsService();

        SantopiaPlugin.instance().gameProfilesService().loadProfile(targetName).thenAcceptAsync(profile -> {
            final UUID targetId = profile.getId();

            SantopiaAPI.instance().playersService().account(playerId).thenAcceptAsync(account -> guildsService.request(targetId, playerId).thenAcceptAsync(guildId -> {
                if (guildId == null) {
                    player.sendMessage(Message.GUILD_NO_REQUEST.value());
                    return;
                }

                guildsService.guild(guildId).thenAcceptAsync(guild -> {
                    if (guild == null) {
                        player.sendMessage(Message.GUILD_NO_REQUEST.value());
                        return;
                    }

                    guildsService.removeRequest(targetId, playerId);

                    guild.addMember(playerId);
                    guild.update().join();

                    account.guild(guildId);
                    account.update().join();

                    playerHandler.guildId(guildId);
                    playerHandler.updateDisplayName();
                    playerHandler.refreshTabList();

                    SantopiaAPI.instance().messagingService().send(PacketsChannel.GUILDS, new GuildPlayerJoinedPacket(playerId, guildId));
                });
            }));
        });
    }

    private void deny(Player player, String targetName) {
        final UUID playerId = player.getUniqueId();
        final GuildsService guildsService = SantopiaAPI.instance().guildsService();

        SantopiaPlugin.instance().gameProfilesService().loadProfile(targetName).thenAcceptAsync(profile -> {
            final UUID targetId = profile.getId();

            SantopiaAPI.instance().playersService().account(profile.getId()).thenAcceptAsync(account -> {
                if (account == null) {
                    player.sendMessage(Message.PLAYER_NOT_CONNECTED.value().replace("%player%", targetName));
                    return;
                }

                guildsService.request(targetId, playerId).thenAcceptAsync(guildId -> {
                    if (guildId == null) {
                        player.sendMessage(Message.GUILD_NO_REQUEST.value());
                        return;
                    }

                    guildsService.removeRequest(targetId, playerId);

                    player.sendMessage(Message.GUILD_REQUEST_DENIED.value().replace("%player%", account.name()));
                });
            });
        });
    }

    private void kick(Player player, String targetName) {
        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);

        if (playerHandler.guildId() == null) {
            player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
            return;
        }

        playerHandler.guild().thenAcceptAsync(guild -> {
            if (!GuildPermission.KICK.has(guild.member(playerId))) {
                player.sendMessage(Message.INVALID_PERMISSION.value());
                return;
            }

            SantopiaPlugin.instance().gameProfilesService().loadProfile(targetName).thenAcceptAsync(profile -> {
                final UUID targetId = profile.getId();

                if (playerId.equals(targetId)) {
                    player.sendMessage(Message.CANT_PERFORM_ON_YOURSELF.value());
                    return;
                }

                if (guild.member(targetId) == null) {
                    player.sendMessage(Message.GUILD_NOT_IN.value());
                    return;
                }

                guild.removeMember(targetId);
                guild.update().join();

                SantopiaAPI.instance().messagingService().send(PacketsChannel.GUILDS, new GuildPlayerKickPacket(targetId, guild.id()));
            });
        });
    }

    private void disband(Player player) {
        final UUID playerId = player.getUniqueId();

        SantopiaAPI.instance().guildsService().playerGuild(playerId).thenAccept(guild -> {
            if (guild == null) {
                player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
                return;
            }

            if (!GuildPermission.DISBAND.has(guild.member(playerId))) {
                player.sendMessage(Message.INVALID_PERMISSION.value());
                return;
            }

            SantopiaPlugin.runSync(() -> new ConfirmGUI(player)
                    .confirm(event -> SantopiaAPI.instance().guildsService()
                            .removeGuild(guild)
                            .thenAcceptAsync(__ -> SantopiaAPI.instance()
                                    .messagingService()
                                    .send(PacketsChannel.GUILDS, new GuildDisbandPacket(guild.id()))))
                    .cancel(event -> player.sendMessage(Message.ACTION_CANCELLED.value())).open());
        });
    }

    private void leave(Player player) {
        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);

        SantopiaAPI.instance().guildsService().playerGuild(playerId).thenAcceptAsync(guild -> {
            if (guild == null) {
                player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
                return;
            }

            if (!GuildPermission.LEAVE.has(guild.member(playerId))) {
                player.sendMessage(Message.INVALID_PERMISSION.value());
                return;
            }

            SantopiaPlugin.runSync(() -> new ConfirmGUI(player).confirm(event -> {
                guild.removeMember(playerId);
                guild.update().thenAcceptAsync(__ -> {
                    playerHandler.guildId(null);
                    playerHandler.updateDisplayName();
                    playerHandler.refreshTabList();

                    playerHandler.player().sendMessage(Message.GUILD_LEFT.value());

                    SantopiaAPI.instance().messagingService().send(PacketsChannel.GUILDS, new GuildPlayerLeftPacket(playerId, guild.id()));
                });
            }).cancel(event -> player.sendMessage(Message.ACTION_CANCELLED.value())).open());
        });
    }

    private void editRole(Player player, String targetName, RoleEdit roleEdit) {
        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);
        final ObjectId guildId = playerHandler.guildId();

        if (guildId == null) {
            player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
            return;
        }

        SantopiaPlugin.instance().gameProfilesService().loadProfile(targetName).thenAcceptAsync(profile -> {
            final UUID targetId = profile.getId();

            if (playerId.equals(targetId)) {
                player.sendMessage(Message.CANT_PERFORM_ON_YOURSELF.value());
                return;
            }

            SantopiaAPI.instance().playersService().account(targetId).thenAcceptAsync(account -> {
                if (account == null) {
                    player.sendMessage(Message.PLAYER_NOT_CONNECTED.value().replace("%player%", targetName));
                    return;
                }

               if (!guildId.equals(account.guild())) {
                   player.sendMessage(Message.GUILD_NOT_IN_THE_SAME.value());
                   return;
               }

               SantopiaAPI.instance().guildsService().guild(guildId).thenAcceptAsync(guild -> {
                   if (!roleEdit.permission().get().has(guild.member(playerId))) {
                       player.sendMessage(Message.INVALID_PERMISSION.value());
                       return;
                   }

                   final GuildMember member = guild.member(targetId);
                   final GuildRole newRole = roleEdit.newRole().apply(member.role());

                   if (newRole == GuildRole.LEADER) {
                       player.sendMessage(Message.GUILD_CANT_PROMOTE.value());
                       return;
                   } else if (newRole == null) {
                       player.sendMessage(Message.GUILD_CANT_DEMOTE.value());
                       return;
                   }

                   member.role(newRole);

                   guild.update().join();

                   player.sendMessage(roleEdit.successMessage().get().replace("%player%", account.name()));
               });
            });
        });
    }

    private void lead(Player player, String targetName) {
        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);
        final ObjectId guildId = playerHandler.guildId();

        if (guildId == null) {
            player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
            return;
        }

        SantopiaAPI.instance().guildsService().guild(guildId).thenAcceptAsync(guild -> {
            if (!GuildPermission.CHANGE_LEADER.has(guild.member(playerId))) {
                player.sendMessage(Message.INVALID_PERMISSION.value());
                return;
            }

            SantopiaPlugin.instance().gameProfilesService().loadProfile(targetName).thenAcceptAsync(profile -> {
                final UUID targetId = profile.getId();

                if (playerId.equals(targetId)) {
                    player.sendMessage(Message.CANT_PERFORM_ON_YOURSELF.value());
                    return;
                }

                SantopiaAPI.instance().playersService().account(targetId).thenAcceptAsync(account -> {
                    if (account == null) {
                        player.sendMessage(Message.PLAYER_NOT_CONNECTED.value().replace("%player%", targetName));
                        return;
                    }

                    if (!guildId.equals(account.guild())) {
                        player.sendMessage(Message.GUILD_NOT_IN_THE_SAME.value());
                        return;
                    }

                    SantopiaPlugin.runSync(() -> new ConfirmGUI(player)
                            .confirm(event -> {
                                guild.member(targetId).role(GuildRole.LEADER);
                                guild.member(playerId).role(GuildRole.OFFICER);
                                guild.leader(targetId);
                                guild.update().join();

                                player.sendMessage(Message.GUILD_LEADER_CHANGED.value().replace("%player%", account.name()));
                            })
                            .cancel(event -> player.sendMessage(Message.ACTION_CANCELLED.value())).open());
                });
            });
        });
    }

    private void claim(Player player) {
        if (SantopiaAPI.instance().currentServer().type() != ServerType.SURVIVAL) {
            player.sendMessage(Message.INVALID_PERMISSION.value());
            return;
        }

        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);
        final ObjectId guildId = playerHandler.guildId();

        if (guildId == null) {
            player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
            return;
        }

        playerHandler.guild().thenAcceptAsync(guild -> {
            if (!GuildPermission.CLAIM.has(guild.member(playerId))) {
                player.sendMessage(Message.INVALID_PERMISSION.value());
                return;
            }

            final ClaimsService claimsService = SantopiaAPI.instance().claimsService();
            final Location location = player.getLocation();
            final String world = location.getWorld().getName();
            final int x = location.getBlockX() >> 4;
            final int z = location.getBlockZ() >> 4;

            claimsService.loadClaim(world, x, z).thenAcceptAsync(claim -> {
                if (claim != null) {
                    player.sendMessage(Message.GUILD_ALREADY_CLAIMED.value());
                    return;
                }

                final SantopiaClaim newClaim = new SantopiaClaim(guildId, world, x, z);

                claimsService.saveClaim(newClaim).join();

                SantopiaPlugin.instance().chunksService().addLoadedChunk(world, x, z, newClaim);

                player.sendMessage(Message.GUILD_CLAIMED.value());
            });
        });
    }

    private void unclaim(Player player) {
        if (SantopiaAPI.instance().currentServer().type() != ServerType.SURVIVAL) {
            player.sendMessage(Message.INVALID_PERMISSION.value());
            return;
        }

        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);
        final ObjectId guildId = playerHandler.guildId();

        if (guildId == null) {
            player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
            return;
        }

        playerHandler.guild().thenAcceptAsync(guild -> {
            if (!GuildPermission.UNCLAIM.has(guild.member(playerId))) {
                player.sendMessage(Message.INVALID_PERMISSION.value());
                return;
            }

            final ClaimsService claimsService = SantopiaAPI.instance().claimsService();
            final Location location = player.getLocation();
            final String world = location.getWorld().getName();
            final int x = location.getBlockX() >> 4;
            final int z = location.getBlockZ() >> 4;

            claimsService.loadClaim(world, x, z).thenAcceptAsync(claim -> {
                if (claim == null) {
                    player.sendMessage(Message.GUILD_NOT_CLAIMED.value());
                    return;
                }

                claimsService.deleteClaim(claim).join();

                SantopiaPlugin.instance().chunksService().disableClaim(world, x, z);

                player.sendMessage(Message.GUILD_UNCLAIMED.value());
            });
        });
    }

    private void rename(Player player) {
        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);
        final ObjectId guildId = playerHandler.guildId();

        if (guildId == null) {
            player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
            return;
        }

        playerHandler.guild().thenAcceptAsync(guild -> {
            if (!GuildPermission.RENAME.has(guild.member(playerId))) {
                player.sendMessage(Message.INVALID_PERMISSION.value());
                return;
            }

            final ConversationFactory conversationFactory = new ConversationFactory(SantopiaPlugin.instance());
            final Conversation conversation = conversationFactory
                    .withFirstPrompt(new RenamePrompt(name -> new ConfirmGUI(player).confirm(event -> {
                        guild.name(name);
                        guild.update().thenAcceptAsync(__ -> SantopiaAPI.instance().messagingService().send(PacketsChannel.GUILDS, new GuildRenamePacket(guildId)));
                    }).cancel(event -> player.sendMessage(Message.ACTION_CANCELLED.value())).open()))
                    .withLocalEcho(false)
                    .buildConversation(player);

            conversation.begin();
        });
    }

    private void changePrefix(Player player) {
        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);
        final ObjectId guildId = playerHandler.guildId();

        if (guildId == null) {
            player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
            return;
        }

        playerHandler.guild().thenAcceptAsync(guild -> {
            if (!GuildPermission.CHANGE_PREFIX.has(guild.member(playerId))) {
                player.sendMessage(Message.INVALID_PERMISSION.value());
                return;
            }

            final ConversationFactory conversationFactory = new ConversationFactory(SantopiaPlugin.instance());
            final Conversation conversation = conversationFactory
                    .withFirstPrompt(new ChangePrefixPrompt(prefix -> new ConfirmGUI(player).confirm(event -> {
                        guild.prefix(prefix);
                        guild.update().thenAcceptAsync(__ -> SantopiaAPI.instance().messagingService().send(PacketsChannel.GUILDS, new GuildPrefixChangePacket(guildId)));
                    }).cancel(event -> player.sendMessage(Message.ACTION_CANCELLED.value())).open()))
                    .withLocalEcho(false)
                    .buildConversation(player);

            conversation.begin();
        });
    }

    private void color(Player player) {
        final UUID playerId = player.getUniqueId();
        final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(playerId);
        final ObjectId guildId = playerHandler.guildId();

        if (guildId == null) {
            player.sendMessage(Message.GUILD_PLAYER_DOESNT_HAVE.value());
            return;
        }

        playerHandler.guild().thenAccept(guild -> {
            if (!GuildPermission.COLOR.has(guild.member(playerId))) {
                player.sendMessage(Message.INVALID_PERMISSION.value());
                return;
            }

            SantopiaPlugin.runSync(() -> new GuildColorGUI(player, guild.prefix(), color -> {
                guild.prefixColor(color.toString());
                guild.update().thenAcceptAsync(__ -> SantopiaAPI.instance().messagingService().send(PacketsChannel.GUILDS, new GuildPrefixColorChangePacket(guildId)));
            }).open());
        });
    }

    private void info(Player player, String targetName) {
        final CompletableFuture<UUID> targetIdFuture = targetName != null ? SantopiaPlugin.instance().gameProfilesService()
                .loadProfile(targetName)
                .thenApplyAsync(GameProfile::getId) : CompletableFuture.completedFuture(player.getUniqueId());

        targetIdFuture.thenAcceptAsync(targetId -> {
            if (!targetId.equals(player.getUniqueId())) {
                if (SantopiaAPI.instance().playersService().account(targetId).join() == null) {
                    player.sendMessage(Message.PLAYER_NOT_CONNECTED.value().replace("%player%", targetName));
                    return;
                }
            }

            SantopiaAPI.instance().guildsService().playerGuild(targetId).thenAcceptAsync(guild -> {
                if (guild == null) {
                    player.sendMessage((targetName != null ? Message.GUILD_TARGET_DOESNT_HAVE : Message.GUILD_PLAYER_DOESNT_HAVE).value());
                    return;
                }

                final StringBuilder builder = new StringBuilder(DASH_LINE).append("\n");
                final PlayersService playersService = SantopiaAPI.instance().playersService();
                final Function<UUID, String> playerFormatter = playerId -> {
                    final SantopiaAccount account = playersService.account(playerId).join();

                    return ChatColor.RESET + playersService.rankPrefix(playerId) + " " + account.name() + (playersService.session(playerId).join() != null ? ChatColor.GREEN : ChatColor.RED) + " ● ";
                };
                final List<GuildMember> officers = guild.members(GuildRole.OFFICER);
                final List<GuildMember> members = guild.members(GuildRole.MEMBER);

                builder.append("§2Informations de la guilde:").append("\n")
                        .append("§7Nom: §a").append(guild.name()).append("\n")
                        .append("§7Préfixe: ").append(guild.prefixColor()).append("[").append(guild.prefix()).append("]\n")
                        .append("§7Chef: ").append(playerFormatter.apply(guild.leader())).append("\n");

                if (officers.size() > 0) {
                    builder.append("§7Officier(s): ").append("§8(").append(officers.size()).append(") ");

                    for (GuildMember officer : officers) {
                        builder.append(playerFormatter.apply(officer.uuid()));
                    }

                    builder.append("\n");
                }

                if (members.size() > 0) {
                    builder.append("§7Membre(s): ").append("§8(").append(members.size()).append(") ");

                    for (GuildMember member : members) {
                        builder.append(playerFormatter.apply(member.uuid()));
                    }

                    builder.append("\n");
                }

                builder.append(DASH_LINE);

                player.sendMessage(builder.toString());
            }).exceptionally(error -> {
                try {
                    throw error;
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });;
        }).exceptionally(error -> {
            try {
                throw error;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void help(String label, Player player) {
        final StringBuilder builder = new StringBuilder(DASH_LINE).append("\n");
        final BiConsumer<String, String> appender = (usage, description) -> builder.append("§a/").append(label)
                .append(" ").append(usage)
                .append(" §8- §7").append(description).append("\n");

        appender.accept("help", "Affiche la liste des commandes disponibles");
        appender.accept("create", "Permet de créer ta propre guilde");
        appender.accept("rename", "Permet de renommer ta guilde");
        appender.accept("prefix", "Change le préfixe de ta guilde");
        appender.accept("color", "Change la couleur de ta guilde");
        appender.accept("info [joueur]", "Affiche les informations de ta guilde ou celle d'un autre joueur");
        appender.accept("invite <joueur>", "Invite un joueur à rejoindre ta guilde");
        appender.accept("accept <joueur>", "Accepte la dernière demande reçue ou celle d'un joueur précis");
        appender.accept("deny <joueur>", "Refuse la dernière demande reçue ou celle d'un joueur précis");
        appender.accept("kick <joueur>", "Expulse un joueur de ta guilde");
        appender.accept("promote <joueur>", "Promeut un joueur de ta guilde");
        appender.accept("demote <joueur>", "Rétrograde un joueur de ta guilde");
        appender.accept("lead <joueur>", "Définit le nouveau chef de la guilde");
        appender.accept("disband", "Dissout ta guilde");
        appender.accept("leave", "Quitte la guilde dans laquelle tu es");
        appender.accept("claim", "Claim le chunk dans lequel tu te trouves");
        appender.accept("unclaim", "Unclaim le chunk dans lequel tu te trouves");

        builder.append(DASH_LINE);

        player.sendMessage(builder.toString());
    }

    private static class CreationPrompt extends ValidatingPrompt {

        private Type type = Type.NAME;
        private final BiConsumer<String, String> onFinish;

        public CreationPrompt(BiConsumer<String, String> onFinish) {
            this.onFinish = onFinish;
        }

        @Override
        protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
            if (this.type.existsCheck().test(input)) {
                context.setSessionData("already-taken", true);

                System.out.println("Existing " + input);
                return false;
            }
            return this.type.regex().get().matcher(input).find();
        }

        @Nullable
        @Override
        protected Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
            if (this.type == Type.NAME) {
                context.setSessionData("name", input);

                this.type = Type.PREFIX;
                return this;
            }

            this.onFinish.accept((String) context.getSessionData("name"), input);

            return END_OF_CONVERSATION;
        }

        @NotNull
        @Override
        public String getPromptText(@NotNull ConversationContext context) {
            return this.type.inputMessage().get();
        }

        @Nullable
        @Override
        protected String getFailedValidationText(@NotNull ConversationContext context, @NotNull String invalidInput) {
            if (context.getSessionData("already-taken") != null) {
                context.setSessionData("already-taken", null);

                return this.type.existsMessage().get();
            }
            return this.type.invalidMessage().get();
        }

        public enum Type {

            NAME(() -> SantopiaGuild.NAME_REGEX, Message.GUILD_NAME_INPUT::value, Message.GUILD_INVALID_NAME::value,
                    name -> SantopiaAPI.instance().guildsService().guildByName(name).join() != null, Message.GUILD_NAME_ALREADY_EXISTS::value),
            PREFIX(() -> SantopiaGuild.PREFIX_REGEX, Message.GUILD_PREFIX_INPUT::value, Message.GUILD_INVALID_PREFIX::value,
                    prefix -> SantopiaAPI.instance().guildsService().guildByPrefix(prefix).join() != null, Message.GUILD_PREFIX_ALREADY_EXISTS::value);

            private final Supplier<Pattern> regex;
            private final Supplier<String> inputMessage;
            private final Supplier<String> invalidMessage;
            private final Predicate<String> existsCheck;
            private final Supplier<String> existsMessage;

            Type(Supplier<Pattern> regex, Supplier<String> inputMessage, Supplier<String> invalidMessage, Predicate<String> existsCheck, Supplier<String> existsMessage) {
                this.regex = regex;
                this.inputMessage = inputMessage;
                this.invalidMessage = invalidMessage;
                this.existsCheck = existsCheck;
                this.existsMessage = existsMessage;
            }

            public Supplier<Pattern> regex() {
                return this.regex;
            }

            public Supplier<String> inputMessage() {
                return this.inputMessage;
            }

            public Supplier<String> invalidMessage() {
                return this.invalidMessage;
            }

            public Predicate<String> existsCheck() {
                return this.existsCheck;
            }

            public Supplier<String> existsMessage() {
                return this.existsMessage;
            }

        }

    }

    private static class RenamePrompt extends ValidatingPrompt {

        private final Consumer<String> onFinish;

        public RenamePrompt(Consumer<String> onFinish) {
            this.onFinish = onFinish;
        }

        @Override
        protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
            if (SantopiaAPI.instance().guildsService().guildByName(input).join() != null) {
                context.setSessionData("already-taken", true);
                return false;
            }
            return SantopiaGuild.NAME_REGEX.matcher(input).find();
        }

        @Nullable
        @Override
        protected Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
            this.onFinish.accept(input);

            return END_OF_CONVERSATION;
        }

        @NotNull
        @Override
        public String getPromptText(@NotNull ConversationContext context) {
            return Message.GUILD_NAME_INPUT.value();
        }

        @Nullable
        @Override
        protected String getFailedValidationText(@NotNull ConversationContext context, @NotNull String invalidInput) {
            if (context.getSessionData("already-taken") != null) {
                context.setSessionData("already-taken", null);

                return Message.GUILD_INVALID_PREFIX.value();
            }
            return Message.GUILD_INVALID_NAME.value();
        }
    }

    private static class ChangePrefixPrompt extends ValidatingPrompt {

        private final Consumer<String> onFinish;

        public ChangePrefixPrompt(Consumer<String> onFinish) {
            this.onFinish = onFinish;
        }

        @Override
        protected boolean isInputValid(@NotNull ConversationContext context, @NotNull String input) {
            if (SantopiaAPI.instance().guildsService().guildByPrefix(input).join() != null) {
                context.setSessionData("already-taken", true);
                return false;
            }
            return SantopiaGuild.PREFIX_REGEX.matcher(input).find();
        }

        @Nullable
        @Override
        protected Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull String input) {
            this.onFinish.accept(input);

            return END_OF_CONVERSATION;
        }

        @NotNull
        @Override
        public String getPromptText(@NotNull ConversationContext context) {
            return Message.GUILD_PREFIX_INPUT.value();
        }

        @Nullable
        @Override
        protected String getFailedValidationText(@NotNull ConversationContext context, @NotNull String invalidInput) {
            if (context.getSessionData("already-taken") != null) {
                context.setSessionData("already-taken", null);

                return Message.GUILD_PREFIX_ALREADY_EXISTS.value();
            }
            return Message.GUILD_INVALID_PREFIX.value();
        }
    }

    private enum RoleEdit {

        PROMOTE(() -> GuildPermission.PROMOTE, GuildRole::next, Message.GUILD_PROMOTED::value),
        DEMOTE(() -> GuildPermission.DEMOTE, GuildRole::previous, Message.GUILD_DEMOTED::value);

        private final Supplier<GuildPermission> permission;
        private final Function<GuildRole, GuildRole> newRole;
        private final Supplier<String> successMessage;

        RoleEdit(Supplier<GuildPermission> permission, Function<GuildRole, GuildRole> newRole, Supplier<String> successMessage) {
            this.permission = permission;
            this.newRole = newRole;
            this.successMessage = successMessage;
        }

        public Supplier<GuildPermission> permission() {
            return this.permission;
        }

        public Function<GuildRole, GuildRole> newRole() {
            return this.newRole;
        }

        public Supplier<String> successMessage() {
            return this.successMessage;
        }

    }

}
