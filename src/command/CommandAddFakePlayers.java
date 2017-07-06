package command;

import mainpackage.Main;
import mainpackage.Queue;
import mainpackage.QueuedPlayer;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * This developer-only command will insert some fake players into the queue to test match creation. 
 * @author feldh
 *
 */
public class CommandAddFakePlayers extends Command {
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
	public CommandAddFakePlayers(GuildMessageReceivedEvent event, 
			MessageChannel DISCORD_TARGET_CHANNEL, 
			Guild DISCORD_TARGET_GUILD,
			Queue DISCORD_TARGET_QUEUE){
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
		this.DISCORD_TARGET_QUEUE = DISCORD_TARGET_QUEUE;
	}
	
    /**
     * Process the owner-only command to generate 9 fake players and inject them into the queue. 
     * THIS WILL BE DELETED SOON. DELETE ME TEMP TODO 
     */
	@Override
	public void execute() {
    	int minuteDuration = 30;
    	long queueTime = System.nanoTime() + ((long)minuteDuration) * (long) 60e9;
    	
    	UserImpl f1 = new UserImpl(1, (JDAImpl) Main.jda);
    	UserImpl f2 = new UserImpl(2, (JDAImpl) Main.jda);
    	UserImpl f3 = new UserImpl(3, (JDAImpl) Main.jda);
    	UserImpl f4 = new UserImpl(4, (JDAImpl) Main.jda);
    	UserImpl f5 = new UserImpl(5, (JDAImpl) Main.jda);
    	UserImpl f6 = new UserImpl(6, (JDAImpl) Main.jda);
    	UserImpl f7 = new UserImpl(7, (JDAImpl) Main.jda);
    	UserImpl f8 = new UserImpl(8, (JDAImpl) Main.jda);
    	UserImpl f9 = new UserImpl(9, (JDAImpl) Main.jda);
    	
    	f1.setName("f1");
    	f2.setName("f2");
    	f3.setName("f3");
    	f4.setName("f4");
    	f5.setName("f5");
    	f6.setName("f6");
    	f7.setName("f7");
    	f8.setName("f8");
    	f9.setName("f9");
    	
    	QueuedPlayer fq1 = new QueuedPlayer(f1, queueTime);
    	DISCORD_TARGET_QUEUE.addPlayerToQueue(fq1);
    	QueuedPlayer fq2 = new QueuedPlayer(f2, queueTime);
    	DISCORD_TARGET_QUEUE.addPlayerToQueue(fq2);
    	QueuedPlayer fq3 = new QueuedPlayer(f3, queueTime);
    	DISCORD_TARGET_QUEUE.addPlayerToQueue(fq3);
    	QueuedPlayer fq4 = new QueuedPlayer(f4, queueTime);
    	DISCORD_TARGET_QUEUE.addPlayerToQueue(fq4);
    	QueuedPlayer fq5 = new QueuedPlayer(f5, queueTime);
    	DISCORD_TARGET_QUEUE.addPlayerToQueue(fq5);
    	QueuedPlayer fq6 = new QueuedPlayer(f6, queueTime);
    	DISCORD_TARGET_QUEUE.addPlayerToQueue(fq6);
    	QueuedPlayer fq7 = new QueuedPlayer(f7, queueTime);
    	DISCORD_TARGET_QUEUE.addPlayerToQueue(fq7);
    	QueuedPlayer fq8 = new QueuedPlayer(f8, queueTime);
    	DISCORD_TARGET_QUEUE.addPlayerToQueue(fq8);
    	QueuedPlayer fq9 = new QueuedPlayer(f9, queueTime);
    	DISCORD_TARGET_QUEUE.addPlayerToQueue(fq9);
    }

}
