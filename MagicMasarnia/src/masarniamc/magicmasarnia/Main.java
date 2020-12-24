package masarniamc.magicmasarnia;

import net.minecraft.server.v1_15_R1.EntityExperienceOrb;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftTippedArrow;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;

public class Main extends JavaPlugin implements CommandExecutor, Listener {

    public static String nl = "\n";
    public static HashMap<Player, String> spellInUse = new HashMap<Player, String>();


    @Override
    public void onEnable(){
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, this);

        for(Player p : Bukkit.getOnlinePlayers()){
            Wand.setWandOnSlot(p);
        }
    }

    public void onDisable() {
        for(Player p : Bukkit.getOnlinePlayers()){
            WandMode wandMode = WandMode.getPlayerWandMode(p);
            if(wandMode != null){
                wandMode.quitWandMode();
            }
        }




        super.onDisable();
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("magicmasarnia") || label.equalsIgnoreCase("mm")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (args.length == 0) {
                    if (p.hasPermission("magicmasarnia.admin")) {
                        String pomoc = nl + "MagicMasarnia Pomoc:" + nl;
                        pomoc += "/mm admin";
                        p.sendMessage(pomoc);
                    } else {
                        p.sendMessage("Komenda dostepna tylko dla adminow!");
                    }
                } else {
                    if (args[0].equalsIgnoreCase("admin")) {
                        if(args.length > 5){
                            return false;
                        } else if(args.length < 2 && args[0].equalsIgnoreCase("admin")) {
                            String pomoc = nl + "MagicMasarnia Admin Pomoc:" + nl
                                    + "/mm admin wand - Gives wand to player" + nl;
                            p.sendMessage(pomoc);
                        } else if(args.length < 4 && args[1].equalsIgnoreCase("wand")) {
                            String pomoc = nl + "MagicMasarnia Admin Pomoc:" + nl
                                    + "/mm admin wand set <player> normal- Gives wand to player" + nl;
                            p.sendMessage(pomoc);
                        } else if(args.length < 5 && args[2].equalsIgnoreCase("set")) {
                            String pomoc = nl + "MagicMasarnia Admin Pomoc:" + nl
                                    + "/mm admin wand set <player> normal - Gives wand to player" + nl;
                            p.sendMessage(pomoc);
                        } else if(args.length < 6 && args[4].equalsIgnoreCase("normal")) {
                            //getLogger().info(offlinePlayer.toString());
                            Player targetPlayer = Bukkit.getPlayer(args[3]);
                            if (targetPlayer == null) {
                                p.sendMessage("Ten gracz nie jest online!");
                            } else {
                                Wand.setPlayerWand(targetPlayer, WandType.NORMAL);
                                Wand.setWandOnSlot(targetPlayer);
                                //p.sendMessage("pass");
                            }
                        }
                    } else {
                        return false;
                    }
                }
            } else {
                Bukkit.getLogger().info("Komenda dostepna tylko z klienta gry!");
            }
        }
            return true;
    }



    @EventHandler
    public void wandMoveBlocker(InventoryClickEvent e) {
        if(e.getView() == e.getWhoClicked().getOpenInventory()) {
                Player p = Bukkit.getPlayer(e.getWhoClicked().getName());
                //p.sendMessage("Clicked slot: " + e.getSlot());
                //p.sendMessage("Clicked raw slot: " + e.getRawSlot());
                if (e.getSlot() == 0 && e.getClickedInventory() == e.getWhoClicked().getInventory()) {
                    e.setCancelled(true);
                }
                if (WandMode.isPlayerInWandMode(p)) {
                    e.setCancelled(true);
                }
        }
    }

    /*
    public void itemDragEvent(InventoryDragEvent e) {
        e.setResult(Event.Result.DENY);
    }
     */


    @EventHandler
    public void switchWandMode(PlayerItemHeldEvent e){
        Player p = e.getPlayer();
        if(e.getNewSlot() == WandMode.wandModeSlot) {
            if(!Wand.hasPlayerWand(p)){
                e.setCancelled(true);
                return;
            }
            if(!WandMode.isPlayerInWandMode(p)) {
                WandMode wandMode = new WandMode(p);
            }
        } else if(e.getNewSlot() == WandMode.quitWandModeSlot) {
            WandMode wandMode = WandMode.getPlayerWandMode(p);
            if(wandMode != null) {
                wandMode.quitWandMode();
            }
        } else if(WandMode.isPlayerInWandMode(p)){
            if(spellInUse.containsKey(p)){
                p.sendMessage("Zaczekaj chwile przed uzyciem kolejnego zaklecia!");
                e.setCancelled(true);
                return;
            }
            switch(e.getNewSlot()){
                case 1:
                    spellInUse.put(p, "Dretwota");
                    break;
                case 7:
                    spellInUse.put(p, "Avada Kedavra");
            }
            p.launchProjectile(Arrow.class);
            Bukkit.getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {
                @Override
                public void run() {
                    spellInUse.remove(p);
                }
            }, 20L);
            e.setCancelled(true);
        }
    }



    @EventHandler
    public void onSpellCast(ProjectileLaunchEvent e) {
        Entity projectile = e.getEntity();
        if(projectile instanceof Arrow){
            Arrow arrow = (Arrow) e.getEntity();
            if(arrow.getShooter() instanceof Player){
                Player shooter = (Player) arrow.getShooter();
                if(spellInUse.containsKey(shooter)){
                    arrow.setMetadata(spellInUse.get(shooter), new FixedMetadataValue(this, true));
                }
            }
        }
    }


    @EventHandler
    public void onSpellHit(ProjectileHitEvent e){
        getLogger().warning(e.getHitEntity() + "");
        if(e.getEntity().hasMetadata("Avada Kedavra")){
            getLogger().warning("dziala");
            if(e.getHitEntity() instanceof LivingEntity){
                ((LivingEntity)e.getHitEntity()).damage(1000);
            }
            ((Arrow) e.getEntity()).remove();

        }
    }

