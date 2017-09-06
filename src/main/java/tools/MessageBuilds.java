package tools;

import objects.NestedMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import settings.Settings;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import java.awt.*;

/**
 * Created by Simen (Scoop#8831) on 13.08.2017.
 *
 * This class contains a collection of embedded message designs, not going to explain it since it is not really core in any way.
 */
public class MessageBuilds {
    public static EmbedObject buildMatchMessage(String name, String bio, int age, boolean superlike, String image, String spotify){
        try {
            EmbedBuilder tmp = new EmbedBuilder();
            tmp.withAuthorName(name).
                    appendField(String.valueOf(age), bio.equals("") ? "{EMPTY BIO}" : bio, false).
                    withImage(image).
                    withThumbnail(superlike ? Settings.getSettings.getString("super_match_thumb") : Settings.getSettings.getString("default_match_thumb")).
                    withColor(superlike ? new Color(0,0,0).decode(Settings.getSettings.getString("super_match_color")) : new Color(0,0,0).decode(Settings.getSettings.getString("default_match_color")));

            if(spotify != null)tmp.withFooterText(spotify).withFooterIcon("http://i.imgur.com/uI8QXiQ.png");
            return tmp.build();
        } catch (JSONException ex){
            System.out.println("Can't access settings properly");
            ex.printStackTrace();
        }
        return null;
    }

    public static EmbedObject buildUnmatchedMessage(){
        return new EmbedBuilder()
                .withColor(new Color(0,0,0).decode(Settings.getSettings.getString("unmatch_match_color_imc")))
                .withTitle(Settings.getSettings.getString("unmatch_match_title_imc"))
                .withDescription(Settings.getSettings.getString("unmatch_match_desc_imc"))
                .withImage(Settings.getSettings.getString("unmatch_match_image_imc")).build();
    }

    public static EmbedObject buildErrorMessage(String stackTrace){
        return new EmbedBuilder().
                withTitle("A critical error has occurred").
                withAuthorName("System").
                withAuthorIcon("http://emojipedia-us.s3.amazonaws.com/cache/34/70/347023bb4d048ab3709c7652df62f322.png").
                withDescription("Error count too high").
                appendField("StackTrace", stackTrace, false).
                withColor(244, 67, 54).build();
    }

    public static EmbedObject buildFacebookStatus(){
        return new EmbedBuilder().
                withAuthorName("Facebook").
                withAuthorIcon("https://images.seeklogo.net/2016/09/facebook-icon-preview.png").
                withTitle("Facebook credentials found").
                withDescription("logging in").
                withColor(41,83,150).build();
    }

    public static EmbedObject buildFacebookSuccess(){
        return new EmbedBuilder().
                withAuthorName("Facebook").
                withAuthorIcon("http://i.imgur.com/mSl8apU.png").
                withTitle("Facebook credentials found").
                withDescription("login success").
                withColor(76, 175, 80).build();
    }

    public static EmbedObject buildFacebookError(){
        return new EmbedBuilder().
                withAuthorName("Facebook").
                withAuthorIcon("http://i.imgur.com/K5s3uzO.png").
                withTitle("Facebook credentials found").
                withDescription("login failed").
                withColor(244, 67, 54).build();
    }

    public static String buildInternalMatchMessage(String[] images, String id, String name, String bio, int age, boolean superlike){
        String allImages = "";
        for(int i = 0; i < images.length; i++){
            allImages += images[i] + "\n";
        }

        return allImages + "\n```\n{\"matchid\":\"" + id + "\"}\n" +
                "\nNAME: " + name +
                "\nAGE: " + age +
                "\nBIO: " + bio +
                "\nSUPER: " + superlike + "```";
    }

    public static NestedMessage buildPotentialMatchMessage(String name, String bio, int age, String[] images, String spotify){
        String imgs = "";
        for(int i = 1; i < images.length; i++){
            imgs += images[i] + "\n";
        }

        EmbedBuilder tmp = new EmbedBuilder().
                withAuthorName(name).
                    appendField(String.valueOf(age), bio, false).
                    withImage(images[0]).
                    withColor(new Color(0,0,0).decode(Settings.getSettings.getString("system_color")));

        if(spotify != null)tmp.withFooterText(spotify).withFooterIcon("http://i.imgur.com/uI8QXiQ.png");

        return new NestedMessage(tmp.build(), imgs);
    }
}
