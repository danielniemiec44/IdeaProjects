package masarniamc.magicmasarnia;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wand {
    private Player p;
    private WandType type;
    public static ArrayList<Wand> wands = new ArrayList<Wand>();
    public static int wandSlot = 0;

    public Wand(Player player, WandType wandType){
        Wand wand = Wand.getPlayerWand(p);
        if(wand != null){
            wands.remove(wand);
        }
            this.p = player;
            this.type = wandType;
            wands.add(this);
    }

    public static Wand getPlayerWand(Player p){
        for(Wand wand : wands){
            if(wand.p == p){
                return wand;
            }
        }
        return null;
    }


    public static boolean hasPlayerWand(Player p) {
        if(getPlayerWand(p) != null){
            return true;
        } else {
            return false;
        }
    }

    public static void setWandOnSlot(Player p) {
        Wand wand = getPlayerWand(p);
        if(wand != null) {
            if(wand.getType() == WandType.NORMAL){
                ItemStack itemStack = new ItemStack(Material.STICK, 1); // Your itemstack
                ItemMeta itemMeta = itemStack.getItemMeta();
                itemMeta.setDisplayName(ChatColor.GREEN + "Różdżka");
                itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, true);
                ArrayList<String> lore = new ArrayList<String>();
                itemMeta.setLore(lore);
                itemStack.setItemMeta(itemMeta);
                p.getInventory().setItem(0, itemStack);
            }
        } else {
            p.getInventory().setItem(wandSlot, new ItemStack(Material.BARRIER));
        }
    }


    public WandType getType() {
        return this.type;
    }


    public static void setPlayerWand(Player p, WandType wandType) {
        new Wand(p, wandType);
    }
}
