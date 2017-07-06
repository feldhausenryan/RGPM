package command;

import mainpackage.Queue;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * Broadcasts the status of the queue when executed. 
 * @author feldh
 *
 */
public class CommandStatus extends Command{
	/**
	 * The queue to add the author into. 
	 */
	private Queue DISCORD_TARGET_QUEUE;
	
	/**
	 * Constructs a CommandQueue instance. Takes one additional paramter over the abstract class. 
	 * @param event The discord event. Used in the abstract and contains the JDA-provided discord information. 
	 * @param DISCORD_TARGET_CHANNEL The channel to talk in 
	 * @param DISCORD_TARGET_GUILD The guild to talk in
	 * @param DISCORD_TARGET_QUEUE The queue to add the author into
	 */
	public CommandStatus(GuildMessageReceivedEvent event, 
			MessageChannel DISCORD_TARGET_CHANNEL, 
			Guild DISCORD_TARGET_GUILD,
			Queue DISCORD_TARGET_QUEUE){
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
		this.DISCORD_TARGET_QUEUE = DISCORD_TARGET_QUEUE;
	}
	
    /**
     * Process the command to ask for the status of the queue. 
     */
	@Override
	public void execute() {
    	DISCORD_TARGET_QUEUE.broadcastStatus();
		
	}

}
