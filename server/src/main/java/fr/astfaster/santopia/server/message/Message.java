package fr.astfaster.santopia.server.message;

import net.md_5.bungee.api.ChatColor;

import static fr.astfaster.santopia.server.SantopiaPlugin.COLORED_SERVER_NAME;
import static fr.astfaster.santopia.server.SantopiaPlugin.DASH_LINE;

public enum Message {

    JOIN_FORMAT("&8(&a+&8) &6&l%player% &f&oa rejoint le serveur.", false),
    QUIT_FORMAT("&8(&c-&8) &6&l%player% &f&oa quitté le serveur.", false),

    JOIN_MESSAGE("&8&m" + DASH_LINE + "\n&fBienvenue sur &6&lSant&b&lopia &r!\n \n&fRejoins le &9&l/&bDiscord &fpour partager ton avancée !\n&fBon jeu sur le serveur en compagnie de &e%players% &fsantopien(s) !\n&8&m" + DASH_LINE, false),
    JOIN_TITLE("&6&lSant&b&lopia", false),
    JOIN_SUBTITLE("&fBienvenue &e%player%", false),

    UNKNOWN_COMMAND("&cErreur: Cette commande n'existe pas !"),
    INVALID_COMMAND("&cUtilisation: %command%"),

    PLAYER_NOT_CONNECTED("&cErreur: Impossible de trouver &f'%player%&f' &c!"),

    INVALID_PERMISSION("&cErreur: Vous n'avez pas la permission nécessaire pour effectuer cette action !"),
    CANT_PERFORM_ON_YOURSELF("&cErreur: Vous ne pouvez pas effectuer cette action sur vous même !"),

    ACTION_CANCELLED("&cAction annulée."),

    TIKTOK("&fTikTok de FloxyS: &etiktok.com/@floo0oooo"),
    TWITCH("&fTwitch de FloxyS: &etwitch.tv/floxys_twitch"),
    IP("&fIP du serveur: &esantopia.floxys.fr"),
    DISCORD("&fDiscord du serveur: &e%discord%"),

    SPAWN_TELEPORTED("&fVous avez été téléporté au point d'apparition du serveur."),

    TELEPORTATION_REQUEST_SENT("&fDemande de téléportation envoyée à &6%player%&f."),
    TELEPORTATION_REQUEST_ALREADY_SENT("&cVous avez déjà envoyé une demande de téléportation à ce joueur."),
    TELEPORTATION_REQUEST_RECEIVED("&6%player% &fsouhaite se téléporter vers vous. Tapez &a/tpaccept &fpour accepter ou &c/tpdeny &fpour refuser."),
    TELEPORTATION_NO_REQUEST("&cVous n'avez pas de demandes de téléportation !"),
    TELEPORTATION_NO_REQUEST_FROM_PLAYER("&cVous n'avez pas de demande de téléportsation de ce joueur !"),
    TELEPORTATION_REQUESTER_NOT_ONLINE("&cCe joueur n'est plus en ligne !"),
    TELEPORTATION_REQUEST_ACCEPTED("&fVotre demande de téléportation à &6%player% &fa été acceptée."),
    TELEPORTATION_REQUEST_ACCEPTED_TARGET("&fVous avez accepté la demande de téléportation de &6%player%&f."),
    TELEPORTATION_REQUEST_DENIED("&fVotre demande de téléportation à &6%player% &fa été refusée."),
    TELEPORTATION_REQUEST_DENIED_TARGET("&fVous avez refusé la demande de téléportation de &6%player%&f."),

    WELCOME_NO_PLAYER("&cIl n'y a pas de nouveau joueur à accueillir."),
    WELCOME_ALREADY_WELCOMED("&cVous avez déjà souhaité la bienvenue à ce joueur."),
    WELCOME_PLAYER_WELCOMED("&6%player% &fsouhaite la bienvenue à &e&l%new_player% &fsur " + COLORED_SERVER_NAME + " &f!"),
    WELCOME_MESSAGE("&fVeuillez accueillir &e%player% &fsur le serveur ! Utilisez le &6&l/bienvenue &fpour lui souhaiter la bienvenue !"),

    CLAIM_ERROR("&cCe chunk est claim par la guilde &f'%guild%&f'&c !"),

