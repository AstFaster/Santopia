package fr.astfaster.santopia.api.messaging;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.SantopiaException;
import fr.astfaster.santopia.api.config.Config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class MessagingService {

    private final Map<Integer, Class<? extends SantopiaPacket>> idToClass = new HashMap<>();
    private final Map<Class<? extends SantopiaPacket>, Integer> classToId = new HashMap<>();

    private final Map<PacketsChannel, Set<PacketsHandler>> handlers = new ConcurrentHashMap<>();

    private final PacketsCodec codec = new PacketsCodec(this);

    private Connection connection;
    private Channel channel;

    public void start() {
        final ConnectionFactory factory = new ConnectionFactory();
        final Config.RabbitMQ config = SantopiaAPI.instance().config().rabbitMQ();

        factory.setHost(config.hostname());
        factory.setPort(config.port());
        factory.setUsername(config.username());
        factory.setPassword(config.password());

        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();

            this.registerChannels();
        } catch (IOException | TimeoutException e) {
            throw new SantopiaException("Failed to start messaging system!", e);
        }
    }

    private void registerChannels() {
        for (PacketsChannel channel : PacketsChannel.values()) {
            try {
                this.channel.exchangeDeclare(channel.id(), "fanout");

                final String queue = this.channel.queueDeclare().getQueue();

                this.channel.queueBind(queue, channel.id(), "");
                this.channel.basicConsume(queue, true, (consumerTag, delivery) -> {
                    final String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    final SantopiaPacket packet = this.codec.decode(message);
                    final Set<PacketsHandler> handlers = this.handlers.get(channel);

                    if (handlers != null) {
                        for (PacketsHandler handler : handlers) {
                            handler.handle(packet);
                        }
                    }
                }, consumerTag -> {});
            } catch (IOException e) {
                throw new SantopiaException("Couldn't register '" + channel + "' channel!", e);
            }
        }
    }

    public void stop() {
        try {
            this.channel.close();
            this.connection.close();
        } catch (IOException | TimeoutException e) {
            throw new SantopiaException("Failed to close messaging system!", e);
        }
    }

    public void send(PacketsChannel channel, SantopiaPacket packet) {
        SantopiaAPI.instance().executorService().execute(() -> {
            try {
                this.channel.basicPublish(channel.id(), "", null, this.codec.encode(packet).getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new SantopiaException("Couldn't send packet!", e);
            }
        });
    }

    public void registerHandler(PacketsChannel channel, PacketsHandler handler) {
        this.handlers.merge(channel, new HashSet<>(), (oldValue, newValue) -> oldValue).add(handler);
    }

    public void registerPacket(int packetId, Class<? extends SantopiaPacket> packetClass) {
        this.idToClass.put(packetId, packetClass);
        this.classToId.put(packetClass, packetId);
    }

    Class<? extends SantopiaPacket> packetClass(int packetId) {
        return this.idToClass.get(packetId);
    }

    int packetId(Class<? extends SantopiaPacket> packetClass) {
        return this.classToId.get(packetClass);
    }

}
