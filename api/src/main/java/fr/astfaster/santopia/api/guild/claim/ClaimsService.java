package fr.astfaster.santopia.api.guild.claim;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.database.mongodb.MongoSerializer;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.concurrent.CompletableFuture;

public class ClaimsService {

    private final MongoSerializer documentSerializer = SantopiaAPI.instance().mongoDB().serializer();

    private final ClaimsCache cache = new ClaimsCache();

    /** The MongoDB collection for guilds */
    private final MongoCollection<Document> collection;

    public ClaimsService() {
        this.collection = SantopiaAPI.instance().mongoDB().defaultDatabase().getCollection("claims");
    }

    public CompletableFuture<Void> saveClaim(SantopiaClaim claim) {
        return SantopiaAPI.instance().newFuture(() -> {
            // Save it in local cache
            this.cache.add(claim);
            // Save it in MongoDB
            this.collection.insertOne(this.documentSerializer.serialize(claim));

            return null;
        });
    }

    public CompletableFuture<Void> deleteClaim(SantopiaClaim claim) {
        return SantopiaAPI.instance().newFuture(() -> {
            // Save it in local cache
            this.cache.remove(claim);
            // Save it in MongoDB
            this.collection.deleteOne(Filters.eq("_id", claim.id()));

            return null;
        });
    }

    public CompletableFuture<Void> clearClaims(ObjectId guildId) {
        return SantopiaAPI.instance().newFuture(() -> {
            this.cache.clear(guildId);
            this.collection.deleteMany(Filters.eq("guildId", guildId));
            return null;
        });
    }

    public CompletableFuture<SantopiaClaim> loadClaim(String world, int x, int z) {
        return SantopiaAPI.instance().newFuture(() -> {
            // Load it from local cache
            SantopiaClaim claim = this.cache.get(world, x, z);

            if (claim != null) {
                return claim;
            }

            // Load it from MongoDB
            final Document document = this.collection.find(Filters.and(Filters.eq("world", world), Filters.eq("x", x), Filters.eq("z", z))).first();

            if (document == null) {
                return null;
            }

            claim = this.documentSerializer.deserialize(document, SantopiaClaim.class);

            // Save it in local cache for next time
            this.cache.add(claim);

            return claim;
        });
    }

    public SantopiaClaim loadedClaim(String world, int x, int z) {
        return this.cache.get(world, x, z);
    }

    public boolean isChunkLoaded(String world, int x, int z) {
        return this.cache.contains(world, x, z);
    }

}
