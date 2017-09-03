package mainpackage;
import java.util.ArrayList;
import java.util.Iterator;

import net.dv8tion.jda.core.entities.*;

/**
 * This class handles a queue of players and has functions to start a match when 10 players are found,
 * remove players who have disconnected, broadcast status, and more. 
 * @author feldh
 */
public class Queue {
	private ArrayList<QueuedPlayer> queue;
	 
	/**
	 * Initiates a new empty queue
	 */
	public Queue(){
		queue = new ArrayList<QueuedPlayer>();
	}

	/**
	 * Adds a player to the queue and checks if they are already in the queue.
	 * If they are in the queue, update the time rather than replace them. 
	 * @param player QueuedPlayer to add to the queue. 
	 * @return True if the player was added to the queue. False if only the time was updated. 
	 */
	boolean addPlayerToQueue(QueuedPlayer player){
		for(QueuedPlayer queuedPlayer : queue){
			if(queuedPlayer.getUser().getId().equals(player.getUser().getId())){
				queuedPlayer.setQueueTime(player.getQueueTime());
				return false;
			}
		}
		queue.add(player);
		checkCount();
		return true;
	}
	
	/**
	 * Remove a player from the queue and return true if the player was successfully found and removed. 
	 * @param user User to remove from the queue.
	 * @return true if the player was removed from the queue. false if they couldn't be found. 
	 */
	public boolean removePlayerFromQueue(User user){
		for(QueuedPlayer queuedPlayer : queue){
			if(queuedPlayer.getUser().getId().equals(user.getId())){
				queue.remove(queuedPlayer);
				return true;
			}
		}
		return false;
	}
	
	public int getQueueLength(){
		return queue.size();
	}
	
	/**
	 * Checks to see if any players have been in queue for longer than they subscribed to. 
	 * Also checks to see if any players have become disconnected / DND/ etc.
	 * Removes players from the queue based on these conditions. 
	 */
	public void cleanQueue(){
		Iterator<QueuedPlayer> itr = queue.iterator();
		while(itr.hasNext()){
			QueuedPlayer player = itr.next();
			
			// USED ONLY FOR TESTING PURPOSES
			if(player.getUser().getName().startsWith("f")) continue;
			
			switch(Main.DISCORD_TARGET_GUILD.getMember(player.getUser()).getOnlineStatus()){
			case ONLINE:
				if((player.getQueueTime() - System.nanoTime()) / 60e9 < 0){
					itr.remove();
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%s removed from queue. Duration expired. (%d/10)",  player.getUser().getName(), queue.size()).queue();
				}
				break;
			case IDLE:
				if((player.getQueueTime() - System.nanoTime()) / 60e9 < 0){
					itr.remove();
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%s removed from queue. Duration expired. (%d/10)",  player.getUser().getName(), queue.size()).queue();
				}				
				break;
			case DO_NOT_DISTURB:
				itr.remove();
				Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%s removed from queue. Status is DO_NOT_DISTURB. (%d/10)", player.getUser().getName(), queue.size()).queue();
				break;
			case INVISIBLE:
				itr.remove();
				Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%s removed from queue. Status is INVISIBLE. (%d/10)", player.getUser().getName(), queue.size()).queue();
				break;
			case OFFLINE:
				itr.remove();
				Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%s removed from queue. Status is OFFLINE. (%d/10)", player.getUser().getName(), queue.size()).queue();
				break;
			case UNKNOWN:
				itr.remove();
				Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%s removed from queue. Status is UNKNOWN. (%d/10)", player.getUser().getName(), queue.size()).queue();
				break;
			default:
				break;
			}
		}
	}
	
	/**
	 * States who is in queue, how long they are in queue, and how many players total are in queue. 
	 */
	public void broadcastStatus(){
		cleanQueue();
		for(QueuedPlayer player : queue){

			// USED ONLY FOR TESTING PURPOSES TEMP
			if(player.getUser().getName().startsWith("f")) continue;
			
			int remainingMinutes = (int) ((player.getQueueTime() - System.nanoTime()) / 60e9);
			switch(Main.DISCORD_TARGET_GUILD.getMember(player.getUser()).getOnlineStatus()){
			case ONLINE:
				Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%s is ONLINE in queue for %d more minutes.", player.getUser().getName(), remainingMinutes).queue();
				break;
			case IDLE:
				Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%s is IDLE in queue for %d more minutes.", player.getUser().getName(), remainingMinutes).queue();
				break;
			default:
				break;
			}
		}
		if (queue.size() == 0){
			Main.DISCORD_TARGET_CHANNEL.sendMessage("There is nobody currently in queue (0/10)").queue();
		}
		else{
			Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("(%d/10) Players in queue.", queue.size()).queue();
		}
	}
	
	/**
	 * Checks to see if there are more than 10 players in the queue. 
	 * If this condition is met, a new match is created. 
	 */
	public void checkCount(){
		if(queue.size() >= 10){
			new Match(queue.subList(0, 10));
			if(queue.size() == 10){
				queue.clear();
			}
			else{
				for(int x = 0; x < 10; x++){
					queue.remove(0);
				}
			}
		}
	}	
}
