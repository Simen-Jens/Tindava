package tunnels;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Simen (Scoop#8831) on 13.08.2017.
 *
 * Only contains one method for using HttpURLConnection, originally I had this in my TinderTunnel, but after realizing
 * WebHooks would need to use HttpURLConnection as well I decided it would be best to have a separate tunnel for HTTP.
 */
public class HttpTunnel {

    /*
    *  @param String url - The url for our HttpURLConnection
    *  @param String method - The HTTP method that should be used (GET/POST/DELETE...).
    *  @param JSONObject data - RAW data to be sent together with the HTTP request (if we use POST, DELETE, PATCH..., leave it empty otherwise).
    *  @param String xauth - The data we should place in the "X-Auth-Token" header (this is needed for connection with Tinder). null-value drops the "X-Auth-Token" header.
    *  @return String - Response from the server, in all our cases raw JSON data, but this can be anything.
    * */
    public static String handleData(String url, String metohd, JSONObject data, String xauth) {
        try {
            URL object = new URL(url);
            HttpURLConnection con = (HttpURLConnection) object.openConnection();

            con.setDoOutput(true);
            con.setDoInput(true);

            if (xauth != null) con.setRequestProperty("X-Auth-Token", xauth);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("User-agent", "Tinder/3.0.4 (iPhone; iOS 7.1; Scale/2.00)");

            con.setRequestMethod(metohd);

            if (metohd.equals("POST")) {
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(data.toString());
                wr.flush();
            } else if (metohd.equals("DELETE")) {
                con.connect();
                System.out.println("DELETE " + String.valueOf(con.getResponseCode()));
                return String.valueOf(con.getResponseCode());
            }

            StringBuilder sb = new StringBuilder();
            int HttpResult = con.getResponseCode();
            if (HttpResult == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                System.out.println("HTTP " + metohd + " - " + con.getResponseCode() + " (" + url + ")\"" + (sb.toString().length() > 10 ? sb.substring(0,10) : "TOO SHORT") + "\"...");
            } else {
                System.out.println(con.getResponseMessage() + " - " + url);
                return "INVALID JSON OBJECT :)";
            }

            return sb.toString();
        } catch (Exception ex){
            System.out.println("CRITICAL ERROR");
            ex.printStackTrace();
            System.exit(0);
            return null;
        }
    }
}
