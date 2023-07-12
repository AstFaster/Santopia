package fr.astfaster.santopia.api.player;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.serializer.DataInput;
import fr.astfaster.santopia.api.serializer.DataOutput;
import fr.astfaster.santopia.api.serializer.DataSerializable;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SantopiaAccount implements DataSerializable {

    @Expose
    @SerializedName("_id")
    private UUID uuid;

    @Expose
    private String name;

    @Expose
    private long firstLogin;
    @Expose
    private long lastLogin;

    @Expose
    private long playTime;

    @Expose
    private ObjectId guild = null;

    @Expose
    private SantopiaHome home = null;

    public SantopiaAccount(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.firstLogin = System.currentTimeMillis();
    }

    SantopiaAccount() {}

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeUUID(this.uuid);
        output.writeString(this.name);
        output.writeLong(this.firstLogin);
        output.writeLong(this.lastLogin);
        output.writeLong(this.playTime);
        output.writeString(this.guild != null ? this.guild.toHexString() : null);

        output.writeBoolean(this.home != null);

        if (this.home != null) {
            this.home.writeData(output);
        }
    }

    @Override
    public void readData(DataInput input) throws IOException {
        this.uuid = input.readUUID();
        this.name = input.readString();
        this.firstLogin = input.readLong();
        this.lastLogin = input.readLong();
        this.playTime = input.readLong();

        final String guildId = input.readString();

        this.guild = guildId != null ? new ObjectId(guildId) : null;

        if (input.readBoolean()) {
            this.home = new SantopiaHome();
            this.home.readData(input);
        }
    }

    public UUID uuid() {
        return this.uuid;
    }

    public String name() {
        return this.name;
    }

    public void name(String name) {
        this.name = name;
    }

    public long firstLogin() {
        return this.firstLogin;
    }

    public long lastLogin() {
        return this.lastLogin;
    }

    public void lastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public long playTime() {
        return this.playTime;
    }

    public void addPlayTime(long playTime) {
        this.playTime += playTime;
    }

    public Rank rank() {
        return SantopiaAPI.instance().playersService().playerRank(this.uuid);
    }

    public ObjectId guild() {
        return this.guild;
    }

    public void guild(ObjectId guild) {
        this.guild = guild;
    }

    public SantopiaHome home() {
        return this.home;
    }

    public void home(SantopiaHome home) {
        this.home = home;
    }

    public CompletableFuture<Void> update() {
        return SantopiaAPI.instance().playersService().updateAccount(this);
    }

}