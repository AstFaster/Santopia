package fr.astfaster.santopia.api.network;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.server.SantopiaServer;

public class NetworkService {

    public int playersCounter() {
        int players = 0;
        for (SantopiaServer server : SantopiaAPI.instance().serversService().servers().values()) {
            players += server.players().size();
        }
        return players;
    }

    public int slots() {
        return 50; // TODO
    }

}
