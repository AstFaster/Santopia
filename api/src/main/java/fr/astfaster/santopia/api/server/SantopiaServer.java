package fr.astfaster.santopia.api.server;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.messaging.PacketsChannel;
import fr.astfaster.santopia.api.messaging.impl.server.ServerUpdatePlayersPacket;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class SantopiaServer {

    public static final long HEARTBEAT_FREQUENCY = 10 * 1000L; // Send a heartbeat every 10 seconds
    public static final long TIMED_OUT = 20 * 1000L; // 20 seconds to wait before timed out

    private final ServerType type;

    private final Set<UUID> players = new HashSet<>();
    private final ReentrantReadWriteLock playersLock = new ReentrantReadWriteLock();

    private long lastHeartbeat;

    public SantopiaServer(ServerType type) {
        this.type = type;
    }

    public ServerType type() {
        return this.type;
    }

    public String display() {
        return this.type.display();
    }

    public void addPlayer(UUID player) {
        this.playersLock.writeLock().lock();

        try {
            this.players.add(player);

            this.playersPacket();
        } finally {
            this.playersLock.writeLock().unlock();
        }
    }

    public void removePlayer(UUID player) {
        this.playersLock.writeLock().lock();

        try {
            this.players.remove(player);

            this.playersPacket();
        } finally {
            this.playersLock.writeLock().unlock();
        }
    }

    private void playersPacket() {
        SantopiaAPI.instance().messagingService().send(PacketsChannel.SERVERS, new ServerUpdatePlayersPacket(this.type, this.players));
    }

    public Set<UUID> players() {
        this.playersLock.readLock().lock();

        try {
            return Collections.unmodifiableSet(this.players);
        } finally {
            this.playersLock.readLock().unlock();
        }
    }

    void players(Set<UUID> players) {
        this.playersLock.writeLock().lock();

        try {
            this.players.clear();
            this.players.addAll(players);
        } finally {
            this.playersLock.writeLock().unlock();
        }
    }

    public long lastHeartbeat() {
        return this.lastHeartbeat;
    }

    void lastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public boolean reachable() {
        return System.currentTimeMillis() - this.lastHeartbeat <= TIMED_OUT;
    }

    @Override
    public String toString() {
        return this.type.name();
    }

}
