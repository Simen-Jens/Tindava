import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.MessageSendEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.TypingEvent;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.EmbedBuilder;

/**
 * Created by Simen (Scoop#8831) on 05.02.2017.
 */
public class AnnotationListener extends Main {
    private static CommandCentral cmd;

    public AnnotationListener(IDiscordClient client) {
        EventDispatcher dispatcher = client.getDispatcher(); // Gets the client's event dispatcher
        dispatcher.registerListener(new EventSubscriberExample()); // Registers the event listener
        cmd = new CommandCentral();
    }

    public static class EventSubscriberExample {
        @EventSubscriber
        public void onReady(ReadyEvent event) { // This is called when the ReadyEvent is dispatched
            IDiscordClient client = event.getClient(); // Gets the client from the event object
            IUser ourUser = client.getOurUser();// Gets the user represented by the client
            String name = ourUser.getName();// Gets the name of our user
            System.out.println("Logged in as " + name);
            client.changeStatus(Status.game("with no credentials"));
            cmd.initDone();
        }

        @EventSubscriber
        public void onMessageReceivedEvent(MessageReceivedEvent event) throws Exception {
            if(event.getMessage().getAuthor() != event.getMessage().getGuild().getOwner() && settings.restrictNotifications && event.getMessage().getChannel().getID().equals(settings.defaultChannels.split(" ")[0])){ //message in notifications not allowed
                EmbedBuilder tmp = new EmbedBuilder();
                tmp.withAuthorName(event.getMessage().getAuthor().getName());
                tmp.withAuthorIcon(event.getMessage().getAuthor().getAvatarURL());
                tmp.withDescription(event.getMessage().getContent());
                event.getMessage().getGuild().getChannelByID(settings.defaultChannels.split(" ")[1]).sendMessage(event.getMessage().getAuthor().mention() + " please use <#" + settings.defaultChannels.split(" ")[1] + "> instead", tmp.build(), false);
                event.getMessage().delete();
            } else{
                cmd.interp(event);
            }
        }

        @EventSubscriber
        public void onTypingEvent(TypingEvent event) throws Exception{

        }

        @EventSubscriber
        public void onMessageSendEvent(MessageSendEvent event) throws Exception{
            myLastSent = event.getMessage();
        }
    }
}