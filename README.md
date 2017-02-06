<img align="right" src="http://i.imgur.com/e2BWJKr.png" height="320" width="320"/>

# Tindava
Tinder bot controlled through a Discord server





# Compile with gradle
In you `build.gradle` add
```gradle
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

WIP
