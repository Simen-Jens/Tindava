import org.json.JSONObject;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.Image;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.Thread.sleep;


/**
 * Created by Simen on 05.02.2017.
 */
public class CommandCentral extends Main{
    private String fb_id;
    private String fb_token;
    private JSON_Interpreter interp;
    private Tinder_Object tndr;

    public void initDone(){
        tndr = new Tinder_Object(this, client.getGuilds().get(0));
        interp = new JSON_Interpreter("", tndr);
    }

    public void interp(MessageReceivedEvent event) throws Exception{
        String[] message = event.getMessage().getContent().split(" ");
        if(message[0].equals("<@277597781399437312>") || message[0].equals("\uD83D\uDD25")){
            if(message[1].equals("create") && message[2].equals("channel")){
                if(message.length != 6){
                    cmd_messageDiscord("expected 3 paramters, got " + (message.length-3) + ". <name> <hookName> <hookImage>", event.getMessage().getChannel(), false, false);
                    return;
                }
                cmd_createChannel(message[3], message[4], message[5], event.getMessage().getGuild());
            } else if(message[1].equals("my") && message[2].equals("roles")){
                event.getMessage().delete();
                cmd_getRoleID(event.getMessage().getGuild(), event.getMessage().getAuthor());
            } else if(message[1].equals("supply") && message[2].equals("id")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <id>", event.getMessage().getChannel(), false, false);
                    return;
                }
                fb_id = message[3];
                cmd_messageDiscord((":ok_hand: Facebook ID set to " + fb_id), event.getMessage().getChannel(), false, false);
            } else if(message[1].equals("supply") && message[2].equals("token")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <token>", event.getMessage().getChannel(), false, false);
                    return;
                }
                fb_token = message[3];
                cmd_messageDiscord((":ok_hand: Facebook Token set to " + fb_token), event.getMessage().getChannel(), false, false);
            } else if(message[1].equals("purge")){
                if(message.length != 3){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <amount to delete>", event.getMessage().getChannel(), false, false);
                    return;
                }
                cmd_purge(event, Integer.parseInt(message[2]));
            } else if(message[1].equals("add") && message[2].equals("match")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <matchID>", event.getMessage().getChannel(), false, false);
                    return;
                }
                //tndr.addMatch(message[3]);
            } else if(message[1].equals("request") && message[2].equals("update")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <json>", event.getMessage().getChannel(), false, false);
                    return;
                }
                interp.updateTinder(message[3]);
            } else if(message[1].equals("remove") && message[2].equals("chats")){
                for(int i = 2; i < event.getMessage().getGuild().getChannels().size(); i++){
                    event.getMessage().getGuild().getChannels().get(i).delete();
                }
            }
        }
    }

    public void cmd_purge(MessageReceivedEvent event, int ant){
        for(int i = 0; i < ant; i++){
            System.out.println("deleting " + i);
            try{
                sleep(100);
                event.getMessage().getChannel().getMessages().get(0).delete();
            } catch (Exception e){
                System.out.println(e.toString());
            }

        }
        System.out.println("done purging");
    }

    public IChannel cmd_createChannel(String name, String hookName, String hookImage, IGuild guild) throws Exception{
        if(hookName == null)hookName = "UNKNOWN";
        if(hookImage == null)hookImage = "http://barabasilab.neu.edu/people/baruch/WebPage_files/Silhouette.jpg";

        IChannel tmp = guild.createChannel(name);
        tmp.createWebhook(hookName, hookImage).changeDefaultAvatar(Image.forUrl("jpg",hookImage));
        System.out.println("chat '" + name + "' created - bot can behave as '" + hookName + "'");
        return tmp;
    }

    public IMessage cmd_messageDiscord(String message, IChannel channel, boolean alert, boolean masked) throws Exception{
        if(!masked){
            return channel.sendMessage(alert ? "@everyone " + message : message);
        } else{
            String url="https://discordapp.com/api/webhooks/" + channel.getWebhooks().get(0).getID() + "/" + channel.getWebhooks().get(0).getToken();
            JSONObject msg = new JSONObject();
            msg.put("content",message);
            postJSON(url, msg);
            sleep(100); //gives the webhook time to post
            for(int i = 0; i < channel.getMessages().size(); i++){
                if(channel.getMessages().get(i).getContent().equals(message)){
                    return channel.getMessages().get(i);
                }
            }
            return channel.getMessages().get(0);
        }
    }

    public void cmd_getRoleID(IGuild guild, IUser user) throws Exception{
        String build = "";
        build += ("Your roles for " + guild.getName() + " are\n\n");
        for(int i = 0; i < guild.getRolesForUser(user).size(); i++){
            build += (guild.getRolesForUser(user).get(i).getName() + " - " + guild.getRolesForUser(user).get(i).getID() + "\n");
        }

        cmd_messageDiscord(build, user.getOrCreatePMChannel(), false, false);
    }

    public void postJSON(String url, JSONObject json) throws Exception{
        URL object=new URL(url);

        HttpURLConnection con = (HttpURLConnection) object.openConnection();
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("User-agent", "Tindava 1.0-SNAPSHOT");
        con.setRequestMethod("POST");

        OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
        wr.write(json.toString());
        wr.flush();

        StringBuilder sb = new StringBuilder();
        int HttpResult = con.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            System.out.println("" + sb.toString());
        } else {
            System.out.println(con.getResponseMessage());
        }
    }
}
