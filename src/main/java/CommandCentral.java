import org.json.JSONObject;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.Image;

import static java.lang.Thread.sleep;


/**
 * Created by Scoop on 05.02.2017.
 */
public class CommandCentral extends Main{
    private String fb_id;
    private String fb_token;
    public JSON_Interpreter interp;
    private Tinder_Object tndr;
    public Postman pat;
    private boolean chattoggle = true;
    private Update_Thread updater = new Update_Thread(10000L, this);

    /*
    Called upon when the bot is ready

    Creates a Tinder_Object for guild(0) (bot is only ment to be run on one guild)
    Creates a JSON_Interpreter for the Tinder_Object
     */
    public void initDone(){
        tndr = new Tinder_Object(this, client.getGuilds().get(0));
        interp = new JSON_Interpreter(tndr);
    }


    /*
    Called upon whenever the bot registers a new message

        1. Splits the message based on spaces.
        2. Checks for prefix, either @Tindava or :fire:.
            2a. Handels create client command.
            2b. Handels my roles command.
            2c. Handels supply id command, checks if it can create a Postman (will be done when both id and token is supplied).
            2d. Handels supply token command, checks if it can create a Postman (will be done when both id and token is supplied).
            2e. Handels supply xauth command, will outright create a Postman with empty facebook details (not needed).
            2f. Handels purge command, a nasty for-loop... will eat all messages if used incorrectly! (would be wise to remove this, but it's just too useful).
            2g. REDACTED (removed for now, I will get back to this one).
            2h. Handels request update command, where the magic happens. If the paramter "!auth!" is sent and Postman is created it will try to login (generate a xauth-token)
                If the login created a xauth-token or a xauth-token was supplied it will the read previous JSON data (if any) and generate a save state.
                After the save state is generated it will start a new thread to request new updates from the Tinder server every ten seconds.
            2i. Removes excess chat-channels (warning something is wrong in this metohd, I will get around to it).
            2j. Handels chat toggle command, toggles whether or not messages will be sent to Tinder matches.
            2k. Handels stop updates command, stops the update thread.
     */
    public void interp(MessageReceivedEvent event) throws Exception{
        // 1
        String[] message = event.getMessage().getContent().split(" ");
        // 2
        if(message[0].equals("<@" + client.getOurUser().getID() + ">") || message[0].equals("\uD83D\uDD25")){
            // A    - create client
            if(message[1].equals("create") && message[2].equals("channel")){
                if(message.length != 6){
                    cmd_messageDiscord("expected 3 paramters, got " + (message.length-3) + ". <name> <hookName> <hookImage>", event.getMessage().getChannel(), false, false);
                    return;
                }
                cmd_createChannel(message[3], message[4], message[5], event.getMessage().getGuild());
            }
            // B    - my roles
            else if(message[1].equals("my") && message[2].equals("roles")){
                event.getMessage().delete();
                cmd_getRoleID(event.getMessage().getGuild(), event.getMessage().getAuthor());
            }
            // C    - supply id
            else if(message[1].equals("supply") && message[2].equals("id")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <id>", event.getMessage().getChannel(), false, false);
                    return;
                }
                fb_id = message[3];
                if(fb_token != null && fb_id != null){
                    pat = new Postman(fb_token, fb_id);
                }
                cmd_messageDiscord((":ok_hand: Facebook ID set to " + fb_id), event.getMessage().getChannel(), false, false);
            }
            // D    - supply token
            else if(message[1].equals("supply") && message[2].equals("token")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <token>", event.getMessage().getChannel(), false, false);
                    return;
                }
                fb_token = message[3];
                if(fb_token != null && fb_id != null){
                    pat = new Postman(fb_token, fb_id);
                }
                cmd_messageDiscord((":ok_hand: Facebook Token set to " + fb_token), event.getMessage().getChannel(), false, false);
            }
            // E    - supply xauth
            else if(message[1].equals("supply") && message[2].equals("xauth")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <xauth>", event.getMessage().getChannel(), false, false);
                    return;
                }
                pat = new Postman(null, null);
                pat.xauth = message[3];
            }
            // F    - purge
            else if(message[1].equals("purge")){
                if(message.length != 3){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <amount to delete>", event.getMessage().getChannel(), false, false);
                    return;
                }
                cmd_purge(event, Integer.parseInt(message[2]));
            }
            // G    - add match
            /* REDACTED
            else if(message[1].equals("add") && message[2].equals("match")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <matchID>", event.getMessage().getChannel(), false, false);
                    return;
                }
                //tndr.addMatch(message[3]);
            } */
            // H    - request update
            else if(message[1].equals("request") && message[2].equals("update")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <json>", event.getMessage().getChannel(), false, false);
                    return;
                }
                if(message[3].equals("!auth!")){
                    System.out.println("SENT FOR");
                    if(pat != null){
                        cmd_messageDiscord((pat.auth() ? "login success" : pat.xauth != null ? "xauth token supplied, skipping login" : "login failed"), event.getMessage().getChannel(), false, false);
                    } else{
                        cmd_messageDiscord(":postal_horn: Missing postman, supply bot with a love letter :love_letter: (facebook token + id or a tinder xauth-token)", event.getMessage().getChannel(), false, false);
                        return;
                    }
                    if(pat.xauth != null){
                        // login a-ok
                        interp.updateTinderFromFile();
                        updater.start();
                    }
                } else{
                    interp.updateTinder(message[3]);
                }
            }
            // I    - remove chats
            else if(message[1].equals("remove") && message[2].equals("chats")){
                for(int i = 2; i < event.getMessage().getGuild().getChannels().size(); i++){
                    event.getMessage().getGuild().getChannels().get(i).delete();
                }
            }
            // J    - toggle chat
            else if(message[1].equals("toggle") && message[2].equals("chat")){
                chattoggle = !chattoggle;
                cmd_messageDiscord((chattoggle ? ":negative_squared_cross_mark: chat is now disabled" : ":white_check_mark: is now enabled"), event.getMessage().getChannel(), false, false);
            }
            // K    - stops the update thread
            else if(message[1].equals("stop") && message[2].equals("updates")){
                updater.runn = false;
                cmd_messageDiscord((":ok_hand: update thread `runn = " + updater.runn + "`"), event.getMessage().getChannel(), false, false);
            }
        }
        // 3    - the block that controls messages that does not have a prefix i.e messages that are meant for matches
        else if(event.getMessage().getChannel().getID() != "277596483631579137" && event.getMessage().getChannel().getID() != "277893008806903809" && !chattoggle) {
            /*
                "Guard-function" makes it so that only people with a certain roleID will be able to actually send messages to Tinder matches
                "is this really necessary?? I want everyone to send messages :(".. If you don't have this every match will receive a duplicate of their own
                message. Why? Webhooks WILL trigger a MessageReceivedEvent just like any other user...
             */
            boolean doorman = false;
            for(int i = 0; i < event.getMessage().getAuthor().getRolesForGuild(event.getMessage().getGuild()).size(); i++){
                if(event.getMessage().getAuthor().getRolesForGuild(event.getMessage().getGuild()).get(i).getID().equals("278645767311196160")){
                    doorman = true;
                    break;
                }
            }

            /*
                If it got past the bouncer and the bot as sufficient access to Tinder (e.i has a xauth-token)
                go ahead and send that message (uses gifIntegrator from JSON_Interpreter to generate a giphy if
                message has prefix ":gif:" followed by a giphy link).
             */
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

    /*
        Purge command, simple code that WILL delete messages.
     */
    public void cmd_purge(MessageReceivedEvent event, int ant){
        for(int i = 0; i < ant; i++){
            System.out.println("purge deleting " + i);
            try{
                sleep(100); //not having a pause might result in weird results
                event.getMessage().getChannel().getMessages().get(0).delete();
            } catch (Exception e){
                break;
            }

        }
        System.out.println("done purging");
    }

    /*
        Creates a new Discord channel with a pre configured webhook.
     */
    public IChannel cmd_createChannel(String name, String hookName, String hookImage, IGuild guild) throws Exception{
        if(hookName == null)hookName = "UNKNOWN";
        if(hookImage == null)hookImage = "http://barabasilab.neu.edu/people/baruch/WebPage_files/Silhouette.jpg";

        IChannel tmp = guild.createChannel(name);
        tmp.createWebhook(hookName, hookImage).changeDefaultAvatar(Image.forUrl("jpg",hookImage));
        return tmp;
    }

    /*
        Sends a message to a specified channel, alert = tags @everyone, masked = uses webhook(0) instead of posting message as self
     */
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

    /*
        Whispers you with the roles and their ID's (useful for configuring the "bouncer")
     */
    public void cmd_getRoleID(IGuild guild, IUser user) throws Exception{
        String build = "";
        build += ("Your roles for " + guild.getName() + " are\n\n");
        for(int i = 0; i < guild.getRolesForUser(user).size(); i++){
            build += (guild.getRolesForUser(user).get(i).getName() + " - " + guild.getRolesForUser(user).get(i).getID() + "\n");
        }

        cmd_messageDiscord(build, user.getOrCreatePMChannel(), false, false);
    }


/*  hopefully old code (not tested though...)
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
