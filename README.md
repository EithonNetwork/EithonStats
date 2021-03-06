# EithonStats plugin

Keep track of Player statistics

## Release history

### 2.6 (2016-12-29)

* CHANGE: Bukkit 1.11.

### 2.5.4 (2016-11-27)

* BUG: Bad JDapper usage.

### 2.5.3 (2016-10-22)

* BUG: SQL syntax error

### 2.5.2 (2016-09-25)

* BUG: Null pointer exception. Introduced safer time conversions.

### 2.5.1 (2016-09-18)

* BUG: Null pointer exception.

### 2.5 (2016-09-18)

* CHANGE: New DB library.

### 2.4 (2016-06-30)

* CHANGE: Minecraft 1.10

### 2.3 (2016-06-19)

* NEW: The API can now tell if a player is on his/her first interval for this day.
* CHANGE: Removed dependency to EithonBungee.

### 2.2 (2016-05-10)

* CHANGE: Moved all Bungee related code from EithonLibrary to BungeePlugin.

### 2.1.4 (2016-03-05)

* BUG: Could not get statistics for offline players.

### 2.1.3 (2016-02-26)

* BUG: Still didn't remove players that are leaving the server.

### 2.1.2 (2016-02-25)

* BUG: Calculation error due to copy-paste of code.
* BUG: Now has a try-catch to ensure that we always remove players that are leaving the server.

### 2.1.1 (2016-02-22)

* BUG: TimeSpan was not updated if the player left before the whole hour had passed.
* BUG: TimeSpan was not updated. SQL error.
* BUG: TimeSpan did not allow more than one record per player id. (Fixed in MySQL)
* BUG: Commands should be more relaxed on player names. Know about on-line players, accept any.

### 2.1 (2016-02-21)

* CHANGE: Removed code for reading statistics from file

### 2.0 (2016-02-20)

* CHANGE: Now uses the new Command pattern.
* NEW: Changed from storing statistics in files to storing the statistics in a DB.
* BUG: Player play time was not updated to the latest if the player himself was issuing the "/estats for" command.

### 1.16 (2016-02-13)

* NEW: Experimenting with transferring statistics between BungeeChord servers.

### 1.15 (2016-01-09)

* NEW: API can return play time in seconds.

### 1.14.1 (2015-11-18)

* BUG: Alive detection after AFK derped

### 1.14 (2015-11-13)

* CHANGE: Optimization of PlayerMoveEvent

### 1.13 (2015-11-13)

* NEW: Added sub commands to the /stats add command (time, consecutivedays, placed, broken)
* NEW: Added sub commands to the /stats remove command (time, consecutivedays, placed, broken)
* CHANGED: Renamed "/stats take" to "/stats remove"

### 1.12.3 (2015-10-18)

* BUG: Disabled stats transfer due to BungeeChord problems.

### 1.12.2 (2015-10-18)

* BUG: BungeeChord problems.

### 1.12.1 (2015-10-18)

* BUG: Used the wrong method.

### 1.12 (2015-10-18)

* NEW: First try of support for BungeeChord

### 1.11 (2015-10-18)

* NEW: Added first support for BungeeChord
* CHANGE: Refactoring EithonLibrary.

### 1.10 (2015-10-13)

* CHANGE: Now broadcasts AFK messages to all servers.

### 1.9.2 (2015-09-10)

* BUG: Consecutive days were reset next day.

### 1.9.1 (2015-09-09)

* BUG: Consecutive days were not zeroed if they were past time.

### 1.9 (2015-08-29)

* NEW: Added an event when someone has updated their consecutive days.
* CHANGE: /estats status now has a message when there are no AFK players.
* BUG: Could not use diffstats for offline players.

### 1.8 (2015-08-28)

* NEW: Now counts the number of consecutive days that the player has been active for more than TimeSpanPerDayForConsecutiveDays.
* BUG: /estats status was showing all off-line players as idle.

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
