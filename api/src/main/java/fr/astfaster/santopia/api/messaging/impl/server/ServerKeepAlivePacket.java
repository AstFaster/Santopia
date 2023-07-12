package fr.astfaster.santopia.api.messaging.impl.server;

import fr.astfaster.santopia.api.messaging.SantopiaPacket;
import fr.astfaster.santopia.api.server.ServerType;

public record ServerKeepAlivePacket(ServerType server, long time) implements SantopiaPacket {}