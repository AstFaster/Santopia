package fr.astfaster.santopia.api.player;

import com.google.gson.annotations.Expose;
import fr.astfaster.santopia.api.serializer.DataInput;
import fr.astfaster.santopia.api.serializer.DataOutput;
import fr.astfaster.santopia.api.serializer.DataSerializable;

import java.io.IOException;

public final class SantopiaHome implements DataSerializable {

    @Expose
    private String world;
    @Expose
    private double x;
    @Expose
    private double y;
    @Expose
    private double z;
    @Expose
    private float yaw;
    @Expose
    private float pitch;

    public SantopiaHome(String world, double x, double y, double z, float yaw, float pitch) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    SantopiaHome() {}

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeString(this.world);
        output.writeDouble(this.x);
        output.writeDouble(this.y);
        output.writeDouble(this.z);
        output.writeFloat(this.yaw);
        output.writeFloat(this.pitch);
    }

    @Override
    public void readData(DataInput input) throws IOException {
        this.world = input.readString();
        this.x = input.readDouble();
        this.y = input.readDouble();
        this.z = input.readDouble();
        this.yaw = input.readFloat();
        this.pitch = input.readFloat();
    }

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

}
