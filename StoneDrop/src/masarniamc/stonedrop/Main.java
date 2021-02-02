package masarniamc.stonedrop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Main extends JavaPlugin implements CommandExecutor,Listener {
    public static LinkedHashMap<Material, Integer> drop = new LinkedHashMap<>();
    int lower = 0;
    int upper = 1;
    FileConfiguration config = getConfig();

        public int getRandom(int lower, int upper) {
            Random random = new Random();
            return random.nextInt((upper - lower) + 1) + lower;
        }



    @Override
    public void onEnable(){
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, this);

        config.options().copyDefaults(true);
        saveConfig();


        for(String d : config.getConfigurationSection("drop").getKeys(true)){
            drop.put(Material.valueOf(d.toUpperCase()), config.getInt("drop." + d));
            //getLogger().info(Material.valueOf(d.toUpperCase()).toString() + " - " + config.getInt("drop." + d));
        }



        /*
        drop.put(Material.DIAMOND, 1);
        drop.put(Material.LAPIS_LAZULI, 2);
        drop.put(Material.GOLD_INGOT, 5);
        drop.put(Material.EMERALD, 5);
        drop.put(Material.IRON_INGOT, 10);
        drop.put(Material.COAL, 20);
        drop.put(Material.COBBLESTONE, 100);
        */


    }


    @Override
    public void onDisable() {
        super.onDisable();
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("drop")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                p.openInventory(GUI.guis.get(p).getInventory());
            } else {
                Bukkit.getLogger().info("Komenda dostępna tylko z klienta gry!");
            }
        }
        return true;
    }




    @EventHandler
    public void stoneDrop(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if(e.getBlock().getType() == Material.STONE) {
            GameMode gm = p.getGameMode();
            if (gm == GameMode.SURVIVAL || gm == GameMode.ADVENTURE) {
                if(e.getBlock().getLocation().getWorld().getName().equalsIgnoreCase("plots")){
                    return;
                }
                e.setDropItems(false);
                for (Material m : drop.keySet()) {
                    if (GUI.getGUI(p).getDropEnabled(m)) {
                        int percentage = drop.get(m);
                        if (getRandom(1, 100) <= drop.get(m)) {
                            addItemsToPlayer(p, new ItemStack(m));
                        }
                    }
                }
            }
        }
    }



    public void addItemsToPlayer(Player p, ItemStack itemStack){
            p.getInventory().addItem(itemStack);
    }



    @EventHandler
    public void onPlayerJoin(PlayerLoginEvent e) {
            new GUI(e.getPlayer());
    }


    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
            GUI.removeGUI(e.getPlayer());
    }




    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked(); // The player that clicked the item
        ItemStack clicked = event.getCurrentItem(); // The item that was clicked
        Inventory inventory = event.getInventory(); // The inventory that was clicked in
        if (event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Drop:")) { // The inventory is our custom Inventory
            int slot = event.getSlot();
            if(slot < drop.size()){
                GUI gui = GUI.getGUI(player);
                if(gui.getDropEnabled(clicked.getType())){
                  gui.setDropEnabled(clicked.getType(), false);
                } else {
                    gui.setDropEnabled(clicked.getType(), true);
                }
            }
            event.setCancelled(true);
        }
    }



}
