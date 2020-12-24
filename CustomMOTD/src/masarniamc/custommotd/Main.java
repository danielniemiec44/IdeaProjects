package masarniamc.custommotd;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.output.ByteArrayOutputStream;
import org.bukkit.plugin.java.JavaPlugin;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class Main extends JavaPlugin {


    @Override
    public void onEnable(){
        super.onEnable();





        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        //if(config.getBoolean("enabled")) {
            protocolManager.addPacketListener(
                    new PacketAdapter(this, ListenerPriority.NORMAL,
                            PacketType.Status.Server.SERVER_INFO) {
                        @Override
                        public void onPacketSending(PacketEvent e) {
                            if (e.getPacketType() == PacketType.Status.Server.SERVER_INFO) {
                                WrappedServerPing ping = (WrappedServerPing) e.getPacket().getServerPings().read(0);
                                ArrayList<WrappedGameProfile> desc = new ArrayList<>();
                                desc.add(new WrappedGameProfile(UUID.randomUUID(), "§eMasarniaMC.pl"));
                                desc.add(new WrappedGameProfile(UUID.randomUUID(), "§ePolski serwer FreeBuild"));
                                desc.add(new WrappedGameProfile(UUID.randomUUID(), "§eGracze online: " + Bukkit.getOnlinePlayers().size()));
                                //desc.add(new WrappedGameProfile(UUID.randomUUID(), ""));
                                //desc.add(new WrappedGameProfile(UUID.randomUUID(), "§o§8Sky-Block zostanie dodany w przyszlosci"));
                                ping.setPlayers(desc);
                                String[] motd = new String[2];
                                motd[0] = "§6MasarniaMC.pl§r";
                                motd[1] = "§eOficjalne otwarcie serwera >> §n24.11.2020";


                                ping.setMotD(String.join("\n", motd));


                                try {
                                    URL url = new URL("http://masarniamc.pl/logo.png");
                                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                                    ImageIO.write(ImageIO.read(url), "png", bos);
                                    byte[] data = bos.toByteArray();
                                    WrappedServerPing.CompressedImage compressedImage = new WrappedServerPing.CompressedImage("image/png", data);
                                    ping.setFavicon(compressedImage);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }

                            }
                        }
                    });
        }
    //}







    @Override
    public void onDisable() {
        super.onDisable();
    }



}
