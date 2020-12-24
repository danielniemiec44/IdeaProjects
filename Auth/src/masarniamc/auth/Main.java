package masarniamc.auth;

import masarniamc.sqllibrary.SQLConnection;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import pl.masarniamc.guilibrary.Title;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

public class Main extends JavaPlugin implements Listener {

    public static ArrayList<UUID> unLoggedPlayers = new ArrayList<>();
    public static HashMap<UUID, String> waitingToConfirmPassword = new HashMap<>();
    public PluginManager pm = this.getServer().getPluginManager();
    public static String url = "jdbc:mysql://localhost/masarniamc?useSSL=false";
    public static String username = "root";
    public static String password = "zaq1@WSX";


    @Override
    public void onEnable() {
        super.onEnable();
        pm.registerEvents(this, this);
    }

            @EventHandler(priority = EventPriority.MONITOR)
            public void onJoin(PlayerJoinEvent e) throws SQLException {
                Player p = e.getPlayer();
                p.setGameMode(GameMode.SPECTATOR);
                unLoggedPlayers.add(p.getUniqueId());
                SQLConnection conn = new SQLConnection(url, username, password);
                ResultSet results = conn.getResults("select * from users where uuid='" + p.getUniqueId().toString() + "'");
                if(results.first()){
                    new Title("§aZaloguj sie", Title.Type.TITLE, 5, 99999, 5).show(p);
                } else {
                    new Title("§aZarejestruj sie", Title.Type.TITLE, 5, 99999, 5).show(p);
                }
                new Title("§aWprowadz haslo na czacie", Title.Type.SUBTITLE, 5, 99999, 5).show(p);

                conn.close();

            }


            @EventHandler
            public void onPasswordEnter(AsyncPlayerChatEvent e){
                Player p = e.getPlayer();
                try {
                    if (unLoggedPlayers.contains(p.getUniqueId())) {
                        String pass = e.getMessage();
                        final MessageDigest digest = MessageDigest.getInstance("SHA-512");
                        final byte[] hashbytes = digest.digest(pass.getBytes(StandardCharsets.UTF_8));
                        String sha_256hex = bytesToHex(hashbytes);

                        SQLConnection conn = new SQLConnection(url, username, password);
                        ResultSet results = conn.getResults("select password from users where uuid='" + p.getUniqueId().toString() + "'");
                        if(results.first()) {
                            if (results.getString("password").equals(sha_256hex)) {
                                new Title("", Title.Type.TITLE, 0, 1, 0).show(p);
                                new Title("", Title.Type.SUBTITLE, 0, 1, 0).show(p);
                                unLoggedPlayers.remove(p.getUniqueId());
                                p.sendMessage("Zalogowano.");
                                changeGamemode(p,GameMode.SURVIVAL);
                            } else {
                                p.sendMessage("Haslo nieprawidlowe.");
                            }
                        } else {
                            if(!waitingToConfirmPassword.containsKey(p.getUniqueId())) {
                                if (pass.length() < 5 || !Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$").matcher(pass).matches()) {
                                    p.sendMessage("Haslo musi zawierac co najmniej 5 znakow, 1 wielka litere, 1 cyfre i 1 znak specjalny!");
                                } else {
                                    p.sendMessage("Wprowadz ponownie haslo.");
                                    waitingToConfirmPassword.put(p.getUniqueId(), pass);
                                }
                            } else {
                                if(pass.equals(waitingToConfirmPassword.get(p.getUniqueId()))) {
                                    conn.execute("insert into users values('" + p.getUniqueId().toString() + "','" + p.getName() + "','" + sha_256hex + "');");
                                    new Title("", Title.Type.TITLE, 0, 1, 0).show(p);
                                    new Title("", Title.Type.SUBTITLE, 0, 1, 0).show(p);
                                    unLoggedPlayers.remove(p.getUniqueId());
                                    p.sendMessage("Zarejestrowano!");
                                    changeGamemode(p,GameMode.SURVIVAL);

                                }
                            }
                        }
                        conn.close();
                        e.setCancelled(true);
                    }
                } catch (Exception exception){
                    exception.printStackTrace();
                    p.sendMessage("Wystapil nieoczekiwany blad. Prosimy o kontakt z administracja serwera.");
                    e.setCancelled(true);
                }
            }

