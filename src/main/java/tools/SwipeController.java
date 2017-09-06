package tools;

import objects.MatchObject;
import objects.NestedMessage;
import objects.SwipeableMatch;
import org.json.JSONObject;
import settings.Settings;
import sx.blah.discord.handle.impl.events.guild.channel.message.reaction.ReactionAddEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.RateLimitException;
import tunnels.TinderTunnel;

import static java.lang.Thread.sleep;

/**
 * Created by Simen on 15.08.2017.
 */
public class SwipeController {
    public static SwipeableMatch currentSwipeable = null;

    public static void reactionEvent(ReactionAddEvent event){
        System.out.println("DEBUG - got reaction");
        if(event.getReaction().getUsers().size() > 1){
            System.out.println("DEBUG - someone else reacted");
            if (currentSwipeable != null) {
                System.out.println("DEBUG - currentSwipeable is not null");
                if (event.getReaction().getMessage().getStringID().equals(currentSwipeable.swipeMessage.getStringID())) {
                    System.out.println("DEBUG - reaction happend on same message");
                    if (event.getReaction().toString().equals("\u274c")) {
                        //This was reacted with a cross
                        swipeLeft();
                    } else if (event.getReaction().toString().equals("\u2764")) {
                        //This was reacted with a heart
                        swipeRight();
                    } else if (event.getReaction().toString().equals("\u2B50")){
                        //This was reacted with a star
                        swipeUp();
                    }
                    createNewSwipeable();
                }
            }
        }
    }

    public static void createNewSwipeable(){
        IChannel swipechannel = JSONDecoder.client.getChannelByID(Settings.getSettings.getString("swipe_channel"));
        if(TinderTunnel.getRemainingLikes(Settings.getSettings.getString("xauth")) >= 1){
            JSONObject potential = TinderTunnel.getRecommendation(Settings.getSettings.getString("xauth"));

            MatchObject tmv = new MatchObject(potential, null, null);   //temporary match variable
            NestedMessage nm = MessageBuilds.buildPotentialMatchMessage(tmv.getName(), tmv.getBio(), tmv.getAge(), tmv.getImages(), null);

            currentSwipeable = new SwipeableMatch(tmv.getID(), swipechannel.sendMessage(nm.message, nm.eo, false));
            try {
                sleep(1000);
            } catch (InterruptedException ex){}
            currentSwipeable.swipeMessage.addReaction("\u274c");
            try {
                sleep(1000);
            } catch (InterruptedException ex){}
            currentSwipeable.swipeMessage.addReaction("\u2764");
            try {
                sleep(1000);
            } catch (InterruptedException ex){}
            currentSwipeable.swipeMessage.addReaction("\u2B50");
        } else {
            swipechannel.sendMessage("out of likes (I will make this message look better ... SoonTM)");
        }

    }

    public static boolean swipeLeft(){
        return currentSwipeable.leftSwipe();
    }

    public static boolean swipeRight(){
        if(currentSwipeable.rightSwipe()){
            //TO DO - makes a better visual feedback for the user
            System.out.println("GOTMATCH");
            return true;
        }
        return false;
    }

    public static boolean swipeUp(){
        return currentSwipeable.upSwipe();
    }
}
