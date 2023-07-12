package fr.astfaster.santopia.api.serializer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static fr.astfaster.santopia.api.serializer.DataSerializer.NULL_ARRAY_LENGTH;

public class DataOutput implements java.io.DataOutput, Closeable {

    private final ByteArrayOutputStream byteArrayOutputStream;
    private final DataOutputStream outputStream;

    public DataOutput() {
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        this.outputStream = new DataOutputStream(this.byteArrayOutputStream);
    }

    @Override
    public void write(int b) throws IOException {
        this.outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.outputStream.write(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        this.outputStream.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        this.outputStream.writeByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        this.outputStream.writeShort(v);
    }

    @Override
    public void writeChar(int v) throws IOException {
        this.outputStream.writeChar(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        this.outputStream.writeInt(v);
    }

    @Override
    public void writeLong(long v) throws IOException {
        this.outputStream.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        this.outputStream.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        this.outputStream.writeDouble(v);
    }

    @Override
    public void writeBytes(String s) throws IOException {
        this.outputStream.writeBytes(s);
    }

    @Override
    public void writeChars(String s) throws IOException {
        this.outputStream.writeChars(s);
    }

    public void writeUUID(UUID uuid) throws IOException {
        if (uuid == null) {
            this.writeLong(NULL_ARRAY_LENGTH);
            return;
        }

        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
    }

    @Override
    public void writeUTF(String string) throws IOException {
        this.writeString(string);
    }

    public void writeString(String string) throws IOException {
        if (string == null) {
            this.writeInt(NULL_ARRAY_LENGTH);
            return;
        }

        final byte[] bytes = string.getBytes(StandardCharsets.UTF_8);

        this.writeInt(bytes.length);
        this.write(bytes);
    }

    public void writeByteArray(byte[] values) throws IOException {
        if (values == null) {
            this.writeInt(NULL_ARRAY_LENGTH);
            return;
        }

        this.writeInt(values.length);
        this.write(values);
    }

    public void writeBooleanArray(boolean[] values) throws IOException {
        if (values == null) {
            this.writeInt(NULL_ARRAY_LENGTH);
            return;
        }

        this.writeInt(values.length);

        for (boolean b : values) {
            this.writeBoolean(b);
        }
    }

    public void writeCharArray(char[] values) throws IOException {
        if (values == null) {
            this.writeInt(NULL_ARRAY_LENGTH);
            return;
        }

        this.writeInt(values.length);

        for (char c : values) {
            this.writeChar(c);
        }
    }

    public void writeShortArray(short[] values) throws IOException {
        if (values == null) {
            this.writeInt(NULL_ARRAY_LENGTH);
            return;
        }

        this.writeInt(values.length);

        for (short s : values) {
            this.writeShort(s);
        }
    }

    public void writeIntArray(int[] values) throws IOException {
        if (values == null) {
            this.writeInt(NULL_ARRAY_LENGTH);
            return;
        }

        this.writeInt(values.length);

        for (int i : values) {
            this.writeInt(i);
        }
    }

    public void writeLongArray(long[] values) throws IOException {
        if (values == null) {
            this.writeInt(NULL_ARRAY_LENGTH);
            return;
        }

        this.writeInt(values.length);

        for (long l : values) {
            this.writeLong(l);
        }
    }

    public void writeDoubleArray(double[] values) throws IOException {
        if (values == null) {
            this.writeInt(NULL_ARRAY_LENGTH);
            return;
        }

        this.writeInt(values.length);

        for (double d : values) {
            this.writeDouble(d);
        }
    }

    public void writeFloatArray(float[] values) throws IOException {
        if (values == null) {
            this.writeInt(NULL_ARRAY_LENGTH);
            return;
        }

        this.writeInt(values.length);

        for (float f : values) {
            this.writeFloat(f);
        }
    }

    public void writeUTFArray(String[] values) throws IOException {
        this.writeStringArray(values);
    }

    public void writeStringArray(String[] values) throws IOException {
        if (values == null) {
            this.writeInt(NULL_ARRAY_LENGTH);
            return;
        }

        this.writeInt(values.length);

        for (String s : values) {
            this.writeString(s);
        }
    }

    public byte[] toByteArray() {
        return this.byteArrayOutputStream.toByteArray();
    }

    @Override
    public void close() throws IOException {
        this.outputStream.close();
    }
    
}
