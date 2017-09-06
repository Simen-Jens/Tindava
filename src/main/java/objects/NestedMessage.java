package objects;

import sx.blah.discord.api.internal.json.objects.EmbedObject;

/**
 * Created by Simen on 15.08.2017.
 */
public class NestedMessage {
    public EmbedObject eo;
    public String message;

    public NestedMessage(EmbedObject eo, String message){
        this.eo = eo;
        this.message = message;
    }
}