            public void changeGamemode(Player p, GameMode gm){
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                p.setGameMode(gm);
            }
        }, 0L);
            }

            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent e) throws Exception {
                Player p = e.getPlayer();
                if(unLoggedPlayers.contains(p.getUniqueId())){
                    unLoggedPlayers.remove(p.getUniqueId());
                }
                if(waitingToConfirmPassword.containsKey(p.getUniqueId())){
                    waitingToConfirmPassword.remove(p.getUniqueId());
                }
            }



            @EventHandler
            public void onUnloggedPlayerInteract(PlayerInteractEvent e){
                Player p = e.getPlayer();
                if(unLoggedPlayers.contains(p.getUniqueId())){
                    e.setCancelled(true);
                }
            }

            @EventHandler
            public void onMobTriggerUnloggedPlayer(EntityTargetLivingEntityEvent e){
                LivingEntity livingEntity = e.getTarget();
                if(livingEntity instanceof Player){
                    Player p = (Player) livingEntity;
                    if(unLoggedPlayers.contains(p.getUniqueId())){
                        e.setCancelled(true);
                    }
                }
            }


    @EventHandler
    public void onUnloggedPlayerTeleport(PlayerTeleportEvent e){
        Player p = e.getPlayer();
        if(unLoggedPlayers.contains(p.getUniqueId())){
            e.setCancelled(true);
        }
    }


            //TODO: Zablokowac granaty.


            @EventHandler
            public void onEntityDamageByEntity(EntityDamageByEntityEvent e){
                if(e.getDamager() instanceof Player){
                    Player p = (Player) e.getDamager();
                    if(unLoggedPlayers.contains(p.getUniqueId())){
                        e.setCancelled(true);
                    }
                }
            }

//TODO: bug gdy gracz znajduje sie w lawie odbierane sa 2 serca hp nie wiadomo czemu
    //TODO: gracz moze wyrzucac przedmioty z eq


    @EventHandler
    public void onEntityDamage(EntityDamageEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            if(unLoggedPlayers.contains(p.getUniqueId())){
                e.setCancelled(true);
            }
        }
    }




            @EventHandler
            public void onMove(PlayerMoveEvent e) {
                if (unLoggedPlayers.contains(e.getPlayer().getUniqueId())) {
                    e.setCancelled(true);
                }
            }


    public void onUnloggedPlayerHealthRegain(EntityRegainHealthEvent e) {
        if(e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (unLoggedPlayers.contains(p.getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }


    /*
    public void onUnloggedPlayerTabCompleteEvent(TabCompleteEvent e) {
            if (unLoggedPlayers.contains(Bukkit.getPlayer(e.getSender().getName()).getUniqueId())) {
                e.setCancelled(true);
            }
    }
     */



            @EventHandler
            public void onCommand(PlayerCommandPreprocessEvent e) {
                if (unLoggedPlayers.contains(e.getPlayer().getUniqueId())) {
                    e.getPlayer().sendMessage("Musisz być zalogowany aby móć używać komend!");
                    e.setCancelled(true);
                }
            }

    @EventHandler
        public void onPlayerInteractEvent(PlayerInteractEvent e) {
        if (unLoggedPlayers.contains(e.getPlayer().getUniqueId())) {
            e.getPlayer().sendMessage("Musisz być zalogowany aby móć wykonać tą akcję!");
            e.setCancelled(true);
        }
    }

    @EventHandler
        public void onPlayerInventoryInteract(InventoryClickEvent e) {
        if (unLoggedPlayers.contains(e.getWhoClicked().getUniqueId())) {
                e.setCancelled(true);
        }
    }


    /*
    @EventHandler
    public void onPlayerChangeWorldEvent(PlayerChangedWorldEvent e) {
        if (unLoggedPlayers.contains(e.getPlayer().getUniqueId())) {
            e.s;
        }
    }
     */


            //TODO: Dokonczyc!!!
    /*
    @EventHandler
    public void onInventory(InventoryMoveItemEvent e){
        if(unLoggedPlayers.contains(((Player)(e.get)).getUniqueId())){
            e.setCancelled(true);
        }
    }
     */






            private static String bytesToHex(byte[] hash) {
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString();
            }


    @Override
    public void onDisable() {
        super.onDisable();
    }



}
