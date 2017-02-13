import org.json.JSONObject;

/**
 * Created by Simen (Scoop#8831) on 08.02.2017.
 */
public class Auto_Swipe {
    JSON_Interpreter intep;
    Postman post;
    CommandCentral cmd;

    public Auto_Swipe(JSON_Interpreter intep, Postman post, CommandCentral cmd){
        this.intep = intep;
        this.post = post;
        this.cmd = cmd;
    }

    public int getRemainingSwipes() throws Exception{
        JSONObject meta = new JSONObject(post.handleData("https://api.gotinder.com/meta", "GET", new JSONObject()));
        return meta.getJSONObject("rating").getInt("likes_remaining");
    }

    public String[] getRecommendations() throws Exception{
        JSONObject recommendations = new JSONObject(post.handleData("https://api.gotinder.com/recs", "GET", new JSONObject()));
        String[] tmp = new String[recommendations.getJSONArray("results").length()];
        for(int i = 0; i < tmp.length; i++){
            tmp[i] = recommendations.getJSONArray("results").getJSONObject(i).getString("_id");
        }
        return tmp;
    }

    public void swipeAll() throws Exception{
        String[] tmp = getRecommendations();
        int remaining = getRemainingSwipes();

        for(int i = 0; i < (remaining < tmp.length ? remaining : tmp.length); i++){
            System.out.println(post.handleData(("https://api.gotinder.com/like/" + tmp[i]), "GET", new JSONObject()));
        }
        cmd.cmd_messageDiscord(("Swiped on " + tmp.length + " people, " + (remaining-tmp.length < 1 ? 0 : remaining-tmp.length) + " remaining swipes :robot:"), cmd.client.getChannelByID(cmd.settings.defaultChannels.split(" ")[0]), false, false);
    }

    public void unmatchAll() throws Exception{
        for(Tinder_Object.Match m : cmd.tndr.matches){
            m.unmatch();
        }
    }
}
