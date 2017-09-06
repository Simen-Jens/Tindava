import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.EventSubscriber;

import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageSendEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import tools.SwipeController;
import tunnels.DiscordTunnel;

/**
 * Created by Simen (Scoop#8831) on 05.02.2017.
 */
public class AnnontationListener extends Main {
    public static IMessage lastSent;


    public AnnontationListener(IDiscordClient client) {
        EventDispatcher dispatcher = client.getDispatcher(); // Gets the client's event dispatcher
        dispatcher.registerListener(new EventSubscriberExample()); // Registers the event listener
    }

    public static class EventSubscriberExample {
        @EventSubscriber
        public void onReady(ReadyEvent event) throws Exception { // This is called when the ReadyEvent is dispatched
            IDiscordClient client = event.getClient(); // Gets the client from the event object
            IUser ourUser = client.getOurUser();// Gets the user represented by the client
            String name = ourUser.getName();// Gets the name of our user
            System.out.println("Logged in as " + name);
            client.changePlayingText("with no credentials");
            DiscordTunnel.client = client;
        }

        @EventSubscriber
        public void onMessageReceivedEvent(MessageReceivedEvent event) throws Exception {
            CommandCentral.messageInterp(event);
        }

        @EventSubscriber
        public void onMessageSendEvent(MessageSendEvent event) throws Exception{
            lastSent = event.getMessage();
        }

        @EventSubscriber
        public void onReactionAddEvent(ReactionAddEvent event) throws Exception{
            SwipeController.reactionEvent(event);
        }
    }
}