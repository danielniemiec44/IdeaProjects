package pl.masarniamc.lootbox;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.logging.Logger;


public class Main extends JavaPlugin implements CommandExecutor, Listener {
    static Logger console;
    static String menuName = ChatColor.DARK_GREEN + "Lootboxy";
    //public static LinkedHashMap<Material, Integer> drop = new LinkedHashMap<>();
    static Inventory lootBoxMenu = Bukkit.createInventory(null,9, menuName);

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, this);


        LootBox pospolity = new LootBox(ChatColor.DARK_GREEN, "Pospolity");
        pospolity.addItem(new ItemStack(Material.IRON_INGOT), 50);

        LootBox rzadki = new LootBox(ChatColor.DARK_GREEN, "Rzadki");
        rzadki.addItem(new ItemStack(Material.IRON_INGOT), 50);


        LootBox bardzoRzadki = new LootBox(ChatColor.DARK_GREEN, "Bardzo rzadki");
        bardzoRzadki.addItem(new ItemStack(Material.IRON_INGOT), 50);


        LootBox mityczny = new LootBox(ChatColor.DARK_GREEN, "Mityczny");
        mityczny.addItem(new ItemStack(Material.IRON_INGOT), 50);



        LootBox legendarny = new LootBox(ChatColor.DARK_GREEN, "Legendarny");
        legendarny.addItem(new ItemStack(Material.IRON_INGOT), 50);





        int slot = 0;
        for(LootBox lootBox : LootBox.lootBoxes){
            ItemStack itemStack = new ItemStack(Material.CHEST);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(lootBox.getName());
            ArrayList<String> lore = new ArrayList<>();
            lore.add("Kliknij aby dowiedzieć się wiecej.");
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            lootBoxMenu.setItem(slot, itemStack);
            slot++;
        }


        /*
        drop.put(Material.DIAMOND, 25);
        drop.put(Material.GOLD_INGOT, 50);
        drop.put(Material.IRON_INGOT, 100);

         */


    }

    @Override
    public void onDisable() {
        super.onDisable();
    }


    public int getRandom(int lower, int upper) {
        Random random = new Random();
        return random.nextInt((upper - lower) + 1) + lower;
    }






    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            console.info("Komenda jest dostepna tylko z poziomu klienta gry.");
            return true;
        }

        if(label.equalsIgnoreCase("lootbox")) {
            Player p = (Player) sender;
            if(args.length > 0){
                return false;
            }

            p.openInventory(lootBoxMenu);
        }

        return true;

    }


    @EventHandler
    public void onLootBoxMenuClick(InventoryClickEvent e){
        String itemName = e.getCurrentItem().getItemMeta().getDisplayName();
        Player p = (Player) e.getWhoClicked();
        if(e.getView().getTitle().equals(menuName)) {
            e.setCancelled(true);
            for(LootBox lootBox : LootBox.getAll()) {
                if (itemName.equals(lootBox.getName())) {
                    //p.sendMessage("dziala");
                    p.openInventory(lootBox.getMenu());
                    return;
                }
            }




        }


        for(LootBox lootBox : LootBox.getAll()){
            String name = e.getView().getTitle();
            if(name.split(" LootBox")[0].equals(lootBox.getName())){
                e.setCancelled(true);
                if(itemName.equals(ChatColor.RED + "Wstecz")){
                    p.openInventory(p.openInventory(lootBoxMenu));
                    return;
                }
                if(itemName.equals(ChatColor.GREEN + "Zakup")){
                    ItemStack is = new ItemStack(Material.CHEST);
                    ItemMeta im = is.getItemMeta();
                    im.setDisplayName(e.getView().getTitle());
                    is.setItemMeta(im);
                    p.getInventory().addItem(is);
                    return;
                }
            }
        }

    }



    @EventHandler
    public void onLootBoxOpen(BlockPlaceEvent e) {
        String inHand = e.getItemInHand().getItemMeta().getDisplayName().split(" LootBox")[0];
        for(LootBox lootBox : LootBox.getAll()) {
            if (lootBox.getName().equals(inHand)) {
                Block block = e.getBlock();
                Location centerOfBlock = block.getLocation().add(0.5, 0.5, 0.5);
                block.setType(Material.AIR);
                for (ItemStack is : lootBox.getItems().keySet()) {
                    Integer percentage = lootBox.getItems().get(is);
                    e.getPlayer().sendMessage(percentage + "");
                    int random = getRandom(1, 100);
                    if (random <= percentage) {
                        block.getWorld().dropItemNaturally(centerOfBlock, is);
                    }
                }
                return;
            }
        }

    }

}
