package fr.astfaster.santopia.api.guild.claim;

import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class ClaimsCache {

    /** World -> X position -> Z position -> Claim */
    private final Map<String, Map<Integer, Map<Integer, SantopiaClaim>>> handle = new ConcurrentHashMap<>();

    public SantopiaClaim get(String world, int x, int z) {
        final Map<Integer, Map<Integer, SantopiaClaim>> claims = this.handle.get(world);

        if (claims == null) {
            return null;
        }

        final Map<Integer, SantopiaClaim> zClaims = claims.get(x);

        if (zClaims == null) {
            return null;
        }
        return zClaims.get(z);
    }

    public void add(SantopiaClaim claim) {
        this.handle.merge(claim.world(), new ConcurrentHashMap<>(), (oldValue, newValue) -> oldValue)
                .merge(claim.x(), new ConcurrentHashMap<>(), (oldValue, newValue) -> oldValue)
                .put(claim.z(), claim);
    }

    public void remove(SantopiaClaim claim) {
        Optional.ofNullable(this.handle.get(claim.world()))
                .flatMap(claims -> Optional.ofNullable(claims.get(claim.x())))
                .ifPresent(zClaims -> zClaims.remove(claim.z()));
    }

    public boolean contains(String world, int x, int z) {
        return this.get(world, x, z) != null;
    }

    public void clear(ObjectId guildId) {
        for (Map<Integer, Map<Integer, SantopiaClaim>> claims : this.handle.values()) {
            for (Map<Integer, SantopiaClaim> zClaims : claims.values()) {
                for (Map.Entry<Integer, SantopiaClaim> entry : zClaims.entrySet()) {
                    if (entry.getValue().guildId().equals(guildId)) {
                        zClaims.remove(entry.getKey());
                    }
                }
            }
        }
    }

}
