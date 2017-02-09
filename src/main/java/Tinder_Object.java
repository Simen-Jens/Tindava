import org.json.JSONObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.EmbedBuilder;

import java.text.Normalizer;
import java.util.ArrayList;

import static java.lang.Thread.sleep;
/*
    Will comment this later, still some work to do here and it's a mess
 */

/**
 * Created by Scoop on 05.02.2017.
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
        private IMessage profileMessage;

        private String name;
        private String bio;
        int age;
        String[] images;
        private boolean superlike;

        public Match(String id, IChannel myChannel, String name, String bio, int age, String images, boolean superlike, IMessage profileMessage){
            matchID = id;
            personID = matchID.replace(myID, "");
            System.out.println(matchID);
            this.myChannel = myChannel;
            this.profileMessage = profileMessage;

            this.name = name;
            this.bio = bio;
            this.age = age;
            this.images = images.split("\n");
            this.superlike = superlike;
        }

        public void unmatched() throws Exception{
            EmbedBuilder tmp = new EmbedBuilder();
            tmp.withColor(231, 57, 69);
            tmp.withTitle("Unmatched");
            tmp.withDescription("aw nuts...");
            tmp.withImage("http://i.imgur.com/pAi00xj.png");
            myChannel.sendMessage("", tmp.build(), false);
        }

        public void unmatch() throws Exception{
            //thank the overlords for garbage collector
            cmd.pat.handleData(("https://api.gotinder.com/user/matches/" + matchID), "DELETE", new JSONObject());
            myChannel.delete();
            matches.remove(this);
        }

        public void sendMessage(String s) throws Exception{
            JSONObject msg = new JSONObject();
            msg.put("message", s);
            cmd.pat.handleData(("https://api.gotindaer.com/user/matches/" + matchID), "POST", msg);
        }

        public Message addMessage(String messageID, String matchID, String to_matchID, String from_matchID, String messageContent, String sent_date, String created_date, String timestamp){
            for(int i = 0; i < messages.size(); i++){
                if(messages.get(i).messageID.equals(messageID)){
                    //message already exists
                    return messages.get(i);
                }
            }
            //new message found
            if(alert){
                System.out.println("found new message");
                //System.out.println(matchID + " {} " + from_matchID);
                if(personID.equals(from_matchID)){
                    try{
                        cmd.cmd_messageDiscord(messageContent, myChannel, false, true);
                    } catch (Exception ex){
                        System.out.println("TIMED OUT - RETRYING");
                        try{
                            sleep(2000);
                        } catch (Exception ex2){
                            System.out.println("zZzzZZZzzzZ error sleeping (ln 66)");
                        }
                        return addMessage(messageID, matchID, to_matchID, from_matchID, messageContent, sent_date, created_date, timestamp);
                    }
                } else{
                    if(alertME) {
                        //you probably don't want this part, it would possibly duplicate messages. Comment out the whole block if needed
                        try {
                            cmd.cmd_messageDiscord(messageContent, myChannel, false, false);
                        } catch (Exception ex) {
                            System.out.println("TIMED OUT - RETRYING");
                            try {
                                sleep(2000);
                            } catch (Exception ex2) {
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
            return messages.get(messages.size()-1);
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
            try{
                guild.getChannelByID(cmd.defaultChannels.split(" ")[0]).sendMessage("**New match!**", cmd.buildMatchMessage(cmd.sanitize(name), bio, age, superlike, image.substring(0, image.indexOf("\n"))), false);
            } catch (Exception ex){
                cmd.cmd_messageDiscord(("Got new match, could not create embed... <#" + tmp.getID() + ">"), guild.getChannelByID(cmd.defaultChannels.split(" ")[0]), false, false);
            }
            tmp.pin(firstmsg);
            matches.add(new Match(id, tmp, name, bio, age, image, superlike, firstmsg));
        } else{
            System.out.print("assign match " + name);
            IChannel tmp = null;
            if(guild.getChannelsByName(cmd.sanitize(name).toLowerCase()).size() < 1){
                System.out.println(" !!" + cmd.sanitize(name) + " < 1!!\ncant add this match");
                return null;
            }
            for(int i = 0; i < guild.getChannelsByName(cmd.sanitize(name).toLowerCase()).size(); i++){
                System.out.print(" " + i + ",");
                if(guild.getChannelsByName(cmd.sanitize(name).toLowerCase()).get(i).getPinnedMessages().size() > 0){
                    if(guild.getChannelsByName(cmd.sanitize(name).toLowerCase()).get(i).getPinnedMessages().get(0).getContent().contains(id)){
                        tmp = guild.getChannelsByName(cmd.sanitize(name).toLowerCase()).get(i);
                        System.out.println();
                        matches.add(new Match(id, tmp, name, bio, age, image, superlike, tmp.getPinnedMessages().get(0)));
                        break;
                    }
                }
            }

        }
        System.out.println();
        cmd.client.changeStatus(Status.game("with " + matches.size() + " matches"));
        return matches.get(matches.size()-1);
    }
}
