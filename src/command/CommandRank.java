package command;

import mainpackage.Main;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import structures.Player;

/**
 * This command displays the rating of players mentioned in the text of the command. 
 * Example !rank @joe
 * @author feldr
 *
 */
public class CommandRank extends Command {
	
	/** Generate a command from an event. This can be extended (probably)
	 * to any event type if needed.
	 * @param event The event this command was created because of. 
	 * @param DISCORD_TARGET_CHANNEL The channel to talk in
	 * @param DISCORD_TARGET_GUILD The guild to talk in
	 */
	public CommandRank(GuildMessageReceivedEvent event, MessageChannel DISCORD_TARGET_CHANNEL,
			Guild DISCORD_TARGET_GUILD) {
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
	}

    /**
     * Process the command to handle a request for player(s) ranks
     * @param msg The jda message
     * @param author The author of the jda message
     */
	@Override
	public void execute() {
    	for(User user : message.getMentionedUsers()){
    		if (Main.playerDatabase.containsKey(user.getId())){
	    		Player p = (Player) Main.playerDatabase.get(user.getId());
	    		DISCORD_TARGET_CHANNEL.sendMessageFormat("%s has a ranking of %d", p.getName(), p.getU()).queue();
    		}
    		else{
    			DISCORD_TARGET_CHANNEL.sendMessageFormat("%s is not in the database", user.getName()).queue();
    		}
    	}
	}

}
