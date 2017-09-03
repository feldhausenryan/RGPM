package mainpackage;
import net.dv8tion.jda.core.entities.*;

/**
 * QueuedPlayer extends a regular JDA user with a queue time. This allows the system to know how 
 * long the JDA user has been in queue for, allowing the system to remove the user after a length of time. 
 * @author feldh
 */
public class QueuedPlayer {
	private User discordUser;
	private long queueTime;
	
	public QueuedPlayer(User discordUser, long queueTime){
		this.discordUser = discordUser;
		this.queueTime = queueTime;
	}
	
	public long getQueueTime(){
		return queueTime;
	}
	
	public User getUser(){
		return discordUser;
	}

	/**
	 * Sets the new queue time. Used when a player queues twice. The player in queue has his time 
	 * updated rather than his place removed and recreated.
	 * @param newQueueTime long Representing the new queue time for the player. 
	 */
	public void setQueueTime(long newQueueTime){
		queueTime = newQueueTime;
	}
}
