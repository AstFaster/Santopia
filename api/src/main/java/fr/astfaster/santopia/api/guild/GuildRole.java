package fr.astfaster.santopia.api.guild;

import java.util.HashMap;
import java.util.Map;

public enum GuildRole {

    LEADER(0),
    OFFICER(1),
    MEMBER(2),

    ;

    private static final Map<Integer, GuildRole> BY_ID = new HashMap<>();

    static {
        for (GuildRole role : values()) {
            BY_ID.put(role.id(),  role);
        }
    }

    private final int id;

    GuildRole(int id) {
        this.id = id;
    }

    public int id() {
        return this.id;
    }

    public GuildRole next() {
        return BY_ID.get(this.id - 1);
    }

    public GuildRole previous() {
        return BY_ID.get(this.id + 1);
    }

    public static GuildRole byId(int id) {
        return BY_ID.get(id);
    }

}
