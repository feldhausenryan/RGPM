package command;

import mainpackage.Main;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * This command will set a new identifier used to establish the authority of a user.
 * To explain. A role will have a text name (ex "@admin"). One of these roles will need to be
 * made and then passed to this command to establish who is an admin of the bot. 
 * @author feldh
 *
 */
public class CommandSetAdminIdentifier extends Command {
	
	/** Generate a command from an event. This can be extended (probably)
	 * to any event type if needed.
	 * @param event The event this command was created because of. 
	 * @param DISCORD_TARGET_CHANNEL The channel to talk in
	 * @param DISCORD_TARGET_GUILD The guild to talk in
	 */
	public CommandSetAdminIdentifier(GuildMessageReceivedEvent event, MessageChannel DISCORD_TARGET_CHANNEL,
			Guild DISCORD_TARGET_GUILD) {
		super(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD);
	}
	
    /**
     * Changes the admin identifier to a set string. Admins are people with the role attached.
     * There isn't any error checking here. The instance will just crash if bad input is presented.
     * The rest of the program will continue running. (As written 7/5/2017).
     * @param msgText the command text. 
     */
	@Override
	public void execute() {
    	String identifier = msgText.split(" ")[1];
    	Main.DISCORD_ADMIN_IDENTIFIER = identifier;
    	Main.settingsDatabase.put(Main.STRING_DISCORD_ADMIN_IDENTIFIER, Main.DISCORD_ADMIN_IDENTIFIER);
    }

}
