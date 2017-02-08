import org.json.JSONObject;

/**
 * Created by Scoop on 07.02.2017.
 */
public class Update_Thread extends Thread {
    long sleeptime;
    long sleeptimeLong;
    CommandCentral cmd;
    public boolean runn = true;
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
            } catch (InterruptedException ex){
                System.out.println("CRITICAL ERROR! SLEEP INTRP!\n\n" + ex.toString());
            }
            System.out.println("New call to updateTinder started, current pull rate = " + (pulls > 10 ? sleeptimeLong : sleeptime));
            try {
                cmd.interp.updateTinder(cmd.pat.handleData("https://api.gotinder.com/updates", "POST", new JSONObject()));
            } catch (Exception ex){
                runn = false;
                System.out.println("CRITICAL ERROR! UPDATE ERROR\n\n" + ex.toString());
                try{
                    cmd.cmd_messageDiscord((":skull_crossbones: well shit, <@109348691525058560> sucks at programming :skull_crossbones:\nupdate thread in danger, error message sent to server owner(" + cmd.client.getGuilds().get(0).getOwner().mention() + ")"),cmd.client.getGuilds().get(0).getChannels().get(0), false, false);
                    cmd.client.getGuilds().get(0).getOwner().getOrCreatePMChannel().sendMessage("yeah error and such and such\n" + ex.toString());
                } catch (Exception exF){
                    System.out.print("something very is very wrong... you're screwd.\n\n" + exF.toString());
                }
            }
            pulls++;

        }
    }
}
