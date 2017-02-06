import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.Status;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

/**
 * Created by Simen on 05.02.2017.
 */
public class Tinder_Object {
    public boolean alert = true;
    private CommandCentral cmd = null;
    private IGuild guild = null;
    private String myID = "58604bf5cb80e1af340e4b54";
    ArrayList<Match> matches = new ArrayList<Match>();

    public Tinder_Object(CommandCentral cmd, IGuild guild){
        this.cmd = cmd;
        this.guild = guild;
    }

    public class Match{
        ArrayList<Message> messages = new ArrayList<Message>();
        private String matchID;
        private String personID;
        private IChannel myChannel;

        public Match(String id, IChannel myChannel){
            matchID = id;
            personID = matchID.replace(myID, "");
            this.myChannel = myChannel;
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

                        }
                        return addMessage(messageID, matchID, to_matchID, from_matchID, messageContent, sent_date, created_date, timestamp);
                    }
                } else{
                    try{
                        cmd.cmd_messageDiscord(messageContent, myChannel, false, false);
                    } catch (Exception ex){
                        System.out.println("TIMED OUT - RETRYING");
                        try{
                            sleep(1000);
                        } catch (Exception ex2){

                        }
                        return addMessage(messageID, matchID, to_matchID, from_matchID, messageContent, sent_date, created_date, timestamp);
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

    public Match addMatch(String id, String name, String bio, int age, String image) throws Exception{
        for(int i = 0; i < matches.size(); i++){
            if(matches.get(i).matchID.equals(id)){
                //match already exsists
                return matches.get(i);
            }
        }
        //new match found
        if(alert){
            IChannel tmp = cmd.cmd_createChannel(name, name, image, guild);
            cmd.cmd_messageDiscord(("\nhttp://i.imgur.com/HUirNMb.png\n<#" + tmp.getID() + ">\n```\n -ID: " + id + "\n -Name: " + name + "\n -Age: " + age + "\n -Bio: " + bio + "\n```"), guild.getChannels().get(0), true, false);
            tmp.pin(cmd.cmd_messageDiscord((image + "\n```\n{\"matchid\":\"" + id + "\"}\n\nNAME: " + name +"\nAGE: " + age +"\nBIO: " + bio + "\n```"), tmp, false, false));
            matches.add(new Match(id, tmp));
        } else{
            IChannel tmp = null;
            for(int i = 0; i < guild.getChannels().size(); i++){
                if(guild.getChannels().get(i).getMessages().get(guild.getChannels().get(i).getMessages().size()-1).getContent().contains(id)){
                    tmp = guild.getChannels().get(i);
                    break;
                }
            }
            matches.add(new Match(id, tmp));
        }
        cmd.client.changeStatus(Status.game("with " + (guild.getChannels().size()-2) + " matches"));
        return matches.get(matches.size()-1);
    }
}
