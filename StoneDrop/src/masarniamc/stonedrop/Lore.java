package masarniamc.stonedrop;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class Lore {
    public static ItemStack set(ItemStack is, boolean isEnabled) {
        ItemMeta im = is.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        if(isEnabled) {
            lore.add("Drop: WŁĄCZONY");
            lore.add("Szansa na drop: " + Main.drop.get(is.getType()) + "%");
            lore.add("Kliknij aby wyłączyć");
        } else {
            lore.add("Drop: WYŁĄCZONY");
            lore.add("Szansa na drop: " + Main.drop.get(is.getType()) + "%");
            lore.add("Kliknij aby włączyć");
        }
        im.setLore(lore);
        is.setItemMeta(im);
        return is;
    }

}
