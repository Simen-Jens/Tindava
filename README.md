<img align="right" src="http://i.imgur.com/mjcs71U.png" height="200" width="200"/>

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

# settings.json
In the root directory for the compiled jar file you need a settings.json file.

The settings.json allows you to set certain filters for which matches to display/look for and allows you to set roles and default channels (all channels other than "match channels" needs to be defined in this array). <br /> Default settings.json is:
```json
{
	"bot_token": "",
	"google_api_key": null,
	"cleverbot_key": "",
	"language": "en",

	"facebook_email": "",
	"facebook_password": "",
	"autologin": true,

	"facebook_auth": null,
	"facebook_id": null,
	"xauth": null,
	"send_stat": false,


	"cache_path": "cache.json",
	"empty_path": "empty.json",
	"pullrate_min": 10000,
	"pullrate_max": 10000,


	"default_channels": [
		""
	],
	"messenger_role": "",
	"hard_banned_users": [],
	"system_color": "#ffffff",


	"exclude_matches_with_name": [],
	"exclude_first_int_matches": 72,
	"exclude_last_int_matches": 0,


	"default_match_color": "#78b159",
	"default_match_thumb": "http://emojipedia-us.s3.amazonaws.com/cache/16/22/1622b595a25ee401f56aa047cd4520eb.png",

	"super_match_color": "#01b6cb",
	"super_match_thumb": "http://pre01.deviantart.net/db85/th/pre/i/2016/295/b/0/tinder_super_like_star_by_topher147-dalwd0y.png",

	"unmatch_match_color": "#d55a70",
	"unmatch_match_thumb": "http://emojipedia-us.s3.amazonaws.com/cache/51/3a/513a734baf098ead6eb961f8d4092fc3.png",

	"unmatch_match_color_imc": "#e73945",
	"unmatch_match_title_imc": "Unmatched",
	"unmatch_match_desc_imc": "aw nuts...",
	"unmatch_match_image_imc": "http://i.imgur.com/pAi00xj.png"
}
``` 
<sub>please keep in mind that these settings are not sanitized in any way, you will break your bot if you supply it with illegal characters</sub></br >
In order for the bot to work at a minimum:
* `bot_token` needs to be filled out.
* `default_channels` needs at least one channel ID (used for notifications).
* `messenger_role`needs to be filled out (can be @everyone's ID).

# Discord commands
All commands can be invoked by 🔥(`:fire:`) as a prefix

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

`🔥 facebook email <email>`<br />
*supplies the bot with attached email for Tinder-account's Facebook account.*

`🔥 facebook password <password>`<br />
~~*supplies the bot with attached password for Tinder-account's Facebook account. *~~*disabled for safety, use settings.json instead*

`🔥 purge <number>`<br />
*deletes messages in the chat based on number given*

`🔥 unmatch`<br />
*unmatches the the channel's respective user*

`🔥 remove chats`<br />
*removes generated chats<br />

`🔥 toggle chat`<br />
*toggles whether the bot will send messages to tinder matches when messaged*

`🔥 toggle updates`<br />
*toggles the update thread off/on*

`🔥 swipe all`<br />
*gets all recommendations from Tinder and swipes right, returns the amount of people swiped and how many swipes are left*

`🔥 organize`<br />
*organizes all text-channels lexicographically (default_channels remain at the top)*

`🔥 unmatch all`<br />
*removes all matches for the Tinder account*

`🔥 request update <json>`<br />
*allows you to send pre-defined json data*

`🔥 set address <any place on earth>`<br />
*request lat and lon from Google Maps and updates the position for your Tinder account*

`🔥 instance`<br />
*re-instances the bot should this be needed (becomes relevant if there are a lot of channels on a guild, Discord4J seems to have some trouble generating data on these channels within the required time)*

`🔥 link`<br />
*send in a match-channel to link it up with the next match-channel you send this in (only two and two channels can be linked together)*

`🔥 cleverbot`<br />
*links the channel up to cleverbot (cannot be linked with another channel at the same time)*

`🔥 update settings`<br />
*re-reads the settings.json file*

`🔥 login`<br />
*tries to log the bot into Tinder (if successful this will also start the bot)*


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
