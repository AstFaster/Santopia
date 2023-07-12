package fr.astfaster.santopia.api.player;

import com.google.gson.annotations.Expose;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.serializer.DataInput;
import fr.astfaster.santopia.api.serializer.DataOutput;
import fr.astfaster.santopia.api.serializer.DataSerializable;
import fr.astfaster.santopia.api.server.ServerType;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SantopiaSession implements DataSerializable {

    @Expose
    private UUID playerId;

    @Expose
    private ServerType currentServer;
    @Expose
    private boolean moderating = false;

    public SantopiaSession(UUID playerId, ServerType currentServer) {
        this.playerId = playerId;
        this.currentServer = currentServer;
    }

    SantopiaSession() {}

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeUUID(this.playerId);
        output.writeString(this.currentServer != null ? this.currentServer.name() : null);
        output.writeBoolean(this.moderating);
    }

    @Override
    public void readData(DataInput input) throws IOException {
        this.playerId = input.readUUID();

        final String currentServerStr = input.readString();

        this.currentServer = currentServerStr != null ? ServerType.valueOf(currentServerStr) : null;

        this.moderating = input.readBoolean();
    }

    public UUID playerId() {
        return this.playerId;
    }

    public ServerType currentServer() {
        return this.currentServer;
    }

    public void currentServer(ServerType currentServer) {
        this.currentServer = currentServer;
    }

    public boolean moderating() {
        return this.moderating;
    }

    public void moderating(boolean moderating) {
        this.moderating = moderating;
    }

    public CompletableFuture<Void> update() {
        return SantopiaAPI.instance().playersService().updateSession(this);
    }

}
