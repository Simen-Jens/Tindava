import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Created by Simen (Scoop#8831) on 11.02.2017.
 */
public class Swipe_Control extends Auto_Swipe{
    public Swipe_Control(JSON_Interpreter intep, Postman post, CommandCentral cmd){
        super(intep, post, cmd);
    }

    public IMessage postRecom() throws Exception{
        return cmd.client.getChannelByID(cmd.defaultChannels.split(" ")[1]).sendMessage("", wipeTemplate("Test", "En kul bio", 22, "http://images.gotinder.com/55681cef25c9a285308f5f2f/640x640_beb48a9b-4f92-4505-9f63-a99fb2e23487.jpg"), false);
    }

    public EmbedObject wipeTemplate(String matchName, String matchBio, int matchAge, String matchImage){
        return new EmbedBuilder().
                withImage(matchImage).
                withFooterText(matchName + " " + matchAge).build();
    }
}
