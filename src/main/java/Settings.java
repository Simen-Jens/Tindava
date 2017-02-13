import org.json.JSONObject;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Simen (Scoop#8831) on 11.02.2017.
 */
public class Settings {
    public String botToken;
    public String googleKey;
    public String cleverbotKey;
    public String language;

    public String facebook_email;
    public String facebook_password;
    public boolean autoLogin;

    public String facebook_id;
    public String facebook_token;
    public String xauth;
    public boolean stats;

    public String cache_path;
    public String empty_path;
    public int pullrate_min;
    public int pullrate_max;

    public String defaultChannels = "";
    public String messageRole;
    public Color systemColor;

    public String excludeName;
    public int excludeBefore;
    public int excludeAfter;

    public Color defaultMatchColor;
    public String defaultMatchThumb;

    public Color superMatchColor;
    public String superMatchThumb;

    public Color unMatchColor;
    public String unMatchThumb;

    // IMC - In match chat
    public Color unMatchColorIMC;
    public String unMatchTitleIMC;
    public String unMatchDescIMC;
    public String unMatchImageIMC;


    public Settings(){
        try{
            String settings = new String(Files.readAllBytes(Paths.get("settings.json")), StandardCharsets.UTF_8);

            JSONObject setting = new JSONObject(settings);
            botToken = setting.getString("bot_token");
            googleKey = setting.getString("google_api_key");
            cleverbotKey = setting.getString("cleverbot_key");
            language = setting.getString("language");

            facebook_email = setting.getString("facebook_email");
            facebook_password = setting.getString("facebook_password");
            autoLogin = setting.getBoolean("autologin");


            facebook_id = setting.getString("facebook_id");
            facebook_token = setting.getString("facebook_auth");
            xauth = setting.getString("xauth");
            stats = setting.getBoolean("send_stat");

            cache_path = setting.getString("cache_path");
            empty_path = setting.getString("empty_path");
            pullrate_min = setting.getInt("pullrate_min");
            pullrate_max = setting.getInt("pullrate_max");

            for(int i = 0; i < setting.getJSONArray("default_channels").length(); i++){
                defaultChannels += setting.getJSONArray("default_channels").getString(i) + " ";
            }
            defaultChannels = defaultChannels.substring(0, defaultChannels.length()-1);
            messageRole = setting.getString("messenger_role");
            systemColor = new Color(0,0,0).decode(setting.getString("system_color"));

            for(int i = 0; i < setting.getJSONArray("exclude_matches_with_name").length(); i++){
                excludeName += setting.getJSONArray("exclude_matches_with_name").getString(i) + " ";
            }
            excludeBefore = setting.getInt("exclude_first_int_matches");
            excludeAfter = setting.getInt("exclude_last_int_matches");

            defaultMatchColor = new Color(0,0,0).decode(setting.getString("default_match_color"));
            defaultMatchThumb = setting.getString("default_match_thumb");
            superMatchColor = new Color(0,0,0).decode(setting.getString("super_match_color"));
            superMatchThumb = setting.getString("super_match_thumb");
            unMatchColor = new Color(0,0,0).decode(setting.getString("unmatch_match_color"));
            unMatchThumb = setting.getString("unmatch_match_thumb");
            unMatchColorIMC = new Color(0,0,0).decode(setting.getString("unmatch_match_color_imc"));
            unMatchTitleIMC = setting.getString("unmatch_match_title_imc");
            unMatchDescIMC = setting.getString("unmatch_match_desc_imc");
            unMatchImageIMC = setting.getString("unmatch_match_image_imc");
        } catch (Exception ex){
            ex.printStackTrace();
            System.out.println("Error reading settingsfile! - exit(1)");
            System.exit(1);
        }
    }

}
