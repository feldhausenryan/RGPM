package command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

/**
 * Command is an abstract class that represents the general execution structure of RGPM commands. 
 * Each command is parsed and then an instance of command is created and then executed once. 
 * @author feldh
 * 
 */
public abstract class Command {
	/**
	 * message contains information about the discord api-provided message. For example,
	 * it contains mentions, the text, when it was sent etc. 
	 */
	protected Message        message;
	/**
	 * channel contains information about the discord api-provided channel. It allows
	 * us to transmit messages etc. 
	 */
	protected MessageChannel channel;
	/**
	 * author contians information about the person who is created the event. Name,
	 * Discriminator (the #xxxxx numbers), Id, etc.
	 */
	protected User           author;
	/**
	 * guild is the discord server the event originated in. 
	 */
	protected Guild          guild;
	/**
	 * The raw text of the command's message.
	 */
	protected String         msgText;
	/**
	 * This is the channel for the discord bot to talk in. 
	 */
	protected MessageChannel DISCORD_TARGET_CHANNEL;
	/**
	 * This is the guild for the discord bot talk in. 
	 */
	protected Guild		     DISCORD_TARGET_GUILD;
	
	/** Generate a command from an event. This can be extended (probably)
	 * to any event type if needed.
	 * @param event The event this command was created because of. 
	 * @param DISCORD_TARGET_CHANNEL The channel to talk in
	 * @param DISCORD_TARGET_GUILD The guild to talk in
	 */
	public Command(GuildMessageReceivedEvent event, MessageChannel DISCORD_TARGET_CHANNEL, Guild DISCORD_TARGET_GUILD){
		this.message = event.getMessage();
		this.channel = event.getChannel();
		this.author  = event.getAuthor();
		this.guild   = event.getGuild();
		this.msgText = message.getContent();
		this.DISCORD_TARGET_CHANNEL = DISCORD_TARGET_CHANNEL;
		this.DISCORD_TARGET_GUILD = DISCORD_TARGET_GUILD;
	}
	
	/**
	 * Execute the command. (Do what the message says). 
	 */
	public abstract void execute();
	
}
