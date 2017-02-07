<img align="right" src="http://i.imgur.com/V3pKOwS.png" height="200" width="200"/>

# Tindava
Tinder bot controlled through a Discord server





# Compile with gradle
In you `build.gradle` add
```groovy
task fatJar(type: Jar) {
    manifest {
        attributes 'Implementation-Title': 'Gradle Jar File Example',
                'Implementation-Version': version,
                'Main-Class': 'Main'
    }
    baseName = project.name + '-all'
    from { configurations.compile.collect { it.isDirectory() ? it : zipTree(it) } }
    with jar
}
```
<sub>https://www.mkyong.com/gradle/gradle-create-a-jar-file-with-dependencies/</sub>


Then run `gradle fatJar`

# Args[]
Supply Tindava with your BOT token from Discord (`args[0] = <token>`)

Example `java -jar Tindava.jar Mjc3NTuJiKKW9AWNDM3MzEy.C3lt9g.RbGELaaW9AfMflBl4VcIYNJ_Yt`


# Discord commands
All commands can be invoked by using @Tindava or 🔥(`:fire:`) as a prefix

### Commandlist (🔥 command <required argument> [multiple | choice | argument] [optional argument] ###

`🔥 create channel <channel name> <bot name in channel> <bot avatar url in channel>`<br />
*adds a channel to the server, with a pre-configured webhook*

`🔥 my roles`<br />
*whispers you with your roles and role_ids for the server (useful for developing since there is no other way to get a roles id)*

`🔥 supply id <facebook id>`<br />
*facebook id for the controlled tinder account*

`🔥 supply auth_token <facebook auth_token>`<br />
*facebook oauth2 token for tinder, you can find get your token <a href="https://www.facebook.com/dialog/oauth?client_id=464891386855067&redirect_uri=fbconnect://success&scope=basic_info,email,public_profile,user_about_me,user_activities,user_birthday,user_education_history,user_friends,user_interests,user_likes,user_location,user_photos,user_relationship_details&response_type=token">HERE</a> (look for an AJAX POST `confirm?dpr=x.xx` <sub>↳ `jsmods` ↳ `require` ↳ `0` ↳ `3` ↳ `0` ↳ `access_token=`</sub>)*

~~`🔥 add match <tinder id>`~~<br />
~~*calls the method for adding a match with specified id, will NOT actually make you a match*~~

`🔥 request update <full json data>`<br />
*will read the given json data (see <a href="#JSON-data-format">json data format</a> for more info) and create messages / matches appropriately*


# JSON data format
The tinder JSON format will look something like this
```JSON
```

For more information check out <a href="https://gist.github.com/rtt/10403467">Tinder API documentation</a> by <a href="https://gist.github.com/rtt">Rich T</a>.


## THIS IS A WORK IN PROGRESS
