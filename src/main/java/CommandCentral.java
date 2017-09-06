import objects.MatchObject;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import tools.MatchHandler;
import settings.Settings;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import tools.StringManipulation;
import tools.SwipeController;
import tunnels.DiscordTunnel;
import tunnels.TinderTunnel;

import java.util.Map;

import static java.lang.Thread.sleep;


/**
 * Created by Simen (#Scoop#8831) on 13.08.2017.
 */
public class CommandCentral {
    public static UpdateThread upt = new UpdateThread();

    /*
    *  This is the method that keep all of the :fire: commands
    *
    *  @param MessageReceivedEvent message - The event received from the Discord4J library
    * */
    public static void messageInterp(MessageReceivedEvent message) throws Exception{
        IUser author = message.getMessage().getAuthor();
        IChannel channel = message.getMessage().getChannel();
        String content = message.getMessage().getContent();
        String[] dividedContent = content.split(" ");
        if(dividedContent[0].equals(Settings.getSettings.getString("prefix")) || (channel.getStringID().equals("347176626640912406") && !author.getStringID().equals("109348691525058560"))){
            System.out.println("COMMAND - " + content.substring(2));
            //This is a command

            //Collection of all commands


            /*
            * Whispers the roles and their corresponding id's for a user on a server.
            * */
            if(dividedContent.length == 3 && dividedContent[1].equals("my") && dividedContent[2].equals("roles")){
                String sb = "";
                for(IRole r : author.getRolesForGuild(AnnontationListener.client.getGuilds().get(0))){
                    sb += r.getID() + " - " + r.getName() + "\n";
                }
                author.getOrCreatePMChannel().sendMessage(sb);
            }

            /*
            * Updates the settings with a new facebook ID
            * */
            if(dividedContent.length == 4 && dividedContent[1].equals("supply") && dividedContent[2].equals("facebook_id")){
                Settings.setStringValue("facebook_id", dividedContent[3]);
            }

            /*
            * Updates the settings with a new facebook auth token
            * */
            if(dividedContent.length == 4 && dividedContent[1].equals("supply") && dividedContent[2].equals("auth_token")){
                Settings.setStringValue("facebook_auth", dividedContent[3]);
            }

            /*
            * Updates the settings with a new facebook email
            * */
            if(dividedContent.length == 4 && dividedContent[1].equals("supply") && dividedContent[2].equals("email")){
                Settings.setStringValue("facebook_email", dividedContent[3]);
            }

            /*
            * Updates the settings with a new facebook password
            * */
            if(dividedContent.length == 4 && dividedContent[1].equals("supply") && dividedContent[2].equals("password")){
                Settings.setStringValue("facebook_password", dividedContent[3]);
            }

            /*
            * Updates the settings with a new tinder xauth
            * */
            if(dividedContent.length == 4 && dividedContent[1].equals("supply") && dividedContent[2].equals("xauth")){
                Settings.setStringValue("xauth", dividedContent[3]);
            }

            /*
            * Deletes @param number ([2]) amout of messages from the chat
            * */
            if(dividedContent.length == 3 && dividedContent[1].equals("purge")){
                for(int i = 0; i < Integer.parseInt(dividedContent[2]); i++){
                    try{
                        sleep(200);
                        channel.getMessages().get(0).delete();
                    } catch (Exception ex){
                        break;
                    }
                }
                System.out.println("Purging complete");
            }

            /*
            * Removes ALL chats that are not a part of "do_not_delete_channels" in settings
            * */
            if(dividedContent.length == 3 && dividedContent[1].equals("remove") && dividedContent[2].equals("chats")){
                channel.getGuild().getChannels();
                for(IChannel c : channel.getGuild().getChannels()){
                    boolean delete = true;
                    for(int i = 0; i < Settings.getSettings.getJSONArray("do_not_delete_channels").length(); i++){
                        if(c.getID().equals(Settings.getSettings.getJSONArray("do_not_delete_channels").getString(i))){
                            delete = false;
                            break;
                        }
                    }
                    if(delete){
                        c.delete();
                    }
                }
            }

            /*
            * Swipes once on a random recommendation from Tinder, combine with lef or right such as:
            * ":fire: swipe right once" / ":fire: swipe left once"
            * */
            if(dividedContent.length == 4 && dividedContent[1].equals("swipe") && dividedContent[3].equals("once")){
                if(dividedContent[2].equals("right")){
                    TinderTunnel.swipeRight(TinderTunnel.getRecommendation(Settings.getSettings.getString("xauth")).getString("_id"), Settings.getSettings.getString("xauth"));
                } else if(dividedContent[2].equals("left")){
                    TinderTunnel.swipeLeft(TinderTunnel.getRecommendation(Settings.getSettings.getString("xauth")).getString("_id"), Settings.getSettings.getString("xauth"));
                }
            }

            if(dividedContent.length == 4 && dividedContent[1].equals("swipe") && dividedContent[2].equals("up")){
                TinderTunnel.swipeUp(dividedContent[3], Settings.getSettings.getString("xauth"));
            }

            /*
            * Swipes on all current recommendations (usually max 15 at a time)
            * */
            if(dividedContent.length == 3 && dividedContent[1].equals("swipe") && dividedContent[2].equals("all")){
                int remaining = TinderTunnel.swipeAll(Settings.getSettings.getString("xauth"));
                if(remaining < 1){
                    message.getMessage().addReaction("\u274c");
                }
            }

            /*
            * Organizes the match-channels in discord alphabetically
            * */
            if(dividedContent.length == 2 && dividedContent[1].equals("organize")){
                DiscordTunnel.organizeChannels(channel.getGuild());
            }

            /*
            * Unmatches all the users matches
            * */
            if(dividedContent.length == 3 && dividedContent[1].equals("unmatch") && dividedContent[2].equals("all")){
                for(Map.Entry<String, MatchObject> entry : MatchHandler.getMatches.entrySet()){
                    entry.getValue().unmatch(Settings.getSettings.getString("xauth"));
                }
            }

            /*
            * Unmatches the current match
            * */
            if(dividedContent.length == 2 && dividedContent[1].equals("unmatch")){
                MatchObject potentialMatch = StringManipulation.findMatchFromChannel(channel);
                if(potentialMatch != null){
                    potentialMatch.unmatch(Settings.getSettings.getString("xauth"));
                }
            }

            /*
            * Changes the location of the Tinder bot, this can only be done once or twice per day (limitation on Tinders server)
            * */
            if(dividedContent.length > 3 && dividedContent[1].equals("set") && dividedContent[2].equals("address")){
                String sb = "";
                for(int i = 3; i < dividedContent.length; i++){
                    sb += dividedContent[i];
                }
                TinderTunnel.setLocation(sb, Settings.getSettings.getString("xauth"));
            }

            /*
            * Reloads the settings from settings.json
            * */
            if(dividedContent.length == 3 && dividedContent[1].equals("reload") && dividedContent[2].equals("settings")){
                Settings.updateSettings();
            }

            /*
            * Saves the settings from settings.json
            * */
            if(dividedContent.length == 3 && dividedContent[1].equals("save") && dividedContent[2].equals("settings")){
                Settings.saveSettings();
            }

            /*
            * Starts the update thread and initiates the bot
            * */
            if(dividedContent.length == 2 && dividedContent[1].equals("start")){
                upt.start();
            }

            /*
            *
            * */
            if(dividedContent.length == 3 && dividedContent[1].equals("swipe") && dividedContent[2].equals("here")){
                if(channel.getStringID().equals(Settings.getSettings.getString("swipe_channel"))){
                    SwipeController.createNewSwipeable();
                } else {
                    //wrong channel
                    message.getMessage().addReaction("\u274c");
                }
            }




        } else {
            //This is not a command, check if it is a message to a match
            MatchObject potentialReceiver = StringManipulation.findMatchFromChannel(channel);
            if(!author.isBot() && potentialReceiver != null){
                //This message is not from a bot and is meant for a match, we will now check if the sender has permissions
                if(hasMessengerRole(author)){
                    //User has permission to communicate with matches, forward the message to the match
                    potentialReceiver.sendMessage(content, Settings.getSettings.getString("xauth"));
                } else {
                    //This user does not have permission to send message, react with cross
                    message.getMessage().addReaction("\u274c");
                }
            }
        }
    }

    /*
    *  Checks if the user has permission to send messages to tinder matches
    *
    *  @param IUser user - The user to check
    *  @return boolean - True or false depending on permissions
    * */
    private static boolean hasMessengerRole(IUser user) throws Exception{
        for(IRole r : user.getRolesForGuild(AnnontationListener.client.getGuilds().get(0))){
            if(r.getStringID().equals(Settings.getSettings.getString("messenger_role"))){
                return true;
            }
        }
        return false;
    }
}
