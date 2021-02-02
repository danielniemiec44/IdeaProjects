package pl.masarniamc.cuboids;


import masarniamc.sqllibrary.SQLQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class Cuboid {
    private String tag;
    private String name;
    private int size;
    private int x;
    private int z;
    private String owner;

    private int fromX;
    private int fromZ;
    private int toX;
    private int toZ;

    private int homeX;
    private int homeY;
    private int homeZ;

    private BossBar bossBar;


    static ArrayList<Cuboid> cuboids = new ArrayList<>();
    public static HashMap<String, ArrayList<Cuboid>> members = new HashMap<String, ArrayList<Cuboid>>();
    public static FileConfiguration config = Main.getPlugin(Main.class).getConfig();

    Cuboid(OfflinePlayer p, String tag, String name, int size, int x, int z, String owner, int homeX, int homeY, int homeZ, boolean save) throws Exception {
        if(save) {
            Player player = (Player) p;
            Main.queryQueue.add(new SQLQuery(SQLQuery.Command.INSERT, "cuboids", tag.toUpperCase(), name, config.getInt("size"), player.getLocation().getBlockX(), player.getLocation().getBlockZ(), player.getUniqueId().toString(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()).getQuery());
        }

        this.tag = tag;
        this.name = name;
        this.size = size;
        this.x = x;
        this.z = z;
        this.owner = owner;

        this.fromX = x - (size / 2);
        this.fromZ = z - (size / 2);
        this.toX = x + (size / 2);
        this.toZ = z + (size / 2);

        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;

        this.bossBar = Bukkit.createBossBar("You're standing on " + tag + " - " + name + " guild grounds!", BarColor.RED, BarStyle.SOLID);

        this.bossBar.setVisible(true);

        cuboids.add(this);
    }

    public static ArrayList<Cuboid> getAll() {
        return cuboids;
    }

    public String getTag() {
        return tag;
    }

    public String getOwner() {
        return owner;
    }

    /*
    public static void remove(String owner) {
        List<Cuboid> list = Cuboid.getAll().stream().filter(cuboid -> cuboid.getOwner().equals(owner)).collect(Collectors.toList());
        cuboids.remove(list.get(0));
    }

     */

    public int getSize() {
        return size;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public String getName() {
        return name;
    }


    public int getFromX() {
        return fromX;
    }

    public int getFromZ() {
        return fromZ;
    }

    public int getToX() {
        return toX;
    }

    public int getToZ() {
        return toZ;
    }

    public BossBar getBossBar() {
        return bossBar;
    }


    public boolean isLocationInGuild(Location loc) {
        if (loc.getWorld().getName().equals(config.getString("world")) && loc.getBlockX() >= this.fromX && loc.getBlockZ() >= this.fromZ && loc.getBlockX() <= this.toX && loc.getBlockZ() <= this.toZ) {
            return true;
        } else {
            return false;
        }
    }

    public static Cuboid isInAnyGuild(Location loc){
        if(loc.getWorld().getName().equals(config.getString("world"))) {
            for (Cuboid cuboid : Cuboid.getAll()) {
                if (cuboid.isLocationInGuild(loc)) {
                    return cuboid;
                }
            }
        }
        return null;
    }

    public void setHome(int x, int y, int z) {
        this.homeX = x;
        this.homeY = y;
        this.homeZ = z;
    }

    public Location getHome(){
        return new Location(Bukkit.getWorld(config.getString("world")), this.homeX, this.homeY, this.homeZ);
    }

    public static Cuboid getByOwner(String owner) {
        for(Cuboid cuboid : Cuboid.getAll()){
            if (cuboid.getOwner().equals(owner)) {
                return cuboid;
            }
        }
        return null;
    }

    public static Cuboid getByTag(String tag){
        for(Cuboid cuboid : Cuboid.getAll()){
            if (cuboid.getTag().equals(tag.toUpperCase())) {
                return cuboid;
            }
        }
        return null;
    }


    public ArrayList<String> getMembers() {
        ArrayList<String> members = new ArrayList<>();
        for(String uuid : Cuboid.members.keySet()) {
            ArrayList<Cuboid> cuboids = Cuboid.members.get(uuid);
            if(cuboids.contains(this)){
                members.add(uuid);
            }
        }
        return members;
    }

    public void addMember(String uuid, boolean save) {
        try {
            if(save) {
                Main.queryQueue.add(new SQLQuery(SQLQuery.Command.INSERT, "members", uuid, this.tag).getQuery());
            }

            ArrayList<Cuboid> arrayList = new ArrayList<Cuboid>();
            if(Cuboid.members.containsKey(uuid)){
                arrayList = Cuboid.members.get(uuid);
            }
            arrayList.add(Cuboid.getByTag(this.tag));
            Cuboid.members.put(uuid, arrayList);
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void removeMember(String uuid, boolean save) {
        try {
            if(save) {
                Main.queryQueue.add(new SQLQuery(SQLQuery.Command.DELETE, "members", "tag=\"" + this.tag + "\" and member=\"" + uuid + "\"").getQuery());
            }

            for(String s : Cuboid.members.keySet()){
                if(Cuboid.members.get(s).remove(this)) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                    if (offlinePlayer.isOnline()) {
                        ((Player) offlinePlayer).sendMessage("You have been removed from " + this.tag + " - " + this.name + " guild!");
                    }
                    Player owner = Bukkit.getPlayer(this.getOwner());
                    owner.sendMessage("Player " + offlinePlayer.getName() + " has been successfully removed from your guild!");
                }
            }
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }
    }

    public void remove(boolean save) {
        try {
            if(save) {
                Main.queryQueue.add(new SQLQuery(SQLQuery.Command.DELETE, "cuboids", "owner = \"" + this.owner + "\"").getQuery());
            }

            this.bossBar.removeAll();
            this.removeAllMembers();

            cuboids.remove(this);
            Bukkit.getPlayer(UUID.fromString(this.owner)).sendMessage("Your guild has been removed successfully!");
        } catch (Exception throwables) {
            throwables.printStackTrace();
        }

    }

    public void removeAllMembers() {
        for(String member : this.getMembers()) {
            this.removeMember(member, true);
        }
    }

    public void teleport(Player p) {
        Bukkit.getScheduler().runTask(Main.getPlugin(Main.class), () -> {
            p.teleport(new Location(Bukkit.getWorld(config.getString("world")), this.homeX, this.homeY, this.homeZ));
            p.sendMessage("You have been teleported to " + this.tag + " - " + this.name + " guild home!");
        });
    }


    public void cooldownBeforeTeleport(Player p) {
        if (Main.waitingToBeTeleported.containsKey(p)) {
            p.sendMessage("You are already waiting for teleport!");
            return;
        }

        p.sendMessage("Don't move! You will be teleported to guild " + this.tag + " - " + this.name + " in " + config.getInt("teleport-time") + " seconds!");
        Main.waitingToBeTeleported.put(p, Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getPlugin(Main.class), () -> {
            if (Main.waitingToBeTeleported.containsKey(p)) {
                Main.waitingToBeTeleported.remove(p);
                this.teleport(p);
            }
        }, config.getInt("teleport-time") * 20L));
    }


    public void extend() {

    }


}
