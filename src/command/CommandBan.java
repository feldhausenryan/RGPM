package command;

import java.util.List;

import mainpackage.Main;
import mainpackage.Match;
import mainpackage.Queue;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import structures.AuthorityEnum;

/**
 * This command bans mentioned players when executed. 
 * @author feldh
 *
 */
public class CommandBan extends Command{
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
	public CommandBan(GuildMessageReceivedEvent event, 
			MessageChannel DISCORD_TARGET_CHANNEL, 
			Guild DISCORD_TARGET_GUILD,
			Queue DISCORD_TARGET_QUEUE){
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
		this.DISCORD_TARGET_QUEUE = DISCORD_TARGET_QUEUE;
	}
	   
    /**
     * Process the command to ban a user. 
     * @param msg The jda message
     * @param author The author of the jda message
     */
	@Override
	public void execute() {
    	if(Match.getUserAuthority(author, DISCORD_TARGET_GUILD) == AuthorityEnum.ADMIN || 
    			Match.getUserAuthority(author, DISCORD_TARGET_GUILD) == AuthorityEnum.OWNER){
	    	List<User> usersToBan = message.getMentionedUsers();
	    	for (User user : usersToBan){
	        	if(Match.getUserAuthority(user, DISCORD_TARGET_GUILD) == AuthorityEnum.ADMIN || 
	        			Match.getUserAuthority(user, DISCORD_TARGET_GUILD) == AuthorityEnum.OWNER){
	        		DISCORD_TARGET_CHANNEL.sendMessageFormat("Invalid Command: %s is an admin or owner", user.getName()).queue();
	        		return;
	        	}
	        	String id = user.getId();
	        	DISCORD_TARGET_CHANNEL.sendMessageFormat("Banning user %s from queuing.", user.getName()).queue();
	    		Main.banDatabase.put(id, true);
	    		DISCORD_TARGET_QUEUE.removePlayerFromQueue(user);
	    	}
    	}
    }

}
