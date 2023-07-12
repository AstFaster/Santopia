package fr.astfaster.santopia.server.item;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Supplier;

public abstract class CustomItem {

    private final String id;
    private final Supplier<ItemStack> handle;

    public CustomItem(String id, Supplier<ItemStack> handle) {
        this.id = id;
        this.handle = handle;
    }

    protected ItemStack onPreGive(Player player, int slot, ItemStack itemStack) {
        return itemStack;
    }

    protected void onGive(Player player, int slot, ItemStack itemStack) {}

    protected void onLeftClick(Player player, PlayerInteractEvent event) {}

    protected void onRightClick(Player player, PlayerInteractEvent event) {}

    public Supplier<ItemStack> handle() {
        return this.handle;
    }

    public String id() {
        return this.id;
    }

}
