package tunnels;

import objects.MatchLink;
import objects.MatchObject;
import org.json.JSONException;
import org.json.JSONObject;
import settings.Settings;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;
import sx.blah.discord.util.Image;
import objects.ComparableChannel;
import tools.ChannelLinker;
import tools.MessageBuilds;
import tools.StringManipulation;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Thread.sleep;
import static tools.StringManipulation.sanitize;

/**
 * Created by Simen (Scoop#8831) on 15.05.2017.
 *
 * This will serve as the pipeline to the Discord4J API.
 * This is not really necessary, but is cleaner in my opinion
 */
public class DiscordTunnel {
    public static IDiscordClient client;

    public static void receiveMessageFromMatch(String message, IChannel matchChannel) throws JSONException, InterruptedException{
        MatchLink pol = ChannelLinker.partOfLink(matchChannel);
        if(pol != null){
            //Linked behaviour
            String url = "https://discordapp.com/api/webhooks/" + pol.getRespectiveWebHook(StringManipulation.findMatchFromChannel(matchChannel)).getStringID() + "/" + pol.getRespectiveWebHook(StringManipulation.findMatchFromChannel(matchChannel)).getToken();
            JSONObject msg = new JSONObject();
            msg.put("content",message);
            HttpTunnel.handleData(url, "POST", msg, null);
            sleep(100); //gives the webhook time to post

            //Send the message to the other participant aswell
            pol.sendMessage(StringManipulation.findMatchFromChannel(matchChannel), message);
        } else {
            //Normal behaviour
            String url="https://discordapp.com/api/webhooks/" + matchChannel.getWebhooks().get(0).getStringID() + "/" + matchChannel.getWebhooks().get(0).getToken();
            JSONObject msg = new JSONObject();
            msg.put("content",message);
            HttpTunnel.handleData(url, "POST", msg, null);
            sleep(100); //gives the webhook time to post
        }
    }

    public static void createMatch(MatchObject profile, boolean superlike, IChannel channel) throws JSONException, RateLimitException, MissingPermissionsException, DiscordException{
        //Tinder server seems to have some soft of issue where the "bio" tag does not appear in the JSON data,
        //searching for the datatag in a try-catch resolves this issue with a custom handle.

        String bio = "";
        try{
            bio = profile.getBio();
        } catch (JSONException ex){
            bio = "{EMPTY BIO (missing tag)}";
        }

        //Create a nice looking profile for the notifications channel
        EmbedObject eo = MessageBuilds.buildMatchMessage(profile.getName(),
                bio,
                profile.getAge(),
                superlike,
                profile.getImages()[0],
                null);
        client.getChannelByID(Settings.getSettings.getString("notifications_channel")).sendMessage("<#" + channel.getStringID() + ">", eo, false);

        //Create the internal pinned message
        IMessage first = channel.sendMessage(MessageBuilds.buildInternalMatchMessage(
                profile.getImages(),
                profile.getID(),
                profile.getName(),
                bio,
                profile.getAge(),
                superlike));

        /*String allImages = "";
        for(int i = 0; i < profile.getJSONArray("photos").length(); i++){
            allImages += profile.getJSONArray("photos").getJSONObject(i).getString("url") + "\n";
        }

        IMessage first = channel.sendMessage(allImages + "\n```\n{\"matchid\":\"" + profile.getString("_id") + "\"}\n" +
                "\nNAME: " + profile.getString("name") +
                "\nAGE: " + calculateAge(profile.getString("birth_date")) +
                "\nBIO: " + bio +
                "\nSUPER: " + superlike + "```");*/

        channel.pin(first);
    }

    public static IChannel createChannel(JSONObject profile){
        IChannel tmp = null;
        try {
            String channelName = sanitize(profile.getString("name"));
            tmp = client.getGuilds().get(0).createChannel(channelName);
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return tmp;
    }

    public static IWebhook addWebHook(IChannel inChannel, MatchObject forProfile){
        IWebhook webH = null;
        try{
            String webhookImage = forProfile.getImages().length < 1 ?
                    Settings.getSettings.getString("default_hook_image") : forProfile.getImages()[0];

            String webhookName = sanitize(forProfile.getName());

            webH = inChannel.createWebhook(webhookName, webhookImage);
            webH.changeDefaultAvatar(Image.forUrl("jpg", webhookImage));
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return webH;
    }

    public static void deleteChannel(){

    }

    public static void unmatched(IChannel channelForMatch){
        channelForMatch.sendMessage("", MessageBuilds.buildUnmatchedMessage(), false);
    }

    public static void updateGameStatus(String status){
        client.changeStatus(Status.game(status));
    }

    public static IChannel searchForChannel(String name, String id) throws InterruptedException{
        System.out.println("Searching for " + name + " (" + id + ")");

        // We get the amout of channels that could possibly be our target
        List<IChannel> possibleChannels = client.getGuilds().get(0).getChannelsByName(sanitize(name));

        // For every possible channel, check if the first pinned message contains out matchID
        for(IChannel channel : possibleChannels){
            try {
                if (channel.getPinnedMessages().get(0).getContent().contains(id)){
                    return channel;
                }
            } catch (DiscordException ex){
                ex.printStackTrace();
            } catch (RateLimitException ex){
                System.out.println("Timed out by Discord, waiting for 3000");
                sleep(3000);
            }
        }

        // There are no channels that have the name we were supplied! This will surely cause a NullPointerException
        System.out.println("I found no channel - possible channels by name: " + name);
        return null;
    }

    /*
    *  Du er ikke endru Simen, dette må du se over imorgen....
    *  Jeg kan ikke stole på "fullesimen" på at dette funker.
    * */
    public static void organizeChannels(IGuild forGuild){
        LinkedList<ComparableChannel> tmp = new LinkedList<>();

        for(IChannel ic: forGuild.getChannels()){
            ComparableChannel cc = new ComparableChannel(ic);
            if(!cc.safeChannel()) tmp.add(cc);
        }

        Collections.sort(tmp);

        for(int i = 0; i < tmp.size(); i++){
            try{
                tmp.get(i).channelOcj.changePosition(i + Settings.getSettings.getJSONArray("do_not_delete_channels").length());
            } catch (RateLimitException ex){
                System.out.println("ERROR - Timed out while sorting (waiting for 3000)");
                try {
                    sleep(3000);
                } catch (InterruptedException rx){

                }
                i--;
            }
        }
    }
}
