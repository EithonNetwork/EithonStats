# Stats plugin

Keep track of Player statistics

## Release history

### 1.8 (2015-08-28)

* NEW: Now counts the number of consecutive days that the player has been active for more than TimeSpanPerDayForConsecutiveDays.

### 1.7 (2015-08-11)

* CHANGE: Now censors the AFK messages using EithonCop.

### 1.6 (2015-08-10)

* CHANGE: All time span configuration values are now in the general TimeSpan format instead of hard coded to seconds or minutes or hours.

### 1.5 (2015-07-21)

* NEW: Added command /stats playerdiff to show the diff for a specific player
* BUG: Corrected commanddocumentation for /stats diff

### 1.4 (2015-07-19)

* NEW: New API method for EithonRankUp.

### 1.3 (2015-07-14)

* CHANGE: Changed from AFK Yes/No to Online/Offline/AFK
* CHANGE: savedelta() now is synchronized for concurrency reasons.

### 1.2 (2015-07-09)

* NEW: Added command "take" that is the reverse of the "add" command.
* NEW: Added command "reset" that removes all play time for a player.
* NEW: Added command "who" that lists all online players divided into active and AFK.
* BUG: Archiving at midnight caused every on-line player to be marked as idle.

### 1.1 (2015-07-08)

* CHANGE: When a player is already AFK, going idle does not change anything.
* BUG: Couldn't add playtime to offline players.
* BUG: Now shows subcommands if no subcommand was given.

### 1.0 (2015-06-13)

* NEW: Now has an API

### 0.1 (2015-05-01)

* NEW: First Eithon release
