package fr.astfaster.santopia.server.player;

import com.mojang.authlib.GameProfile;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.guild.SantopiaGuild;
import fr.astfaster.santopia.api.player.Rank;
import fr.astfaster.santopia.api.player.SantopiaAccount;
import fr.astfaster.santopia.api.player.SantopiaSession;
import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.tablist.TabListHandler;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerHandler {

    protected final AtomicInteger loadingState = new AtomicInteger(0);

    protected ObjectId guildId;

    protected final UUID playerId;
    protected final Player player;

    public PlayerHandler(UUID playerId) {
        this.playerId = playerId;
        this.player = Bukkit.getPlayer(playerId);
    }

    public void onJoin(boolean networkConnection) {
        // Save guild id in cache
        this.guild().thenAcceptAsync(guild -> {
            this.guildId = guild.id();
            this.loadingState.incrementAndGet();
        });

        this.player.setPlayerListHeader("\n里\n ");

        // Set player's display name
        this.updateDisplayName();

        // Save player's GameProfile
        SantopiaPlugin.instance().gameProfilesService()
                .saveProfile(this.gameProfile())
                .thenAcceptAsync(__ -> this.loadingState.incrementAndGet());

        if (!this.player.hasPlayedBefore()) {
            // Teleport player to spawn
            this.player.teleport(SantopiaPlugin.instance().config().spawn().asBukkit());
        }

        // Add player in tab-list
        SantopiaPlugin.instance().tabListHandler().createPlayerTeam(this.player);
    }

    public void onQuit() {
        // Remove player from tab-list
        SantopiaPlugin.instance().tabListHandler().removePlayerTeam(this.player);
    }

    public boolean finishedLoading() {
        return this.loadingState.get() == 2;
    }

    public void updateDisplayName() {
        this.player.setDisplayName(this.prefix().join() + this.player.getName());
    }

    public void refreshTabList() {
        final TabListHandler tabListHandler = SantopiaPlugin.instance().tabListHandler();

        tabListHandler.removePlayerTeam(this.player);
        tabListHandler.createPlayerTeam(this.player);
    }

    public CompletableFuture<String> prefix() {
        return this.guild().thenApplyAsync(guild -> {
            final String rankPrefix = SantopiaAPI.instance().playersService().rankPrefix(this.playerId) + " ";

            if (guild == null) {
                return rankPrefix;
            }
            return guild.prefixColor() + "[" + guild.prefix() + "] §f" + rankPrefix;
        });
    }

    public Rank rank() {
        return SantopiaAPI.instance().playersService().playerRank(this.playerId);
    }

    public Player player() {
        return this.player;
    }

    public GameProfile gameProfile() {
        return ((CraftPlayer) this.player).getProfile();
    }

    public ObjectId guildId() {
        return this.guildId;
    }

    public void guildId(ObjectId guildId) {
        this.guildId = guildId;
    }

    public CompletableFuture<SantopiaGuild> guild() {
        return SantopiaAPI.instance().guildsService().playerGuild(this.playerId);
    }

    public CompletableFuture<SantopiaAccount> account() {
        return SantopiaAPI.instance().playersService().account(this.playerId);
    }

    public CompletableFuture<SantopiaSession> session() {
        return SantopiaAPI.instance().playersService().session(this.playerId);
    }

}
