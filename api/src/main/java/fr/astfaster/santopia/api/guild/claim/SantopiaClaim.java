package fr.astfaster.santopia.api.guild.claim;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bson.types.ObjectId;

public final class SantopiaClaim {

    @Expose
    @SerializedName("_id")
    private ObjectId id;
    @Expose
    private ObjectId guildId;

    @Expose
    private String world;
    @Expose
    private int x;
    @Expose
    private int z;

    public SantopiaClaim(ObjectId guildId, String world, int x, int z) {
        this.world = world;
        this.id = new ObjectId();
        this.guildId = guildId;
        this.x = x;
        this.z = z;
    }

    SantopiaClaim() {}

    public ObjectId id() {
        return this.id;
    }

    public ObjectId guildId() {
        return this.guildId;
    }

    public String world() {
        return this.world;
    }

    public int x() {
        return this.x;
    }

    public int z() {
        return this.z;
    }

}
