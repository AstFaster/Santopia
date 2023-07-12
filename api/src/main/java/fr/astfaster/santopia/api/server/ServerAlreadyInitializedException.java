package fr.astfaster.santopia.api.server;

import fr.astfaster.santopia.api.SantopiaException;

public class ServerAlreadyInitializedException extends SantopiaException {

    public ServerAlreadyInitializedException() {
        super("The server your are trying to initialize was already initialized!");
    }

}
