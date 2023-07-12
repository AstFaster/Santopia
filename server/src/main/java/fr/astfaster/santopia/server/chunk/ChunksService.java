package fr.astfaster.santopia.server.chunk;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.guild.claim.ClaimsService;
import fr.astfaster.santopia.api.guild.claim.SantopiaClaim;
import org.bson.types.ObjectId;
import org.bukkit.Chunk;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ChunksService {

    /** World -> X position -> Z position -> Claim */
    private final Map<String, Map<Integer, Map<Integer, LoadedChunk>>> loadedChunks = new ConcurrentHashMap<>();

    private final ClaimsService claimsService = SantopiaAPI.instance().claimsService();

    public void onLoad(Chunk chunk) {
        final String world = chunk.getWorld().getName();
        final int x = chunk.getX();
        final int z = chunk.getZ();

        this.claimsService.loadClaim(world, x, z).thenAcceptAsync(claim -> this.addLoadedChunk(world, x, z, claim));
    }

    public void onNewChunk(Chunk chunk) {
        this.addLoadedChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), null);
    }

    public void addLoadedChunk(String world, int x, int z, SantopiaClaim claim) {
        this.loadedChunks.merge(world, new HashMap<>(), (oldValue, newValue) -> oldValue)
                .merge(x, new HashMap<>(), (oldValue, newValue) -> oldValue)
                .put(z, new LoadedChunk(x, z, claim));
    }

    public void disableClaim(String world, int x, int z) {
        Optional.ofNullable(this.loadedChunks.get(world))
                .flatMap(chunks -> Optional.ofNullable(chunks.get(x)))
                .ifPresent(zChunks -> zChunks.get(z).claim(null));
    }

    public LoadedChunk loadedChunk(String world, int x, int z) {
        final Map<Integer, Map<Integer, LoadedChunk>> chunks = this.loadedChunks.get(world);

        if (chunks == null) {
            return null;
        }

        final Map<Integer, LoadedChunk> zChunks = chunks.get(x);

        if (zChunks == null) {
            return null;
        }
        return zChunks.get(z);
    }

    public void clearClaims(ObjectId guildId) {
        for (Map<Integer, Map<Integer, LoadedChunk>> chunks : this.loadedChunks.values()) {
            for (Map<Integer, LoadedChunk> zChunks : chunks.values()) {
                for (Map.Entry<Integer, LoadedChunk> entry : zChunks.entrySet()) {
                    final LoadedChunk chunk = entry.getValue();

                    if (!chunk.claimed()) {
                        continue;
                    }

                    final SantopiaClaim claim = chunk.claim();

                    if (claim.guildId().equals(guildId)) {
                        chunk.claim(null);
                    }
                }
            }
        }
    }

}
