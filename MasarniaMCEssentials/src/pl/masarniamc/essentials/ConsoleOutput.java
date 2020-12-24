package pl.masarniamc.essentials;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;  // Import the IOException class to handle errors
import java.util.logging.FileHandler;

public class ConsoleOutput {

    static String path = "console.log";
    static File dataFolder = Main.getPlugin(Main.class).getDataFolder();
    static File file = new File(dataFolder, "console.log");

    static void out(String out) {
        try {
            if(!dataFolder.exists()) {
                dataFolder.mkdir();
            }

            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(out);
            bw.newLine();
            bw.close();
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
