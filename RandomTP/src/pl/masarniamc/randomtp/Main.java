package pl.masarniamc.randomtp;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.HashMap;
import java.util.logging.Logger;

//TODO: Sprawdzanie czy teleportuje na ocean

public class Main extends JavaPlugin implements CommandExecutor {
    public static Logger console;
    public static HashMap<Player, Long> lastUse = new HashMap<Player, Long>();
    public static int cooldown;
    FileConfiguration config = getConfig();
    String world = "";

    @Override
    public void onEnable() {
        super.onEnable();

        config.options().copyDefaults(true);
        saveConfig();
        world = config.getString("world");
        cooldown = config.getInt("cooldown");
    }

    @Override
    public void onDisable() {
        super.onDisable();

    }

    public static String secondsToString(long seconds) {
        String time = "";
        final int year = 31104000;
        final int month = 2592000;
        final int day = 86400;
        final int hour = 3600;
        final int minute = 60;

        int years = (int)(seconds / year);
        seconds -= year * years;
        int months = (int)(seconds / month);
        seconds -= month * months;
        int days = (int)(seconds / day);
        seconds -= day * days;
        int hours = (int)(seconds / hour);
        seconds -= hour * hours;
        int minutes = (int)(seconds / minute);
        seconds -= minute * minutes;

        if(years > 0) {
            time += years + " lat";
        }

        if(months > 0) {
            if(years > 0){
                time += " ";
            }
            time += months + " miesiecy";
        }

        if(days > 0) {
            if(months > 0){
                time += " ";
            }
            time += days + " dni";
        }

        if(hours > 0) {
            if(days > 0){
                time += " ";
            }
            time += hours + " godzin";
        }

        if(minutes > 0) {
            if(hours > 0){
                time += " ";
            }
            time += minutes + " minut";
        }

        if(seconds > 0) {
            if(minutes > 0){
                time += " ";
            }
            time += seconds + " sekund";
        }

        return time;
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

        if(label.equalsIgnoreCase("randomtp")) {
            Player p = (Player) sender;
            long now = Instant.now().getEpochSecond();
            if(lastUse.containsKey(p)){
                Long remaining = (lastUse.get(p) + cooldown) - now;
                if(now < lastUse.get(p) + cooldown){
                    if(p.hasPermission("randomtp.bypass")){
                        p.sendMessage("Powinienes zaczekac jeszcze " + secondsToString(remaining) + ", lecz masz uprawnienie do omijania ograniczenia czasowego!");
                    } else {
                        p.sendMessage("Musisz zaczekać jeszcze " + secondsToString(remaining) + " zanim będziesz mógł użyć ponownie tej komendy!");
                        return true;
                    }
                }
            }

            lastUse.put(p, now);
            RandomTP.teleportRandomly(p, world);
        }

        return true;

    }

}
