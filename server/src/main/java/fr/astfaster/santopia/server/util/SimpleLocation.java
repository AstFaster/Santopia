package fr.astfaster.santopia.server.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class SimpleLocation {

    private Location cachedLocation;

    private String world;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;

    public SimpleLocation(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public SimpleLocation(String world, double x, double y, double z) {
        this(world, x, y, z, 0.0f, 0.0f);
    }

    public SimpleLocation(@NotNull Location location) {
        this(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    SimpleLocation() {}

    public String world() {
        return this.world;
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }

    public float yaw() {
        return this.yaw;
    }

    public float pitch() {
        return this.pitch;
    }

    public Location asBukkit() {
        return this.cachedLocation != null ? this.cachedLocation : (this.cachedLocation = new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z, this.yaw, this.pitch));
    }

}
