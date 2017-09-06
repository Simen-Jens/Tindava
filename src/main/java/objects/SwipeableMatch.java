package objects;

import settings.Settings;
import sx.blah.discord.handle.obj.IMessage;
import tunnels.TinderTunnel;

/**
 * Created by Simen on 15.08.2017.
 */
public class SwipeableMatch {
    public IMessage swipeMessage;
    private String matchID;

    public SwipeableMatch(String matchID, IMessage swipeMessage){
        this.matchID = matchID;
        this.swipeMessage = swipeMessage;
    }

    public boolean leftSwipe(){
        return TinderTunnel.swipeLeft(matchID, Settings.getSettings.getString("xauth"));
    }

    public boolean rightSwipe(){
        return TinderTunnel.swipeRight(matchID, Settings.getSettings.getString("xauth"));
    }

    public boolean upSwipe(){
        return TinderTunnel.swipeUp(matchID, Settings.getSettings.getString("xauth"));
    }
}
