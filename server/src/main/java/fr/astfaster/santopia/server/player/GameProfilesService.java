package fr.astfaster.santopia.server.player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import fr.astfaster.santopia.api.SantopiaAPI;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class GameProfilesService {

    private final Supplier<Gson> serializer = () -> SantopiaAPI.instance().jsonSerializers().get("game-profiles");

    public GameProfilesService() {
        SantopiaAPI.instance().jsonSerializers().add("game-profiles", new GsonBuilder()
                .registerTypeHierarchyAdapter(PropertyMap.class, new PropertyMap.Serializer()));
    }

    public CompletableFuture<GameProfile> loadProfile(UUID uuid) {
        return SantopiaAPI.instance().newFuture(() -> {
            final String json = SantopiaAPI.instance().redis().get(jedis -> jedis.get("game-profiles:" + uuid.toString()));

            GameProfile profile;
            if (json != null) {
                profile = this.serializer.get().fromJson(json, GameProfile.class);
            } else {
                profile = MinecraftServer.getServer().am().fillProfileProperties(new GameProfile(uuid, null), true);

                if (profile != null) {
                    this.saveProfile(profile).join();
                }
            }
            return profile;
        });
    }

    @SuppressWarnings("deprecation")
    public CompletableFuture<GameProfile> loadProfile(String name) {
        return SantopiaAPI.instance()
                .newFuture(() -> Bukkit.getOfflinePlayer(name).getUniqueId())
                .thenApplyAsync(uuid -> this.loadProfile(uuid).join());
    }

    public CompletableFuture<Void> saveProfile(GameProfile profile) {
        return SantopiaAPI.instance().newFuture(() -> {
            SantopiaAPI.instance()
                    .redis()
                    .process(jedis -> jedis.set("game-profiles:" + profile.getId().toString(), this.serializer.get().toJson(profile)));
            return null;
        });
    }

}
