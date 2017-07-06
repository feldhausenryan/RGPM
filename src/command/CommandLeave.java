package command;

import mainpackage.Queue;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * This command makes the author leave the queue when executed.
 * @author feldh
 *
 */
public class CommandLeave extends Command{
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
	public CommandLeave(GuildMessageReceivedEvent event, 
			MessageChannel DISCORD_TARGET_CHANNEL, 
			Guild DISCORD_TARGET_GUILD,
			Queue DISCORD_TARGET_QUEUE){
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
		this.DISCORD_TARGET_QUEUE = DISCORD_TARGET_QUEUE;
	}
	
    /**
     * Process the command to have a user leave the queue. 
     * @param author
     */
	@Override
	public void execute() {
    	if(DISCORD_TARGET_QUEUE.removePlayerFromQueue(author)){
    		DISCORD_TARGET_CHANNEL.sendMessageFormat("User %s removed from queue. (%d/10)", author.getName(), DISCORD_TARGET_QUEUE.getQueueLength()).queue();
    	}else{
    		DISCORD_TARGET_CHANNEL.sendMessageFormat("User %s was not in queue.", author.getName()).queue();
    	}
	}

}
