package pl.masarniamc.essentials;

import org.apache.logging.log4j.LogManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;


public class Main extends JavaPlugin implements CommandExecutor, Listener {
    static Logger console;
    //public static LinkedHashMap<Material, Integer> drop = new LinkedHashMap<>();
    FileConfiguration config = getConfig();
    String discord = "";
    String website = "";

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, this);

        //Bukkit.getLogger().addHandler(new ConsoleHandler());

        //org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        //logger.addAppender(new LogAppender());


        config.options().copyDefaults(true);
        saveConfig();

        discord = config.getString("discord");
        website = config.getString("website");
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            console.info("Komenda jest dostepna tylko z poziomu klienta gry.");
            return true;
        }

        //Komenda discord
        if(label.equalsIgnoreCase("discord")) {
            if(args.length > 0){
                return false;
            }
            Player p = (Player) sender;
            p.sendMessage(ChatColor.RED + "\nDiscord serwerowy: " + ChatColor.DARK_RED + ChatColor.BOLD + discord + "\n ");
        }

        //Komenda nasza strona
        if(label.equalsIgnoreCase("naszastrona")){
            if(args.length > 0){
                return false;
            }
            Player p = (Player) sender;
            p.sendMessage(ChatColor.RED + "\nNasza strona: " + ChatColor.DARK_RED + ChatColor.BOLD + website + "\n ");
        }

        return true;

    }









    //Blokowanie wysylania linkow
    @EventHandler
    public void onLinkSend(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        message = String.join(" ", message.split("\\."));
        //message = Arrays.toString(message.split("\\."));
        e.setMessage(message);
    }










    //Wlatywanie dropu do EQ
    @EventHandler
    public void onBlockDropItem(BlockDropItemEvent e){
        Player p = e.getPlayer();
        if(!p.hasPermission("masarniamcessentials.drop")){
            return;
        }
        Inventory inv = p.getInventory();
        List<Item> items = e.getItems();
        for(Item item : items){
            inv.addItem(item.getItemStack());
            items.remove(item);
        }
        e.setCancelled(true);
    }








/*
    //Gracze nie moga spasc ze spawnu i dostaja obrazen
    @EventHandler
    public void onFall(PlayerMoveEvent e) {
        Location to = e.getTo();
        if(to.getY() < 0) {
            if (to.getWorld().getName().equalsIgnoreCase("spawn")) {
                e.getPlayer().teleport(to.getWorld().getSpawnLocation());
            }
        }
    }

 */



    /*
    @EventHandler
    public void cancelHurt(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        if(entity.getLocation().getWorld().getName().equalsIgnoreCase("spawn") && entity instanceof Player){
            e.setCancelled(true);
        }
    }

     */








}
