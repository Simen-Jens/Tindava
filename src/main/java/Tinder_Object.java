import org.json.JSONObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.Status;

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
            this.myChannel = myChannel;
            this.profileMessage = profileMessage;

            this.name = name;
            this.bio = bio;
            this.age = age;
            this.images = images.split("\n");
            this.superlike = superlike;
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
                System.out.println(matchID + " {} " + from_matchID);
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

            if(superlike){
                cmd.cmd_messageDiscord(("\nhttp://i.imgur.com/6VsMgAL.png\n<#" + tmp.getID() + ">\n```\n -ID: " + id + "\n -Name: " + name + "\n -Age: " + age + "\n -Bio: " + bio + "\n```"), guild.getChannels().get(0), true/*change this to false if you hate @everyone*/, false);
                firstmsg.addReaction("\ud83d\udc99");
            } else{
                cmd.cmd_messageDiscord(("\nhttp://i.imgur.com/HUirNMb.png\n<#" + tmp.getID() + ">\n```\n -ID: " + id + "\n -Name: " + name + "\n -Age: " + age + "\n -Bio: " + bio + "\n```"), guild.getChannels().get(0), true/*change this to false if you hate @everyone*/, false);
            }

            tmp.pin(firstmsg);
            matches.add(new Match(id, tmp, name, bio, age, image, superlike, firstmsg));
        } else{
            String s1 = Normalizer.normalize(name, Normalizer.Form.NFKD);
            String regex = "[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+";
            String sanitizedName = new String(s1.replaceAll(regex, "").getBytes("ascii"), "ascii").toLowerCase().replace("?","o");

            System.out.print("assign match " + name);
            IChannel tmp = null;
            for(int i = 0; i < guild.getChannelsByName(sanitizedName).size(); i++){
                System.out.print(" " + i + ",");
                if(guild.getChannelsByName(sanitizedName).get(i).getPinnedMessages().size() > 0){
                    if(guild.getChannelsByName(sanitizedName).get(i).getPinnedMessages().get(0).getContent().contains(id)){
                        tmp = guild.getChannelsByName(sanitizedName).get(i);
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
