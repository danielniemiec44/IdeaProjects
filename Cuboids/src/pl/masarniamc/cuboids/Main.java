package pl.masarniamc.cuboids;

import masarniamc.sqllibrary.SQLConnection;
import masarniamc.sqllibrary.SQLQuery;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;


public class Main extends JavaPlugin implements CommandExecutor, Listener, TabCompleter {
    static Logger console;
    FileConfiguration config = getConfig();
    public static LinkedHashMap<String, String> arguments0 = new LinkedHashMap<>();
    public static Queue<String> queryQueue = new LinkedList<String>();
    public static HashMap<Player, BukkitTask> waitingToBeTeleported = new HashMap<>();

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, this);

        config.options().copyDefaults(true);
        saveConfig();

        arguments0.put("create", "Creates new guild.");
        arguments0.put("remove", "Removes your guild.");
        arguments0.put("home", "Teleports you to your guild home.");
        arguments0.put("sethome", "Set's home of your guild.");
        arguments0.put("trust" , "Adds player to your guild.");
        arguments0.put("untrust" , "Removes player from your guild.");
        arguments0.put("members" , "Lists members of your guild.");

        try {
            SQLConnection conn = new SQLConnection("jdbc:mysql://127.0.0.1/cuboids?useSSL=false", "root", "zaq1@WSX");
            ResultSet rs = conn.getResults("select * from cuboids");
            while(rs.next()) {
                new Cuboid(Bukkit.getOfflinePlayer(UUID.fromString(rs.getString("owner"))), rs.getString("tag"), rs.getString("name"), rs.getInt("size"), rs.getInt("x"), rs.getInt("z"), rs.getString("owner"), rs.getInt("home_x"), rs.getInt("home_y"), rs.getInt("home_z"), false);
            }
            rs = conn.getResults("select * from members");
            while(rs.next()) {
                Cuboid.getByTag(rs.getString("tag")).addMember(rs.getString("member"), false);
            }
            conn.close();
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                SQLConnection conn = new SQLConnection("jdbc:mysql://127.0.0.1/cuboids?useSSL=false", "root", "zaq1@WSX");
                while(queryQueue.peek() != null){
                    conn.execute(queryQueue.poll());
                }
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }, 0L, 50L);

        getCommand("guild").setTabCompleter(this);
    }

    @Override
    public void onDisable() {
        for(Cuboid cuboid : Cuboid.getAll()){
            cuboid.getBossBar().removeAll();
            cuboid.getBossBar().setVisible(false);
        }
        super.onDisable();
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        //if(command.getLabel().equalsIgnoreCase("guild") || command.getLabel().equalsIgnoreCase("g")) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if(args.length > 1) {
                ArrayList<String> arrayList = new ArrayList<>();
                Cuboid cuboid = Cuboid.getByOwner(p.getUniqueId().toString());
                if (args[0].equalsIgnoreCase("home")) {
                    if (cuboid != null) {
                        arrayList.add(cuboid.getTag());
                    }
                    for (Cuboid c : Cuboid.getAll()) {
                        if (c.getMembers().contains(p.getUniqueId().toString())) {
                            arrayList.add(c.getTag());
                        }
                    }
                    return arrayList;
                } else if (args[0].equalsIgnoreCase("untrust")) {
                    if (cuboid != null) {
                        for(String uuid : cuboid.getMembers()){
                            arrayList.add(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
                        }
                    }
                }
                return arrayList;
            }
        }
        return new ArrayList<String>(arguments0.keySet());
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            console.info("Command is available only in-game.");
            return true;
        }
        Player p = (Player) sender;

        cancelTeleport(p);

        if(label.equalsIgnoreCase("guild") || label.equalsIgnoreCase("g")) {
            if (args.length < 1) {
                StringBuilder message = new StringBuilder("\n" + ChatColor.GREEN + "--------------" + ChatColor.RESET + " Guild commands: " + ChatColor.GREEN + " ------------------------\n");
                for (String arg : arguments0.keySet()) {
                    message.append(ChatColor.RED + " - " + ChatColor.DARK_GRAY + "/" + ChatColor.GRAY).append(arg).append(ChatColor.RESET + "  -  ").append(arguments0.get(arg)).append("\n");
                }
                message.append(ChatColor.GREEN + "-----------------------------------------------------\n ");
                p.sendMessage(String.valueOf(message));

            } else if (args[0].equalsIgnoreCase("create")) {
                if(Cuboid.getByOwner(p.getUniqueId().toString()) != null){
                    p.sendMessage("You already have a guild!");
                    return true;
                }
                if(args.length < 3) {
                    p.sendMessage("You have to specify tag and name of the guild!");
                } else if(args.length == 3) {
                    //p.sendMessage(Cuboid.getAll().get(0).getOwner());
                    if(!p.getLocation().getWorld().getName().equals(config.getString("world"))){
                        p.sendMessage("The world you are in is not the survival world!");
                        return true;
                    }

                    Location spawnLoc = Bukkit.getWorld(config.getString("world")).getSpawnLocation();
                    int spawnX = spawnLoc.getBlockX();
                    int spawnZ = spawnLoc.getBlockZ();
                    int spawnSize = config.getInt("spawn-size");
                    int fromX = spawnX - (spawnSize / 2);
                    int fromZ = spawnZ - (spawnSize / 2);
                    int toX = spawnX + (spawnSize / 2);
                    int toZ = spawnZ + (spawnSize / 2);
                    Location loc = p.getLocation();
                    int x = loc.getBlockX();
                    int z = loc.getBlockZ();
                    //p.sendMessage(fromX + ", " + fromZ + ", " + toX + ", " + toZ + ", ");
                    if(x >= fromX && z >= fromZ && x <= toX && z <= toZ) {
                        p.sendMessage("You have to go outside spawn! Go " + (spawnSize / 2) + " blocks ahead from spawn to create a guild!");
                        return true;
                    }

                    int size = config.getInt("size");
                    fromX = x - (size / 2);
                    fromZ = z - (size / 2);
                    toX = x + (size / 2);
                    toZ = z + (size / 2);

                    if(Cuboid.isInAnyGuild(new Location(Bukkit.getWorld(config.getString("world")), toX, p.getLocation().getBlockY(), toZ)) != null || Cuboid.isInAnyGuild(new Location(Bukkit.getWorld(config.getString("world")), toX, p.getLocation().getBlockY(), fromZ)) != null || Cuboid.isInAnyGuild(new Location(Bukkit.getWorld(config.getString("world")), fromX, p.getLocation().getBlockY(), toZ)) != null || Cuboid.isInAnyGuild(new Location(Bukkit.getWorld(config.getString("world")), fromX, p.getLocation().getBlockY(), fromZ)) != null){
                        p.sendMessage("You can't create a guild at your position because there is another guild nearby!");
                        return true;
                    }



                    if(args[1].length() < 3 || args[1].length() > 5) {
                        p.sendMessage("Guild tag has to have minimum 3 and maximum 5 letters!");
                        return true;
                    }
                    if(args[2].length() < 5 || args[2].length() > 20) {
                        p.sendMessage("Guild name has to have minimum 5 and maximum 10 letters!");
                        return true;
                    }
                    ItemStack itemStack = new ItemStack(Material.PAPER, 10);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setDisplayName("§a§lMasarnia§1§lMC §3§lCash§2§l$");
                    itemMeta.addEnchant(Enchantment.DURABILITY, 10, true);
                    itemStack.setItemMeta(itemMeta);

                    p.getInventory().addItem(itemStack);

                    if(!p.getInventory().containsAtLeast(itemStack, config.getInt("cost"))){
                        p.sendMessage("You don't have at least " + config.getInt("cost") + " currency!");
                        return true;
                    }

                    if(Cuboid.getByTag(args[1].toUpperCase()) != null){
                        p.sendMessage("Guild with that tag already exists!");
                        return true;
                    }



                    try {
                        new Cuboid(p, args[1].toUpperCase(), args[2], config.getInt("size"), p.getLocation().getBlockX(), p.getLocation().getBlockZ(), p.getUniqueId().toString(), p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ(), true);
                        p.getInventory().removeItem(itemStack);
                        p.sendMessage("Guild " + args[1].toUpperCase() + " - " + args[2] + " has been successfully created!");

                    } catch (Exception throwables) {
                        p.sendMessage("Unexpected error occurred, please contact to staff!");
                        throwables.printStackTrace();
                    }
                } else {
                    return false;
                }
            } else if(args[0].equalsIgnoreCase("remove")) {
                if (args.length > 1) {
                    return false;
                }
                if (!Cuboid.getAll().stream().anyMatch(cuboid -> {
                    if (cuboid.getOwner().equalsIgnoreCase(p.getUniqueId().toString())) {
                        return true;
                    } else {
                        return false;
                    }
                })) {
                    p.sendMessage("You don't have a guild!");
                    return true;
                }

                try {
                    Cuboid.getByOwner(p.getUniqueId().toString()).remove(true);
                } catch (Exception throwables) {
                    throwables.printStackTrace();
                }

            } else if(args[0].equalsIgnoreCase("sethome")) {
                Cuboid cuboid = Cuboid.getByOwner(p.getUniqueId().toString());
                if(cuboid == null){
                    p.sendMessage("You don't have a guild!");
                    return true;
                }

                if(!p.getLocation().getWorld().getName().equals(config.getString("world"))){
                    p.sendMessage("The world you are in is not the survival world!");
                    return true;
                }

                if(cuboid.isLocationInGuild(p.getLocation())){
                    try {
                        Main.queryQueue.add(new SQLQuery(SQLQuery.Command.UPDATE, "cuboids", "home_x = \"" + p.getLocation().getBlockX() + "\", home_y=\"" + p.getLocation().getBlockY() + "\", home_z=\"" + p.getLocation().getBlockZ() + "\"", "owner=\"" + cuboid.getOwner() + "\"").getQuery());

                        cuboid.setHome(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
                        p.sendMessage("Your guild home has been moved to your position!");
                    } catch (Exception throwables) {
                        throwables.printStackTrace();
                    }
                } else {
                    p.sendMessage("You are outside your guild grounds!");
                }
            } else if(args[0].equalsIgnoreCase("home")) {
                if(args.length > 2){
                    return false;
                }
                if(args.length == 1) {
                    Cuboid cuboid = Cuboid.getByOwner(p.getUniqueId().toString());
                    if (cuboid == null) {
                        p.sendMessage("You don't have a guild!");
                        return true;
                    }
                    cuboid.cooldownBeforeTeleport(p);
                } else {
                    String tag = args[1].toUpperCase();
                    Cuboid cuboid = Cuboid.getByTag(tag);
                    if(cuboid == null){
                        p.sendMessage("Guild with tag " + tag + " does not exist!");
                        return true;
                    }
                    if(!cuboid.getMembers().contains(p.getUniqueId().toString()) && !cuboid.getOwner().equals(p.getUniqueId().toString())){
                        p.sendMessage("Guild " + tag + " does not trust you!");
                        return true;
                    }
                   cuboid.cooldownBeforeTeleport(p);
                }
            } else if(args[0].equalsIgnoreCase("trust")) {
                if (args.length != 2) {
                    return false;
                }
                Cuboid cuboid = Cuboid.getByOwner(p.getUniqueId().toString());
                if (cuboid == null) {
                    p.sendMessage("You don't have a guild!");
                    return true;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (cuboid.getMembers().contains(offlinePlayer.getUniqueId().toString())) {
                    p.sendMessage("Player " + args[1] + " is already a member of your guild!");
                    return true;
                }
                if (!offlinePlayer.hasPlayedBefore()) {
                    p.sendMessage("Player " + args[1] + " has never played on this server!");
                    return true;
                }
                cuboid.addMember(offlinePlayer.getUniqueId().toString(), true);
                p.sendMessage("Player " + offlinePlayer.getName() + " has been successfully added to your guild!");
            } else if(args[0].equalsIgnoreCase("untrust")) {
                if(args.length != 2){
                    return false;
                }
                Cuboid cuboid = Cuboid.getByOwner(p.getUniqueId().toString());
                if (cuboid == null) {
                    p.sendMessage("You don't have a guild!");
                    return true;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
                if (cuboid.getMembers().contains(offlinePlayer.getUniqueId().toString())) {
                    cuboid.removeMember(offlinePlayer.getUniqueId().toString(), true);
                    p.sendMessage("Player " + args[1] + " is no longer a member of your guild!");
                    return true;
                }
                if (!offlinePlayer.hasPlayedBefore()) {
                    p.sendMessage("Player " + args[1] + " has never played on this server!");
                    return true;
                }
                p.sendMessage("Player " + args[1] + " is not a member of your guild!");
            } else if(args[0].equalsIgnoreCase("members")) {
                if (args.length > 2) {
                    return false;
                }
                Cuboid cuboid = Cuboid.getByOwner(p.getUniqueId().toString());
                if (cuboid == null) {
                    p.sendMessage("You don't have a guild!");
                    return true;
                }
                ArrayList<String> members = cuboid.getMembers();
                if(members.isEmpty()) {
                    p.sendMessage("You don't have any member in your guild!");
                    return true;
                }
                ArrayList<String> m = new ArrayList<>();
                for(String uuid : cuboid.getMembers()) {
                    m.add(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
                }

                p.sendMessage("Members: " + String.join(", ", m));
                return true;
            } else {
                return false;
            }

            return true;
        }



        return true;

    }





    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        cancelTeleport(p);
        Location loc = p.getLocation();
        if (e.getTo().getWorld().getName().equals(config.getString("world"))) {
            Cuboid cuboid = Cuboid.isInAnyGuild(loc);
            if (cuboid != null) {
                //p.sendMessage("You entered guild " + cuboid.getTag() + " - " + cuboid.getName() + "!");
                cuboid.getBossBar().addPlayer(p);
                return;
            } else {
                //p.sendMessage("You leaved " + cuboid.getTag() + " - " + cuboid.getName() + " guild grounds!");
                for (Cuboid c : Cuboid.getAll())
                    c.getBossBar().removePlayer(p);
                return;
            }
        }
    }


    public void cancelTeleport(Player p) {
        if(waitingToBeTeleported.containsKey(p)){
            waitingToBeTeleported.get(p).cancel();
            waitingToBeTeleported.remove(p);
            p.sendMessage("Teleport has been canceled!");
        }
    }


    public boolean check(Player p, Location loc) {
        if(!loc.getWorld().getName().equals(config.getString("world"))){
            return false;
        }
        for(Cuboid cuboid : Cuboid.getAll()) {
            int fromX = cuboid.getFromX();
            int fromZ = cuboid.getFromZ();
            int toX = cuboid.getToX();
            int toZ = cuboid.getToZ();

            if (cuboid.isLocationInGuild(loc)) {
                if (cuboid.getOwner().equals(p.getUniqueId().toString()) || p.hasPermission("cuboid.bypass")) {
                    return false;
                }
                if(cuboid.getMembers().contains(p.getUniqueId().toString())){
                    return false;
                }
                //p.sendMessage("This area belongs to the " + cuboid.getTag() + " - " + cuboid.getName() + " guild");
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("This area belongs to the " + cuboid.getTag() + " - " + cuboid.getName() + " guild"));
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerLoginEvent e){
        Player p = e.getPlayer();
        Location loc = p.getLocation();
        Cuboid cuboid = Cuboid.isInAnyGuild(loc);
        if(cuboid != null){
            cuboid.getBossBar().addPlayer(p);
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        for (Cuboid c : Cuboid.getAll()) {
            c.getBossBar().removePlayer(p);
            return;
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        cancelTeleport(p);
        waitingToBeTeleported.remove(p);
    }

    /*
    public void onPlayerQuit(PlayerQuitEvent e){
        Player p;
        Cuboid cuboid = Cuboid.getAll().stream().filter(cuboid -> {
            if (cuboid.areCoordsInGuild(loc.getBlockX(), loc.getBlockZ())) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList()).get(0);
        cuboid.getBossBar().removePlayer(p);
    }

     */




    /*
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if(a(e.getPlayer(), e.getBlock().getLocation())){
            e.setCancelled(true);
        }
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if(a(e.getPlayer(), e.getBlock().getLocation())){
            e.setCancelled(true);
        }
    }
*/





    @EventHandler (priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        Block block = e.getClickedBlock();
        Player p = e.getPlayer();
        cancelTeleport(p);
        if(block == null) return;
        boolean check = check(p, block.getLocation());
            if (check) {
                e.setCancelled(check);
            }
        //}
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if(damager instanceof Player) {
            Player p = (Player) damager;
            cancelTeleport(p);
            Entity entity = e.getEntity();
            if(entity instanceof Player) {
                cancelTeleport(((Player) entity));
            }
            boolean check = check(p, entity.getLocation());
            if (check) {
                if (!(entity instanceof Monster || entity instanceof Player)) {
                    e.setCancelled(true);
                }
            }
        }
    }






/*
    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent e){
        e.setCancelled(a((Player) e.getPlayer(), e.getInventory().getLocation()));
    }


    @EventHandler
    public void onInventoryOpenEvent(EntityDamageByEntityEvent e){
        e.setCancelled(a((Player) e.getDamager(), e.getEntity().getLocation()));
    }

     */



}
