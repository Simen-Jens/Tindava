package settings;

import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Simen (Scoop#8831) on 16.05.2017.
 */
public class Settings {
    public static JSONObject getSettings = new JSONObject();
    public static boolean firstRun = true;

    /*
    *  Method that loads the settings file (settings.json)
    *
    *  @return boolean - True or false depending on success of loading the settings
    * */
    public static boolean updateSettings(){
        try{
            getSettings = new JSONObject(new String(Files.readAllBytes(Paths.get("settings.json")), StandardCharsets.UTF_8));
            return true;
        } catch (Exception ex){
            return false;
        }
    }

    /*
    *  Method that checks if a string is null through null-value and textual-value
    *
    *  @param String json - String to test
    *  @return boolean - result of test
    * */
    public static boolean isNull(String json){
        return json == null || json.equals("null");
    }


    /*
    *  Method for changing settings on the fly
    *
    *  @param String valueKey - The value key to change
    *  @param String value - The new value for selected value key
    * */
    public static void setStringValue(String valueKey, String value){
        try {
            getSettings.remove(valueKey);
            getSettings.put(valueKey, value);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    /*
    *  Method for saving the settings to settings.json
    *
    *  @return boolean - True or false depending on successful save
    * */
    public static boolean saveSettings(){
        try{
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("settings.json"), "UTF-8"));
            try {
                out.write(getSettings.toString());
            } finally {
                out.close();
            }
            return true;
        } catch (IOException ex){
            return false;
        }
    }
}
