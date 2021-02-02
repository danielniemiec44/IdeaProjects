package pl.masarniamc.cobblex;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.logging.Logger;

//TODO: Sprawdzanie czy teleportuje na ocean

public class Main extends JavaPlugin implements CommandExecutor, Listener {
    public static Logger console;
    public static LinkedHashMap<Material, Integer> drop = new LinkedHashMap<>();
    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, this);

        config.options().copyDefaults(true);
        saveConfig();


        for(String d : config.getConfigurationSection("drop").getKeys(true)){
            drop.put(Material.valueOf(d.toUpperCase()), config.getInt("drop." + d));
            //getLogger().info(Material.valueOf(d.toUpperCase()).toString() + " - " + config.getInt("drop." + d));
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

        if(args.length > 0){
            return false;
        }

        if(label.equalsIgnoreCase("cobblex")) {
            Player p = (Player) sender;
            Inventory inv = p.getInventory();
            if(!inv.containsAtLeast(new ItemStack(Material.COBBLESTONE), 576)){
                p.sendMessage("Aby stworzyć cobblexa musisz mieć co najmniej 9 staków cobbla!");
                return true;
            }

            //for(int i = 0; i < 9; i++) {
                inv.removeItem(new ItemStack(Material.COBBLESTONE, 576));
            //}
            p.sendMessage("Stworzyłeś cobblexa!");
            ItemStack cobblex = new ItemStack(Material.COBBLESTONE);
            ItemMeta itemMeta = cobblex.getItemMeta();
            itemMeta.setDisplayName(ChatColor.RESET + "Cobblex");
            itemMeta.addEnchant(Enchantment.DURABILITY, 10, true);
            cobblex.setItemMeta(itemMeta);
            inv.addItem(cobblex);
        }

        return true;

    }


    @EventHandler
    public void onCobblexPlace(BlockPlaceEvent e) {
        ItemMeta itemMeta = e.getItemInHand().getItemMeta();
        if(itemMeta.hasEnchant(Enchantment.DURABILITY) && itemMeta.getDisplayName().equals("Cobblex")){
            Block block = e.getBlock();
            Location centerOfBlock = block.getLocation().add(0.5, 0.5, 0.5);
            block.setType(Material.AIR);
            for (Material m : drop.keySet()) {
                int percentage = drop.get(m);
                if (getRandom(1, 100) <= drop.get(m)) {
                    block.getWorld().dropItemNaturally(centerOfBlock, new ItemStack(m, getRandom(1, 10)));
                }
            }
        }
    }

}
