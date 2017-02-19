import org.json.JSONObject;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.Image;

import java.text.Normalizer;

import static java.lang.Thread.sleep;


/**
 * Created by Simen (Scoop#8831) on 05.02.2017.
 */
public class CommandCentral extends Main{
    private String fb_id;
    private String fb_token;
    public JSON_Interpreter interp;
    public Tinder_Object tndr;
    public Postman pat = new Postman(this);
    FacebookLogin fblogin;
    private boolean chattoggle = false;
    private Update_Thread updater = new Update_Thread(10000L, 10000L, this);
    public Swipe_Control swiper;
    private Links linkController = new Links(this);
    private Cleverbot cleverbot = new Cleverbot(this);

    /*
    Called upon when the bot is ready

    Creates a Tinder_Object for guild(0) (bot is only ment to be run on one guild)
    Creates a JSON_Interpreter for the Tinder_Object
     */
    public void initDone(){
        try {
            tndr = new Tinder_Object(this, client.getGuilds().get(0));
            interp = new JSON_Interpreter(tndr);
            swiper = new Swipe_Control(interp, pat, this);
            if(settings.autoLogin) {
                cmd_reauth(false);
            } else{
                System.out.println("autologin false, login '\uD83D\uDD25 login`");
            }
        } catch (Exception ex){
            ex.printStackTrace();
            System.out.println("Failed to instantiate Tinder_Object, JSON_Interpreter and Swipe_Control.\nDo this manually with '\uD83D\uDD25 instance'");
        }
    }


