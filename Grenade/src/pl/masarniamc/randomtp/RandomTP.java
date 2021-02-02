package pl.masarniamc.randomtp;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashMap;

public class RandomTP implements Listener {
    public static HashMap<Chunk, HashMap<Player, Location>> chunksWaitingToBeLoaded = new HashMap<>();

    public RandomTP(Main plugin){
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    public static Location blockToEntityLocation(Location loc){
        World w = loc.getWorld();
        double x = loc.getBlockX() + 0.5;
        double y = loc.getBlockY() + 0.5;
        double z = loc.getBlockZ() + 0.5;

        return new Location(w, x, y, z);
    }

    public static int getHighestBlockY(World w, int x, int z) throws Exception {
        for(int y=255;y>0;y--){
            if(w.getBlockAt(x, y, z).getType().isSolid()) return y;
        }
        throw new Exception("Hole in the floor!");
    }



    public static void teleportRandomly(Player p, String world){
        World w = Bukkit.getWorld(world);
        int x = 0;
        int y = 0;
        int z = 0;
        Location loc = null;
        Chunk chunk = null;
        assert w != null;
        do {
            try {
                x = RandomNumber.getInteger(-1000000, 1000000);
                z = RandomNumber.getInteger(-1000000, 1000000);
                y = getHighestBlockY(w, x, z) + 3;
            } catch (Exception e) {
                p.sendMessage("Unexpected error occurred! Please contact to stuff!");
                e.printStackTrace();
            }

        } while (w.getBiome(x, y, z) == Biome.DEEP_OCEAN || w.getBiome(x, y, z) == Biome.OCEAN);
        loc = new Location(w, x, y, z);
        chunk = w.getChunkAt(x, z);
        HashMap<Player, Location> players;
        if(chunksWaitingToBeLoaded.containsKey(chunk)){
            players = chunksWaitingToBeLoaded.get(chunk);
        } else {
            players = new HashMap<>();
        }
        chunksWaitingToBeLoaded.put(chunk, players);
        if(chunk.isLoaded()){
            teleportPlayer(p, loc);
        } else {
            chunk.load(true);
        }
    }

    public static void teleportPlayer(Player p, Location loc){
        p.teleport(blockToEntityLocation(loc));
        p.sendMessage("Zostales przeteleportowany na koordynaty: " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ());
    }


    /*
    @EventHandler
    void onRTPLag(PlayerTeleportEvent e){
        Player p = e.getPlayer();
        //if(chunksWaitingToBeLoaded.containsValue(HashMap<>))
        //p.sendMessage("y = " + p.getVelocity().getY());
    }
*/

    @EventHandler
    void chunkReadyToTeleport(ChunkLoadEvent e){
        Chunk chunk = e.getChunk();
        if(!chunksWaitingToBeLoaded.containsKey(chunk)){
            return;
        }
        HashMap<Player, Location> players = chunksWaitingToBeLoaded.get(chunk);
        for(Player p : players.keySet()){
            Location loc = players.get(p);
            teleportPlayer(p, loc);
        }
    }

}