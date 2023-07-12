package fr.astfaster.santopia.api.database.redis;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.config.Config;
import fr.astfaster.santopia.api.database.DatabaseConnection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class Redis implements DatabaseConnection {

    private JedisPool pool;

    private boolean connected;

    @Override
    public void connect() {
        this.initPool();

        SantopiaAPI.instance().executorService().scheduleAtFixedRate(() -> {
            try {
                this.getResource().close();
            } catch (Exception e) {
                System.err.println("An error occurred in Redis connection! (trying to reconnect...)");

                e.printStackTrace();

                this.connected = false;
                this.connected();
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    private void initPool() {
        final Config.Redis config = SantopiaAPI.instance().config().redis();
        final JedisPoolConfig poolConfig = new JedisPoolConfig();

        poolConfig.setMaxTotal(-1);
        poolConfig.setJmxEnabled(false);

        if (config.password() == null) {
            this.pool = new JedisPool(poolConfig, config.hostname(), config.port(), 2000);
        } else {
            this.pool = new JedisPool(poolConfig, config.hostname(), config.port(), 2000, config.password());
        }

        try {
            this.getResource().close();

            this.connected = true;

            System.out.println("Connection set between Redis and Santopia API.");
        } catch (Exception e) {
            System.err.println("An error occurred while connecting to Redis!");
            e.printStackTrace();

            System.exit(-1);
        }
    }

    public void process(Consumer<Jedis> process) {
        try (final Jedis jedis = this.getResource()) {
            if (jedis != null) {
                process.accept(jedis);
            }
        }
    }

    public <R> R get(Function<Jedis, R> getter) {
        try (final Jedis jedis = this.getResource()) {
            if (jedis != null) {
                return getter.apply(jedis);
            }
        }
        return null;
    }

    @Override
    public void stop() {
        if (this.connected) {
            this.pool.close();
            this.pool = null;
        }
    }

    @Override
    public boolean connected() {
        return this.connected;
    }

    private Jedis getResource() {
        return this.pool.getResource();
    }

}
