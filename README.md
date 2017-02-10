<img align="right" src="http://i.imgur.com/V3pKOwS.png" height="200" width="200"/>

# Tindava ![TravisCI](https://travis-ci.org/Simen-Jens/Tindava.svg?branch=master)
Tinder bot controlled through a Discord server<br />





# Compile with gradle
In your `build.gradle` add
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
Supply Tindava with your BOT token from Discord (`args[0] = <token>` `args[1] = <default channel>`)

Example `java -jar Tindava.jar Mjc3NTuJiKKW9AWNDM3MzEy.C3lt9g.RbGELaaW9AfMflBl4VcIYNJ_Yt 277596483631579137`


# Discord commands
All commands can be invoked by using @Tindava or 🔥(`:fire:`) as a prefix

### Commandlist (🔥 command <\required argument> [multiple | choice | argument] [optional argument] ###

`🔥 create channel <channel name> <bot name in channel> <bot avatar url in channel>`<br />
*adds a channel to the server, with a pre-configured webhook*

`🔥 my roles`<br />
*whispers you with your roles and role_ids for the server (useful for developing since there is no other way to get a roles id)*

`🔥 supply id <facebook id>`<br />
*facebook id for the controlled tinder account*

`🔥 supply auth_token <facebook auth_token>`<br />
*facebook oauth2 token for tinder, you can find your token <a href="https://www.facebook.com/dialog/oauth?client_id=464891386855067&redirect_uri=fbconnect://success&scope=basic_info,email,public_profile,user_about_me,user_activities,user_birthday,user_education_history,user_friends,user_interests,user_likes,user_location,user_photos,user_relationship_details&response_type=token">HERE</a> (look for an AJAX POST `confirm?dpr=x.xx` <sub>↳ `jsmods` ↳ `require` ↳ `0` ↳ `3` ↳ `0` ↳ `access_token=`</sub>)*

`🔥 supply xauth <tinder xauth-token>`<br />
*allows you to skip supplying facebook id and facebook auth_token*

`🔥 request update <full json data | !auth!>`<br />
*will read the given json data (see <a href="#JSON-data-format">json data format</a> for more info) and create messages / matches appropriately. If supplied with `!auth!` instead it will use the facebook credentials / xauth to pull json directly from tinder*<br />
<sub>__THIS IS HOW YOU "START" THE BOT__</sub>

`🔥 purge <number>`<br />
*deletes messages in the chat based on number given*

`🔥 unmatch`<br />
*unmatches the the channel's respective user*

`🔥 remove chats`<br />
*removes generated chats<br />

`🔥 toggle chat`<br />
*toggles whether the bot will send messages to tinder matches when messaged*

`🔥 toggle updates`<br />
*toggles whether the update thread off/on*

~~`🔥 t_o_alert`<br />~~
~~*turn off alert, just mutes messages from yourself, use this command if you find that the bot duplicates your messages*~~<br />
<sub>not needed anymore (still usable, should you experience issues)</sub>

`🔥 swipe`<br />
*gets all recommendations from Tinder and swipes right, returns the amount of people swiped and how many swipes are left*


# JSON data format
The tinder JSON format will look something like this
```JSON
{"matches": [
		{
			"_id": "52b0a7034d589899bc68bd52b8034d58a00034034d58999bc68bd",
			"closed": false,
			"common_friend_count": 0,
			"common_like_count": 0,
			"created_date": "2017-02-08T15:49:48.781Z",
			"dead": false,
			"last_activity_date": "2017-02-08T17:17:01.038Z",
			"message_count": 0,
			"messages": [
				{
					"_id": "52b8034d58989c13a00034034d59bc68bd",
					"match_id": "52b0a7034d589899bc68bd52b8034d58a00034034d58999bc68bd",
					"to": "52b8adc4bbe0ccc13a00034d",
					"from": "58989939dca7aec30abc68bd",
					"message": "Hey",
					"sent_date": "2017-02-08T17:17:01.038Z",
					"created_date": "2017-02-08T17:17:01.038Z",
					"timestamp": 1486574221038,
					"isolate": true
				}
			],
			"muted": false,
			"participants": [
				"52b8adc4bbe0ccc13a00034d"
			],
			"pending": false,
			"is_super_like": false,
			"is_boost_match": false,
			"following": true,
			"following_moments": true,
			"id": "52b8034d58989c13a00034034d59bc68bd",
			"person": {
				"_id": "52b8adc4bbe0ccc13a00034d",
				"badges": [],
				"bio": "...",
				"birth_date": "1994-02-11T21:21:56.395Z",
				"gender": 0,
				"name": "Peter",
				"ping_time": "2017-02-08T17:05:54.217Z",
				"photos": []
			}
		}
    ]
}
```

For more information check out <a href="https://gist.github.com/rtt/10403467">Tinder API documentation</a> by <a href="https://gist.github.com/rtt">Rich T</a>.

# Setup
* Step 1. download or clone the project.

* Step 2. <a href="#compile-with-gradle">compile</a> the project.

* Step 3. create a new file called `empty.json` in the same directory as the `.jar` file.
The `.json` file should contain an empty json object like so.
```JSON
{
	"matches": [],
	"blocks": [],
	"lists": [],
	"deleted_lists": [],
	"last_activity_date": "2017-02-06T16:41:33.813Z"
}
```

* Step 4. add the bot to your guild with `https://discordapp.com/api/oauth2/authorize?client_id=157730590492196864&scope=bot&permissions=0` (fill out your own bot's client id)

* Step 5. run the .jar file with `java -jar Tindava.jar <discord bot token> <default channel ids>`

* Step 6. message the bot with an ID and token from Facebook as described <a href="#discord-commands">here</a>

* Step 7. message the bot with this exact line `🔥 request update !auth!`

* Step 8. The bot should now login and connect to Tinder, it will start to fill your server with all your previous matches and messages if this is not a new account
## THIS IS A WORK IN PROGRESS
