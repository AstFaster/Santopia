package fr.astfaster.santopia.api.database;

public interface DatabaseConnection {

    void connect();

    void stop();

    boolean connected();

}
