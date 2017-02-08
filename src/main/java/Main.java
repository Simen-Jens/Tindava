import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

/**
 * Created by Scoop on 05.02.2017.
 */
public class Main {
    public static IDiscordClient client; // The instance of the discord client.
    public static String defaultChannels;
    public static String matchNameFilter = "";
    public static int matchCountFliter = -1;

    public static void main(String[] args) {
        /*
            0 = bot token
            1 = default channels

            2 = match name filter (optional) - type: include filter
            3 = match filter after count (optional)
         */

        login(args[0]);
        defaultChannels = args[1];
        if(args.length > 2) matchNameFilter = args[2];
        if(args.length > 3) matchCountFliter = Integer.parseInt(args[3]);

        EventDispatcher dispatcher = client.getDispatcher();
        dispatcher.registerListener(new AnnotationListener(client));
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
