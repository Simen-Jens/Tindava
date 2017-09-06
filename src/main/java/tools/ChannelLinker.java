package tools;

import objects.MatchLink;
import objects.MatchObject;
import sx.blah.discord.handle.obj.IChannel;

import java.util.LinkedList;

/**
 * Created by Simen on 15.08.2017.
 */
public class ChannelLinker {
    public static LinkedList<MatchLink> getLinks = new LinkedList<>();

    public static void setLinkStage(IChannel here){

    }

    public static MatchLink createLink(MatchObject recipientA, MatchObject recipientB){
        return null;
    }

    public static MatchLink partOfLink(IChannel thisChannel){
        for(MatchLink ml : getLinks){
            for(IChannel ic : ml.getChannels()){
                if(ic.getStringID().equals(thisChannel.getStringID())){
                    return ml;
                }
            }
        }
        return null;
    }
}
