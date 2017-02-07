import org.json.JSONObject;

import java.net.CookieHandler;

/**
 * Created by Simen on 07.02.2017.
 */
public class Update_Thread extends Thread {
    long sleeptime;
    CommandCentral cmd;
    public boolean runn = true;

    public Update_Thread(long sleepTime, CommandCentral cmd){
        this.sleeptime = sleepTime;
        this.cmd = cmd;
    }

    public void run(){
        while (runn){
            try {
                Thread.sleep(sleeptime);
            } catch (InterruptedException ex){
                System.out.println("CRITICAL ERROR! SLEEP INTRP!\n\n" + ex.toString());
            }
            System.out.println("starting update...");
            try {
                cmd.interp.updateTinder(cmd.pat.handleData("https://api.gotinder.com/updates", "POST", new JSONObject()));
            } catch (Exception ex){
                System.out.println("CRITICAL ERROR! UPDATE ERROR\n\n" + ex.toString());
            }
        }
    }
}