    public void interp(MessageReceivedEvent event) throws Exception{
        if(tndr == null){
            tndr = new Tinder_Object(this, client.getGuilds().get(0));
            interp = new JSON_Interpreter(tndr);
        }

        String[] message = event.getMessage().getContent().split(" ");
        if(message[0].equals("\uD83D\uDD25")){
            if(message[1].equals("my") && message[2].equals("roles")){
                cmd_getRoleID(event.getMessage().getGuild(), event.getMessage().getAuthor());
            }
            //
            else if(message[1].equals("supply") && message[2].equals("id")){
                cmd_supply_info(message[3], "id");
                event.getMessage().addReaction("\ud83d\udc9c");
            }
            //
            else if(message[1].equals("supply") && message[2].equals("auth_token")){
                cmd_supply_info(message[3], "oauth2");
                event.getMessage().addReaction("\ud83d\udc9c");
            }
            //
            else if(message[1].equals("supply") && message[2].equals("xauth")){
                cmd_supply_info(message[3], "xauth");
                event.getMessage().addReaction("\ud83d\udc9c");
            }
            //
            else if(message[1].equals("facebook") && message[2].equals("email")){
                cmd_supply_info(message[3], "email");
                event.getMessage().addReaction("\ud83d\udc9c");
            }
            //
            else if(message[1].equals("facebook") && message[2].equals("password")){
                cmd_supply_info(message[3], "password");
                event.getMessage().addReaction("\ud83d\udc9c");
            }
            //
            else if(message[1].equals("purge")){
                cmd_purge(event, Integer.parseInt(message[2]));
            }
            //
            else if(message[1].equals("unmatch")){
                cmd_unmatch(event.getMessage().getChannel());
            }
            //
            else if(message[1].equals("remove") && message[2].equals("chats")){
                cmd_removeChats(event);
            }
            //
            else if(message[1].equals("toggle") && message[2].equals("chat")){
                chattoggle = !chattoggle;
                client.getChannelByID(settings.defaultChannels.split(" ")[0]).sendMessage(chattoggle ? "\ud83d\udd35 chat is now enabled" : "\ud83d\udd34 chat is now disabled");
            }
            //
            else if(message[1].equals("toggle") && message[2].equals("updates")){
                updater.toggle = !updater.toggle;
                client.getChannelByID(settings.defaultChannels.split(" ")[0]).sendMessage(updater.toggle ? "\ud83d\udd35 thread is now enabled" : "\ud83d\udd34 thread is now disabled");
            }
            //
            else if(message[1].equals("swipe") && message[2].equals("all")){
                swiper.swipeAll();
            }
            //
            else if(message[1].equals("organize")){
                event.getMessage().addReaction("\ud83d\udc9c");
                organize();
            }
            //
            else if(message[1].equals("unmatch") && message[2].equals("all")){
                boolean togPrevState = updater.toggle;
                updater.toggle = false;
                swiper.unmatchAll();
                event.getMessage().addReaction("\ud83d\udc9c");
                wait(5000);
                event.getMessage().addReaction("\ud83c\udd97");
                updater.toggle = togPrevState;
            }
            //
            else if(message[1].equals("request") && message[2].equals("update")){
                if(message[3].equals("!auth!")){
                    cmd_reauth(false);
                } else{
                  interp.updateTinder(message[3]);
                }
            }
            //
            else if(message[1].equals("set") && message[2].equals("address")){
                if(isJSONnull(settings.googleKey)){
                    System.out.println("No google API key found! use 'set lat' & 'set lon' instead");
                    event.getMessage().addReaction("\u274c");
                } else {
                    JSONObject mapsAdrs = new JSONObject(pat.handleData("https://maps.googleapis.com/maps/api/geocode/json?address=" + event.getMessage().getContent().substring(13).replace(" ", "+") + "&key=" + settings.googleKey, "GET", new JSONObject()));
                    mapsAdrs = mapsAdrs.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                    cmd_changeLocation(mapsAdrs.getString("lat"), mapsAdrs.getString("lng"), event.getMessage());
                }
            }
            //
            else if(message[1].equals("instance")){
                settings.autoLogin = true;
                initDone();;
            }
            //
            else if(message[1].equals("link")){
                if(settings.defaultChannels.contains(event.getMessage().getChannel().getID())){
                    event.getMessage().addReaction("\u274c");
                } else {
                    cmd_link(event.getMessage().getChannel());
                    event.getMessage().addReaction("\ud83d\udd17");
                }
            }
            //
            else if(message[1].equals("cleverbot")){
                if(!isJSONnull(settings.cleverbotKey)) {
                    if (settings.defaultChannels.contains(event.getMessage().getChannel().getID())) {
                        event.getMessage().addReaction("\u274c");
                    } else {
                        if (cmd_cleverbot(event.getMessage().getChannel())) {
                            event.getMessage().addReaction("\ud83d\udcbb");
                        } else {
                            event.getMessage().addReaction("\u274c");
                        }
                    }
                } else{
                    System.out.println("No cleverbot API key detected");
                    event.getMessage().addReaction("\u274c");
                }
            }
            //
            else if(message[1].equals("update") && message[2].equals("settings")){
                event.getMessage().addReaction("\ud83d\udc9c");
                settings = new Settings();
            }
            //
            else if(message[1].equals("login")){
                event.getMessage().addReaction("\ud83d\udc9c");
                cmd_reauth(false);
            }
        } else{
            if(!settings.defaultChannels.contains(event.getMessage().getChannel().getID())){
                if(!event.getMessage().getChannel().isPrivate()){
                    if(!event.getMessage().getAuthor().isBot()){
                        if(chattoggle){
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

                            if(doorman){
                                String matchidFmsg = "";
                                for (int i = 0; i < tndr.matches.size(); i++) {
                                    if (tndr.matches.get(i).myChannel.getID().equals(event.getMessage().getChannel().getID())) {
                                        matchidFmsg = tndr.matches.get(i).matchID;
                                    }
                                }
                                pat.handleData(("https://api.gotinder.com/user/matches/" + matchidFmsg), "POST", interp.gifIntegrator(interp.stripCode(event.getMessage().getContent())));
                                //event.getMessage().addReaction("\ud83d\udc8c");
                                //updater.pulls = 0;
                                return;
                            }
                        }
                        if(!event.getMessage().getAuthor().isBot())event.getMessage().addReaction("\u274c");
                    }
                }
                if(event.getMessage().getChannel().isPrivate()){
                    event.getMessage().addReaction("\u2754");
                }
            }
        }
    }

