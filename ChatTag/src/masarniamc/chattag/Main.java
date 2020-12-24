package masarniamc.chattag;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {


    @Override
    public void onEnable(){
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, this);


        }


    @Override
    public void onDisable() {
        super.onDisable();
    }


    @EventHandler
    public void onPLayerTagsPlayer(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        for (String word : message.split("\\s+")) {
            for (Player p : e.getRecipients()) {
                if(p.getName().equalsIgnoreCase(word)) {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 10, 1);
                    getLogger().info(message);
                    break;
                }
            }
        }
    }



}
