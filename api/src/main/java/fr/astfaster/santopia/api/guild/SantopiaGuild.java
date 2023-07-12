package fr.astfaster.santopia.api.guild;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.serializer.DataInput;
import fr.astfaster.santopia.api.serializer.DataOutput;
import fr.astfaster.santopia.api.serializer.DataSerializable;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class SantopiaGuild implements DataSerializable {

    /** The regex that the names have to match */
    public static final Pattern NAME_REGEX = Pattern.compile("^\\S(\\S|( ?)){1,32}\\S$");
    /** The regex that the prefixes have to match */
    public static final Pattern PREFIX_REGEX = Pattern.compile("^[A-Z0-9]{1,4}$");

    /** A list of consumer to trigger when the guild is updated. Useful to update other things at the same time. */
    private final List<Consumer<CompletableFuture<Void>>> onUpdate = new LinkedList<>();

    @Expose
    @SerializedName("_id")
    private ObjectId id = new ObjectId();

    @Expose
    private String name;
    @Expose
    private String prefix;
    @Expose
    private String prefixColor = "§7";

    @Expose
    private UUID leader;
    @Expose
    private List<GuildMember> members = new ArrayList<>();

    public SantopiaGuild(String name, String prefix, UUID leader) {
        this.name = name;
        this.prefix = prefix;
        this.leader = leader;

        this.members.add(new GuildMember(leader, GuildRole.LEADER));
    }

    SantopiaGuild() {}

    @Override
    public void writeData(DataOutput output) throws IOException {
        output.writeString(this.id.toHexString());
        output.writeString(this.name);
        output.writeString(this.prefix);
        output.writeString(this.prefixColor);
        output.writeUUID(this.leader);

        output.writeInt(this.members.size());

        for (GuildMember member : this.members) {
            member.writeData(output);
        }
    }

    @Override
    public void readData(DataInput input) throws IOException {
        this.id = new ObjectId(input.readString());
        this.name = input.readString();
        this.prefix = input.readString();
        this.prefixColor = input.readString();
        this.leader = input.readUUID();

        final int membersSize = input.readInt();

        for (int i = 0; i < membersSize; i++) {
            final GuildMember member = new GuildMember();

            member.readData(input);

            this.members.add(member);
        }
    }

    public ObjectId id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public void name(String name) {
        final String oldName = this.name;

        this.name = name;
        this.onUpdate.add(future -> future.thenAcceptAsync(__ -> SantopiaAPI.instance().guildsService().updateGuildName(this, this.name, oldName)));
    }

    public String prefix() {
        return this.prefix;
    }

    public void prefix(String prefix) {
        final String oldPrefix = this.prefix;

        this.prefix = prefix;
        this.onUpdate.add(future -> future.thenAcceptAsync(__ -> SantopiaAPI.instance().guildsService().updateGuildPrefix(this, this.prefix, oldPrefix)));
    }

    public String prefixColor() {
        return this.prefixColor;
    }

    public void prefixColor(String prefixColor) {
        this.prefixColor = prefixColor;
    }

    public UUID leader() {
        return this.leader;
    }

    public void leader(UUID leader) {
        this.leader = leader;
    }

    public List<GuildMember> members() {
        return this.members;
    }

    public List<GuildMember> members(GuildRole role) {
        final List<GuildMember> result = new ArrayList<>();

        for (GuildMember member : this.members) {
            if (member.role() == role) {
                result.add(member);
            }
        }
        return result;
    }

    public GuildMember member(UUID playerId) {
        for (GuildMember member : this.members) {
            if (member.uuid().equals(playerId)) {
                return member;
            }
        }
        return null;
    }

    public void addMember(UUID player) {
        this.members.add(new GuildMember(player, GuildRole.MEMBER));
    }

    public void removeMember(UUID playerId) {
        this.members.remove(this.member(playerId));

        this.onUpdate.add(future -> future.thenAcceptAsync(__ -> SantopiaAPI.instance().guildsService().removeMember(playerId)));
    }

    public CompletableFuture<Void> update() {
        final CompletableFuture<Void> future = SantopiaAPI.instance().guildsService().updateGuild(this);

        this.onUpdate.forEach(consumer -> consumer.accept(future));
        this.onUpdate.clear();

        return future;
    }

}
