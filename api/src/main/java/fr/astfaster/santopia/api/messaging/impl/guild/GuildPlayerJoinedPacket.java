package fr.astfaster.santopia.api.messaging.impl.guild;

import fr.astfaster.santopia.api.messaging.SantopiaPacket;
import org.bson.types.ObjectId;

import java.util.UUID;

public record GuildPlayerJoinedPacket(UUID playerId, ObjectId guildId) implements SantopiaPacket {}
