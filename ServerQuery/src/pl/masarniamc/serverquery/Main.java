package pl.masarniamc.serverquery;

import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class Main extends JavaPlugin implements CommandExecutor, Listener {


    @Override
    public void onEnable() {
        super.onEnable();
        //Bukkit.getPluginManager().registerEvents(this, this);
        try {
            WebSocketServer.start();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        try {
            WebSocketServer.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }








}
