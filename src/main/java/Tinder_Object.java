import org.json.JSONObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;

import static java.lang.Thread.sleep;
/*
    Will comment this later, still some work to do here and it's a mess
 */

/**
 * Created by Simen (Scoop#8831) on 05.02.2017.
 */
public class Tinder_Object {
    public boolean alert = true;
    public boolean alertME = true;
    public CommandCentral cmd = null;
    private IGuild guild = null;
    public String myID = null;
    ArrayList<Match> matches = new ArrayList<Match>();

    public Tinder_Object(CommandCentral cmd, IGuild guild){
        this.cmd = cmd;
        this.guild = guild;
    }

    public void updateID(String myID){
        this.myID = myID;
    }

    public class Match{
        ArrayList<Message> messages = new ArrayList<Message>();
        public String matchID;
        private String personID;
        public IChannel myChannel;
        private IMessage profileMessage;    //displayed publicly on notifications
        private IMessage profileInfo;       //displayed locally on myChannel
        public Links.Symbiotic_Link link;
        public Cleverbot.Clever_Instance cleverAI;

        public String name;
        private String bio;
        private int age;
        String[] images;
        private boolean superlike;

        private int distance;
        private String songName;
        private String songArtist;

        public Match(String id, IChannel myChannel, String name, String bio, int age, String images, boolean superlike, IMessage profileMessage, IMessage profileInfo){
            matchID = id;
            personID = matchID.replace(myID, "");
            System.out.println(matchID);
            this.myChannel = myChannel;
            this.profileMessage = profileMessage;
            this.profileInfo = profileInfo;

            this.name = name;
            this.bio = bio;
            this.age = age;
            this.images = images.split("\n");
            this.superlike = superlike;

            try {
                generateAdditionStats();
            } catch (Exception ex){
                ex.printStackTrace();
            }
        }

        public void generateAdditionStats() throws Exception{
            if(profileMessage != null) {
                //addistatsa paa, ser ut som en dopselger
                JSONObject addiStats = new JSONObject(cmd.pat.handleData("https://api.gotinder.com/user/" + personID, "GET", new JSONObject()));
                distance = addiStats.getJSONObject("results").getInt("distance_mi");
                try {
                    songName = addiStats.getJSONObject("results").getJSONObject("spotify_theme_track").getString("name");
                    songArtist = addiStats.getJSONObject("results").getJSONObject("spotify_theme_track").getJSONArray("artists").getJSONObject(0).getString("name");
                    profileMessage.edit("**New match!** " + myChannel.mention(), cmd.buildMatchMessage(cmd.sanitize(name), bio, age, superlike, images[0], (songName + " - " + songArtist)));
                } catch (Exception ex) {

                }
            }
        }

        public void unmatched() throws Exception{
            EmbedBuilder tmp = new EmbedBuilder();
            tmp.withColor(cmd.settings.unMatchColorIMC);
            tmp.withTitle(cmd.settings.unMatchTitleIMC);
            tmp.withDescription(cmd.settings.unMatchDescIMC);
            tmp.withImage(cmd.settings.unMatchImageIMC);
            myChannel.sendMessage("", tmp.build(), false);

            try{
                profileMessage.edit("~~**New match!**~~ unmatched", cmd.unmatchMessageObject(name, bio, age, images[0]));
            } catch (Exception ex){}
        }

        public void unmatch() throws Exception{
            //thank the overlords for garbage collector
            cmd.pat.handleData(("https://api.gotinder.com/user/matches/" + matchID), "DELETE", new JSONObject());
            myChannel.delete();
            matches.remove(this);
        }

        public void sendMessage(String s) throws Exception{
            //JSONObject msg = new JSONObject();
            //msg.put("message", cmd.interp.gifIntegrator(cmd.interp.stripCode(s)));
            cmd.pat.handleData(("https://api.gotinder.com/user/matches/" + matchID), "POST", cmd.interp.gifIntegrator(cmd.interp.stripCode(s)));
        }

