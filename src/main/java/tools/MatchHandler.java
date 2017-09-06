package tools;

import objects.MatchObject;
import org.json.JSONException;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.IChannel;
import tunnels.DiscordTunnel;

import java.util.HashMap;

import static java.lang.Thread.sleep;

/**
 * Created by Simen (Scoop#8831) on 16.05.2017.
 */
public class MatchHandler {
    // All matchObjects gets stored in a HashMap with the matchID as the key
    public static HashMap<String, MatchObject> getMatches = new HashMap<>();
    public static boolean firstRun = true;

    /*
    *  Method for adding a match object to the HashMap
    *  The method will check if the match already exsists, if
    *  it comes back as a new match it will add it and return true.
    *
    *  @param JSONObject matchInfo - The JSON for our match
    *  @param boolean isSuper - Weather the match has super lied us or not
    *  @return MatchObject - Returns the newly added match, returns null if the match already exists
    * */
    public static MatchObject addMatch(JSONObject matchInfo, boolean isSuper, String fullMatchID) throws JSONException{
        //Check if the match already exsists
        if(getMatches.containsKey(matchInfo.getString("_id"))){
            return null;
        }

        //The match does not exsist, check if we have a reference channel for it or if we need to create a new channel
        IChannel reference = null;
        try {
            reference = DiscordTunnel.searchForChannel(matchInfo.getString("name").toLowerCase(), matchInfo.getString("_id"));
        } catch (InterruptedException ex){
            ex.printStackTrace();
        }

        if(reference == null){
            //We do not have a reference channel for this match, this will also automatically result in an alert
            IChannel newChannel = DiscordTunnel.createChannel(matchInfo);
            try {
                sleep(1000);
            } catch (InterruptedException ex){}

            MatchObject tmp = new MatchObject(matchInfo, newChannel, fullMatchID);
            getMatches.put(matchInfo.getString("_id"), tmp);
            DiscordTunnel.addWebHook(newChannel, tmp);
            DiscordTunnel.createMatch(tmp, isSuper, newChannel);
            return tmp;
        } else {
            //We have a reference channel for this match, this will not result in an alert
            return getMatches.put(matchInfo.getString("_id"), new MatchObject(matchInfo, reference, fullMatchID));
        }
    }
}
