package fr.astfaster.santopia.api.serializer;

import java.io.IOException;

public interface DataSerializable {

    void writeData(DataOutput output) throws IOException;

    void readData(DataInput input) throws IOException;

}
