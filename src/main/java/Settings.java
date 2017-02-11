import org.json.JSONObject;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Simen (Scoop#8831) on 11.02.2017.
 */
public class Settings {
    public String botToken = "";
    public String facebook_id;
    public String facebook_token;
    public String xauth;
    public boolean stats;

    public String cache_path;
    public String empty_path;

    public String defaultChannels = "";
    public String messageRole;

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
            String settings = new String(Files.readAllBytes(Paths.get("C:/Users/Simen/Desktop/Settings.json")), StandardCharsets.UTF_8);

            JSONObject setting = new JSONObject(settings);
            botToken = setting.getString("bot_token");
            facebook_id = setting.getString("facebook_id");
            facebook_token = setting.getString("facebook_auth");
            xauth = setting.getString("xauth");
            stats = setting.getBoolean("send_stat");

            cache_path = setting.getString("cache_path");
            empty_path = setting.getString("empty_path");
            for(int i = 0; i < setting.getJSONArray("default_channels").length(); i++){
                defaultChannels += setting.getJSONArray("default_channels").getString(i) + " ";
            }
            defaultChannels = defaultChannels.substring(0, defaultChannels.length()-1);
            messageRole = setting.getString("messenger_role");
            for(int i = 0; i < setting.getJSONArray("exclude_matches_with_name").length(); i++){
                excludeName += setting.getJSONArray("exclude_matches_with_name").getString(i) + " ";
            }
            excludeBefore = setting.getInt("exclude_first_int_matches");
            excludeAfter = setting.getInt("excludes_last_int_matches");

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
