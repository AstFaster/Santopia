package fr.astfaster.santopia.server.config;

import fr.astfaster.santopia.api.config.Config;
import fr.astfaster.santopia.api.server.ServerType;
import fr.astfaster.santopia.server.util.SimpleLocation;

import java.util.Map;

public class ServerConfig implements Config {

    private MongoDB mongoDB = new MongoDB();
    private Redis redis = new Redis();
    private RabbitMQ rabbitMQ = new RabbitMQ();

    private String discordURL;

    private ServerType server;
    private SimpleLocation spawn;

    public ServerConfig() {}

    public ServerConfig(MongoDB mongoDB, Redis redis, RabbitMQ rabbitMQ, String discordURL, ServerType server, SimpleLocation spawn) {
        this.mongoDB = mongoDB;
        this.redis = redis;
        this.rabbitMQ = rabbitMQ;
        this.discordURL = discordURL;
        this.server = server;
        this.spawn = spawn;
    }

    @Override
    public MongoDB mongoDB() {
        return this.mongoDB;
    }

    @Override
    public Redis redis() {
        return this.redis;
    }

    @Override
    public RabbitMQ rabbitMQ() {
        return this.rabbitMQ;
    }

    @Override
    public String discordURL() {
        return this.discordURL;
    }

    public ServerType server() {
        return this.server;
    }

    public SimpleLocation spawn() {
        return this.spawn;
    }

}
