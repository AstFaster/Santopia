package fr.astfaster.santopia.api.messaging;

public enum PacketsChannel {

    SERVERS("servers"),
    PLAYERS("players"),
    GUILDS("guilds")

    ;

    private final String id;

    PacketsChannel(String id) {
        this.id = "santopia." + id;
    }

    public String id() {
        return this.id;
    }

}
