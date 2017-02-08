import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Year;

/**
 * Created by Scoop on 05.02.2017.
 */
public class JSON_Interpreter {
    private Tinder_Object tinder;
    public JSON_Interpreter(Tinder_Object tinder){
        this.tinder = tinder;
    }

    public void updateTinderFromFile() throws Exception{
        String empty = "{\"matches\":[],\"blocks\":[],\"lists\":[],\"deleted_lists\":[],\"last_activity_date\":\"2017-02-06T16:41:33.813Z\"}";
        tinder.alert = false;
        updateTinder(empty);
        tinder.alert = true;
    }

    public void updateTinder(String json) throws Exception{
        long time = System.currentTimeMillis();

        JSONObject updates = new JSONObject(json);
        JSONArray matches = updates.getJSONArray("matches");

        for(int i = 0; i < matches.length(); i++){
            try {
                JSONObject thisMatch = new JSONObject(matches.get(i).toString());
                if(thisMatch.getJSONObject("person").getString("name").equals("")) {  //just a test if-test, will get redacted
                    String images = "";
                    for (int k = 0; k < thisMatch.getJSONObject("person").getJSONArray("photos").length(); k++) {
                        images += (thisMatch.getJSONObject("person").getJSONArray("photos").getJSONObject(k).getJSONArray("processedFiles").getJSONObject(0).getString("url") + "\n");
                    }
                    thisMatch.getJSONObject("person");
                    Tinder_Object.Match thisMatchObject = tinder.addMatch(thisMatch.getString("_id"), thisMatch.getJSONObject("person").getString("name"), thisMatch.getJSONObject("person").getString("bio"), (Year.now().getValue() - (Integer.parseInt(thisMatch.getJSONObject("person").getString("birth_date").substring(0, 4)))), images);

                    JSONArray messages = thisMatch.getJSONArray("messages");
                    for (int k = 0; k < messages.length(); k++) {
                        JSONObject thisMessage = new JSONObject(messages.get(k).toString());
                        thisMatchObject.addMessage(thisMessage.getString("_id"), thisMessage.getString("match_id"), thisMessage.getString("to"), thisMessage.getString("from"), thisMessage.getString("message"), thisMessage.getString("sent_date"), thisMessage.getString("created_date"), thisMessage.getString("timestamp"));
                    }
                }
            } catch (Exception ex){
                System.out.println("Error, probably someone who deleted their Tinder / unmatched you :c");
            }
        }
        //save to file here!
        System.out.println("Data structure updated\nalert = " + tinder.alert + "\nTime used:" + (System.currentTimeMillis() - time) + "ms");
    }

    public String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public JSONObject gifIntegrator(String message) throws Exception{
        JSONObject jssss = null;
        if(message.length() > 4){
            if(message.substring(0, 5).equals(":gif:")){
                jssss = new JSONObject();
                jssss.put("message", message.substring(6));
                String gifid = message.substring(6).replace("https://media.giphy.com/media/", "").replace("/giphy.gif", "");
                jssss.put("gif_id", gifid);
                jssss.put("type", "gif");
            } else {
                jssss = new JSONObject();
                jssss.put("message", message);
            }
        } else{
            jssss = new JSONObject();
            jssss.put("message", message);
        }
        return jssss;
    }
}
