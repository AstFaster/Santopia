package fr.astfaster.santopia.api.messaging;

import com.google.gson.*;
import fr.astfaster.santopia.api.SantopiaAPI;

import java.lang.reflect.Type;

public class PacketsCodec {

    private static final String PACKET_SERIALIZER = "messaging-codec";

    private final MessagingService messaging;

    public PacketsCodec(MessagingService messaging) {
        this.messaging = messaging;

        SantopiaAPI.instance().jsonSerializers().add(PACKET_SERIALIZER, new GsonBuilder()
                .registerTypeHierarchyAdapter(SantopiaPacket.class, new JsonAdapter()));
    }

    public String encode(SantopiaPacket packet) {
        return this.serializer().toJson(packet);
    }

    public SantopiaPacket decode(String encoded) {
        return this.serializer().fromJson(encoded, SantopiaPacket.class);
    }

    private Gson serializer() {
        return SantopiaAPI.instance().jsonSerializers().get(PACKET_SERIALIZER);
    }

    private class JsonAdapter implements JsonSerializer<SantopiaPacket>, JsonDeserializer<SantopiaPacket> {

        @Override
        public JsonElement serialize(SantopiaPacket packet, Type typeOfSrc, JsonSerializationContext ctx) {
            final JsonObject result = new JsonObject();
            final JsonElement body = SantopiaAPI.instance().jsonSerializers().getDefault().toJsonTree(packet);

            result.addProperty("id", messaging.packetId(packet.getClass()));
            result.add("body", body);

            return result;
        }

        @Override
        public SantopiaPacket deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
            final JsonObject object = json.getAsJsonObject();
            final int packetId = object.get("id").getAsInt();
            final JsonElement body = object.get("body");

            return SantopiaAPI.instance().jsonSerializers().getDefault().fromJson(body, messaging.packetClass(packetId));
        }

    }

}
