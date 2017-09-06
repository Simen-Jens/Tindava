package objects;

import org.json.JSONObject;

public class MessageObject implements Comparable<MessageObject>{
    public JSONObject messageInfo;

    public MessageObject(JSONObject messageInfo){
        this.messageInfo = messageInfo;
    }

    @Override
    public int compareTo(MessageObject o) {
        try {
            return messageInfo.getString("_id").compareTo(o.messageInfo.getString("_id"));
        } catch (Exception ex){
            ex.printStackTrace();
            System.exit(0);
            return 0;
        }
    }
}
