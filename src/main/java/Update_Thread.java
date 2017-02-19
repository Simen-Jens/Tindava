import org.json.JSONObject;
import sx.blah.discord.util.EmbedBuilder;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by Simen (Scoop#8831) on 07.02.2017.
 */
public class Update_Thread extends Thread {
    long sleeptime;
    long sleeptimeLong;
    CommandCentral cmd;
    public boolean runn = true;
    public boolean toggle = true;
    public int pulls = 0;
    public int errorCount = 0;
    StringWriter stackTrace = new StringWriter();

    public Update_Thread(long sleepTime, long sleeptimeLong, CommandCentral cmd){
        this.sleeptime = sleepTime;
        this.sleeptimeLong = sleeptimeLong;
        this.cmd = cmd;
    }

    public void run(){
        runn = true;
        while (runn){
            if(errorCount > 1){
                try {
                    cmd.client.getChannelByID(cmd.settings.defaultChannels.split(" ")[0]).sendMessage("", new EmbedBuilder().
                            withTitle("A critical error has occurred").
                            withAuthorName("System").
                            withAuthorIcon("http://emojipedia-us.s3.amazonaws.com/cache/34/70/347023bb4d048ab3709c7652df62f322.png").
                            withDescription("Error count too high").
                            appendField("StackTrace", stackTrace.toString(), false).
                            withColor(244, 67, 54).build(), false);
                    //cmd.client.getGuilds().get(0).getOwner().getOrCreatePMChannel().sendMessage(stackTrace.toString());
                    System.exit(1);
                } catch (Exception ex3){
                    System.out.println("CRITICAL! NEED RESTART");
                    ex3.printStackTrace();
                    System.exit(1);
                }
            }
            try {
                Thread.sleep(pulls > 10 ? sleeptimeLong : sleeptime);
            } catch (InterruptedException ex) {
                System.out.println("CRITICAL ERROR! SLEEP INTRP!\n\n" + ex.toString());
            }
            if(toggle) {
                System.out.println("New call to updateTinder started, current pull rate = " + (pulls > 10 ? sleeptimeLong : sleeptime));
                try {
                    cmd.interp.updateTinder(cmd.pat.handleData("https://api.gotinder.com/updates", "POST", new JSONObject()));
                } catch (Exception ex) {
                    errorCount++;
                    toggle = false;
                    ex.printStackTrace();
                    PrintWriter pw = new PrintWriter(stackTrace);
                    ex.printStackTrace(pw);
                    System.out.println("CRITICAL ERROR! UPDATE ERROR\n\n" + ex.toString());
                    try {
                        cmd.client.getChannelByID(cmd.settings.defaultChannels.split(" ")[0]).sendMessage("", new EmbedBuilder().
                                withTitle("An error has occurred").
                                withAuthorName("System").
                                withAuthorIcon("http://emojipedia-us.s3.amazonaws.com/cache/63/0e/630ef8d5206007bc000ff28aa052c373.png").
                                withDescription("Error fetching update, trying to relog").
                                withColor(255, 204, 77).build(), false);

                        cmd.cmd_reauth(true);
                    } catch (Exception ex3){
                        System.out.println("CRITICAL! NEED RESTART");
                        ex3.printStackTrace();
                        System.exit(1);
                    }
                }
                if(pulls%30 == 0){
                    try {
                        cmd.organize();
                    } catch (Exception ex){}
                }
                pulls++;
            }
        }
    }
}
