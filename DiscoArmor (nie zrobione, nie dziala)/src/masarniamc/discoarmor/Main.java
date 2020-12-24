package masarniamc.discoarmor;

import io.netty.handler.codec.redis.ArrayRedisMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Main extends JavaPlugin implements CommandExecutor,Listener {
    public static String nl = "\n";
    public static ItemStack[] armorTypes = new ItemStack[4];
    public static String displayName = ChatColor.DARK_RED + "D" + ChatColor.GOLD + "I" + ChatColor.YELLOW + "S" + ChatColor.DARK_GREEN + "C" + ChatColor.BLUE + "O " + ChatColor.LIGHT_PURPLE + "Z" + ChatColor.DARK_PURPLE + "B" + ChatColor.DARK_AQUA + "R" + ChatColor.RED + "O" + ChatColor.GRAY + "J" + ChatColor.WHITE + "A";
    public static HashMap<Player, Integer> equippedDiscoArmor = new HashMap<Player, Integer>();


    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, this);

        armorTypes[0] = new ItemStack(Material.LEATHER_BOOTS);
        armorTypes[1] = new ItemStack(Material.LEATHER_LEGGINGS);
        armorTypes[2] = new ItemStack(Material.LEATHER_CHESTPLATE);
        armorTypes[3] = new ItemStack(Material.LEATHER_HELMET);


        ItemMeta itemMeta = armorTypes[0].getItemMeta();
        itemMeta.setDisplayName(displayName);
        armorTypes[0].setItemMeta(itemMeta);

        itemMeta = armorTypes[1].getItemMeta();
        itemMeta.setDisplayName(displayName);
        armorTypes[1].setItemMeta(itemMeta);

        itemMeta = armorTypes[2].getItemMeta();
        itemMeta.setDisplayName(displayName);
        armorTypes[2].setItemMeta(itemMeta);

        itemMeta = armorTypes[3].getItemMeta();
        itemMeta.setDisplayName(displayName);
        armorTypes[3].setItemMeta(itemMeta);


        for(Player p : Bukkit.getOnlinePlayers()){
            if(isWearingDiscoArmor(p)){
                wearArmor(p);
            }
        }
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("discoarmor")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if(!p.hasPermission("discoarmor")){
                    p.sendMessage("Komenda dostepna tylko dla adminow!");
                    return true;
                }
                if (args.length == 0) {
                    for(ItemStack itemStack : armorTypes){
                        p.getInventory().addItem(itemStack);
                    }
                } else {
                    return false;
                }
            } else {
                Bukkit.getLogger().info("Komenda dostepna tylko z klienta gry!");
            }
        }
        return true;
    }



    @Override
    public void onDisable() {
        super.onDisable();
        for(Player p : Bukkit.getOnlinePlayers()) {
            if (isWearingDiscoArmor(p)) {
                takeOffArmor(p);
            }
        }
    }


    /*
    @EventHandler
    public void onDiscoArmorEquip(InventoryClickEvent e) {
        ItemStack itemStack = e.getInventory().getItem(e.getSlot());
        if(e.getSlotType() == InventoryType.SlotType.ARMOR){
            if(itemStack != null){
                getLogger().info(itemStack.toString());
            }
        }



        if (slot > 35 && slot < 40) {
        ItemStack currentItem = e.getCursor();


            if (e.getSlotType().equals(InventoryType.SlotType.ARMOR)) {
                if (p.getInventory().getHelmet() == null) {
                    if(currentItem != null) {
                        ItemMeta itemMeta = currentItem.getItemMeta();
                        assert itemMeta != null;
                        if(itemMeta.hasDisplayName()) {
                            String currentDisplayName = itemMeta.getDisplayName();
                            if (currentDisplayName.equals(displayName)) {
                                getLogger().info(currentDisplayName);
                                //wearArmor(p);
                            }
                        }
                    }
                }

        }
        }

    }
    */


    public void wearArmor(Player p){
        equippedDiscoArmor.put(p, Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if(!isWearingDiscoArmor(p)){
                    takeOffArmor(p);
                } else {
                    changeArmorColor(p);
                }
            }
        }, 0L, 20L));
    }


    public void changeArmorColor(Player p){
        ItemStack armor = p.getEquipment().getHelmet();
        LeatherArmorMeta meta =  (LeatherArmorMeta) armor.getItemMeta();
        Color color = meta.getColor();
        if (Color.RED.equals(color)) {
            meta.setColor(Color.GREEN);
        } else if (Color.GREEN.equals(color)) {
            meta.setColor(Color.RED);
        }
    }



    public void takeOffArmor(Player p) {
        Bukkit.getScheduler().cancelTask(equippedDiscoArmor.get(p));
        equippedDiscoArmor.remove(p);
    }



    public boolean isWearingDiscoArmor(Player p){
        for(int slot = 36; slot < 40; slot++){
            if(p.getInventory().getItem(slot) != null) {
                if (p.getInventory().getItem(slot).getItemMeta().getDisplayName().equals(displayName)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }


    @EventHandler
    public void onJoin(PlayerLoginEvent e) {
        Player p = e.getPlayer();
        if(isWearingDiscoArmor(p)){
            wearArmor(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(isWearingDiscoArmor(p)) {
            takeOffArmor(p);
        }
    }



}
