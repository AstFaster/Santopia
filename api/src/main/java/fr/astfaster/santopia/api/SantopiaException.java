package fr.astfaster.santopia.api;

public class SantopiaException extends RuntimeException {

    public SantopiaException(String message) {
        super(message);
    }

    public SantopiaException(String message, Throwable cause) {
        super(message, cause);
    }

    public SantopiaException(Throwable cause) {
        super(cause);
    }

}
