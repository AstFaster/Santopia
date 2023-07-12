package fr.astfaster.santopia.api.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.astfaster.santopia.api.SantopiaException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class JsonSerializers {

    public static final String DEFAULT = "default";

    private final Map<String, GsonBuilder> builders = new HashMap<>();
    private final Map<String, Gson> instances = new HashMap<>();

    public JsonSerializers() {
        this.add(DEFAULT, new GsonBuilder());
    }

    public void add(String id, GsonBuilder builder) {
        this.builders.put(id, builder);
        this.instances.put(id, builder.create());
    }

    public void edit(String id, Consumer<GsonBuilder> builderConsumer) {
        final GsonBuilder builder = this.builders.get(id);

        if (builder == null) {
            throw new SantopiaException("Couldn't find a serializer with the id: " + id);
        }

        builderConsumer.accept(builder);
    }

    public Gson get(String id) {
        return this.instances.get(id);
    }

    public Gson getDefault() {
        return this.get(DEFAULT);
    }

}
