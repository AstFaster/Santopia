package fr.astfaster.santopia.api.database.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.database.DatabaseConnection;
import org.bson.conversions.Bson;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MongoDB implements DatabaseConnection {

    private final MongoSerializer serializer = new MongoSerializer();

    private boolean connected;
    private MongoClient client;

    @Override
    public void connect() {
        final ConnectionString connectionString = new ConnectionString(SantopiaAPI.instance().config().mongoDB().toURL());
        final MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToSocketSettings(builder -> builder.connectTimeout(2000, TimeUnit.MILLISECONDS))
                .retryWrites(true)
                .build();

        this.client = MongoClients.create(settings);
        this.connected = true;
    }

    @Override
    public void stop() {
        this.client.close();
        this.connected = false;
    }

    @Override
    public boolean connected() {
        return this.connected;
    }

    public MongoDatabase database(String database) {
        return this.client.getDatabase(database);
    }

    public MongoDatabase defaultDatabase() {
        return this.client.getDatabase("santopia");
    }

    public MongoSerializer serializer() {
        return this.serializer;
    }

    public static Bson eqIgn(String fieldName, String value) {
        return Filters.regex(fieldName, Pattern.compile("(?i)^" + value + "$", Pattern.CASE_INSENSITIVE));
    }

    public static Bson notEqIgn(String fieldName, String value) {
        return Filters.not(eqIgn(fieldName, value));
    }

}
