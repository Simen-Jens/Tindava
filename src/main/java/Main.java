import settings.Settings;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;


/**
 * Created by Simen (Scoop#8831( on 16.05.2017.
 */
public class Main {
    public static IDiscordClient client; // The instance of the discord client.
    public static String[] argsM;

    public static void main(String[] args) throws Exception{
        Settings.updateSettings();
        argsM = args;
        login(settings.Settings.getSettings.getString("bot_token"));
        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener(new AnnontationListener(client));
    }

    public static void login(String token){
        ClientBuilder builder = new ClientBuilder();
        builder.withToken(token);

        try{
            client = builder.login();
        } catch (DiscordException e){
            System.err.println("Error occurred while logging in!");
            e.printStackTrace();
        }
    }
}
