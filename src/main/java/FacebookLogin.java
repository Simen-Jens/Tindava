import com.gistlabs.mechanize.MechanizeAgent;
import com.gistlabs.mechanize.document.Document;
import com.gistlabs.mechanize.document.html.form.Form;

/**
 * Created by Simen (Scoop#8831) on 12.02.2017.
 */
public class FacebookLogin {
    private String facebookUserID;
    private String facebookOAuth2;
    private String facebookEmail;
    private String facebookPassword;
    private CommandCentral cmd;

    public FacebookLogin(String facebookEmail, String facebookPassword, String facebookUserID, CommandCentral cmd){
        this.facebookEmail = facebookEmail;
        this.facebookPassword = facebookPassword;
        this.facebookUserID = facebookUserID;
        this.cmd = cmd;
    }

    public void supplyOauth(String facebookOAuth2){
        this.facebookOAuth2 = facebookOAuth2;
    }

    public static String tinder_api_url = "https://www.facebook.com/login.php?skip_api_login=1&api_key=464891386855067&signed_next=1&next=https%3A%2F%2Fwww.facebook.com%2Fv2.6%2Fdialog%2Foauth%3Fredirect_uri%3Dfb464891386855067%253A%252F%252Fauthorize%252F%26display%3Dtouch%26state%3D%257B%2522challenge%2522%253A%2522IUUkEUqIGud332lfu%25252BMJhxL4Wlc%25253D%2522%252C%25220_auth_logger_id%2522%253A%252230F06532-A1B9-4B10-BB28-B29956C71AB1%2522%252C%2522com.facebook.sdk_client_state%2522%253Atrue%252C%25223_method%2522%253A%2522sfvc_auth%2522%257D%26scope%3Duser_birthday%252Cuser_photos%252Cuser_education_history%252Cemail%252Cuser_relationship_details%252Cuser_friends%252Cuser_work_history%252Cuser_likes%26response_type%3Dtoken%252Csigned_request%26default_audience%3Dfriends%26return_scopes%3Dtrue%26auth_type%3Drerequest%26client_id%3D464891386855067%26ret%3Dlogin%26sdk%3Dios%26logger_id%3D30F06532-A1B9-4B10-BB28-B29956C71AB1&cancel_url=fb464891386855067%3A%2F%2Fauthorize%2F%3Ferror%3Daccess_denied%26error_code%3D200%26error_description%3DPermissions%2Berror%26error_reason%3Duser_denied%26state%3D%257B%2522challenge%2522%253A%2522IUUkEUqIGud332lfu%25252BMJhxL4Wlc%25253D%2522%252C%25220_auth_logger_id%2522%253A%252230F06532-A1B9-4B10-BB28-B29956C71AB1%2522%252C%2522com.facebook.sdk_client_state%2522%253Atrue%252C%25223_method%2522%253A%2522sfvc_auth%2522%257D%23_%3D_&display=page&locale=en_US&logger_id=30F06532-A1B9-4B10-BB28-B29956C71AB1";
    public String generateNewOauth(){
        MechanizeAgent agent = new MechanizeAgent();
        agent.setUserAgent("Mozilla/5.0 (Linux; U; en-gb; KFTHWI Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.16 Safari/535.19");
        Document page = agent.get(tinder_api_url);
        Form form = page.forms().get(0);
        form.get("email").set(facebookEmail);
        form.get("pass").set(facebookPassword);
        Document page2 = form.submit().getAgent().get(tinder_api_url);

        facebookOAuth2 = page2.forms().get(0).submit().asString();
        try {
            facebookOAuth2 = facebookOAuth2.substring(facebookOAuth2.indexOf("access_token=") + 13, facebookOAuth2.indexOf("&expires_in"));
        } catch (Exception ex){
            facebookOAuth2 = "FAILED";
        }
        return facebookOAuth2;
    }

}
