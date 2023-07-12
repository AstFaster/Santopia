package fr.astfaster.santopia.api.serializer;

import java.io.IOException;

public class DataSerializer {

    public static final int NULL_ARRAY_LENGTH = -1;

    public <T extends DataSerializable> byte[] serialize(T object) {
        try (final DataOutput output = new DataOutput()) {
            object.writeData(output);

            return output.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T extends DataSerializable> T deserialize(T object, byte[] bytes)  {
        try (final DataInput input = new DataInput(bytes)) {
            object.readData(input);

            return object;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
