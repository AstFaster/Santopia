package fr.astfaster.santopia.server.listener;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.chunk.ChunksService;
import fr.astfaster.santopia.server.chunk.LoadedChunk;
import fr.astfaster.santopia.server.message.Message;
import fr.astfaster.santopia.server.player.PlayerHandler;
import org.bson.types.ObjectId;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.jetbrains.annotations.Nullable;

/**
 * The handler of events for guilds' claims
 */
public class ClaimsListener implements Listener {

    private final ChunksService chunksService = SantopiaPlugin.instance().chunksService();

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        // Chunk is not new, so it might be claimed
        if (!event.isNewChunk()) {
            this.chunksService.onLoad(event.getChunk());
            return;
        }

        // Chunk is new, no need to load
        this.chunksService.onNewChunk(event.getChunk());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Block block = event.getClickedBlock();

        if (block != null) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && block.getType() == Material.ENDER_CHEST) {
                return;
            }

            final boolean cancel = this.claimCheck(block.getWorld().getName(), block.getX(), block.getZ(), event.getPlayer(), event.getAction() != Action.RIGHT_CLICK_BLOCK);

            if (cancel) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();

        if (entity instanceof Creature) {
            if (entity instanceof Monster) {
                return;
            }

            final Entity damager = event.getDamager();

            Player player = null;
            if (damager instanceof Player) {
                player = (Player) damager;
            }

            final Location location = entity.getLocation();
            final boolean cancel = this.claimCheck(location.getWorld().getName(), location.getBlockX(), location.getBlockZ(), player, false);

            if (cancel) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        final Hanging entity = event.getEntity();
        final Entity remover = event.getRemover();

        Player player = null;
        if (remover instanceof Player) {
            player = (Player) remover;
        }

        final Location location = entity.getLocation();
        final boolean cancel = this.claimCheck(location.getWorld().getName(), location.getBlockX(), location.getBlockZ(), player, false);

        if (cancel) {
            event.setCancelled(true);
        }
    }

    private boolean claimCheck(String world, int x, int z, @Nullable Player player, boolean message) {
        final LoadedChunk chunk = this.chunksService.loadedChunk(world, x >> 4, z >> 4);

        // Chunk has not been loaded!
        if (chunk == null) {
            return true;
        }

        if (chunk.claimed()) {
            if (player == null) {
                return true;
            }

            final PlayerHandler playerHandler = SantopiaPlugin.instance().playerHandler(player.getUniqueId());
            final ObjectId guildId = chunk.claim().guildId();

            if (!guildId.equals(playerHandler.guildId())) {
                if (message) {
                    SantopiaAPI.instance().guildsService()
                            .guild(guildId)
                            .thenAcceptAsync(guild -> player.sendMessage(Message.CLAIM_ERROR.value().replace("%guild%", guild.name())));
                }

                return true;
            }
        }
        return false;
    }

}
