# Graphite
## Compiling
The bot uses Maven for building.

To compile the bot, use

```
$ mvn package
```

which will generate a `Graphite-VERSION.jar` in the `target` folder.

## Using the bot
First, compile the bot or download a prebuilt JAR [not yet available].

### Configuration
To create a configuration file, you can run the bot and provide a path to a config file that does not yet exist. The bot will then create a default config file which you can edit.

TODO: config instructions

### Running
To run the bot, use any Java 17+ VM. You can run the bot like so:

```
java -jar Graphite-VERSION.jar path/to/config.json
```