    GUILD_PLAYER_DOESNT_HAVE("&cErreur: Vous n'avez pas de guilde !"),
    GUILD_TARGET_DOESNT_HAVE("&cErreur: Ce joueur n'est dans aucune guilde !"),
    GUILD_ALREADY_IN_ONE("&cErreur: Vous faites déjà partie d'une guilde !"),
    GUILD_INVALID_NAME("&cErreur: Nom invalide (32 caractères maximum) !"),
    GUILD_INVALID_PREFIX("&cErreur: Prefixe invalide (4 caractères en majuscules maximum) !"),
    GUILD_NAME_INPUT("&fEcrit dans le chat le nom pour ta guilde (32 caractères maximum)."),
    GUILD_PREFIX_INPUT("&fEcrit dans le chat le prefixe pour ta guilde (4 caractères en majuscules maximum)."),
    GUILD_CREATED("&fVotre guilde a correctement été créée."),
    GUILD_LEFT("&cVous avez quitté votre guilde."),
    GUILD_PLAYER_LEFT("&6%player% &fa &cquitté &fla guilde."),
    GUILD_KICKED("&cVous avez été expulsé de votre guilde."),
    GUILD_PLAYER_KICKED("&6%player% &fa été &cexpulsé &fde la guilde."),
    GUILD_PLAYER_JOINED("&6%player% &fa &arejoint &fla guilde."),
    GUILD_DISBAND("&cVotre guilde a été dissoute."),
    GUILD_RENAMED("&fVotre guilde a été renommée."),
    GUILD_PREFIX_ALREADY_EXISTS("&cCe préfixe est déjà utilisé !"),
    GUILD_PREFIX_CHANGED("&fLe préfixe de votre guilde a été modifié."),
    GUILD_PREFIX_COLOR_CHANGED("&fLa couleur du préfixe de votre guilde a été modifiée."),
    GUILD_NAME_ALREADY_EXISTS("&cCe nom est déjà utilisé !"),
    GUILD_NOT_IN_THE_SAME("&cErreur: Vous n'êtes pas dans la même guilde que ce joueur !"),
    GUILD_CANT_PROMOTE("&cErreur: Ce joueur ne peut pas être promu !"),
    GUILD_PROMOTED("&6%player% &fa été promu."),
    GUILD_CANT_DEMOTE("&cErreur: Ce joueur ne peut pas être rétrogradé !"),
    GUILD_DEMOTED("&6%player% &fa été rétrogradé."),
    GUILD_LEADER_CHANGED("&6%player% &fest désormais le leader de votre guilde."),
    GUILD_ALREADY_CLAIMED("&cErreur: Ce chunk est déjà claim !"),
    GUILD_CLAIMED("&fCe chunk est désormais &aclaim&f."),
    GUILD_NOT_CLAIMED("&cErreur: Ce chunk n'est pas claim !"),
    GUILD_UNCLAIMED("&fCe chunk n'est désormais plus &cclaim&f."),
    GUILD_REQUEST_SENT("&6%player% &fa été invité dans votre guilde. Il a &e60 &fsecondes pour accepter l'invitation."),
    GUILD_ALREADY_REQUESTED("&cVous avez déjà invité ce joueur dans votre guilde !"),
    GUILD_REQUEST_RECEIVED("&6%player% &fvous invite à rejoindre la guilde : &e%guild%&f. Vous avez &a60 &fsecondes pour faire &a/g accept %player% &fou &c/g deny %player%&f."),
    GUILD_NO_REQUEST("&cErreur: Ce joueur ne vous a pas invité dans sa guilde !"),
    GUILD_REQUEST_DENIED("&cVous avez refusé de rejoindre la guilde de %player%."),
    GUILD_NOT_IN("&cCe joueur ne fait pas partie de votre guilde !"),

    HOME_NO_HOME("&cVous ne possédez pas de home !"),
    HOME_SET("&fVotre &ehome &fa été défini à votre position actuelle."),
    HOME_TELEPORTING("&fTéléportation vers votre home..."),

    ;

    private final String value;

    Message(String value, boolean prefix) {
        this.value = (prefix ? COLORED_SERVER_NAME + " &8&l» &r": "") + value;
    }

    Message(String value) {
        this(value, true);
    }

    public String value() {
        return ChatColor.translateAlternateColorCodes('&', this.value);
    }

}
