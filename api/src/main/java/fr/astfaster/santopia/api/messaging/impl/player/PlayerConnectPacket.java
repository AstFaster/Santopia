package fr.astfaster.santopia.api.messaging.impl.player;

import fr.astfaster.santopia.api.messaging.SantopiaPacket;
import fr.astfaster.santopia.api.server.ServerType;

import java.util.UUID;

public record PlayerConnectPacket(UUID player, ServerType server) implements SantopiaPacket {}
