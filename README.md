# RGPM
Ryan's Guild Wars 2 PvP Matchmaker Discord Bot

This is an open sourced bot that matches players in the game Guild Wars 2 together for private custom games. It has ELO based matchmaking, and attempts to provide optimal balanced matches through bute force computation of the 126ish possible arrangements of two 5 player teams given 10 total players. 

This contrasts to the in game system (https://wiki.guildwars2.com/wiki/PvP_Matchmaking_Algorithm) which doesn't assure balanced matches due to it using queue time / roster size/ games played / rank rather than pure rating and butchering the implementation in the process.

At the moment the bot is run on a server using Google Compute Engine with Java installed and is used in the Discord server for one one of the game's major PvP guilds.  
