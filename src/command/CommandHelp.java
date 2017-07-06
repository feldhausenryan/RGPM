package command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * This command displays help information when exectued. 
 * @author feldh
 *
 */
public class CommandHelp extends Command {
	
	/** Generate a command from an event. This can be extended (probably)
	 * to any event type if needed.
	 * @param event The event this command was created because of. 
	 * @param DISCORD_TARGET_CHANNEL The channel to talk in
	 * @param DISCORD_TARGET_GUILD The guild to talk in
	 */
	public CommandHelp(GuildMessageReceivedEvent event, MessageChannel DISCORD_TARGET_CHANNEL,
			Guild DISCORD_TARGET_GUILD) {
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
	}
	
    /**
     * Process the command to display help information. 
     */
	@Override
	public void execute() {
    	DISCORD_TARGET_CHANNEL.sendMessage("RGPM is a discord bot that provides basic matchmaking for GW2 inhouses. "
    			+ "It provides an alternative to team-captain style matchmaking."
    			+ "Use !commands for a list of commands.").queue();
    }
}
