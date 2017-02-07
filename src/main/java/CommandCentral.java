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
    public JSON_Interpreter interp;
    private Tinder_Object tndr;
    public Postman pat;
    private boolean gifaen = true;
    private Update_Thread updater = new Update_Thread(10000L, this);

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
                if(fb_token != null && fb_id != null){
                    pat = new Postman(fb_token, fb_id);
                }
                cmd_messageDiscord((":ok_hand: Facebook ID set to " + fb_id), event.getMessage().getChannel(), false, false);
            } else if(message[1].equals("supply") && message[2].equals("token")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <token>", event.getMessage().getChannel(), false, false);
                    return;
                }
                fb_token = message[3];
                if(fb_token != null && fb_id != null){
                    pat = new Postman(fb_token, fb_id);
                }
                cmd_messageDiscord((":ok_hand: Facebook Token set to " + fb_token), event.getMessage().getChannel(), false, false);
            } else if(message[1].equals("supply") && message[2].equals("xauth")){
                /*
                xauth here (shortcut skips fb_token and fb_id)
                 */
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <xauth>", event.getMessage().getChannel(), false, false);
                    return;
                }
                pat = new Postman(null, null);
                pat.xauth = message[3];
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
                if(message[3].equals("!auth!")){
                    System.out.println("SENT FOR");
                    if(pat != null){
                        cmd_messageDiscord((pat.auth() ? "login success" : "login failed"), event.getMessage().getChannel(), false, false);
                    } else{
                        cmd_messageDiscord(":postal_horn: Missing postman, supply bot with a love letter :love_letter: (facebook token + id or a tinder xauth-token)", event.getMessage().getChannel(), false, false);
                        return;
                    }
                    if(pat.xauth != null){
                        //updates go here
                        interp.updateTinderFromFile();
                        updater.start();
                    }
                } else{
                    interp.updateTinder(message[3]);
                }
            } else if(message[1].equals("remove") && message[2].equals("chats")){
                for(int i = 2; i < event.getMessage().getGuild().getChannels().size(); i++){
                    event.getMessage().getGuild().getChannels().get(i).delete();
                }
            } else if(message[1].equals("toggle") && message[2].equals("chat")){
                gifaen = !gifaen;
                cmd_messageDiscord((gifaen ? ":negative_squared_cross_mark: chat is now disabled" : ":white_check_mark: is now enabled"), event.getMessage().getChannel(), false, false);
            }
        } else if(event.getMessage().getChannel().getID() != "277596483631579137" && event.getMessage().getChannel().getID() != "277893008806903809" && !gifaen) {
            boolean doorman = false;
            for(int i = 0; i < event.getMessage().getAuthor().getRolesForGuild(event.getMessage().getGuild()).size(); i++){
                if(event.getMessage().getAuthor().getRolesForGuild(event.getMessage().getGuild()).get(i).getID().equals("278645767311196160")){
                    doorman = true;
                    break;
                }
            }

            if(pat != null && doorman){
                if(pat.xauth != null){
                    String matchidFmsg = "";
                    for(int i = 0; i < tndr.matches.size(); i++){
                        if(tndr.matches.get(i).myChannel.getID().equals(event.getMessage().getChannel().getID())){
                            matchidFmsg = tndr.matches.get(i).matchID;
                        }
                    }
                    pat.handleData(("https://api.gotinder.com/user/matches/" + matchidFmsg), "POST", interp.gifIntegrator(event.getMessage().getContent()));
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
                break;
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
            //postJSON(url, msg);   old code, hopefully redacted
            pat.handleData(url, "POST", msg);
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
/*
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
    */
}
