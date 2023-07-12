package fr.astfaster.santopia.api.player;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.database.mongodb.MongoSerializer;
import fr.astfaster.santopia.api.messaging.PacketsChannel;
import fr.astfaster.santopia.api.messaging.impl.player.PlayerConnectPacket;
import fr.astfaster.santopia.api.serializer.DataSerializer;
import fr.astfaster.santopia.api.server.ServerType;
import net.luckperms.api.LuckPermsProvider;
import org.bson.Document;
import redis.clients.jedis.Pipeline;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayersService {

    /** Accounts live 12 hours in Redis cache */
    private static final long ACCOUNTS_TTL = TimeUnit.HOURS.toSeconds(12);

    private final MongoSerializer documentSerializer = SantopiaAPI.instance().mongoDB().serializer();
    private final DataSerializer dataSerializer = SantopiaAPI.instance().dataSerializer();

    /** The MongoDB collection for accounts */
    private final MongoCollection<Document> accountsCollection;

    public PlayersService() {
        this.accountsCollection = SantopiaAPI.instance().mongoDB().defaultDatabase().getCollection("accounts");
    }

    public void connectPlayer(UUID playerId, ServerType server) {
        SantopiaAPI.instance().messagingService().send(PacketsChannel.PLAYERS, new PlayerConnectPacket(playerId, server));
    }

    /**
     * Create an account for a new player
     *
     * @param playerId The {@link UUID} of the new player
     * @param playerName The name of the new player
     * @return The created {@linkplain SantopiaAccount account}
     */
    public SantopiaAccount createAccount(UUID playerId, String playerName) {
        final SantopiaAccount account = new SantopiaAccount(playerId, playerName);

        this.accountsCollection.insertOne(this.documentSerializer.serialize(account));
        this.cacheAccount(account);

        return account;
    }

    /**
     * It first tries to load the account from Redis, and if it's not in Redis, it will load it from MongoDB.
     *
     * @param playerId The {@link UUID} of the player's account to get
     * @return A {@linkplain CompletableFuture future} returning the player's account
     */
    public CompletableFuture<SantopiaAccount> account(UUID playerId) {
        return SantopiaAPI.instance().newFuture(() -> {
            // Load it from Redis
            SantopiaAccount account = SantopiaAPI.instance().redis().get(jedis -> {
                final byte[] key = ("players:accounts:" + playerId.toString()).getBytes(StandardCharsets.UTF_8);
                final byte[] bytes = jedis.get(key);

                if (bytes == null) {
                    return null;
                }
                return this.dataSerializer.deserialize(new SantopiaAccount(), bytes);
            });

            if (account != null) {
                return account;
            }

            // Load it from MongoDB
            final Document document = this.accountsCollection.find(Filters.eq("_id", playerId.toString())).first();

            if (document == null) {
                return null;
            }

            account = this.documentSerializer.deserialize(document, SantopiaAccount.class);

            // Save it in Redis for next time
            this.cacheAccount(account);

            return account;
        });
    }

    /**
     * Update an account in MongoDB
     *
     * @param account The {@linkplain SantopiaAccount account} to update
     * @return A {@linkplain CompletableFuture future} of the updating process
     */
    public CompletableFuture<Void> updateAccount(SantopiaAccount account) {
        return SantopiaAPI.instance().newFuture(() -> {
            this.cacheAccount(account);

            this.accountsCollection.replaceOne(Filters.eq("_id", account.uuid().toString()), this.documentSerializer.serialize(account));
            return null;
        });
    }

    private void cacheAccount(SantopiaAccount account) {
        SantopiaAPI.instance().redis().process(jedis -> {
            final byte[] bytes = this.dataSerializer.serialize(account);
            final byte[] key = ("players:accounts:" + account.uuid().toString()).getBytes(StandardCharsets.UTF_8);
            final Pipeline pipeline = jedis.pipelined();

            pipeline.set(key, bytes);
            pipeline.expire(key, ACCOUNTS_TTL);
            pipeline.sync();
        });
    }

    /**
     * It loads the session from Redis.
     *
     * @param playerId The {@link UUID} of the player's session to get
     * @return A {@linkplain CompletableFuture future} returning the player's session
     */
    public CompletableFuture<SantopiaSession> session(UUID playerId) {
        return SantopiaAPI.instance().newFuture(() -> SantopiaAPI.instance().redis().get(jedis -> {
            final byte[] key = ("players:sessions:" + playerId.toString()).getBytes(StandardCharsets.UTF_8);
            final byte[] bytes = jedis.get(key);

            if (bytes == null) {
                return null;
            }
            return this.dataSerializer.deserialize(new SantopiaSession(), bytes);
        }));
    }

    /**
     * Destroy a session from Redis
     *
     * @param playerId The {@link UUID} of the player's session to destroy
     * @return A {@linkplain CompletableFuture future} of the updating process
     */
    public CompletableFuture<Void> destroySession(UUID playerId) {
        return SantopiaAPI.instance().newFuture(() -> {
            SantopiaAPI.instance().redis().process(jedis -> jedis.del("players:sessions:" + playerId.toString()));
            return null;
        });
    }

    /**
     * Update a session in Redis
     *
     * @param session The {@linkplain SantopiaSession session} to update
     * @return A {@linkplain CompletableFuture future} of the updating process
     */
    public CompletableFuture<Void> updateSession(SantopiaSession session) {
        return SantopiaAPI.instance().newFuture(() -> {
            SantopiaAPI.instance().redis().process(jedis -> jedis.set(("players:sessions:" + session.playerId().toString()).getBytes(StandardCharsets.UTF_8), this.dataSerializer.serialize(session)));
            return null;
        });
    }

    public Rank playerRank(UUID playerId) {
        return Rank.of(Objects.requireNonNull(LuckPermsProvider.get().getUserManager().getUser(playerId)).getPrimaryGroup());
    }

    public String rankPrefix(UUID playerId) {
        return LuckPermsProvider.get().getUserManager().getUser(playerId).getCachedData().getMetaData().getPrefix();
    }

}
