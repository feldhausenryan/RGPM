package mainpackage;
import net.dv8tion.jda.core.entities.*;

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
	
	public void setQueueTime(long newQueueTime){
		queueTime = newQueueTime;
	}
}
