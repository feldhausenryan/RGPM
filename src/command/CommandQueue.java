package command;


import mainpackage.Queue;
import mainpackage.QueuedPlayer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * This command adds a the author of the command into the queue when executed. 
 * @author feldh
 *
 */
public class CommandQueue extends Command{
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
	public CommandQueue(GuildMessageReceivedEvent event, 
			MessageChannel DISCORD_TARGET_CHANNEL, 
			Guild DISCORD_TARGET_GUILD,
			Queue DISCORD_TARGET_QUEUE){
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
		this.DISCORD_TARGET_QUEUE = DISCORD_TARGET_QUEUE;
	}

    /**
     * Executes the command to add a player to the matchmaker queue. 
     * @param msgText The text of the command. 
     * @param author The author of the command. 
     */
	@Override
	public void execute() {
    	// Split up the queue message, confirm it obeys proper convention. !queue DURATION
    	int minDuration = 0;
    	// Split up the message and grab the second command. 
    	String[] messageArgs = msgText.split(" ");
    	// Quick check to make sure there IS a second command. 
    	if (messageArgs.length != 2){
    		DISCORD_TARGET_CHANNEL.sendMessage("Invalid Command: '!queue' takes one argument.").queue();
    		return;
    	}
    	// Try to convert it to a number. Range it to a reasonable size. 
		try{
			minDuration = Integer.valueOf(messageArgs[1]);
	    	if (minDuration < 0)   minDuration = 30;
	    	if (minDuration > 180) minDuration = 180;
		}
		catch(NumberFormatException nfe){
			DISCORD_TARGET_CHANNEL.sendMessage("Invalid Command: Bad Duration.").queue();
			return;
		}
		
		// RGPM uses nanoTime to determine how long to keep people in queue. It runs a service that
		// checks every 10s to remove players who have been in queue too long. 
    	long queueTime = System.nanoTime() + ((long)minDuration) * (long) 60e9;
    	
    	// Don't add DND/Invis/Offline/Unknown players into the queue.
    	switch(DISCORD_TARGET_GUILD.getMember(author).getOnlineStatus()){
		case DO_NOT_DISTURB:
			DISCORD_TARGET_QUEUE.removePlayerFromQueue(author);
			DISCORD_TARGET_CHANNEL.sendMessageFormat("%s not added to queue. Status is DO_NOT_DISTURB.", author.getName()).queue();
			return;
		case INVISIBLE:
			DISCORD_TARGET_QUEUE.removePlayerFromQueue(author);
			DISCORD_TARGET_CHANNEL.sendMessageFormat("%s not added to queue. Status is INVISIBLE.", author.getName()).queue();
			return;
		case OFFLINE:
			DISCORD_TARGET_QUEUE.removePlayerFromQueue(author);
			DISCORD_TARGET_CHANNEL.sendMessageFormat("%s not added to queue. Status is OFFLINE.", author.getName()).queue();
			return;
		case UNKNOWN:
			DISCORD_TARGET_QUEUE.removePlayerFromQueue(author);
			DISCORD_TARGET_CHANNEL.sendMessageFormat("%s not added to queue. Status is UNKNOWN.", author.getName()).queue();
			return;
		default:
			break;
    	}
    	// Add the player to queue and print out a message including the number of players in queue. 
    	QueuedPlayer newPlayer = new QueuedPlayer(author, queueTime);
    	if(DISCORD_TARGET_QUEUE.addPlayerToQueue(newPlayer)){
    		DISCORD_TARGET_CHANNEL.sendMessageFormat("User %s added to queue for %d minutes. (%d/10)", author.getName(), minDuration, DISCORD_TARGET_QUEUE.getQueueLength()).queue();
    	}
    	else{
    		DISCORD_TARGET_CHANNEL.sendMessageFormat("User %s updated queue time to %d minutes. (%d/10)",  author.getName(), minDuration, DISCORD_TARGET_QUEUE.getQueueLength()).queue();
    	}
    }

}
