package fr.astfaster.santopia.api.server;

public enum ServerType {

    SURVIVAL("Survie", "Rejoins les autres joueurs et guildes de la survie 1.20 de Santopia.", 50),
    CREATIVE("Créatif", "Crée un plot et construis y tes propres créations.", 50),
    WONDERS("Merveilles", "Observe les plus belles créations des joueurs du serveur.", 50);

    private final String display;
    private final String description;
    private final int slots;

    ServerType(String display, String description, int slots) {
        this.display = display;
        this.description = description;
        this.slots = slots;
    }

    public String display() {
        return this.display;
    }

    public String description() {
        return this.description;
    }

    public int slots() {
        return this.slots;
    }

}
