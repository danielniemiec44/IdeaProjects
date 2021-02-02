package pl.masarniamc.custommotd;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Log;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin {
public class Main extends JavaPlugin {


    @Override
    public void onEnable(){
        super.onEnable();

        FileConfiguration config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();



        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        //if(config.getBoolean("enabled")) {
            protocolManager.addPacketListener(
                    new PacketAdapter(this, ListenerPriority.NORMAL,
                            PacketType.Status.Server.SERVER_INFO) {
                        @Override
                        public void onPacketSending(PacketEvent e) {
                            if (e.getPacketType() == PacketType.Status.Server.SERVER_INFO) {
                                WrappedServerPing ping = (WrappedServerPing) e.getPacket().getServerPings().read(0);

                                List<String> motd = config.getStringList("motd");
                                ping.setMotD(String.join("\n", motd));


                                LinkedList<WrappedGameProfile> profiles = new LinkedList<WrappedGameProfile>();
                                List<String> stringProfiles = config.getStringList("profiles");
                                for(String profile : stringProfiles) {
                                    profiles.add(new WrappedGameProfile(UUID.randomUUID(), profile));
                                }
                                ping.setPlayers(profiles);


                                /*
                                //try {
                                URL url = null;
                                try {
                                    url = new URL(config.getString("icon"));

                                    ping.setFav(encode(ImageIO.read(url)));
                                } catch (IOException malformedURLException) {
                                    malformedURLException.printStackTrace();
                                }
                                    /*
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    ImageIO.write(ImageIO.read(url), "png", bos);
                                    byte[] data = bos.toByteArray();
                                    WrappedServerPing.CompressedImage compressedImage = new WrappedServerPing.CompressedImage("image/png", data);
                                    ping.setFavicon(compressedImage);

                                     */
                                /*
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                */
                            }
                        }
                    });
        }



    //}


    @Override
    public void onDisable() {
        super.onDisable();
    }




    public String encode(BufferedImage image){
        try {
            ByteBuf buffer = Unpooled.buffer();
            ImageIO.write(image, "PNG", new ByteBufOutputStream(buffer));
            ByteBuf encodedBuffer = Base64.encode(buffer);
            String encodedString = "data:image/png;base64," + encodedBuffer.toString(Charsets.UTF_8);
            return encodedString;
        }catch(Exception e){
            getLogger().severe("Error while converting image to string:");
            e.printStackTrace();
            return null;
        }
    }



}
