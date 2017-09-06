import org.json.JSONObject;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.obj.Embed;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

/**
 * Created by Simen(Scoop#8831) on 12.02.2017.
 */
public class Cleverbot {
    public class Clever_Instance{
        private Tinder_Object.Match attachedMatch;
        public String cs;   //This is the conversationSate, used to identify the conversation
        public Clever_Instance(Tinder_Object.Match attachedMatch){
            this.attachedMatch = attachedMatch;
            attachedMatch.cleverAI = this;
        }

        public IMessage sendToCloud(String message) throws Exception{
            sleep(600); //gives the wekhook some time
            attachedMatch.myChannel.sendMessage("", buildMessage(getAwnser(message, this)), false);
            return attachedMatch.myChannel.getMessages().get(1);    //message 1 instead of 0 because we want the targets message
        }
    }

    //-----------------Cleverbot class-----------------
    private ArrayList<Clever_Instance> instances = new ArrayList<Clever_Instance>();
    private CommandCentral cmd;

    public Cleverbot(CommandCentral cmd){
        this.cmd = cmd;
    }

    public Clever_Instance attachClever(Tinder_Object.Match forThisMatch){
        if(forThisMatch.link == null) {
            instances.add(new Clever_Instance(forThisMatch));
            return instances.get(instances.size() - 1);
        } else{
            System.out.println("This match is linked to another match");
            return null;
        }
    }

    public String getAwnser(String message, Clever_Instance currentInstace) throws Exception{
        String codedUrl = "https://www.cleverbot.com/getreply?key=" +
                cmd.settings.cleverbotKey + "&input=" +
                message + "&callback=ProcessReply&cs=" +
                currentInstace.cs;

        JSONObject reply = new JSONObject(cmd.pat.handleData(codedUrl, "GET", new JSONObject()));
        //check transelation capabilities
        /*if(cmd.settings.language != "en" && cmd.settings.googleKey != null){
            JSONObject translateObject = new JSONObject();
            translateObject.put("q", reply.getString("output"));
            translateObject.put("source", "en");
            translateObject.put("target", cmd.settings.language);
            translateObject.put("format", "text");
            translateObject.put("key", cmd.settings.googleKey);
        }*/


        currentInstace.cs = reply.getString("cs");
        return reply.getString("output");
    }

    public EmbedObject buildMessage(String message){
        return new EmbedBuilder().withAuthorName("Cleverbot").
                withAuthorIcon("http://a5.mzstatic.com/nz/r30/Purple3/v4/71/6a/74/716a747d-152f-ab09-2e72-5622fd369655/icon175x175.png").
                withDescription(message).
                withFooterIcon("https://upload.wikimedia.org/wikipedia/commons/d/db/Google_Translate_Icon.png").
                withFooterText("Translated to " + cmd.settings.language + ", via Google Translate").
                withColor(cmd.settings.systemColor).build();
    }
}
