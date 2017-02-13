import org.json.JSONArray;
import org.json.JSONObject;
import sx.blah.discord.handle.obj.Status;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Year;

/**
 * Created by Simen (Scoop#8831) on 05.02.2017.
 */
public class JSON_Interpreter {
    private Tinder_Object tinder;
    private int updateCouner = 0;

    public JSON_Interpreter(Tinder_Object tinder){
        this.tinder = tinder;
    }

    public void updateTinderFromFile() throws Exception{
        tinder.cmd.client.changeStatus(Status.game("with previous cache"));
        System.out.println("reading cache");
        tinder.myID = tinder.cmd.pat.myID;
        String empty = readFile("empty.json", StandardCharsets.UTF_8);
        tinder.alert = false;
        updateTinder(empty);
        tinder.alert = true;
    }

    public void updateTinder(String json) throws Exception{
        JSONObject updates = new JSONObject(json);
        JSONArray matches = updates.getJSONArray("matches");

        for(int i = 0; i < matches.length(); i++){
            if(!tinder.cmd.settings.excludeName.contains(matches.getJSONObject(i).getJSONObject("person").getString("name")) && i >= tinder.cmd.settings.excludeBefore && matches.length()-tinder.cmd.settings.excludeAfter >= i) {
                try {
                    JSONObject thisMatch = new JSONObject(matches.get(i).toString());
                    String images = "";
                    for (int k = 0; k < thisMatch.getJSONObject("person").getJSONArray("photos").length(); k++) {
                        images += (thisMatch.getJSONObject("person").getJSONArray("photos").getJSONObject(k).getJSONArray("processedFiles").getJSONObject(0).getString("url") + "\n");
                    }
                    thisMatch.getJSONObject("person");
                    Tinder_Object.Match thisMatchObject = tinder.addMatch(thisMatch.getString("_id"), thisMatch.getJSONObject("person").getString("name"), thisMatch.getJSONObject("person").getString("bio"), (Year.now().getValue() - (Integer.parseInt(thisMatch.getJSONObject("person").getString("birth_date").substring(0, 4)))), images, thisMatch.getBoolean("is_super_like"));

                    JSONArray messages = thisMatch.getJSONArray("messages");
                    for (int k = 0; k < messages.length(); k++) {
                        JSONObject thisMessage = new JSONObject(messages.get(k).toString());
                        thisMatchObject.addMessage(thisMessage.getString("_id"), thisMessage.getString("match_id"), thisMessage.getString("to"), thisMessage.getString("from"), thisMessage.getString("message"), thisMessage.getString("sent_date"), thisMessage.getString("created_date"), thisMessage.getString("timestamp"));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println(ex);
                    System.out.println("Error, I've had so many problems in the try-block... I don't even know where to begin");
                }
            }
        }

        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tinder.cmd.settings.cache_path), "UTF-8"));
        try {
            out.write(json);
        } finally {
            out.close();
        }

        tinder.alertME = updateCouner > 0 ? false : true;
        cleanUpUnMatch(json);
        updateCouner++;
        tinder.cmd.client.changeStatus(Status.game("with " + tinder.matches.size() + " matches"));
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

    public String stripCode(String s){
        return s.replaceAll("\\s*\\`[^\\)]*\\`\\s*", "");
    }

    public void cleanUpUnMatch(String json) throws Exception{
        JSONObject js = new JSONObject(json);
        int clearance = 0;
        for(int i = 0; i < tinder.matches.size() - clearance; i++){
            boolean deleteFactor = true;
            for(int k = 0; k < js.getJSONArray("matches").length(); k++){
                if(js.getJSONArray("matches").getJSONObject(k).getString("_id").equals(tinder.matches.get(i).matchID)){
                    deleteFactor = false;
                    break;
                }
            }
            if(deleteFactor){
                //this match can be deleted
                System.out.println("removed one inactive match");
                tinder.matches.get(i).unmatched();
                //tinder.matches.get(i).myChannel.delete();
                tinder.matches.remove(i);
                clearance++;
                i--;
            }
        }
    }
}
