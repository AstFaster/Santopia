package fr.astfaster.santopia.server.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {

    public static final String ITEM_NBT = "SantopiaItem";

    private final Map<Class<? extends CustomItem>, CustomItem> itemsByClass = new HashMap<>();
    private final Map<String, CustomItem> itemsById = new HashMap<>();

    public <T extends CustomItem> void registerItem(T item) {
        this.itemsByClass.put(item.getClass(), item);
        this.itemsById.put(item.id(), item);
    }

    public void giveItem(Player player, Class<? extends CustomItem> itemClass, int slot) {
        final CustomItem item = this.itemsByClass.get(itemClass);
        final ItemStack itemStack = item.onPreGive(player, slot, new ItemNBT(item.handle().get())
                .setString(ITEM_NBT, item.id())
                .build());

        player.getInventory().setItem(slot, itemStack);

        item.onGive(player, slot, itemStack);
    }

    CustomItem item(String itemId) {
        return this.itemsById.get(itemId);
    }

}
