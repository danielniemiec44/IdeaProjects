package pl.masarniamc.serverquery;

import org.bukkit.Bukkit;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketServer {
    static int port = Bukkit.getPort() + 10;
    static ServerSocket server;

    static void start() throws IOException, NoSuchAlgorithmException {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getPlugin(Main.class), () -> {
            try {
                server = new ServerSocket(port);
                System.out.println("Server has started on 127.0.0.1:" + port);
                for (; ; ) {
                    System.out.println("Waiting for a connection...");
                    Socket client = server.accept();
                    System.out.println("A client connected.");


                    InputStream in = client.getInputStream();
                    OutputStream out = client.getOutputStream();
                    Scanner s = new Scanner(in, "UTF-8");


                    String data = s.useDelimiter("\\r\\n\\r\\n").next();
                    Matcher get = Pattern.compile("^GET").matcher(data);


                    if (get.find()) {
                        Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
                        match.find();
                        byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                                + "Connection: Upgrade\r\n"
                                + "Upgrade: websocket\r\n"
                                + "Sec-WebSocket-Accept: "
                                + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                                + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
                        out.write(response, 0, response.length);


                        byte[] decoded = new byte[6];
                        byte[] encoded = new byte[]{(byte) 198, (byte) 131, (byte) 130, (byte) 182, (byte) 194, (byte) 135};
                        byte[] key = new byte[]{(byte) 167, (byte) 225, (byte) 225, (byte) 210};
                        for (int i = 0; i < encoded.length; i++) {
                            decoded[i] = (byte) (encoded[i] ^ key[i & 0x3]);
                        }
                    }


                    /*
                    PrintWriter pw =
                            new PrintWriter(out, true);
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(in));
                    String inputLine, outputLine = null;

                    while ((inputLine = br.readLine()) != null) {
                        pw.println(inputLine);
                        if (outputLine.equals("Bye."))
                            break;


                    }

                     */

                }
            } catch (NoSuchAlgorithmException | IOException e) {
                e.printStackTrace();
            }
        });
    }

            static void stop() throws IOException {
        server.close();
        System.out.println("Server stopped");
    }
}