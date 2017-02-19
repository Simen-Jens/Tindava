import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Simen (Scoop#8831) on 07.02.2017.
 */
public class Postman {
    public String facebookToken;
    public String facebookID;
    public String xauth;
    public String myID;
    private CommandCentral cmd;

    public Postman(CommandCentral cmd){
        this.cmd = cmd;
    }


    synchronized public String handleData(String url, String metohd, JSONObject data) throws Exception{
        URL object=new URL(url);
        HttpURLConnection con = (HttpURLConnection) object.openConnection();

        con.setDoOutput(true);
        con.setDoInput(true);

        if(xauth != null)con.setRequestProperty("X-Auth-Token", xauth);
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("User-agent", "Tinder/3.0.4 (iPhone; iOS 7.1; Scale/2.00)");

        con.setRequestMethod(metohd);

        if(metohd.equals("POST")) {
            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            wr.write(data.toString());
            wr.flush();
        } else if(metohd.equals("DELETE")){
            con.connect();
            System.out.println("DELETE " + String.valueOf(con.getResponseCode()));
            return String.valueOf(con.getResponseCode());
        }

        StringBuilder sb = new StringBuilder();
        int HttpResult = con.getResponseCode();
        if (HttpResult == HttpURLConnection.HTTP_OK) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            //System.out.println(sb.toString());
            System.out.println("PAT " + metohd + " - " + con.getResponseCode() + " (" + url + ")");
        } else {
            System.out.println(con.getResponseMessage() + " - " + url);
        }

        return sb.toString();
    }

    public boolean auth() throws Exception{
        if(xauth != null){
            String s = handleData("https://api.gotinder.com/meta", "GET", new JSONObject());
            if (s.contains("user")) myID = new JSONObject(s).getJSONObject("user").getString("_id");
            return true;
        } else {
            JSONObject tmp = new JSONObject();
            tmp.put("facebook_token", facebookToken);
            tmp.put("facebook_id", facebookID);

            String s = handleData("https://api.gotinder.com/auth", "POST", tmp);
            if (s.contains("token")) xauth = new JSONObject(s).getString("token");
            if (s.contains("user")) myID = new JSONObject(s).getJSONObject("user").getString("_id");
            cmd.tndr.myID = myID;
            //System.out.println(xauth);
            return s.contains("token");
        }
    }

    public void reAuthWithFacebook(){

    }
}
