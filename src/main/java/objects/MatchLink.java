package objects;

import org.json.JSONObject;
import settings.Settings;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IWebhook;
import tools.MessageBuilds;
import tunnels.DiscordTunnel;

/**
 * Created by Simen on 15.08.2017.
 */
public class MatchLink {
    public IChannel linkedChannel;

    private MatchObject recipientA;
    private MatchObject recipientB;

    private IWebhook recipientAHook;
    private IWebhook recipientBHook;

    public MatchLink(MatchObject recipientA, MatchObject recipientB){
        this.recipientA = recipientA;
        this.recipientB = recipientB;

        //Creates a new channel in the guild with both names combined
        JSONObject falseChannelName = new JSONObject("{\"name\": \"" + recipientA.getName() + "-+-" + recipientB.getName() + "\"}");
        linkedChannel = DiscordTunnel.createChannel(falseChannelName);

        //Send the first pinned message
        String fullMessage
                = MessageBuilds.buildInternalMatchMessage(recipientA.getImages(), recipientA.getID(), recipientA.getName(), recipientA.getBio(), recipientA.getAge(), false)
                + "\n\n"
                + MessageBuilds.buildInternalMatchMessage(recipientB.getImages(), recipientB.getID(), recipientB.getName(), recipientB.getBio(), recipientB.getAge(), false);
        linkedChannel.sendMessage(fullMessage);

        //Create both webhooks
        recipientAHook = DiscordTunnel.addWebHook(linkedChannel, recipientA);
        recipientBHook = DiscordTunnel.addWebHook(linkedChannel, recipientB);
    }

    public IChannel[] getChannels(){
        IChannel[] tmp = {recipientA.getDiscordChannel(), recipientB.getDiscordChannel()};
        return tmp;
    }

    public IWebhook getRespectiveWebHook(MatchObject from){
        if(from == recipientA){
            return recipientAHook;
        } else if(from == recipientB){
            return recipientBHook;
        } else {
            return null;
        }
    }

    public void sendMessage(MatchObject from, String message){
        if(from == recipientA){
            recipientB.sendMessage(message, Settings.getSettings.getString("xauth"));
        } else if(from == recipientB){
            recipientA.sendMessage(message, Settings.getSettings.getString("xauth"));
        } else {
            System.out.println("ERROR IN LINK - sending from unknown part of link?");
        }
    }
}
