package tools;

import objects.MatchObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import settings.Settings;
import sx.blah.discord.api.IDiscordClient;
import tunnels.DiscordTunnel;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Simen (Scoop#8831) on 12.08.2017.
 */
public class JSONDecoder {
    private static int matchCount = 0;
    public static IDiscordClient client = null;

    public static void tinderUpdateDecode(JSONObject tinderUpdate) throws JSONException, IOException{
        JSONArray matches = tinderUpdate.getJSONArray("matches");

        for(int i = 0; i < matches.length(); i++){
            JSONObject currentMatch = matches.getJSONObject(i);

            /*
            Apparently this one-liner does not work? I guess I might be missing how .put() works with static classes?
            "tools.MatchHandler.MatchObject currentMatchObject = tools.MatchHandler.addMatch(currentMatch.getJSONObject("person"), currentMatch.getBoolean("is_super_like"));"

            \/\/ Replaced by the following two lines \/\/
            */
            MatchHandler.addMatch(currentMatch.getJSONObject("person"), currentMatch.getBoolean("is_super_like"), currentMatch.getString("_id"));
            MatchObject currentMatchObject = MatchHandler.getMatches.get(currentMatch.getJSONObject("person").getString("_id"));

            for(int k = 0; k < currentMatch.getJSONArray("messages").length(); k++){
                currentMatchObject.addMessage(currentMatch.getJSONArray("messages").getJSONObject(k));
            }
        }

        //Saves the update to a cache-file (we can recover data if the bot-crashes)
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Settings.getSettings.getString("cache_path")), "UTF-8"));
        try {
            out.write(tinderUpdate.toString());
        } finally {
            out.close();
        }


        //update our game status to reflect amout of matches
        if(matches.length() > matchCount){
            //we have gained matches, sort channels
            DiscordTunnel.organizeChannels(client.getGuilds().get(0));
            matchCount = MatchHandler.getMatches.size();
            DiscordTunnel.updateGameStatus("with " + matchCount + " matches");
        } else if(matches.length() < matchCount){
            //we got rejected by a match, clean up unmatches
            try{
                cleanUpUnMatch(matches);
            } catch (JSONException ex){
                ex.printStackTrace();
            }
            DiscordTunnel.updateGameStatus("with " + matchCount + " matches");
        }
        Settings.firstRun = false;
    }

    private static void cleanUpUnMatch(JSONArray matches) throws JSONException{
        ArrayList<MatchObject> deleteableMatches = new ArrayList<>();
        for(Map.Entry<String, MatchObject> entry : MatchHandler.getMatches.entrySet()){
            boolean deletefactor = true;
            for(int i = 0; i < matches.length(); i++){
                if(matches.getJSONObject(i).getJSONObject("person").getString("_id").equals(entry.getKey())){
                    //This match still exists
                    deletefactor = false;
                    break;
                }
            }
            if(deletefactor){
                //this match can be deleted
                System.out.println("DEBUG: Removed one inactive match");
                deleteableMatches.add(entry.getValue());
            }
        }
        for(MatchObject mo : deleteableMatches){
            mo.unmatched();
        }
    }
}
