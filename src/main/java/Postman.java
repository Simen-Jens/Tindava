import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Scoop on 07.02.2017.
 */
public class Postman {
    private String token;
    private String id;
    public String xauth;
    public String myID; //will use this later (see ln 65)

    public Postman(String token, String id){
        this.token = token;
        this.id = id;
    }

    public String handleData(String url, String metohd, JSONObject data) throws Exception{
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
            System.out.print("JSON get");
        } else {
            System.out.println(con.getResponseMessage() + " - " + url);
        }

        return sb.toString();
    }

    public boolean auth() throws Exception{
        JSONObject tmp = new JSONObject();
        tmp.put("facebook_token", token);
        tmp.put("facebook_id", id);

        String s = handleData("https://api.gotinder.com/auth", "POST", tmp);
        if(s.contains("token"))xauth = new JSONObject(s).getString("token");
        if(s.contains("user"))myID = new JSONObject(s).getJSONObject("user").getString("_id");

        //System.out.println(xauth);
        return s.contains("token");
    }
}