    public boolean cmd_cleverbot(IChannel matchChannel) throws Exception{
        for(int i = 0; i < tndr.matches.size(); i++){
            if(tndr.matches.get(i).myChannel == matchChannel){
                cleverbot.attachClever(tndr.matches.get(i));
                return (tndr.matches.get(i).cleverAI != null);
            }
        }
        return false;
    }

    public void cmd_link(IChannel matchChannel) throws Exception{
        for(int i = 0; i < tndr.matches.size(); i++){
            if(tndr.matches.get(i).myChannel == matchChannel){
                linkController.setLink(tndr.matches.get(i));
                return;
            }
        }
    }

    public void cmd_changeLocation(String lat, String lon, IMessage caller) throws Exception{
        JSONObject loc = new JSONObject();
        loc.put("lat", lat);
        loc.put("lon", lon);
        String awnser = pat.handleData("https://api.gotinder.com/user/ping", "POST", loc);
        if(awnser.equals("{\"status\":200}")){
            caller.addReaction("\u2708");
        } else{
            caller.addReaction("\u203d");
        }
    }

    public void cmd_unmatch(IChannel matchChannel) throws Exception{
        for(int i = 0; i < tndr.matches.size(); i++){
            if(tndr.matches.get(i).myChannel == matchChannel){
                tndr.matches.get(i).unmatch();
                return;
            }
        }
    }

    public void cmd_reauth(boolean reauth){
        if(!isJSONnull(settings.xauth)){
            System.out.println(settings.xauth);
            pat.xauth = settings.xauth;
        }
        //
        else if(!isJSONnull(settings.facebook_id) && !isJSONnull(settings.facebook_token)){
            pat.facebookID = settings.facebook_id;
            pat.facebookToken = settings.facebook_token;
        }
        //
        else if(!isJSONnull(settings.facebook_email) && !isJSONnull(settings.facebook_password) && !isJSONnull(settings.facebook_id)){
            fblogin = new FacebookLogin(settings.facebook_email, settings.facebook_password, settings.facebook_id, this);
            try {
                client.getChannelByID(settings.defaultChannels.split(" ")[0]).sendMessage("", new EmbedBuilder().
                        withAuthorName("Facebook").
                        withAuthorIcon("https://images.seeklogo.net/2016/09/facebook-icon-preview.png").
                        withTitle("Facebook credentials found").
                        withDescription("logging in").
                        withColor(41,83,150).build(), false);
            } catch (Exception ex){
                ex.printStackTrace();
            }
            pat.facebookID = settings.facebook_id;
            pat.facebookToken = fblogin.generateNewOauth();
        }
        // none of the above
        else{
            System.out.println("no form for credentials found\nfill out settings.json or supply with supply command");
            return;
        }

        try{
            cmd_login(reauth);
        } catch (Exception ex){}
    }

    public void cmd_login(boolean reauth) throws Exception{
        boolean succ = pat.auth();
        try{
            IMessage last = myLastSent;
            if(last.getEmbedded().get(0).getDescription().equals("logging in")){
                last.edit("", new EmbedBuilder().
                        withAuthorName("Facebook").
                        withAuthorIcon(succ ? "http://i.imgur.com/mSl8apU.png" : "http://i.imgur.com/K5s3uzO.png").
                        withTitle("Facebook credentials found").
                        withDescription(succ ? "login success" : "login failed").
                        withColor(succ ? 76 : 244, succ ? 175 : 67, succ ? 80 : 54).build());
            }
            if(!succ)return;
        } catch (Exception ex){
            cmd_messageDiscord((succ ? "login success" : pat.xauth != null ? "xauth token supplied, skipping login" : "login failed"), client.getChannelByID(settings.defaultChannels.split(" ")[0]), false, false);
            if(!succ)return;
        }
        if(pat.xauth != null && succ && !reauth){
            interp.updateTinderFromFile();
            updater.start();
        } else if(reauth && pat.xauth != null && succ){
            updater.toggle = true;
        }
    }

