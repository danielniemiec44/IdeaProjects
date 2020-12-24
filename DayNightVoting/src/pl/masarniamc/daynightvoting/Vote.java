package pl.masarniamc.daynightvoting;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class Vote {
    private Player player;
    public static World world = null;
    private Votes vote;
    public static ArrayList<Vote> votes = new ArrayList<>();

    public Vote(Player player, Votes vote) {
        this.player = player;
        this.vote = vote;

        votes.add(this);
    }

    public static Boolean checkIfVoted(Player p){
        for(Vote vote : votes) {
            if(vote.player.equals(p)){
                return true;
            }
        }
        return false;
    }


    public Votes getVote() {
        return vote;
    }
}
