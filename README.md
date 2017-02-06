<img align="right" src="http://i.imgur.com/e2BWJKr.png" height="200" width="200"/>

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
https://www.mkyong.com/gradle/gradle-create-a-jar-file-with-dependencies/
Then run `gradle fatJar`

# Args[]
Supply Tindava with your BOT token from Discord (`args[0] = <token>`)

Example `java -jar Tindava.jar Mjc3NTuJiKKW9AWNDM3MzEy.C3lt9g.RbGELaaW9AfMflBl4VcIYNJ_Yt`

## THIS IS A WORK IN PROGRESS
