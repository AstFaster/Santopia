package fr.astfaster.santopia.api.guild;

import java.util.function.Predicate;

public class GuildPermission {

    public static final GuildPermission DISBAND = new GuildPermission(member -> member.role().compareTo(GuildRole.LEADER) == 0);
    public static final GuildPermission LEAVE = new GuildPermission(member -> member.role().compareTo(GuildRole.LEADER) != 0);
    public static final GuildPermission PROMOTE = new GuildPermission(member -> member.role().compareTo(GuildRole.LEADER) == 0);
    public static final GuildPermission DEMOTE = new GuildPermission(member -> member.role().compareTo(GuildRole.LEADER) == 0);
    public static final GuildPermission CHANGE_LEADER = new GuildPermission(member -> member.role().compareTo(GuildRole.LEADER) == 0);
    public static final GuildPermission CLAIM = new GuildPermission(member -> member.role().compareTo(GuildRole.OFFICER) <= 0);
    public static final GuildPermission UNCLAIM = new GuildPermission(member -> member.role().compareTo(GuildRole.OFFICER) <= 0);
    public static final GuildPermission INVITE = new GuildPermission(member -> member.role().compareTo(GuildRole.OFFICER) <= 0);
    public static final GuildPermission KICK = new GuildPermission(member -> member.role().compareTo(GuildRole.LEADER) == 0);
    public static final GuildPermission RENAME = new GuildPermission(member -> member.role().compareTo(GuildRole.LEADER) == 0);
    public static final GuildPermission CHANGE_PREFIX = new GuildPermission(member -> member.role().compareTo(GuildRole.LEADER) == 0);
    public static final GuildPermission COLOR = new GuildPermission(member -> member.role().compareTo(GuildRole.LEADER) == 0);

    private final Predicate<GuildMember> check;

    public GuildPermission(Predicate<GuildMember> check) {
        this.check = check;
    }

    public boolean has(GuildMember member) {
        return this.check.test(member);
    }

}
