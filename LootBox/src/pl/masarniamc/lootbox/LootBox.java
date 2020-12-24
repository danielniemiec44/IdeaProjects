package pl.masarniamc.lootbox;

import com.sun.istack.internal.NotNull;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class LootBox {
    private HashMap<ItemStack, Integer> items = new HashMap<>();
    private String name;
    static ArrayList<LootBox> lootBoxes = new ArrayList<>();
    private Inventory menu;



    LootBox(@NotNull ChatColor color, String name) {
        this.name = color + name;
        menu = Bukkit.createInventory(null,9, this.name + " LootBox");

        ItemStack itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.RED + "Wstecz");
        itemStack.setItemMeta(itemMeta);
        menu.addItem(itemStack);


        itemStack = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(ChatColor.GREEN + "Zakup");
        itemStack.setItemMeta(itemMeta);
        menu.setItem(8, itemStack);



        lootBoxes.add(this);
    }


    static LootBox getLootBox(String name) {
        for(LootBox lootBox : lootBoxes) {
            if(lootBox.name.equals(name)){
                return lootBox;
            }
        }
        return null;
    }


    void addItem(ItemStack itemStack, Integer percentage) {
        this.items.put(itemStack, percentage);
        int slot = 2;
        for(ItemStack is : this.items.keySet()){
            ItemMeta im = is.getItemMeta();
            ArrayList<String> lore = new ArrayList<>();
            lore.add(percentage + "% szans na drop");
            im.setLore(lore);
            is.setItemMeta(im);
            this.getMenu().setItem(slot, itemStack);
            slot++;
        }
    }

    HashMap<ItemStack, Integer> getItems() {
        return items;
    }


    String getName() {
        return name;
    }


    static ArrayList<LootBox> getAll() {
        return lootBoxes;
    }


    public Inventory getMenu() {
        return menu;
    }
}
