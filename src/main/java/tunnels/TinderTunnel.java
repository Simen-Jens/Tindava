package tunnels;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import settings.Settings;

import java.util.LinkedList;

/**
 * Created by Simen (Scoop#8831) on 16.05.2017.
 *
 * This will function as our pipeline to the Tinder API.
 * Feel free to extract this class to your own project if
 * you just need to talk to the Tinder server and don't care
 * about the Discord part
 *
 * (Still under the same license even if you only extract the class alone)
 */
public class TinderTunnel {
    private static LinkedList<JSONObject> recommendations = new LinkedList<>();

    /*
    *  Method for receiving xauth-token from the Tinder server
    *
    *  @param String facebookID - The Facebook ID for the user to authenticate
    *  @param String facebookOAuth - The Facebook OAuth token for the official Tinder app
    *  @return String - XAuth token for the user / "error, could not authenticate" if it failed to authenticate
    * */
    public static String getXAuth(String facebookID, String facebookOAuth){
        try {
            String xauth = new JSONObject(HttpTunnel.handleData("https://api.gotinder.com/auth", "POST", new JSONObject().put("facebook_token", facebookOAuth).put("facebook_id", facebookID), null)).getString("token");
            Settings.setStringValue("my_tinder_id", getMyID(xauth));
            System.out.println("myTinerID is set to: " + Settings.getSettings.getString("my_tinder_id"));
            return xauth;
        } catch (Exception ex){
            System.out.println("could not authenticate");
            ex.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    /*
    *  Method for receiving updates for the server, once called a shitton of JSON data will be
    *  received fom the official Tinder server
    *
    *  If anyone knows a better way to get specific updtes (such as messages specific to single match)
    *  please do update.
    *
    *  @param String xauth - XAuth token for the user we will pull updates for
    *  @return JSONObject - The metric shitton of information we got from the server
    * */
    public static JSONObject getUpdate(String xauth){
        try {
            return new JSONObject(HttpTunnel.handleData("https://api.gotinder.com/updates", "POST", new JSONObject(), xauth));
        } catch (JSONException ex){
            System.out.println("ERROR (maybe token expired)");
            ex.printStackTrace();
            // We try to get a new xauth token
            try {
                Settings.setStringValue("xauth",
                        getXAuth(Settings.getSettings.getString("facebook_id"),
                                FacebookTunnel.getOAuth(Settings.getSettings.getString("facebook_email"), Settings.getSettings.getString("facebook_password"))));

                System.out.println("OK got new token: " + Settings.getSettings.getString("xauth"));
                return getUpdate(Settings.getSettings.getString("xauth"));
            } catch (JSONException exx){
                exx.printStackTrace();
                System.exit(0);
                return null;
            }
        }
    }

    /*
    *  Method for sending a message to a specific match
    *
    *  @param String matchID - The match ID you wish to send a message to (match id = myuserid + theiruserid)
    *  @param String message - The message you wish to send, note this gets parsed through messageCreator
    *  @param String xauth - XAuth token for the user we will send a message from
    *  @return String - The response from the server
    * */
    public static String sendMessage(String matchID, String message, String xauth){
        return HttpTunnel.handleData(("https://api.gotinder.com/user/matches/" + matchID), "POST", messageCreator(message), xauth);
    }

    /*
    *  Method for "hearthing" a message, more to come if I can be bothered.
    * */
    public static String hearthMessage(String matchID, String message, String xauth){
        return "NOT IMPLEMENTED";
    }

    /*
    *  Method for unmatching a specific match
    *
    *  @param String matchID - The match ID you wish to unmatch (match id = myuserid + theiruserid)
    *  @param String xauth - XAuth token for the user we will unmatch from
    *  @return String - The response from the server
    * */
    public static String unmatch(String matchID, String xauth){
        return HttpTunnel.handleData(("https://api.gotinder.com/user/matches/" + matchID), "DELETE", new JSONObject(), xauth);
    }

    /*
    *  Method for receiving A SINGLE recommendation from the Tinder servers, it will fetch
    *  somewhere around 10-15 recommendations on every call to the server. We therefor store
    *  all recommendations in a private LinkedList and only call the server once we have popped
    *  the entire list.
    *
    *  @param String xauth - XAuth token for the user we will get recommendations for
    *  @return JSONObject - The JSONObject for our recommendation (only contains one Tinder profile)
    * */
    public static JSONObject getRecommendation(String xauth){
        if(recommendations.isEmpty()){
            try {
                JSONArray tmprecs = new JSONObject(HttpTunnel.handleData("https://api.gotinder.com/recs", "GET", new JSONObject(), xauth)).getJSONArray("results");
                for(int i = 0; i < tmprecs.length(); i++){
                    recommendations.push(tmprecs.getJSONObject(i));
                }
                return getRecommendation(xauth);
            } catch (JSONException ex){
                System.out.println("ERROR (maybe token expired, I will try to fetch a new one)");
                ex.printStackTrace();
                // We try to get a new xauth token
                try {
                    Settings.setStringValue("xauth",
                            getXAuth(Settings.getSettings.getString("facebook_id"),
                                    FacebookTunnel.getOAuth(Settings.getSettings.getString("facebook_email"), Settings.getSettings.getString("facebook_password"))));
                    System.out.println("OK got new token: " + Settings.getSettings.getString("xauth"));
                    return getRecommendation(Settings.getSettings.getString("xauth"));
                } catch (JSONException exx){
                    exx.printStackTrace();
                    System.exit(0);
                    return null;
                }
            }
        } else {
            return recommendations.pop();
        }
    }

    /*
    *  Method for "liking" a specific profile (NOTE! this uses tinderID and not matchID, tinderID = matchID - myID
    *
    *  @param String tinderID - targeted profile's Tinder ID
    *  @param String xauth - XAuth token for the user we will like from
    *  @return boolean - True or false depending on whether it was a match or not
    * */
    public static boolean swipeRight(String tinderID, String xauth){
        return HttpTunnel.handleData(("https://api.gotinder.com/like/" + tinderID), "GET", new JSONObject(), xauth).contains(Settings.getSettings.getString("my_tinder_id"));
    }

    public static boolean swipeUp(String tinderID, String xauth){
        return HttpTunnel.handleData(("https://api.gotinder.com/like/" + tinderID + "/super"), "POST", new JSONObject(), xauth).contains(Settings.getSettings.getString("my_tinder_id"));
    }

    /*
    *  Method for "passing" a specific profile (NOTE! this uses tinderID and not matchID, tinderID = matchID - myID
    *
    *  @param String tinderID - targeted profile's Tinder ID
    *  @param String xauth - XAuth token for the user we will pass from
    *  @return boolean - Should always be false, since we did not match
    * */
    public static boolean swipeLeft(String tinderID, String xauth){
        return HttpTunnel.handleData(("https://api.gotinder.com/pass/" + tinderID), "GET", new JSONObject(), xauth).contains("AlwaysReturnFalseSinceThereIsNoMatch"); //awful oneliner
    }

    /*
    *  Fills up the recommendations array and swipes to the right on all of them.
    *
    *  @param String xauth - XAuth token for the user we will like from
    *  @return int - Amount of remaining likes
    * */
    public static int swipeAll(String xauth){
        if(recommendations.isEmpty()){
            recommendations.add(getRecommendation(xauth));
            swipeAll(xauth); //possible infinite loop
        } else {
            while(!recommendations.isEmpty()){
                swipeRight(recommendations.pop().getString("_id"), xauth);
            }
        }
        return getRemainingLikes(xauth);
    }

    /*
    *  Method for retrieving your own Tinder ID
    *
    *  @param String xauth - XAuth token for the user we will pass from
    *  @return boolean - Tinder ID / "ERROR"
    * */
    public static String getMyID(String xauth){
        try {
            return new JSONObject(HttpTunnel.handleData("https://api.gotinder.com/meta", "GET", new JSONObject(), xauth)).getJSONObject("user").getString("_id");
        } catch (Exception ex){
            ex.printStackTrace();
            return "ERROR";
        }
    }

    /*
    *  Method for getting additional information about a user, Favorite Track, instagram and so on
    *
    *  @param String tinderID - targeted profile's Tinder ID
    *  @param String xauth - XAuth token for the user we will pass from
    *  @return JSONObject - our information
    * */
    public static JSONObject getAdditionalInformation(String tinderID, String xauth){
        try {
            return new JSONObject(HttpTunnel.handleData("https://api.gotinder.com/user/" + tinderID, "GET", new JSONObject(), xauth));
        } catch (Exception ex){
            ex.printStackTrace();
            return new JSONObject();
        }
    }

    /*
    *  Method for setting the location, uses googlemaps to geocode an address to coordinates
    *
    *  @param String address - address
    *  @param String xauth - XAuth token for the user we will change position for
    * */
    public static void setLocation(String address, String xauth){
        try {
            JSONObject mapsAdrs = new JSONObject(HttpTunnel.handleData("https://maps.googleapis.com/maps/api/geocode/json?address=" +
                    address.replace(" ", "+") +
                    "&key=" +
                    Settings.getSettings.getString("google_api_key"), "GET", new JSONObject(), null));
            JSONObject loc = new JSONObject();
            //loc.put("lat", mapsAdrs.getString("lat"));
            //loc.put("lon", mapsAdrs.getString("lng"));
            loc.put("lat", mapsAdrs.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
            loc.put("lon", mapsAdrs.getJSONArray("results").getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
            HttpTunnel.handleData("https://api.gotinder.com/user/ping", "POST", loc, xauth);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /*
    *  Method for getting the amount of remaining likes
    *
    *  @param String xauth - XAuth token for our user
    *  @return int - Amount of likes remaining
    * */
    public static int getRemainingLikes(String xauth){
        return new JSONObject(HttpTunnel.handleData("https://api.gotinder.com/meta", "GET", new JSONObject(), xauth)).getJSONObject("rating").getInt("likes_remaining");
    }

    /*
    *  Method for converting a message as a JSON object, if the message text contains a giphy link it
    *  will create a giphy JSON-message object
    *
    *  @param String message - message text to convert to object
    *  @return JSONObject - our encoded message
    * */
    private static JSONObject messageCreator(String message) {
        if(message.contains("!nosend!")){
            message = "";
        }
        message.replaceAll("\\s*\\`[^\\)]*\\`\\s*", "");
        try {
            JSONObject jssss = null;
            if (message.length() > 4) {
                if (message.contains("giphy.com/")) {
                    jssss = new JSONObject();
                    jssss.put("message", message.substring(0));
                    String gifid = message.substring(0).replace("https://media.giphy.com/media/", "").replace("/giphy.gif", "");
                    jssss.put("gif_id", gifid);
                    jssss.put("type", "gif");
                } else {
                    jssss = new JSONObject();
                    jssss.put("message", message);
                }
            } else {
                jssss = new JSONObject();
                jssss.put("message", message);
            }
            return jssss;
        } catch (Exception ex){
            System.out.println("CRITICAL ERROR");
            ex.printStackTrace();
            System.exit(0);
            return null;
        }
    }
}
