package fr.astfaster.santopia.api.messaging.impl.server;

import fr.astfaster.santopia.api.messaging.SantopiaPacket;
import fr.astfaster.santopia.api.server.ServerType;

import java.util.Set;
import java.util.UUID;

public record ServerUpdatePlayersPacket(ServerType server, Set<UUID> players) implements SantopiaPacket {}
