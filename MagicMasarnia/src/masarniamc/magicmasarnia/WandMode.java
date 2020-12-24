package masarniamc.magicmasarnia;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

public class WandMode {
    private Player p;
    private HashMap<Integer, ItemStack> playerItems = new HashMap<Integer, ItemStack>();
    private float playerXP;
    private int playerLevel;
    public static ArrayList<WandMode> wandMode = new ArrayList<WandMode>();
    public static int fromId = 1;
    public static int toId = 35;
    public static int wandModeSlot = 0;
    public static int quitWandModeSlot = 8;





    public WandMode(Player p){
        this.p = p;
        for(int id = fromId; id <= toId; id++) {
            this.playerItems.put(id, p.getInventory().getItem(id));
        }
        this.playerLevel = p.getLevel();
        this.playerXP = p.getExp();
        //p.sendMessage(this.playerXP + "");
        wandMode.add(this);




        for (int id = fromId; id <= toId; id++) {
            ItemStack itemStack;
            ItemMeta itemMeta;
            ArrayList<String> lore;
            switch (id) {
                case 8:
                    p.getInventory().setItem(id, new ItemStack(Material.BARRIER));
                    break;
                case 35:
                    itemStack = new ItemStack(Material.ANVIL);
                    itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.GREEN + "Informacje na temat używania zaklęć:");
                    lore = new ArrayList<String>();
                    lore.add("Aby użyć zaklęcia kliknij odpowiednia cyfre na klawiaturze");
                    lore.add("Zaklęcia z ekwipunku należy kliknąć myszką");
                    //lore.add("Aby przypisać zaklęcie do przycisku przeciągnij je do odpowiedniego slotu");
                    lore.add("Aby wyjść z trybu różdżki kliknij przycisk 9 na klawiaturze");
                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);
                    p.getInventory().setItem(id, itemStack);
                    break;
                case 1:
                    itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                    itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.GREEN + "Zaklęcie Drętwota");
                    lore = new ArrayList<String>();
                    lore.add("Kliknij 1 na klawiaturze aby wypowiedzieć zaklęcie");
                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);
                    p.getInventory().setItem(id, itemStack);
                    break;
                case 2:
                    itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                    itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.GREEN + "Zaklęcie Expelliarmus");
                    lore = new ArrayList<String>();
                    lore.add("Kliknij 2 na klawiaturze aby wypowiedzieć zaklęcie");
                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);
                    p.getInventory().setItem(id, itemStack);
                    break;
                case 7:
                    itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                    itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.GREEN + "Zaklęcie Avada Kedavra");
                    lore = new ArrayList<String>();
                    lore.add("Kliknij 8 na klawiaturze aby wypowiedzieć zaklęcie");
                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);
                    p.getInventory().setItem(id, itemStack);
                    break;
                case 9:
                    itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                    itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName(ChatColor.GREEN + "Zaklęcie Homenum Revelio");
                    lore = new ArrayList<String>();
                    lore.add("Kliknij aby wypowiedzieć zaklęcie");
                    itemMeta.setLore(lore);
                    itemStack.setItemMeta(itemMeta);
                    p.getInventory().setItem(id, itemStack);
                    break;
                default:
                    p.getInventory().setItem(id, new ItemStack(Material.AIR));
            }
        }



        p.setLevel(200);
        p.setExp(1);
    }

    public HashMap<Integer, ItemStack> getPlayerItems() {
        return playerItems;
    }



    //public ItemStack getItemStack(int slot) {
        //return playerItems.get(slot);
    //}


    private Player getPlayer() {
        return this.p;
    }


    public static HashMap<Integer, ItemStack> getItems(Player player){
        WandMode mode = getPlayerWandMode(player);
        if(mode != null){
            return mode.playerItems;
        } else {
            return null;
        }
    }


    public static WandMode getPlayerWandMode(Player p){
        for(WandMode mode : wandMode) {
            if(mode.getPlayer() == p){
                return mode;
            }
        }
        return null;
    }


    public static boolean isPlayerInWandMode(Player p) {
        if(getPlayerWandMode(p) != null) {
            return true;
        } else {
            return false;
        }
    }

    private float getPlayerXP() {
        return this.playerXP;
    }

    private int getPlayerLevel() {
        return this.playerLevel;
    }


    public void quitWandMode() {
        HashMap<Integer, ItemStack> playerItems = getItems(p);
        for(int id = fromId; id <= toId; id++) {
            p.getInventory().setItem(id, playerItems.get(id));
        }
        WandMode wm = getPlayerWandMode(p);
        p.setLevel(wm.getPlayerLevel());
        p.setExp(wm.getPlayerXP());
        wandMode.remove(getPlayerWandMode(p));
        p.getInventory().setHeldItemSlot(1);
    }


}