/*
    public void test(EntityDamageByEntityEvent e){

    }
 */



/*
    @EventHandler
    public void cancelMovingItemsInWandMode(InventoryMoveItemEvent e) {
        if(e.getDestination().equals(e.getInitiator())){
            getLogger().info("pass");
            e.setCancelled(true);
        }
    }

 */


    //TODO: Funkcja nie dziala do konca tak jak powinna
    @EventHandler(priority = EventPriority.MONITOR)
    public void disableCollectXPInWandMode(EntityTargetLivingEntityEvent e){
        if(e.getEntityType() == EntityType.EXPERIENCE_ORB){
            if(e.getTarget() instanceof Player){
                Player p = (Player) e.getTarget();
                if(WandMode.isPlayerInWandMode(p)){
                    //getLogger().info("xp");
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlaceInWandMode(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if(WandMode.isPlayerInWandMode(p)){
            p.sendMessage("Nie możesz stawiać bloków w trybie różdżki");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreakInWandMode(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if(WandMode.isPlayerInWandMode(p)){
            p.sendMessage("Nie możesz niszczyć bloków w trybie różdżki");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickupInWandMode(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        if(WandMode.isPlayerInWandMode(p)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDropInWandMode(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if(WandMode.isPlayerInWandMode(p)){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpenInWandMode(InventoryOpenEvent e){
        Player p = (Player) e.getPlayer();
        if(WandMode.isPlayerInWandMode(p)){
            p.sendMessage("Aby wykonać tą akcję wyjdź z trybu różdżki");
            e.setCancelled(true);
        }
    }



    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        e.getPlayer().getInventory().setHeldItemSlot(1);
        WandMode wandMode = WandMode.getPlayerWandMode(p);
        if(wandMode != null) {
            wandMode.quitWandMode();
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        Wand.setWandOnSlot(p);
    }





}
