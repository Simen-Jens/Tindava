import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Year;

/**
 * Created by Simen on 05.02.2017.
 */
public class JSON_Interpreter {
    private String data;
    private Tinder_Object tinder;
    public JSON_Interpreter(String data, Tinder_Object tinder){
        this.data = data;
        this.tinder = tinder;
    }

    public Tinder_Object updateTinder(String json) throws Exception{
        String meme = readFile("C:/Users/Simen/Desktop/MYJSON.txt", StandardCharsets.ISO_8859_1);
        System.out.println(meme);

        JSONObject updates = new JSONObject(meme);
        JSONArray matches = updates.getJSONArray("matches");

        for(int i = 0; i < matches.length(); i++){
            JSONObject thisMatch = new JSONObject(matches.get(i).toString());
            Tinder_Object.Match thisMatchObject = tinder.addMatch(thisMatch.getString("_id"), thisMatch.getJSONObject("person").getString("name"), thisMatch.getJSONObject("person").getString("bio"), (Year.now().getValue() - (Integer.parseInt(thisMatch.getJSONObject("person").getString("birth_date").substring(0,4)))), thisMatch.getJSONObject("person").getJSONArray("photos").getJSONObject(0).getJSONArray("processedFiles").getJSONObject(0).getString("url"));

            JSONArray messages = thisMatch.getJSONArray("messages");
            for(int k = 0; k < messages.length(); k++){
                JSONObject thisMessage = new JSONObject(messages.get(k).toString());
                thisMatchObject.addMessage(thisMessage.getString("_id"), thisMessage.getString("match_id"), thisMessage.getString("to"), thisMessage.getString("from"), thisMessage.getString("message"), thisMessage.getString("sent_date"), thisMessage.getString("created_date"), thisMessage.getString("timestamp"));
            }
        }
        return null;
    }

    static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
