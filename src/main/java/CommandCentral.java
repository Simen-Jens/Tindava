import org.json.JSONObject;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.Image;

import java.text.Normalizer;
import java.util.List;

import static java.lang.Thread.sleep;


/**
 * Created by Simen (Scoop#8831) on 05.02.2017.
 */
public class CommandCentral extends Main{
    private String fb_id;
    private String fb_token;
    public JSON_Interpreter interp;
    public Tinder_Object tndr;
    public Postman pat;
    private boolean chattoggle = true;
    private Update_Thread updater = new Update_Thread(10000L, 10000L, this);
    public Swipe_Control swiper;

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
            2l.
            2m.
     */
    public void interp(MessageReceivedEvent event) throws Exception{
        if(tndr == null){
            tndr = new Tinder_Object(this, client.getGuilds().get(0));
            interp = new JSON_Interpreter(tndr);
        }

        // 1
        String[] message = event.getMessage().getContent().split(" ");
        // 2
        if(message[0].equals("\uD83D\uDD25")){
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
                    swiper = new Swipe_Control(interp, pat, this);
                }
                event.getMessage().addReaction("\ud83d\udc4c");
                //cmd_messageDiscord((":ok_hand: Facebook ID set to " + fb_id), event.getMessage().getChannel(), false, false);
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
                    swiper = new Swipe_Control(interp, pat, this);
                }
                event.getMessage().addReaction("\ud83d\udc4c");
                //cmd_messageDiscord((":ok_hand: Facebook Token set to " + fb_token), event.getMessage().getChannel(), false, false);
            }
            // E    - supply xauth
            else if(message[1].equals("supply") && message[2].equals("xauth")){
                if(message.length != 4){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <xauth>", event.getMessage().getChannel(), false, false);
                    return;
                }
                pat = new Postman(null, null);
                pat.xauth = message[3];
                swiper = new Swipe_Control(interp, pat, this);
                event.getMessage().addReaction("\ud83d\udc4c");
            }
            // F    - purge
            else if(message[1].equals("purge")){
                if(message.length != 3){
                    cmd_messageDiscord("expected 1 paramters, got " + (message.length-3) + ". <amount to delete>", event.getMessage().getChannel(), false, false);
                    return;
                }
                cmd_purge(event, Integer.parseInt(message[2]));
            }
            // G    - unmatch
            else if (message[1].equals("unmatch")) {
                for(int i = 0; i < tndr.matches.size(); i++){
                    if(tndr.matches.get(i).myChannel == event.getMessage().getChannel()){
                        tndr.matches.get(i).unmatch();
                        return;
                    }
                }
                cmd_messageDiscord("Can't delete this match", event.getMessage().getChannel(), false, false);
            }
            // H    - request update
            else {
                if (message[1].equals("request") && message[2].equals("update")) {
                    if (message.length != 4) {
                        cmd_messageDiscord("expected 1 paramters, got " + (message.length - 3) + ". <json>", event.getMessage().getChannel(), false, false);
                        return;
                    }
                    if (message[3].equals("!auth!")) {
                        System.out.println("SENT FOR");
                        if (pat != null) {
                            cmd_messageDiscord((pat.auth() ? "login success" : pat.xauth != null ? "xauth token supplied, skipping login" : "login failed"), event.getMessage().getChannel(), false, false);
                        } else {
                            cmd_messageDiscord(":postal_horn: Missing postman, supply bot with a love letter :love_letter: (facebook token + id or a tinder xauth-token)", event.getMessage().getChannel(), false, false);
                            return;
                        }
                        if (pat.xauth != null) {
                            // login a-ok
                            interp.updateTinderFromFile();
                            updater.start();
                        }
                    } else {
                        interp.updateTinder(message[3]);
                    }
                }
                // I    - remove chats
                else if (message[1].equals("remove") && message[2].equals("chats")) {
                    cmd_removeChats(event);
                }
                // J    - toggle chat
                else if (message[1].equals("toggle") && message[2].equals("chat")) {
                    if (event.getMessage().getAuthor().getID().equals(event.getMessage().getGuild().getOwnerID())) {
                        chattoggle = !chattoggle;
                        cmd_messageDiscord((chattoggle ? ":negative_squared_cross_mark: chat is now disabled" : ":white_check_mark: chat is now enabled"), event.getMessage().getChannel(), false, false);
                    } else {
                        cmd_messageDiscord(("only server owner can toggle chat"), event.getMessage().getChannel(), false, false);
                    }
                }
                // K    - stops the update thread
                else if (message[1].equals("toggle") && message[2].equals("updates")) {
                    updater.toggle = !updater.toggle;
                    event.getMessage().addReaction("\u2714");
                    cmd_messageDiscord((":ok_hand: update thread `runn = " + updater.toggle + "`"), event.getMessage().getChannel(), false, false);
                }
                // L    - temp method to disable alerts for your own id
                else if (message[1].equals("t_o_alert")) {
                    tndr.alertME = false;
                    event.getMessage().addReaction("\ud83d\udc4c");
                }
                // M    - requests recommendations from Tinder and swipes right on all of them
                else if (message[1].equals("swipe") && message[2].equals("all")) {
                    if (swiper != null) {
                        swiper.swipeAll();
                    } else {
                        cmd_messageDiscord(("need to login :broken_heart:"), event.getMessage().getChannel(), false, false);
                    }
                } else if (message[1].equals("organize")){
                    organizeChannels(client.getGuilds().get(0).getChannels());
                } else if (message[1].equals("unmatch") && message[2].equals("all")){
                    swiper.unmatchAll();
                } else if (message[1].equals("test")){
                    swiper.postRecom();
                }
            }
        }
        // 3    - the block that controls messages that does not have a prefix i.e messages that are meant for matches
        else if((!settings.defaultChannels.contains(event.getMessage().getChannel().getID()))) {
            if (!event.getMessage().getChannel().isPrivate()) {
                if (!event.getMessage().getAuthor().isBot()) {
                    if (!chattoggle) {
            /*
                "Guard-function" makes it so that only people with a certain roleID will be able to actually send messages to Tinder matches
                "is this really necessary?? I want everyone to send messages :(".. If you don't have this every match will receive a duplicate of their own
                message. Why? Webhooks WILL trigger a MessageReceivedEvent just like any other user...
             */
                        boolean doorman = false;
                        for (int i = 0; i < event.getMessage().getAuthor().getRolesForGuild(event.getMessage().getGuild()).size(); i++) {
                            if (event.getMessage().getAuthor().getRolesForGuild(event.getMessage().getGuild()).get(i).getID().equals(settings.messageRole)) {
                                doorman = true;
                                break;
                            }
                        }

            /*
                If it got past the bouncer and the bot as sufficient access to Tinder (e.i has a xauth-token)
                go ahead and send that message (uses gifIntegrator from JSON_Interpreter to generate a giphy if
                message has prefix ":gif:" followed by a giphy link).
             */
                        if (pat != null && doorman) {
                            if (pat.xauth != null) {
                                String matchidFmsg = "";
                                for (int i = 0; i < tndr.matches.size(); i++) {
                                    if (tndr.matches.get(i).myChannel.getID().equals(event.getMessage().getChannel().getID())) {
                                        matchidFmsg = tndr.matches.get(i).matchID;
                                    }
                                }
                                pat.handleData(("https://api.gotinder.com/user/matches/" + matchidFmsg), "POST", interp.gifIntegrator(interp.stripCode(event.getMessage().getContent())));
                                //event.getMessage().addReaction("\ud83d\udc8c");
                                updater.pulls = 0;
                                return;
                            }
                        }
                    }
                    event.getMessage().addReaction("\u274c");
                }
            }
            if(event.getMessage().getChannel().isPrivate()){
                event.getMessage().addReaction("\u2754");
            }
        }
        //speeds up pullrate
        client.changeStatus(Status.game("with " + tndr.matches.size() + " matches"));
        updater.pulls = 0;
    }

    public void organizeChannels(List<IChannel> channels) throws Exception{
        System.out.print("Starting organize\nClient nodes sorted - .");
        IChannel t;
        int i, max = channels.size() -1;
        for (int k = 0 ; k < max; k++) {
            System.out.print(".");
            if (channels.get(k).getName().compareTo(channels.get(k+1).getName()) > 0) {
                t = channels.get(k+1);
                i = k;

                do{
                    channels.remove(i+1);
                    channels.add(i+1, channels.get(i));
                    i--;
                } while (i >= 0 && channels.get(i).getName().compareTo(t.getName()) > 0);
                channels.remove(i+1);
                channels.add(i+1, t);
            }
        }
        System.out.print("\nServer nodes sorted - ");
        for(int u = 0; u < channels.size(); u++){
            System.out.print(".");
            channels.get(u).changePosition(u);
        }

        for(int u = settings.defaultChannels.split(" ").length-1; u >= 0 ; u--){
            client.getChannelByID(settings.defaultChannels.split(" ")[u]).changePosition(-1);
        }
        System.out.println("\nDone sorting matches");
    }

    public EmbedObject unmatchMessageObject(String name, String bio, int age, String image){
        return new EmbedBuilder().
                withAuthorName(name).
                appendField(String.valueOf(age), bio.equals("") ? "{EMPTY BIO}" : bio, false).
                withImage(image).
                withThumbnail(settings.unMatchThumb).
                /*withThumbnail("http://emojipedia-us.s3.amazonaws.com/cache/51/3a/513a734baf098ead6eb961f8d4092fc3.png").
                withColor(213, 90, 112).*/
                withColor(settings.unMatchColor).
                build();
    }

    public EmbedObject buildMatchMessage(String name, String bio, int age, boolean superlike, String image){
        return new EmbedBuilder().
                withAuthorName(name).
                appendField(String.valueOf(age), bio.equals("") ? "{EMPTY BIO}" : bio, false).
                withImage(image).
                withThumbnail(superlike ? settings.superMatchThumb : settings.defaultMatchThumb).
                /*withThumbnail(superlike ? "http://pre01.deviantart.net/db85/th/pre/i/2016/295/b/0/tinder_super_like_star_by_topher147-dalwd0y.png" : "http://emojipedia-us.s3.amazonaws.com/cache/16/22/1622b595a25ee401f56aa047cd4520eb.png").
                withColor((superlike ? 1 : 120), (superlike ? 182 : 177), (superlike ? 203 : 89))*/
                withColor(superlike ? settings.superMatchColor : settings.defaultMatchColor).
                build();
    }

    public boolean cmd_removeChats(MessageReceivedEvent event) throws Exception{
        String[] idList = new String[event.getMessage().getGuild().getChannels().size()];
        for(int i = 0; i < event.getMessage().getGuild().getChannels().size(); i++){
            idList[i] = event.getMessage().getGuild().getChannels().get(i).getID();
        }
        for(String s : idList){
            if (!settings.defaultChannels.contains(s))event.getMessage().getGuild().getChannelByID(s).delete();
        }
        System.out.println("Done removing chats");
        return true;
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
        if(hookName == null || hookName.equals(""))hookName = "UNKNOWN";
        if(hookImage == null)hookImage = "http://barabasilab.neu.edu/people/baruch/WebPage_files/Silhouette.jpg";

        String sanitizedName = sanitize(name);

        IChannel tmp = guild.createChannel(sanitizedName);
        tmp.createWebhook(sanitizedName, hookImage).changeDefaultAvatar(Image.forUrl("jpg",hookImage));
        //organizeChannels(client.getGuilds().get(0).getChannels());
        return tmp;
    }

    /*
        Sends a message to a specified channel, alert = tags @everyone, masked = uses webhook(0) instead of posting message as self
     */
    public IMessage cmd_messageDiscord(String message, IChannel channel, boolean alert, boolean masked) throws Exception{
        if(!masked){
            try {
                return channel.sendMessage(alert ? ("@everyone " + message) : message);
            } catch (Exception ex){
                ex.printStackTrace();
            }
            return null;
        } else{
            String url="https://discordapp.com/api/webhooks/" + channel.getWebhooks().get(0).getID() + "/" + channel.getWebhooks().get(0).getToken();
            JSONObject msg = new JSONObject();
            msg.put("content",message);
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

    public String sanitize(String sIn) throws Exception{
        String[] tbNom = sIn.split("");
        String[] illgChar = "ç,æ,œ,á,é,í,ó,ú,à,è,ì,ò,ù,ä,ë,ï,ö,ü,ÿ,â,ê,î,ô,û,å,ø,Ø,Å,Á,À,Â,Ä,È,É,Ê,Ë,Í,Î,Ï,Ì,Ò,Ó,Ô,Ö,Ú,Ù,Û,Ü,Ÿ,Ç,Æ,Œ".split(",");
        String[] lgChar = "c,ae,oe,a,e,i,o,u,a,e,i,o,u,a,e,i,o,u,y,a,e,i,o,u,a,o,O,A,A,A,A,A,E,E,E,E,I,I,I,I,O,O,O,O,U,U,U,U,Y,C,AE,OE".split(",");

        for(int i = 0; i < tbNom.length; i++){
            for(int k = 0; k < illgChar.length; k++){
                if(illgChar[k].contains(tbNom[i])){
                    tbNom[i] = lgChar[k];
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        for(String s : tbNom) {
            builder.append(s);
        }

        return Normalizer.normalize(builder.toString(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }
}
