import org.json.JSONObject;
import sx.blah.discord.util.EmbedBuilder;

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

    public Update_Thread(long sleepTime, long sleeptimeLong, CommandCentral cmd){
        this.sleeptime = sleepTime;
        this.sleeptimeLong = sleeptimeLong;
        this.cmd = cmd;
    }

    public void run(){
        runn = true;
        while (runn){
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
                    toggle = false;
                    System.out.println("CRITICAL ERROR! UPDATE ERROR\n\n" + ex.toString());
                    try {
                        cmd.client.getChannelByID(cmd.settings.defaultChannels.split(" ")[0]).sendMessage("", new EmbedBuilder().
                                withTitle("An error has occured").
                                withDescription("Error fetching update, trying to relog").
                                withColor(255, 204, 77).build(), false);

                        cmd.cmd_reauth(true);
                    } catch (Exception ex3){
                        System.out.println("CRITICAL! NEED RESTART");
                        ex3.printStackTrace();
                        System.exit(1);
                    }
                }
                if(pulls%10 == 0){
                    try {
                        cmd.organizeChannels(cmd.client.getGuilds().get(0).getChannels());
                    } catch (Exception ex){}
                }
                pulls++;
            }
        }
    }
}
