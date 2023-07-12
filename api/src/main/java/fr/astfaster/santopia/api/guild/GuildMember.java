package fr.astfaster.santopia.api.guild;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import fr.astfaster.santopia.api.serializer.DataInput;
import fr.astfaster.santopia.api.serializer.DataOutput;
import fr.astfaster.santopia.api.serializer.DataSerializable;

import java.io.IOException;
import java.util.UUID;

public final class GuildMember implements DataSerializable {

    @Expose
    @SerializedName("_id")
    private UUID uuid;

    @Expose
    private GuildRole role;

    @Expose
    private long joinedDate;

    public GuildMember(UUID uuid, GuildRole role) {
        this.uuid = uuid;
        this.role = role;
        this.joinedDate = System.currentTimeMillis();
    }

    GuildMember() {}

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeUUID(this.uuid);
        output.writeInt(this.role.id());
        output.writeLong(this.joinedDate);
    }

    @Override
    public void readData(DataInput input) throws IOException {
        this.uuid = input.readUUID();
        this.role = GuildRole.byId(input.readInt());
        this.joinedDate = input.readLong();
    }

    public UUID uuid() {
        return this.uuid;
    }

    public GuildRole role() {
        return this.role;
    }

    public void role(GuildRole role) {
        this.role = role;
    }

    public long joinedDate() {
        return this.joinedDate;
    }

}
