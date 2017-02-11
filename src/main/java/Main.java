import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

/**
 * Created by Simen (Scoop#8831) on 05.02.2017.
 */
public class Main {
    public static IDiscordClient client; // The instance of the discord client.
    public static String defaultChannels;
    public static String messageRole;

    public static void main(String[] args) {
        /*
            0 = bot token
            1 = default channels
         */

        login(args[0]);
        defaultChannels = args[1];
        messageRole = args[2];

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
