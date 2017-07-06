package mainpackage;
import java.util.ArrayList;
import java.util.Iterator;

import net.dv8tion.jda.core.entities.*;

public class Queue {
	private ArrayList<QueuedPlayer> queue;
	
	public Queue(){
		queue = new ArrayList<QueuedPlayer>();
	}

	public boolean addPlayerToQueue(QueuedPlayer player){
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
