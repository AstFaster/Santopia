package fr.astfaster.santopia.api.database.mongodb;

import com.google.gson.*;
import com.google.gson.annotations.Expose;
import com.mongodb.util.JSON;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.guild.SantopiaGuild;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonReader;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;
import java.util.UUID;
import java.util.function.Supplier;

public class MongoSerializer {

    private final Supplier<Gson> jsonSerializer = () -> SantopiaAPI.instance().jsonSerializers().get("mongodb");

    public MongoSerializer() {
        SantopiaAPI.instance().jsonSerializers().add("mongodb", new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .serializeNulls()
                .registerTypeHierarchyAdapter(ObjectId.class, new ObjectIdSerializer()));
    }

    public Document serialize(Object object) {
        return Document.parse(this.jsonSerializer.get().toJson(object));
    }

    public <T> T deserialize(Document document, Class<T> output) {
        return this.jsonSerializer.get().fromJson(document.toJson(JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build()), output);
    }

    private static class ObjectIdSerializer implements JsonSerializer<ObjectId>, JsonDeserializer<ObjectId> {

        @Override
        public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
            final JsonObject object = new JsonObject();

            object.addProperty("$oid", src.toHexString());

            return object;
        }

        @Override
        public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new ObjectId(json.getAsJsonObject().get("$oid").getAsString());
        }
    }

}
