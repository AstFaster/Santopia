package fr.astfaster.santopia.api.guild;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.database.mongodb.MongoDB;
import fr.astfaster.santopia.api.database.mongodb.MongoSerializer;
import fr.astfaster.santopia.api.messaging.PacketsChannel;
import fr.astfaster.santopia.api.messaging.impl.guild.GuildRequestPacket;
import fr.astfaster.santopia.api.player.SantopiaAccount;
import fr.astfaster.santopia.api.serializer.DataSerializer;
import org.bson.Document;
import org.bson.types.ObjectId;
import redis.clients.jedis.Pipeline;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GuildsService {

    /** Guilds live 12 hours in Redis cache */
    private static final long TTL = TimeUnit.HOURS.toSeconds(12);

    private final MongoSerializer documentSerializer = SantopiaAPI.instance().mongoDB().serializer();
    private final DataSerializer dataSerializer = SantopiaAPI.instance().dataSerializer();

    /** The MongoDB collection for guilds */
    private final MongoCollection<Document> collection;

    public GuildsService() {
        this.collection = SantopiaAPI.instance().mongoDB().defaultDatabase().getCollection("guilds");
    }

    void updateGuildName(SantopiaGuild guild, String newName, String oldName) {
        SantopiaAPI.instance().redis().process(jedis -> {
            final Pipeline pipeline = jedis.pipelined();

            pipeline.del("guilds:name:" + oldName.toLowerCase());
            pipeline.set("guilds:name:" + newName.toLowerCase(), guild.id().toHexString());
            pipeline.sync();
        });
    }

    void updateGuildPrefix(SantopiaGuild guild, String newPrefix, String oldPrefix) {
        SantopiaAPI.instance().redis().process(jedis -> {
            final Pipeline pipeline = jedis.pipelined();

            pipeline.del("guilds:prefix:" + oldPrefix);
            pipeline.set("guilds:prefix:" + newPrefix, guild.id().toHexString());
            pipeline.sync();
        });
    }

    void removeMember(UUID playerId) {
        SantopiaAPI.instance().redis().process(jedis -> jedis.del("players:guild", playerId.toString()));

        final SantopiaAccount account = SantopiaAPI.instance().playersService().account(playerId).join();

        account.guild(null);
        account.update();
    }

    public CompletableFuture<SantopiaGuild> createGuild(String name, String prefix, UUID leader) {
        return SantopiaAPI.instance().newFuture(() -> {
            final SantopiaGuild guild = new SantopiaGuild(name, prefix, leader);

            this.cacheGuild(guild);
            this.collection.insertOne(this.documentSerializer.serialize(guild));

            return guild;
        });
    }

    public CompletableFuture<SantopiaGuild> guild(ObjectId guildId) {
        return SantopiaAPI.instance().newFuture(() -> {
            // Load it from Redis
            SantopiaGuild guild = SantopiaAPI.instance().redis().get(jedis -> {
                final byte[] key = ("guilds:" + guildId.toHexString()).getBytes(StandardCharsets.UTF_8);
                final byte[] bytes = jedis.get(key);

                if (bytes == null) {
                    return null;
                }
                return this.dataSerializer.deserialize(new SantopiaGuild(), bytes);
            });

            if (guild != null) {
                return guild;
            }

            // Load it from MongoDB
            final Document document = this.collection.find(Filters.eq("_id", guildId)).first();

            if (document == null) {
                return null;
            }

            guild = this.documentSerializer.deserialize(document, SantopiaGuild.class);

            // Save it in Redis for next time
            this.cacheGuild(guild);

            return guild;
        });
    }

    public CompletableFuture<SantopiaGuild> guildByName(String name) {
        return SantopiaAPI.instance().newFuture(() -> {
            // Load it from Redis
            String guildId = SantopiaAPI.instance().redis().get(jedis -> jedis.get("guilds:name:" + name.toLowerCase()));

            if (guildId != null) {
                return this.guild(new ObjectId(guildId)).join();
            }

            // Load it from MongoDB
            final Document document = this.collection.find(MongoDB.eqIgn("name", name)).first();

            if (document == null) {
                return null;
            }

            final SantopiaGuild guild = this.documentSerializer.deserialize(document, SantopiaGuild.class);

            // Save index in Redis for next time
            SantopiaAPI.instance().redis().process(jedis -> jedis.set("guilds:name:" + name.toLowerCase(), guild.id().toHexString()));

            return guild;
        });
    }

    public CompletableFuture<SantopiaGuild> guildByPrefix(String prefix) {
        return SantopiaAPI.instance().newFuture(() -> {
            // Load it from Redis
            String guildId = SantopiaAPI.instance().redis().get(jedis -> jedis.get("guilds:prefix:" + prefix));

            if (guildId != null) {
                return this.guild(new ObjectId(guildId)).join();
            }

            // Load it from MongoDB
            final Document document = this.collection.find(MongoDB.eqIgn("prefix", prefix)).first();

            if (document == null) {
                return null;
            }

            final SantopiaGuild guild = this.documentSerializer.deserialize(document, SantopiaGuild.class);

            // Save index in Redis for next time
            SantopiaAPI.instance().redis().process(jedis -> jedis.set("guilds:prefix:" + prefix, guild.id().toHexString()));

            return guild;
        });
    }

    CompletableFuture<Void> updateGuild(SantopiaGuild guild) {
        return SantopiaAPI.instance().newFuture(() -> {
            this.cacheGuild(guild);

            this.collection.replaceOne(Filters.eq("_id", guild.id()), this.documentSerializer.serialize(guild));
            return null;
        });
    }

    private void cacheGuild(SantopiaGuild guild) {
        SantopiaAPI.instance().redis().process(jedis -> {
            final byte[] bytes = this.dataSerializer.serialize(guild);
            final byte[] key = ("guilds:" + guild.id().toHexString()).getBytes(StandardCharsets.UTF_8);
            final Pipeline pipeline = jedis.pipelined();

            pipeline.set(key, bytes);
            pipeline.expire(key, TTL);
            pipeline.sync();
        });
    }

    public CompletableFuture<Void> removeGuild(SantopiaGuild guild) {
        return SantopiaAPI.instance().newFuture(() -> {
            // Remove guild + secondary indexes from Redis
            SantopiaAPI.instance().redis().process(jedis -> {
                final Pipeline pipeline = jedis.pipelined();

                pipeline.del("guilds:" + guild.id().toHexString());
                pipeline.del("guilds:name:" + guild.name().toLowerCase());
                pipeline.del("guilds:prefix:" + guild.prefix());

                for (GuildMember member : guild.members()) {
                    pipeline.del("players:guild", member.uuid().toString());
                }

                pipeline.sync();
            });

            // Remove guild from MongoDB
            this.collection.deleteOne(Filters.eq("_id", guild.id()));

            // Set members' guild as null
            for (GuildMember member : guild.members()) {
                SantopiaAPI.instance().playersService().account(member.uuid()).thenAcceptAsync(account -> {
                    account.guild(null);
                    account.update();
                });
            }

            // Delete claims
            SantopiaAPI.instance().claimsService().clearClaims(guild.id()).join();
            return null;
        });
    }

    public CompletableFuture<SantopiaGuild> playerGuild(UUID playerId) {
        return SantopiaAPI.instance().newFuture(() -> {
            // Try to fetch id from Redis
            final String guildId = SantopiaAPI.instance().redis().get(jedis -> jedis.hget("players:guild", playerId.toString()));

            if (guildId != null) {
                return this.guild(new ObjectId(guildId)).join();
            }

            // Fetch guild from MongoDB
            final Document document = this.collection.find(new Document("members", new Document("$elemMatch", new Document("_id", playerId.toString())))).first();

            if (document == null) {
                return null;
            }

            final SantopiaGuild guild = this.documentSerializer.deserialize(document, SantopiaGuild.class);

            // Save index in Redis for next time
            SantopiaAPI.instance().redis().process(jedis -> jedis.hset("players:guild", playerId.toString(), guild.id().toHexString()));

            return guild;
        });
    }

    public void sendRequest(UUID senderId, UUID targetId, ObjectId guildId) {
        SantopiaAPI.instance().executorService().execute(() -> {
            SantopiaAPI.instance().redis().process(jedis -> {
                final Pipeline pipeline = jedis.pipelined();
                final String key = "guilds:requests:" + targetId.toString() + ":" + senderId.toString();

                pipeline.set(key, guildId.toHexString());
                pipeline.expire(key, 60);
                pipeline.sync();
            });

            SantopiaAPI.instance().messagingService().send(PacketsChannel.GUILDS, new GuildRequestPacket(senderId, targetId, guildId));
        });
    }

    public CompletableFuture<Boolean> hasRequest(UUID senderId, UUID targetId) {
        return SantopiaAPI.instance().newFuture(() -> SantopiaAPI.instance().redis().get(jedis -> jedis.exists("guilds:requests:" + targetId.toString() + ":" + senderId.toString())));
    }

    public CompletableFuture<ObjectId> request(UUID senderId, UUID targetId) {
        return SantopiaAPI.instance().newFuture(() -> SantopiaAPI.instance().redis().get(jedis -> {
            final String idStr = jedis.get("guilds:requests:" + targetId.toString() + ":" + senderId.toString());

            return idStr != null ? new ObjectId(idStr) : null;
        }));
    }

    public void removeRequest(UUID senderId, UUID targetId) {
        SantopiaAPI.instance().executorService().execute(() -> SantopiaAPI.instance().redis().process(jedis -> jedis.del("guilds:requests:" + targetId.toString() + ":" + senderId.toString())));
    }

}
