# Santopia

Santopia is a little 1.20 survival network owned by [FloxyS](https://twitch.tv/floxys_twitch).<br>
Initially it was just a single spigot server, but my goal was to add two more servers (creative and wonders) and a guilds' system.<br>

It runs on Paper for servers and Velocity for the single proxy.

## Technologies
- MongoDB: used as a cold data storage
- Redis: used as a cache to access data quickly inside the network. Every data object is saved by writing its data in a buffer of bytes.
- RabbitMQ: used as a pub/sub system to communicate across the network