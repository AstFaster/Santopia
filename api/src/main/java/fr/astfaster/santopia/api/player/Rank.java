package fr.astfaster.santopia.api.player;

import java.util.HashMap;
import java.util.Map;

public enum Rank {

    ADMINISTRATOR("admin"),
    MODERATOR("mod"),
    DEFAULT("default"),

    ;

    private static final Map<String, Rank> BY_ID = new HashMap<>();

    static {
        for (Rank rank : values()) {
            BY_ID.put(rank.id(), rank);
        }
    }

    private final String id;

    Rank(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public boolean superiorOrEqual(Rank rank) {
        return this.compareTo(rank) >= 0;
    }

    public boolean superior(Rank rank) {
        return this.compareTo(rank) > 0;
    }

    public boolean lessOrEqual(Rank rank) {
        return this.compareTo(rank) <= 0;
    }

    public boolean less(Rank rank) {
        return this.compareTo(rank) < 0;
    }

    public static Rank of(String id) {
        return BY_ID.get(id);
    }

}
