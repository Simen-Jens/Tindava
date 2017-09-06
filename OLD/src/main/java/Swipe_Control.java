import org.json.JSONObject;

import java.time.Year;

/**
 * Created by Simen (Scoop#8831) on 13.02.2017.
 */
public class Swipe_Control extends Auto_Swipe{

    public class Current_Rec {
        private String _id;

        //I don't want to create a Match object, so just duping info, sorry thx garbage collector.
        protected String name;
        protected String bio;
        protected int age;
        protected String[] images;
        protected double distance;
        protected String spotify;


        public Current_Rec(String _id) throws Exception {
            this._id = _id;
            gatherInfo();
        }

        public void gatherInfo() throws Exception {
            JSONObject userData = new JSONObject(cmd.pat.handleData("https://api.gotinder.com/user/" + _id, "GET", new JSONObject()));
            userData = userData.getJSONObject("results");
            name = userData.getString("name");
            bio = userData.getString("bio");
            age = (Year.now().getValue() - (Integer.parseInt(userData.getString("birth_date").substring(0, 4))));
            images = new String[userData.getJSONArray("photos").length()];
            for(int i = 0; i < userData.getJSONArray("photos").length(); i++){
                images[i] = userData.getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url");
            }
            distance = userData.getInt("distance_mi")*1.61;    //I use the metric system, just remove the '*1.61' if you want miles (I might make this a setting)
            try{
                spotify = userData.getJSONObject("spotify_theme_track").getString("name") + " - " + userData.getJSONObject("spotify_theme_track").getJSONArray("artists").getJSONObject(0).getString("name");
            } catch (Exception ex){
                //this is fine
            }
        }
    }
    //-----------------Swipe_Control Class-----------------
    private Current_Rec currec;
    public Swipe_Control(JSON_Interpreter intep, Postman post, CommandCentral cmd){
        super(intep, post, cmd);
    }

    public Current_Rec newRecommendation() throws Exception{
        currec = new Current_Rec(getRecommendations()[0]);
        return currec;
    }

    public void postUI(){

    }


}
