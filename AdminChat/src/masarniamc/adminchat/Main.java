package masarniamc.adminchat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class Main extends JavaPlugin implements CommandExecutor,Listener {
    public static String nl = "\n";
    public static String pomoc = nl + "Channel Pomoc:" + nl + "/adminchat <on|off>";
    public static ArrayList<Player> adminsOnChannel = new ArrayList<Player>();

    @Override
    public void onEnable(){
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(this, this);
        }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("adminchat")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if(!p.hasPermission("adminchat.switch")){
                    p.sendMessage("Komenda dostepna tylko dla adminow!");
                    return true;
                }
                if (args.length == 0) {
                    p.sendMessage(pomoc);
                } else if(args.length == 1) {
                    switch(args[0].toLowerCase()){
                        case "on":
                            if(adminsOnChannel.contains(p)) {
                                p.sendMessage("Już piszesz na kanale administracyjnym!");
                            } else {
                                adminsOnChannel.add(p);
                                p.sendMessage("Od teraz piszesz na kanale administracyjnym!");
                            }
                            break;
                        case "off":
                            if(adminsOnChannel.contains(p)) {
                                adminsOnChannel.remove(p);
                                p.sendMessage("Od teraz piszesz na kanale dla graczy!");
                            } else {
                                p.sendMessage("Już piszesz na kanale dla graczy!");
                            }
                            break;
                        default:
                            p.sendMessage(pomoc);
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
    }


    @EventHandler
    public void onPLayerChatsOnChannel(AsyncPlayerChatEvent e) {
        Player sender = e.getPlayer();
        if(adminsOnChannel.contains(sender)){
            e.getRecipients().removeIf(recipient -> !recipient.hasPermission("adminchat.read"));
            e.setFormat(ChatColor.GRAY + "[" + ChatColor.DARK_RED + "AdminChat" + ChatColor.GRAY + "] " + ChatColor.RESET + e.getFormat());
        }
    }



}
