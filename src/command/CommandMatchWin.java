package command;

import mainpackage.Match;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import structures.WinningTeam;

/**
 * This command sets a match to redwin or bluewin and adjusts mmr when called. 
 * @author feldh
 *
 */
public class CommandMatchWin extends Command {
	/**
	 * winner represents the team red/blue that won the match being recorded.
	 */
	private WinningTeam winner;
	
	/** Generate a command from an event. This can be extended (probably)
	 * to any event type if needed.
	 * @param event The event this command was created because of. 
	 * @param DISCORD_TARGET_CHANNEL The channel to talk in
	 * @param DISCORD_TARGET_GUILD The guild to talk in
	 * @param winner A WinningTeam enumerator representing the winner of the match being recorded. 
	 */
	public CommandMatchWin(GuildMessageReceivedEvent event, MessageChannel DISCORD_TARGET_CHANNEL,
			Guild DISCORD_TARGET_GUILD, WinningTeam winner) {
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
		this.winner = winner;
	}
	
    /**
     * Process the command to handle a resolved match. 
     * @param msg The command text. 
     * @param author The command's author. 
     * @param winner An enumerator representing which team RED/BLUE that won the match. 
     */
	@Override
	public void execute() {
		try {
	    	int matchID = Integer.valueOf(msgText.split(" ")[1]);
			Match.setMatchResult(matchID, winner, author);
		} catch(NumberFormatException nfe){
			DISCORD_TARGET_CHANNEL.sendMessage("Invalid Command: Bad Duration.").queue();
			return;
		} catch (Exception e) {
			DISCORD_TARGET_CHANNEL.sendMessage(e.getMessage()).queue();
		}
	}

}
