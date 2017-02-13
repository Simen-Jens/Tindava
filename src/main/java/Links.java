import org.json.JSONObject;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.Image;

import java.util.ArrayList;

import static java.lang.Thread.sleep;

/**
 * Created by Simen on 12.02.2017.
 */
public class Links {
    public class Symbiotic_Link {
        private Tinder_Object.Match linkedA, linkedB;
        public IChannel linkChannel;
        public Symbiotic_Link(Tinder_Object.Match linkedA, Tinder_Object.Match linkedB, IChannel linkChannel){
            this.linkedA = linkedA;
            this.linkedB = linkedB;
            this.linkChannel = linkChannel;

            linkedA.link = this;
            linkedB.link = this;
        }

        public IMessage linkMessage(String message, Tinder_Object.Match from) throws Exception{
            if(from == linkedA){
                //send to b
                linkedB.sendMessage(message);
                //display from a
                linkMaskedmessage(message, linkChannel, linkedA.name);
            } else if(from == linkedB){
                //send to a
                linkedA.sendMessage(message);
                //display from b
                linkMaskedmessage(message, linkChannel, linkedB.name);
            } else{
                System.out.println("Should never reach this part.... (message in link not from a or b)");
            }
            sleep(500);  //gives the webhook a little bit of time
            return linkChannel.getMessages().get(0);
        }
    }

    //-----------------Link Class-----------------
    private CommandCentral cmd;
    private Tinder_Object.Match subjectA, subjectB;
    private ArrayList<Symbiotic_Link> links = new ArrayList<Symbiotic_Link>();

    //Constructor for Links
    public Links(CommandCentral cmd){
        this.cmd = cmd;
    }

    //Keeping this private this time, so need methods
    public Tinder_Object.Match setLink(Tinder_Object.Match matchToLink){
        try {
            if(matchToLink.cleverAI == null) {
                if (subjectA == null) {
                    subjectA = matchToLink;
                } else if (subjectB == null) {
                    subjectB = matchToLink;
                }
                EmbedBuilder tmp = new EmbedBuilder();
                tmp.withAuthorIcon("http://www.hey.fr/fun/emoji/twitter/en/icon/twitter/577-emoji_twitter_link_symbol.png");
                tmp.withAuthorName("New symbiotic link");
                tmp.withDescription("All messages will be sendt between these two matches:\n");
                tmp.appendField(subjectA.name, subjectA.myChannel.mention(), true);
                tmp.appendField(subjectB.name, subjectB.myChannel.mention(), true);
                tmp.withColor(cmd.settings.systemColor);
                if (subjectA != null & subjectB != null) {
                    links.add(new Symbiotic_Link(subjectA, subjectB, createLinkedChannel(subjectA, subjectB)));
                }
                tmp.withFooterText("New channel: " + "link_" + cmd.sanitize(subjectA.name) + "-" + cmd.sanitize(subjectB.name));
                tmp.withFooterIcon("http://emojipedia-us.s3.amazonaws.com/cache/02/d6/02d6756a2f66cf4aef9c6502b0bc7fce.png");

                subjectA.myChannel.sendMessage("", tmp.build(), false);
                subjectB.myChannel.sendMessage("", tmp.build(), false);
                return matchToLink;
            } else{
                System.out.println("This match is linked with Cleverbot");
                return null;
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
        return null;
    }

    //Creates a channel for the link
    public IChannel createLinkedChannel(Tinder_Object.Match a, Tinder_Object.Match b) throws Exception{
        IGuild forGuild = cmd.client.getGuilds().get(0);
        IChannel tmp = forGuild.createChannel("link_" + cmd.sanitize(a.name) + "-" + cmd.sanitize(b.name));

        tmp.createWebhook(cmd.sanitize(a.name), a.images[0]).changeDefaultAvatar(Image.forUrl("jpg",a.images[0]));
        tmp.createWebhook(cmd.sanitize(b.name), b.images[0]).changeDefaultAvatar(Image.forUrl("jpg",b.images[0]));

        return tmp;
    }

    public void linkMaskedmessage(String message, IChannel channel, String senderName) throws Exception{
        String url="https://discordapp.com/api/webhooks/" + channel.getWebhooksByName(senderName).get(0).getID() + "/" + channel.getWebhooksByName(senderName).get(0).getToken();
        JSONObject msg = new JSONObject();
        msg.put("content",message);
        cmd.pat.handleData(url, "POST", msg);
        sleep(100); //gives the webhook time to post
    }
}