    public void cmd_supply_info(String info, String type){
        if(type.equals("id")){
            settings.facebook_id = info;
        } else if(type.equals("oauth2")){
            settings.facebook_token = info;
        } else if(type.equals("xauth")){
            settings.xauth = info;
        } else if(type.equals("email")){

        }
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



    /*
        non cmd methods
     */

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

    public void organize(){
        System.out.println("!STARTING SORT! (O = no change, X = change, E = error)");
        IChannel[] current = clientStructure();
        IChannel[] sorted = clientSort(current);
        finalSort(current, sorted, 0);
        System.out.println("!DONE SORTING!\n\n");
    }


    public IChannel[] clientStructure(){
        IChannel[] current = new IChannel[client.getGuilds().get(0).getChannels().size() - settings.defaultChannels.split(" ").length];
        for(int i = 0; i < client.getGuilds().get(0).getChannels().size(); i++){
            if(!settings.defaultChannels.contains(client.getGuilds().get(0).getChannels().get(i).getID())){
                current[client.getGuilds().get(0).getChannels().get(i).getPosition()-settings.defaultChannels.split(" ").length] = client.getGuilds().get(0).getChannels().get(i);
            }
        }
        return current;
    }

    public IChannel[] clientSort(IChannel[] current){
        IChannel[] tmp = current.clone();
        IChannel t;
        int i, max = tmp.length -1;
        for (int k = 0 ; k < max; k++) {
            if (tmp[k].getName().compareTo(tmp[k+1].getName()) > 0) {
                t = tmp[k+1];
                i = k;

                do{
                    tmp[i+1] = tmp[i];
                    i--;
                } while (i >= 0 && tmp[i].getName().compareTo(t.getName()) > 0);
                tmp[i+1] = t;
            }
        }
        return tmp;
    }

    public void finalSort(IChannel[] current, IChannel[] sorted, int index){
        for(int i = index; i < sorted.length; i++){
            if(current[i].getID().equals(sorted[i].getID())){
                //this is good
                System.out.print("O");
                //System.out.print(current[i].getName() + " == " + sorted[i].getName());
            } else{
                System.out.print("X");
                //System.out.print(current[i].getName() + " != " + sorted[i].getName() + " || SOLVING || moving " + sorted[i].getName() + " to ");
                try {
                    sorted[i].changePosition(i + settings.defaultChannels.split(" ").length);
                    finalSort(clientStructure(), sorted, i+1);
                    return;
                } catch (Exception ex){
                    ex.printStackTrace();
                }
                System.out.print("E");

            }
        }
        System.out.println("");
    }

/*  old sorting method, new one will hopefully be less intensive for Discord4J lib

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
*/
    public EmbedObject unmatchMessageObject(String name, String bio, int age, String image){
        return new EmbedBuilder().
                withAuthorName(name).
                appendField(String.valueOf(age), bio.equals("") ? "{EMPTY BIO}" : bio, false).
                withImage(image).
                withThumbnail(settings.unMatchThumb).
                withColor(settings.unMatchColor).
                build();
    }

    public EmbedObject buildMatchMessage(String name, String bio, int age, boolean superlike, String image, String spotify){
        EmbedBuilder tmp = new EmbedBuilder();
        tmp.withAuthorName(name).
                appendField(String.valueOf(age), bio.equals("") ? "{EMPTY BIO}" : bio, false).
                withImage(image).
                withThumbnail(superlike ? settings.superMatchThumb : settings.defaultMatchThumb).
                withColor(superlike ? settings.superMatchColor : settings.defaultMatchColor);
        if(spotify != null)tmp.withFooterText(spotify).withFooterIcon("http://i.imgur.com/uI8QXiQ.png");
        return tmp.build();
    }

    public boolean isJSONnull(String json){
        return json.equals("null");
    }
}
