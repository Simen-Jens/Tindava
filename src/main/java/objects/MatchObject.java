package objects;

import tools.MatchHandler;
import org.json.JSONException;
import org.json.JSONObject;
import settings.Settings;
import sx.blah.discord.handle.obj.IChannel;
import tunnels.DiscordTunnel;
import tunnels.TinderTunnel;

import java.util.HashMap;

import static java.lang.Thread.sleep;
import static tools.StringManipulation.calculateAge;

public class MatchObject implements Comparable<MatchObject>{
    // All messages between a match gets stored in a HashMap with the messageID as the key
    private HashMap<String, MessageObject> messages = new HashMap<>();
    private String fullMatchID;
    private JSONObject matchInfo;
    private IChannel discordChannel;

    public MatchObject(JSONObject matchInfo, IChannel discordChannel, String fullMatchID) throws JSONException {
        this.matchInfo = matchInfo;
        this.discordChannel = discordChannel;
        this.fullMatchID = fullMatchID;
    }

    /*
    *  Method for adding messages to a specific match (NOTE: this is not the same as sending the match a message
    *  this simply stores all messages in the memory)
    *
    *  @param JSONObject messageInfo - JSON message as represented by the servers
    *  @return boolean - returns true to symbolize that the message had been added.
    * */
    public boolean addMessage(JSONObject messageInfo) throws JSONException{
        if(messages.containsKey(messageInfo.getString("_id"))) return false;
        messages.put(messageInfo.getString("_id"), new MessageObject(messageInfo));

        if(Settings.firstRun || messageInfo.getString("from").equals(Settings.getSettings.getString("my_tinder_id"))){
            //This message is from us or from the first run, we should not alert Discord
        } else {
            //This message is from the match and is not read on the first run
            try {
                DiscordTunnel.receiveMessageFromMatch(messageInfo.getString("message"), discordChannel);
                sleep(1000);
            } catch (InterruptedException ex){
                ex.printStackTrace();
            }
        }
        return true;
    }

    /*
    *  Method for sending a message to the match
    *
    *  @param String message - The message to be sent (this passes through TinderTunnel.messageCreator)
    *  @param String xauth - XAuth token for our user
    * */
    public void sendMessage(String message, String xauth) throws JSONException{
        TinderTunnel.sendMessage(fullMatchID, message, xauth);
    }

    /*
    *  Method for unmatching the match
    *
    *  @param String xauth - XAuth token for our user
    * */
    public void unmatch(String xauth) throws JSONException{
        TinderTunnel.unmatch(fullMatchID, xauth);
    }

    /*
    *  Method that gets called when we are unmatched by a match
    * */
    public void unmatched() throws JSONException{
        DiscordTunnel.unmatched(discordChannel);

        // Since we use java we can just let the garbage collector remove the match from memory
        MatchHandler.getMatches.remove(matchInfo.getString("_id"));
    }

    public String getID(){
        return matchInfo.getString("_id");
    }

    public String getName(){
        return matchInfo.getString("name");
    }

    public String getBio(){
        String bio = "";
        try{
            bio = matchInfo.getString("bio");
        } catch (JSONException ex){
            bio = "{EMPTY BIO (missing tag)}";
        }
        for(int i = 0; i < bio.length(); i++){
            if(bio.charAt(i) != ' ') return bio;
        }
        return "{EMPTY BIO}";
    }

    public int getAge(){
        return calculateAge(matchInfo.getString("birth_date"));
    }

    public String[] getImages(){
        String sb = "";
        for(int i = 0; i < matchInfo.getJSONArray("photos").length(); i++){
            sb += matchInfo.getJSONArray("photos").getJSONObject(i).getString("url") + "\n";
        }
        return  sb.split("\n");
    }

    public IChannel getDiscordChannel(){
        return discordChannel;
    }

    @Override
    public int compareTo(MatchObject o) {
        try {
            return matchInfo.getString("_id").compareTo(o.matchInfo.getString("_id"));
        } catch (Exception ex){
            ex.printStackTrace();
            System.exit(0);
            return 0;
        }
    }
}