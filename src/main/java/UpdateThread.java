import org.json.JSONObject;
import settings.Settings;
import tools.JSONDecoder;
import tools.MessageBuilds;
import tunnels.TinderTunnel;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Simen (Scoop#8831) on 16.05.2017.
 */
public class UpdateThread extends Thread {
    public void run() {
        JSONDecoder.client = AnnontationListener.client;
        try {
            //Execute first run
            JSONDecoder.tinderUpdateDecode(new JSONObject(readFile(Settings.getSettings.getString("cache_path"), StandardCharsets.UTF_8)));

            while (true) {
                JSONDecoder.tinderUpdateDecode(TinderTunnel.getUpdate(Settings.getSettings.getString("xauth")));
                sleep(Settings.getSettings.getInt("pullrate"));
            }
        } catch (Exception ex){
            StringWriter stacktrace = new StringWriter();
            ex.printStackTrace(new PrintWriter(stacktrace));
            AnnontationListener.client.getGuilds().get(0).getChannelByID(Settings.getSettings.getString("notifications_channel"))
                    .sendMessage("", MessageBuilds.buildErrorMessage(stacktrace.toString()), false);
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private String readFile(String path, Charset encoding) throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
