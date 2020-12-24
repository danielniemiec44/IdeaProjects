package pl.masarniamc.daynightvoting;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

//TODO: Sprawdzanie czy teleportuje na ocean

public class Main extends JavaPlugin implements CommandExecutor {
    public static Logger console;
    public static HashMap<Player, Long> lastUse = new HashMap<Player, Long>();
    public static int cooldown = 600;
    public static int votingTime = 30;

    @Override
    public void onEnable() {
        super.onEnable();

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


    public void setTime(World world, Votes vote) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            if(vote.equals(Votes.DAY)){
                world.setTime(0L);
            } else {
                world.setTime(14000L);
            }
        }, 0L);
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

        if(label.equalsIgnoreCase("dzien") || label.equalsIgnoreCase("noc")) {
            Player p = (Player) sender;
            World world = p.getWorld();
            Votes voteType;
            switch(label.toLowerCase()){
                case "dzien":
                    voteType = Votes.DAY;
                    break;
                case "noc":
                    voteType = Votes.NIGHT;
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + label.toLowerCase());
            }



            long now = Instant.now().getEpochSecond();
            if (Vote.world != null) {
                if(!Vote.world.equals(world)){
                    p.sendMessage(ChatColor.DARK_GRAY + "Musisz zaczekać aż głosowanie na innym świecie zakończy się!");
                } else {
                    if(Vote.checkIfVoted(p)){
                        p.sendMessage(ChatColor.GREEN + "Już zagłosowałeś!");
                    } else {
                        new Vote(p, voteType);
                        Vote.world = world;
                        p.sendMessage(ChatColor.DARK_GRAY + "Zagłosowałeś na " + ChatColor.GREEN + voteType.toString());
                    }
                }
            } else {
                if (lastUse.containsKey(p)) {
                    long remaining = (lastUse.get(p) + cooldown) - now;
                    if (now < lastUse.get(p) + cooldown) {
                        if (p.hasPermission("daynightvoting.bypass")) {
                            p.sendMessage(ChatColor.DARK_GRAY + "Powinienes zaczekac jeszcze " + secondsToString(remaining) + ", " + ChatColor.GREEN + "lecz masz uprawnienie do omijania ograniczenia czasowego!");
                        } else {
                            p.sendMessage(ChatColor.DARK_GRAY + "Musisz zaczekać jeszcze " + ChatColor.GREEN + secondsToString(remaining) + ChatColor.GREEN + " zanim będziesz mógł ponownie rozpoczac glosowanie!");
                            return true;
                        }
                    }
                }
                lastUse.put(p, now);


                new Vote(p, voteType);
                Vote.world = world;
                p.sendMessage(ChatColor.DARK_GRAY + "Zagłosowałeś na " + ChatColor.GREEN + voteType.toString() + ChatColor.DARK_GRAY + " na świecie " + ChatColor.GREEN + world.getName());
                for(Player player : Bukkit.getOnlinePlayers()) {
                    if(player.equals(p)) {
                        continue;
                    }
                    player.sendMessage(ChatColor.DARK_GRAY + "Gracz " + ChatColor.GREEN + p.getName() + ChatColor.DARK_GRAY + " rozpoczal glosowanie na " + ChatColor.GREEN + voteType.toString() + ChatColor.DARK_GRAY + " na świecie " + ChatColor.GREEN + world.getName());
                }

                Bukkit.getScheduler().runTaskLaterAsynchronously(this, () -> {
                    int accepted = 0;
                    int rejected = 0;
                    for(Vote vote : Vote.votes){
                        if(vote.getVote().equals(voteType)){
                            accepted++;
                        } else {
                            rejected++;
                        }
                    }
                    for(Player player : Bukkit.getOnlinePlayers()){
                        player.sendMessage(ChatColor.DARK_GRAY + "Głosowanie na " + ChatColor.GREEN + voteType.toString() + ChatColor.DARK_GRAY + " na swiecie " + ChatColor.GREEN + world.getName() + ChatColor.DARK_GRAY + " zakonczylo sie stosunkiem glosow " + ChatColor.GREEN + accepted + ChatColor.DARK_GRAY + "/" + ChatColor.RED + rejected + ChatColor.DARK_GRAY + "!");
                    }
                    if(accepted > rejected) {
                        if(voteType.equals(Votes.DAY)) {
                            setTime(world, Votes.DAY);
                        } else {
                            setTime(world, Votes.NIGHT);
                        }
                    }
                    Vote.votes = new ArrayList<>();
                    Vote.world = null;
                }, 20L * votingTime);
            }
        }

        return true;

    }

}
