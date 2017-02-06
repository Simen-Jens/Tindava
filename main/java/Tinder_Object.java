import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

import java.util.ArrayList;

/**
 * Created by Simen on 05.02.2017.
 */
public class Tinder_Object {
    public boolean alert = true;
    private CommandCentral cmd = null;
    private IGuild guild = null;
    ArrayList<Match> matches = new ArrayList<Match>();

    public Tinder_Object(CommandCentral cmd, IGuild guild){
        this.cmd = cmd;
        this.guild = guild;
    }

    private class Match{
        ArrayList<Message> messages = new ArrayList<Message>();
        private String matchID;
        private IChannel myChannel;

        public Match(String id, IChannel myChannel){
            matchID = id;
            this.myChannel = myChannel;
        }

        public void addMessage(String messageID, String matchID, String to_matchID, String from_matchID, String messageContent, String sent_date, String created_date, String timestamp){
            for(int i = 0; i < messages.size(); i++){
                if(messages.get(0).messageID.equals(messageID)){
                    //message already exists
                    return;
                }
            }
            //new message found
            if(alert);
            messages.add(new Message(messageID, matchID, to_matchID, from_matchID, messageContent, sent_date, created_date, timestamp));
        }

        private class Message{
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

    public void addMatch(String id) throws Exception{
        for(int i = 0; i < matches.size(); i++){
            if(matches.get(0).matchID.equals(id)){
                //match already exsists
                return;
            }
        }
        //new match found
        if(alert){
            IChannel tmp = cmd.cmd_createChannel(id, "NAME_GOES_HERE", null, guild);
            cmd.cmd_messageDiscord(("\nhttp://i.imgur.com/HUirNMb.png\n" + "```\n -ID: " + id + "\n -Name: \n -Age: \n -Bio: \n```"), guild.getChannels().get(0), false, false);
            tmp.pin(cmd.cmd_messageDiscord(("```\n{\"matchid\":\"" + id + "\"}\n```"), tmp, false, false));
            tmp.pin(cmd.cmd_messageDiscord("**<NAME> - <AGE>**\n*<BIO>*", tmp, false, true));
            matches.add(new Match(id, tmp));
        } else{
            IChannel tmp = null;
            for(int i = 0; i < guild.getChannels().size(); i++){
                if(guild.getChannels().get(i).getName().equals(id)){
                    tmp = guild.getChannels().get(i);
                    break;
                }
            }
            matches.add(new Match(id, tmp));
        }
    }
}
