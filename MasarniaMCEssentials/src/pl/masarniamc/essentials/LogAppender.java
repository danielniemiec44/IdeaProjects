package pl.masarniamc.essentials;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LogAppender extends AbstractAppender {



    // your variables

    public LogAppender() {
        // do your calculations here before starting to capture
        super("MyLogAppender", null, null);
        start();
    }

    @Override
    public void append(LogEvent event) {
        SimpleDateFormat formatter;
//you might want another format
//visit this link for the opportunities: https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html
        formatter = new SimpleDateFormat("HH:mm:ss");

        // if you don`t make it immutable, than you may have some unexpected behaviours
        LogEvent log = event.toImmutable();

        // do what you have to do with the log

        // you can get only the log message like this:
        String message = log.getMessage().getFormattedMessage();

        // and you can construct your whole log message like this:
        message = "[" +formatter.format(new Date(event.getTimeMillis())) + " " + event.getLevel().toString() + "] " + message;
        ConsoleOutput.out(message);

        /*
        Player p = Bukkit.getPlayer("DeeRave");
        if(p.isOnline()){
            p.sendMessage(message);
        }
        
         */
    }

}