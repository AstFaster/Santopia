package fr.astfaster.santopia.api.messaging.impl.guild;

import fr.astfaster.santopia.api.messaging.SantopiaPacket;
import org.bson.types.ObjectId;

public record GuildPrefixColorChangePacket(ObjectId guildId) implements SantopiaPacket {}
