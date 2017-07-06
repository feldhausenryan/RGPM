package command;

import mainpackage.Main;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * This command will redirect the output of the bot to a new channel and guild. 
 * @author feldh
 *
 */
public class CommandTargetChannel extends Command {
	
	/** Generate a command from an event. This can be extended (probably)
	 * to any event type if needed.
	 * @param event The event this command was created because of. 
	 * @param DISCORD_TARGET_CHANNEL The channel to talk in
	 * @param DISCORD_TARGET_GUILD The guild to talk in
	 */
	public CommandTargetChannel(GuildMessageReceivedEvent event, MessageChannel DISCORD_TARGET_CHANNEL,
			Guild DISCORD_TARGET_GUILD) {
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
	}
	
    /**
     * Changes parameters in the database and directs the bot to talk and receive messages
     * from the provided channel and guild. The channel needs to be in the guild. 
     * @param channel The channel to send the messages to.
     * @param guild The guild to send the messages to. 
     */
	@Override
	public void execute() {
    	DISCORD_TARGET_GUILD = guild;
    	DISCORD_TARGET_CHANNEL = channel;
    	Main.settingsDatabase.put(Main.STRING_DISCORD_TARGET_GUILD_ID, DISCORD_TARGET_GUILD.getId());
    	Main.settingsDatabase.put(Main.STRING_DISCORD_TARGET_CHANNEL_ID, DISCORD_TARGET_CHANNEL.getId());
    	DISCORD_TARGET_CHANNEL.sendMessageFormat("RGPM Directed to %s, %s",
    			DISCORD_TARGET_GUILD.getName(), DISCORD_TARGET_CHANNEL.getName());
    }

}
