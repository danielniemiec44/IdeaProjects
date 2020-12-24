package masarniamc.stonedrop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class GUI {
    public static HashMap<Player, GUI> guis = new HashMap<Player, GUI>();
    private Inventory inv;
    private HashMap<Material, Boolean> dropEnabled;


    public GUI(Player p) {
        dropEnabled = new HashMap<Material, Boolean>();
        for(Material m : Main.drop.keySet()){
            dropEnabled.put(m, true);
        }


        inv = Bukkit.createInventory(null, 9, ChatColor.DARK_GREEN + "Drop:");
        LinkedHashMap<Material, Integer> drop = Main.drop;
        int id = 0;
        for(Material material : drop.keySet()){
            inv.setItem(id, Lore.set(new ItemStack(material), true));
            id++;
        }
        guis.put(p, this);
    }


    public Inventory getInventory() {
        return inv;
    }


    public static void removeGUI(Player p){
        guis.remove(p);
    }


    public static GUI getGUI(Player p) {
        return guis.get(p);
    }

    public void setDropEnabled(Material material, Boolean dropEnabled) {
        this.dropEnabled.put(material, dropEnabled);
        Inventory inv = this.getInventory();
        int id = 0;
        for(Material m : Main.drop.keySet()){
            if(material == m){
                break;
            }
            id++;
        }

        this.getInventory().setItem(id, Lore.set(inv.getItem(id), dropEnabled));
    }

    public boolean getDropEnabled(Material m) {
        return dropEnabled.get(m);
    }
}
