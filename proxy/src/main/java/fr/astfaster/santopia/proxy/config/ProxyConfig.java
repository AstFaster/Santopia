package fr.astfaster.santopia.proxy.config;

import fr.astfaster.santopia.api.config.Config;
import fr.astfaster.santopia.api.server.ServerType;

import java.util.HashMap;
import java.util.Map;

public class ProxyConfig implements Config {

    private final Map<ServerType, Server> servers = new HashMap<>();

    private MongoDB mongoDB = new MongoDB();
    private RabbitMQ rabbitMQ = new RabbitMQ();
    private Redis redis = new Redis();

    private String discordURL;
    private String resourcePackURL;
    private String favicon;

    public ProxyConfig() {
        for (ServerType serverType : ServerType.values()) {
            this.servers.put(serverType, new Server("localhost", 25565));
        }
    }

    public ProxyConfig(MongoDB mongoDB, RabbitMQ rabbitMQ, Redis redis, String discordURL, String resourcePackURL, String favicon) {
        this.mongoDB = mongoDB;
        this.rabbitMQ = rabbitMQ;
        this.redis = redis;
        this.discordURL = discordURL;
        this.resourcePackURL = resourcePackURL;
        this.favicon = favicon;
    }

    public Map<ServerType, Server> servers() {
        return this.servers;
    }

    @Override
    public MongoDB mongoDB() {
        return this.mongoDB;
    }

    @Override
    public RabbitMQ rabbitMQ() {
        return this.rabbitMQ;
    }

    @Override
    public Redis redis() {
        return this.redis;
    }

    @Override
    public String discordURL() {
        return this.discordURL;
    }

    public String resourcePackURL() {
        return this.resourcePackURL;
    }

    public String favicon() {
        return this.favicon;
    }

    public static class Server {

        protected String hostname;
        protected int port;

        public Server() {
            this("localhost", 25565);
        }

        public Server(String hostname, int port) {
            this.hostname = hostname;
            this.port = port;
        }

        public String hostname() {
            return this.hostname;
        }

        public int port() {
            return this.port;
        }

    }

}
