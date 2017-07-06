package command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * This command displays what discord commands are available. 
 * @author feldh
 *
 */
public class CommandCommands extends Command{
	
	/** Generate a command from an event. This can be extended (probably)
	 * to any event type if needed.
	 * @param event The event this command was created because of. 
	 * @param DISCORD_TARGET_CHANNEL The channel to talk in
	 * @param DISCORD_TARGET_GUILD The guild to talk in
	 */
	public CommandCommands(GuildMessageReceivedEvent event, MessageChannel DISCORD_TARGET_CHANNEL,
			Guild DISCORD_TARGET_GUILD) {
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
	}
	
    /**
     * Process the command to display what commands are available. 
     */
	@Override
	public void execute() {
    	DISCORD_TARGET_CHANNEL.sendMessage("Commands:\n"
				  + "!queue <duration-minutes> | queues single person solo for x minutes\n"+
					"!leave | removes the player from queue\n"+
					"!status | returns the number of players in queue\n"+
					"!redwin <match-id> | resolves a match as Red Team Win\n"+
					"!bluewin <match-id> | resolves a match as Blue Team Wwin\n"+
					"!rank | returns mentioned players' ranks\n"+
					"!ban | bans mentioned players\n"+
					"!unban | unbans mentioned players\n"+
					"!remove | removes mentioned players from the DB").queue();
	}
}
