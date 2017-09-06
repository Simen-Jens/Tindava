package objects;

import settings.Settings;
import sx.blah.discord.handle.obj.IChannel;

/**
 * Created by Simen (Scoop#8831) on 15.08.2017.
 */

/*
*  This was written while drunk, needs review
* */
public class ComparableChannel implements Comparable<ComparableChannel> {
    public IChannel channelOcj;

    public ComparableChannel(IChannel channelOcj){
        this.channelOcj = channelOcj;
    }

    public boolean safeChannel(){
        boolean safe = false;
        for(int i = 0; i < Settings.getSettings.getJSONArray("do_not_delete_channels").length(); i++){
            if(Settings.getSettings.getJSONArray("do_not_delete_channels").getString(i).equals(channelOcj.getStringID())){
                safe = true;
                break;
            }
        }
        return safe;
    }

    public int compareTo(ComparableChannel cp){
        return channelOcj.getName().compareTo(cp.channelOcj.getName());
    }
}