        public Message addMessage(String messageID, String matchID, String to_matchID, String from_matchID, String messageContent, String sent_date, String created_date, String timestamp){
            for (int i = 0; i < messages.size(); i++) {
                if (messages.get(i).messageID.equals(messageID)) {
                    //message already exists
                    return messages.get(i);
                }
            }
            //new message found
            if(alert){
                System.out.println("new message found");
                if(personID.equals(from_matchID)){
                    if(link == null){
                        try{
                            cmd.cmd_messageDiscord(messageContent, myChannel, false, true);
                            if(cleverAI != null){
                                //send the message to cleverbot aswell
                                cleverAI.sendToCloud(messageContent);
                            }
                        } catch (Exception ex){
                            System.out.println("TIMED OUT - RETRYING");
                            try{
                                sleep(3000);
                            } catch (Exception ex2){
                                System.out.println("zZzzZZZzzzZ error sleeping (ln 66)");
                            }
                        }
                    } else {
                        //send message to link
                        try {
                            link.linkMessage(messageContent, this);
                        } catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }
                } else {
                    if(alertME){
                        try{
                            myChannel.sendMessage(messageContent);
                        } catch (Exception ex){
                            System.out.println("TIMED OUT - RETRYING");
                            try{
                                sleep(3000);
                            } catch (Exception ex2){
                                System.out.println("zZzzZZZzzzZ error sleeping (ln 81)");
                            }
                            return addMessage(messageID, matchID, to_matchID, from_matchID, messageContent, sent_date, created_date, timestamp);
                        }
                    }
                }
                messages.add(new Message(messageID, matchID, to_matchID, from_matchID, messageContent, sent_date, created_date, timestamp));
            } else{
                messages.add(new Message(messageID, matchID, to_matchID, from_matchID, messageContent, sent_date, created_date, timestamp));
            }
            return messages.get(messages.size() - 1);
        }

        public class Message{
            private String messageID, matchID, to_matchID, from_matchID, messageContent, sent_date, created_date, timestamp;

            public Message(String messageID, String matchID, String to_matchID, String from_matchID, String messageContent, String sent_date, String created_date, String timestamp){
                this.messageID = messageID;
                this.matchID = matchID;
                this.to_matchID = to_matchID;
                this.from_matchID = from_matchID;
                this.messageContent = messageContent;
                this.sent_date = sent_date;
                this.created_date = created_date;
                this.timestamp = timestamp;
            }
        }
    }

    public Match addMatch(String id, String name, String bio, int age, String image, boolean superlike) throws Exception{
        for(int i = 0; i < matches.size(); i++){
            if(matches.get(i).matchID.equals(id)){
                //match already exsists
                return matches.get(i);
            }
        }
        //new match found
        if(alert){
            IChannel tmp = cmd.cmd_createChannel(name, name, image.substring(0, image.indexOf("\n")), guild);
            IMessage firstmsg = cmd.cmd_messageDiscord((image + "\n```\n{\"matchid\":\"" + id + "\"}\n\nNAME: " + name +"\nAGE: " + age +"\nBIO: " + bio + "\nSUPER: " + superlike + "```"), tmp, false, false);
            IMessage notiMSG = null;
            try{
                notiMSG = guild.getChannelByID(cmd.settings.defaultChannels.split(" ")[0]).sendMessage("**New match!** " + tmp.mention(), cmd.buildMatchMessage(cmd.sanitize(name), bio, age, superlike, image.substring(0, image.indexOf("\n")), null), false);
            } catch (Exception ex){
                notiMSG = cmd.cmd_messageDiscord(("Got new match, could not create embed... <#" + tmp.getID() + ">"), guild.getChannelByID(cmd.settings.defaultChannels.split(" ")[0]), false, false);
            }
            tmp.pin(firstmsg);
            matches.add(new Match(id, tmp, name, bio, age, image, superlike, notiMSG, firstmsg));
        } else{
            System.out.print("assign match " + name);
            IChannel tmp = null;
            if(guild.getChannelsByName(cmd.sanitize(name).toLowerCase()).size() < 1){
                System.out.println(" !!" + cmd.sanitize(name) + " < 1!!\ncant find this match");
                return null;    //will cause a nullpointerexception......
            }
            for(int i = 0; i < guild.getChannelsByName(cmd.sanitize(name).toLowerCase()).size(); i++){
                System.out.print(" " + i + ",");
                if(guild.getChannelsByName(cmd.sanitize(name).toLowerCase()).get(i).getPinnedMessages().size() > 0){
                    if(guild.getChannelsByName(cmd.sanitize(name).toLowerCase()).get(i).getPinnedMessages().get(0).getContent().contains(id)){
                        tmp = guild.getChannelsByName(cmd.sanitize(name).toLowerCase()).get(i);
                        System.out.println();
                        matches.add(new Match(id, tmp, name, bio, age, image, superlike, null, tmp.getPinnedMessages().get(0)));
                        break;
                    }
                }
            }

        }
        System.out.println();
        return matches.get(matches.size()-1);
    }
}
