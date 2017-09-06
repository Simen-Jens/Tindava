package tools;

import objects.MatchObject;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import sx.blah.discord.handle.obj.IChannel;

import java.text.Normalizer;

/**
 * Created by Simen on 15.08.2017.
 */
public class StringManipulation {
    public static String sanitize(String sIn) {
        String[] tbNom = sIn.replace(" ", "-").split("");
        //String[] illgChar = "ç,æ,œ,á,é,í,ó,ú,à,è,ì,ò,ù,ä,ë,ï,ö,ü,ÿ,â,ê,î,ô,û,å,ø,Ø,Å,Á,À,Â,Ä,È,É,Ê,Ë,Í,Î,Ï,Ì,Ò,Ó,Ô,Ö,Ú,Ù,Û,Ü,Ÿ,Ç,Æ,Œ".split(",");
        String[] illgChar = "\u00E7,\u00E6,\u0153,\u00E1,\u00E9,\u00ED,\u00F3,\u00FA,\u00E0,\u00E8,\u00EC,\u00F2,\u00F9,\u00E4,\u00EB,\u00EF,\u00F6,\u00FC,\u00FF,\u00E2,\u00EA,\u00EE,\u00F4,\u00FB,\u00E5,\u00F8,\u00D8,\u00C5,\u00C1,\u00C0,\u00C2,\u00C4,\u00C8,\u00C9,\u00CA,\u00CB,\u00CD,\u00CE,\u00CF,\u00CC,\u00D2,\u00D3,\u00D4,\u00D6,\u00DA,\u00D9,\u00DB,\u00DC,\u0178,\u00C7,\u00C6,\u0152".split(",");
        String[] lgChar = "c,ae,oe,a,e,i,o,u,a,e,i,o,u,a,e,i,o,u,y,a,e,i,o,u,a,o,O,A,A,A,A,A,E,E,E,E,I,I,I,I,O,O,O,O,U,U,U,U,Y,C,AE,OE".split(",");

        for(int i = 0; i < tbNom.length; i++){
            for(int k = 0; k < illgChar.length; k++){
                if(illgChar[k].contains(tbNom[i])){
                    tbNom[i] = lgChar[k];
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        for(String s : tbNom) {
            builder.append(s);
        }
        return Normalizer.normalize(builder.toString(), Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    public static int calculateAge(String tinderstamp){
        String[] theirage = tinderstamp.substring(0,10).split("-");
        LocalDate birthdate = new LocalDate (Integer.parseInt(theirage[0]), Integer.parseInt(theirage[1]), Integer.parseInt(theirage[2]));
        LocalDate now = new LocalDate();
        Years age = Years.yearsBetween(birthdate, now);
        return age.getYears();
    }

    /*
    *  Allows us to locate the appropriate match to sound our message to (based on where the message was received)
    *
    *  @param IChannel channel - What channel it should search for a match
    *  @return MatchObject - The corresponding match, returns null if no match was found
    * */
    public static MatchObject findMatchFromChannel(IChannel channel) {
        if(channel.getPinnedMessages().size() > 0){
            String inform = channel.getPinnedMessages().get(0).getContent();
            String matchid = inform.substring(inform.indexOf("matchid") + 10, inform.indexOf("matchid") + 34);

            return MatchHandler.getMatches.get(matchid);
        } else {
            //This chat does not have a match attached to it
            return null;
        }
    }
}
