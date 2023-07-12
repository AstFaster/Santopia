package fr.astfaster.santopia.proxy.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.function.Function;

public enum Message {

    SERVER_NOT_ONLINE(component -> component.append(Component.text("Ce serveur n'est pas en ligne !").color(NamedTextColor.RED))),
    SERVER_FULL(component -> component.append(Component.text("Ce serveur est plein ! (%players%/%slots%)").color(NamedTextColor.RED))),
    SERVER_SENDING(component -> component.append(Component.text("Envoi en cours vers %server%...").color(NamedTextColor.GREEN))),
    SERVER_ALREADY_CONNECTED(component -> component.append(Component.text("Vous êtes déjà connecté sur ce serveur !").color(NamedTextColor.RED))),

    ;

    private final Component value;

    Message(Component value) {
        this.value = value;
    }

    Message(Function<Component, Component> value) {
        final Component prefix = Component.empty()
                .append(Component.text("Sant")
                        .decoration(TextDecoration.BOLD, true)
                        .color(NamedTextColor.GOLD))
                .append(Component.text("opia")
                        .decoration(TextDecoration.BOLD, true)
                        .color(NamedTextColor.AQUA))
                .append(Component.text(" » ")
                        .color(NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.BOLD, true));

        this.value = value.apply(prefix);
    }

    public Component value() {
        return this.value;
    }

}
