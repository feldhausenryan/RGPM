package mainpackage;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.impl.JDAImpl;
import net.dv8tion.jda.core.entities.impl.UserImpl;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import structures.AuthorityEnum;
import structures.Player;
import structures.WinningTeam;
import javax.security.auth.login.LoginException;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import mapdb.Db;

import static java.util.concurrent.TimeUnit.*;

public class Main extends ListenerAdapter
{
	public static final int STARTING_U = 1500;
	public static final int STARTING_SIGMA = 100;
	public static final int K_VALUE = 50;
	public static final int CLEAN_QUEUE_INTERVAL_SECONDS = 10;
	public static final int SAVE_DATABASE_INTERVAL_SECONDS = 60;
	public static final String DISCORD_BOT_TOKEN = "MzE3NTE2NTI4OTc3NDQ0ODY2.DAlMbw.BqZN-e-Tiwo0dDKIhLEHRmnKTvo";
	public static final String DISCORD_BOT_OWNER_PREFIX = "Ryan";
	public static final String DISCORD_BOT_OWNER_SUFFIX = "9411";
	
	private static final String DB_NAME = "RGPM_DB";
	private static final String STRING_DISCORD_TARGET_GUILD_ID   = "DISCORD_TARGET_GUILD_ID";
	private static final String STRING_DISCORD_TARGET_CHANNEL_ID = "DISCORD_TARGET_CHANNEL_ID";
	private static final String STRING_DISCORD_ADMIN_IDENTIFIER  = "DISCORD_ADMIN_IDENTIFIER";
	private static ReentrantLock lock = new ReentrantLock();
	public static Db settingsDatabase  = new Db(DB_NAME, "settingsDatabase", lock);
	public static Db playerDatabase    = new Db(DB_NAME, "playerDatabase", lock);
	public static Db matchDatabase     = new Db(DB_NAME, "matchDatabase", lock);
	public static Db nextMatchDatabase = new Db(DB_NAME, "nextMatchDatabase", lock);
	public static Db banDatabase       = new Db(DB_NAME, "banDatabase", lock);
	
	private static Queue mainQueue = new Queue();
	private static JDA jda;
	
	public static Guild          DISCORD_TARGET_GUILD;
	public static MessageChannel DISCORD_TARGET_CHANNEL;
	public static String         DISCORD_ADMIN_IDENTIFIER;
	
    private final static ScheduledExecutorService queueCleanSchedule = Executors.newScheduledThreadPool(1);
    private final static ScheduledExecutorService dbCommitSchedule   = Executors.newScheduledThreadPool(1);
	
	/**
	 * Most of the code within the main function can be found and better understood through the 
	 * example code provided as a part of the JDA documentation. 
	 * @param args
	 */
    public static void main(String[] args)
    {    	
        try
        {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(DISCORD_BOT_TOKEN)   //The token of the account that is logging in.
                    .addEventListener(new Main())  //An instance of a class that will handle events.
                    .buildBlocking();              //There are 2 ways to login, blocking vs async. 
            									   //Blocking guarantees that JDA will be completely loaded.
            
            if(jda.getGuilds().size() == 0) throw new Exception("NoGuildsException");
            
            
            // Grab the guild and channel ID from the database. It is stored this way so it can be changed
            // through commands in discord. Grab the admin identifier. 
            String discordTargetGuildId   = (String) settingsDatabase.getOrDefault(STRING_DISCORD_TARGET_GUILD_ID, "");
            String discordTargetChannelId = (String) settingsDatabase.getOrDefault(STRING_DISCORD_TARGET_CHANNEL_ID, "");
            DISCORD_ADMIN_IDENTIFIER      = (String) settingsDatabase.getOrDefault(STRING_DISCORD_ADMIN_IDENTIFIER, "@admin");
            
            // These might be null. Have to add checks in later in the process of getting messages to make sure 
            // These don't cause any problems. 
            DISCORD_TARGET_GUILD = jda.getGuildById(discordTargetGuildId);
            DISCORD_TARGET_CHANNEL = DISCORD_TARGET_GUILD.getTextChannelById(discordTargetChannelId);
            
            // Every 10s look through the queue and remove inactive / idle players. 
            initiateQueueCleaning();
            // Every 60s save the database. It will last even through a crash, assuming the database 
            // file itself isn't lost. 
            initiateDbCommitting();
        }
        catch (LoginException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (RateLimitedException e) {
            e.printStackTrace();
        } catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * This runnable periodically removes bad players from the queue.
     */
    public static void initiateQueueCleaning() {
        final Runnable cleanQueue = new Runnable() {
            public void run() {
            	mainQueue.cleanQueue();
            }
        };
        queueCleanSchedule.scheduleAtFixedRate(cleanQueue, CLEAN_QUEUE_INTERVAL_SECONDS, CLEAN_QUEUE_INTERVAL_SECONDS, SECONDS);
    }
    
    /**
     * This runnable periodically saves the database. 
     */
    public static void initiateDbCommitting(){
    	final Runnable dbCommit = new Runnable() {
    		public void run() {
    			Main.playerDatabase.commit();
    		}
    	};
    	dbCommitSchedule.scheduleAtFixedRate(dbCommit, SAVE_DATABASE_INTERVAL_SECONDS, SAVE_DATABASE_INTERVAL_SECONDS, SECONDS);
    }
    
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event){
    	// JDA provided information from each event.
    	Message        message = event.getMessage();
    	MessageChannel channel = event.getChannel();
    	User           author  = event.getAuthor();
    	Guild          guild   = event.getGuild();
    	
    	// Message text and the first argument of the text. 
    	String msgText   = message.getContent();
    	String msgPrefix = msgText.split(" ")[0];
    	
    	// Developer commands. This can move the bot responses from server to server and from channel to channel.
    	if (author.getName().equals(DISCORD_BOT_OWNER_PREFIX) && author.getDiscriminator().equals(DISCORD_BOT_OWNER_SUFFIX)){
    		switch(msgPrefix){
    		case("!rgpm_target_channel"):
    			targetChannel(channel, guild);
    			break;
        	case("!rgpm_fake"):
        		commandFake();
        		break;
        	case("rgpm_admin_identifier"):
        		setAdminIdentifier(msgText);
        		break;
    		}
    	}
    	
    	// Return if the guild or channel is invalid. We don't want to crash. Just wait until the owner directs the bot to the 
    	// correct server. Ignore bot messages.
    	if (DISCORD_TARGET_GUILD == null || DISCORD_TARGET_CHANNEL == null || author.isBot()) return;
    	// Return if the message is not in the correct channel or guild.
    	if (!DISCORD_TARGET_GUILD.getId().equals(guild.getId()))     return;
    	if (!DISCORD_TARGET_CHANNEL.getId().equals(channel.getId())) return;
    	
    	if (((Boolean) banDatabase.getOrDefault(author.getId(), false))){
        	if(Match.getUserAuthority(author, guild) == AuthorityEnum.ADMIN || Match.getUserAuthority(author, guild) == AuthorityEnum.OWNER);
        	else{
        		channel.sendMessageFormat("Invalid Command: %s is banned.", author.getName()).queue();
        		return;
        	}
    	}
    	                                   		
    	switch(msgPrefix){
    	case("!queue"):
    		commandQueue(msgText, author);
    		break;
    	case("!leave"):
    		commandLeave(author);
    		break;
    	case("!status"):
    		commandStatus();
    		break;
    	case("!redwin"):
    		commandMatchWin(msgText, author, WinningTeam.REDTEAM);
    		break;
    	case("!bluewin"):
    		commandMatchWin(msgText, author, WinningTeam.BLUETEAM);
    		break;
    	case("!rank"):
    		commandRank(message, author);
    		break;
    	case("!commands"):
    		commandCommands();
    		break;
    	case("!help"):
    		commandHelp();
    		break;
    	case("!ban"):
    		commandBan(message, author);
    		break;
    	case("!unban"):
    		commandUnban(message, author);
    		break;
    	case("!remove"):
    		commandRemove(message, author);
    		break;
    	}
    }
   
    /**
     * Changes parameters in the database and directs the bot to talk and receive messages
     * from the provided channel and guild. The channel needs to be in the guild. 
     * @param channel The channel to send the messages to.
     * @param guild The guild to send the messages to. 
     */
    private void targetChannel(MessageChannel channel, Guild guild){
    	DISCORD_TARGET_GUILD = guild;
    	DISCORD_TARGET_CHANNEL = channel;
    	settingsDatabase.put(STRING_DISCORD_TARGET_GUILD_ID, DISCORD_TARGET_GUILD);
    	settingsDatabase.put(STRING_DISCORD_TARGET_CHANNEL_ID, DISCORD_TARGET_CHANNEL);
    	DISCORD_TARGET_CHANNEL.sendMessageFormat("RGPM Directed to %s, %s",
    			DISCORD_TARGET_GUILD.getName(), DISCORD_TARGET_CHANNEL.getName());
    }
    
    /**
     * Changes the admin identifier to a set string. Admins are people with the role attached.
     * There isn't any error checking here. The instance will just crash if bad input is presented.
     * The rest of the program will continue running. (As written 7/5/2017).
     * @param msgText the command text. 
     */
    private void setAdminIdentifier(String msgText){
    	String identifier = msgText.split(" ")[1];
    	DISCORD_ADMIN_IDENTIFIER = identifier;
    	settingsDatabase.put(STRING_DISCORD_ADMIN_IDENTIFIER, DISCORD_ADMIN_IDENTIFIER);
    }
    
    /**
     * Executes the command to add a player to the matchmaker queue. 
     * @param msgText The text of the command. 
     * @param author The author of the command. 
     */
    private void commandQueue(String msgText, User author){
    	// Split up the queue message, confirm it obeys proper convention. !queue DURATION
    	int minDuration = 0;
    	// Split up the message and grab the second command. 
    	String[] messageArgs = msgText.split(" ");
    	// Quick check to make sure there IS a second command. 
    	if (messageArgs.length != 2){
    		DISCORD_TARGET_CHANNEL.sendMessage("Invalid Command: '!queue' takes one argument.").queue();
    		return;
    	}
    	// Try to convert it to a number. Range it to a reasonable size. 
		try{
			minDuration = Integer.valueOf(messageArgs[1]);
	    	if (minDuration < 0)   minDuration = 30;
	    	if (minDuration > 180) minDuration = 180;
		}
		catch(NumberFormatException nfe){
			DISCORD_TARGET_CHANNEL.sendMessage("Invalid Command: Bad Duration.").queue();
			return;
		}
		
		// RGPM uses nanoTime to determine how long to keep people in queue. It runs a service that
		// checks every 10s to remove players who have been in queue too long. 
    	long queueTime = System.nanoTime() + ((long)minDuration) * (long) 60e9;
    	
    	// Don't add DND/Invis/Offline/Unknown players into the queue.
    	switch(DISCORD_TARGET_GUILD.getMember(author).getOnlineStatus()){
		case DO_NOT_DISTURB:
			mainQueue.removePlayerFromQueue(author);
			DISCORD_TARGET_CHANNEL.sendMessageFormat("%s not added to queue. Status is DO_NOT_DISTURB.", author.getName()).queue();
			return;
		case INVISIBLE:
			mainQueue.removePlayerFromQueue(author);
			DISCORD_TARGET_CHANNEL.sendMessageFormat("%s not added to queue. Status is INVISIBLE.", author.getName()).queue();
			return;
		case OFFLINE:
			mainQueue.removePlayerFromQueue(author);
			DISCORD_TARGET_CHANNEL.sendMessageFormat("%s not added to queue. Status is OFFLINE.", author.getName()).queue();
			return;
		case UNKNOWN:
			mainQueue.removePlayerFromQueue(author);
			DISCORD_TARGET_CHANNEL.sendMessageFormat("%s not added to queue. Status is UNKNOWN.", author.getName()).queue();
			return;
		default:
			break;
    	}
    	// Add the player to queue and print out a message including the number of players in queue. 
    	QueuedPlayer newPlayer = new QueuedPlayer(author, queueTime);
    	if(mainQueue.addPlayerToQueue(newPlayer)){
    		DISCORD_TARGET_CHANNEL.sendMessageFormat("User %s added to queue for %d minutes. (%d/10)", author.getName(), minDuration, mainQueue.getQueueLength()).queue();
    	}
    	else{
    		DISCORD_TARGET_CHANNEL.sendMessageFormat("User %s updated queue time to %d minutes. (%d/10)",  author.getName(), minDuration, mainQueue.getQueueLength()).queue();
    	}
    }
    
    /**
     * Process the command to have a user leave the queue. 
     * @param author
     */
    private void commandLeave(User author){
    	if(mainQueue.removePlayerFromQueue(author)){
    		DISCORD_TARGET_CHANNEL.sendMessageFormat("User %s removed from queue. (%d/10)", author.getName(), mainQueue.getQueueLength()).queue();
    	}else{
    		DISCORD_TARGET_CHANNEL.sendMessageFormat("User %s was not in queue.", author.getName()).queue();
    	}
	}
    
    /**
     * Process the command to ask for the status of the queue. 
     */
    private void commandStatus(){
    	mainQueue.broadcastStatus();
    }
    
    /**
     * Process the owner-only command to generate 9 fake players and inject them into the queue. 
     * THIS WILL BE DELETED SOON. DELETE ME TEMP TODO 
     */
    private void commandFake(){
    	int minuteDuration = 30;
    	long queueTime = System.nanoTime() + ((long)minuteDuration) * (long) 60e9;
    	
    	UserImpl f1 = new UserImpl(1, (JDAImpl) jda);
    	UserImpl f2 = new UserImpl(2, (JDAImpl) jda);
    	UserImpl f3 = new UserImpl(3, (JDAImpl) jda);
    	UserImpl f4 = new UserImpl(4, (JDAImpl) jda);
    	UserImpl f5 = new UserImpl(5, (JDAImpl) jda);
    	UserImpl f6 = new UserImpl(6, (JDAImpl) jda);
    	UserImpl f7 = new UserImpl(7, (JDAImpl) jda);
    	UserImpl f8 = new UserImpl(8, (JDAImpl) jda);
    	UserImpl f9 = new UserImpl(9, (JDAImpl) jda);
    	
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
    	mainQueue.addPlayerToQueue(fq1);
    	QueuedPlayer fq2 = new QueuedPlayer(f2, queueTime);
    	mainQueue.addPlayerToQueue(fq2);
    	QueuedPlayer fq3 = new QueuedPlayer(f3, queueTime);
    	mainQueue.addPlayerToQueue(fq3);
    	QueuedPlayer fq4 = new QueuedPlayer(f4, queueTime);
    	mainQueue.addPlayerToQueue(fq4);
    	QueuedPlayer fq5 = new QueuedPlayer(f5, queueTime);
    	mainQueue.addPlayerToQueue(fq5);
    	QueuedPlayer fq6 = new QueuedPlayer(f6, queueTime);
    	mainQueue.addPlayerToQueue(fq6);
    	QueuedPlayer fq7 = new QueuedPlayer(f7, queueTime);
    	mainQueue.addPlayerToQueue(fq7);
    	QueuedPlayer fq8 = new QueuedPlayer(f8, queueTime);
    	mainQueue.addPlayerToQueue(fq8);
    	QueuedPlayer fq9 = new QueuedPlayer(f9, queueTime);
    	mainQueue.addPlayerToQueue(fq9);
    }
    
    /**
     * Process the command to handle a resolved match. 
     * @param msg The command text. 
     * @param author The command's author. 
     * @param winner An enumerator representing which team RED/BLUE that won the match. 
     */
    private void commandMatchWin(String msgText, User author, WinningTeam winner){
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
    
    /**
     * Process the command to handle a request for player(s) ranks
     * @param msg The jda message
     * @param author The author of the jda message
     */
    private void commandRank(Message message, User author){
    	for(User user : message.getMentionedUsers()){
    		if (playerDatabase.containsKey(user.getId())){
	    		Player p = (Player) playerDatabase.get(user.getId());
	    		DISCORD_TARGET_CHANNEL.sendMessageFormat("%s has a ranking of %d", p.getName(), p.getU()).queue();
    		}
    		else{
    			DISCORD_TARGET_CHANNEL.sendMessageFormat("%s is not in the database", user.getName()).queue();
    		}
    	}
    }
    
    /**
     * Process the command to display what commands are available. 
     */
    private void commandCommands(){
    	DISCORD_TARGET_CHANNEL.sendMessage("Commands:\n"
    					  + "!queue <duration-minutes> | queues single person solo for x minutes\n"+
    						"!leave | removes the player from queue\n"+
    						"!status | returns the number of players in queue\n"+
    						"!redwin <match-id> | resolves a match as Red Team Win\n"+
    						"!bluewin <match-id> | resolves a match as Blue Team Wwin\n"+
    						"!rank | returns mentioned players' ranks\n"+
    						"!ban | bans mentioned players\n"+
    						"!unban | unbans mentioned players\n"+
    						"!remove | removes mentioned players from the DB").queue();
    }
    
    /**
     * Process the command to display help information. 
     */
    private void commandHelp(){
    	DISCORD_TARGET_CHANNEL.sendMessage("RGPM is a discord bot that provides basic matchmaking for GW2 inhouses. "
    			+ "It provides an alternative to team-captain style matchmaking."
    			+ "Use !commands for a list of commands.").queue();
    }
   
    /**
     * Process the command to ban a user. 
     * @param msg The jda message
     * @param author The author of the jda message
     */
    private void commandBan(Message message, User author){
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
	    		banDatabase.put(id, true);
	    		mainQueue.removePlayerFromQueue(user);
	    	}
    	}
    }
    
    /**
     * Process the command to unban a user. 
     * @param message The jda message
     * @param author The author of the jda message
     */
    private void commandUnban(Message message, User author){
    	if(Match.getUserAuthority(author, DISCORD_TARGET_GUILD) == AuthorityEnum.ADMIN || 
    			Match.getUserAuthority(author, DISCORD_TARGET_GUILD) == AuthorityEnum.OWNER){
    		List<User> usersToUnban = message.getMentionedUsers();
    		for (User user : usersToUnban){
	        	if(Match.getUserAuthority(user, DISCORD_TARGET_GUILD) == AuthorityEnum.ADMIN || 
	        			Match.getUserAuthority(user, DISCORD_TARGET_GUILD) == AuthorityEnum.OWNER){
	        		DISCORD_TARGET_CHANNEL.sendMessageFormat("Invalid Command: %s is an admin or owner", user.getName()).queue();
	        		return;
	        	}
    			String id = user.getId();
    			DISCORD_TARGET_CHANNEL.sendMessageFormat("Unbanning user %s from queuing.", user.getName()).queue();
    			banDatabase.put(id, false);
    		}
    	}
    }
    
    /**
     * Process the command to remove a user from the databse
     * @param message The jda message
     * @param author The author of the jda message
     */
    private void commandRemove(Message message, User author){
    	if(Match.getUserAuthority(author, DISCORD_TARGET_GUILD) == AuthorityEnum.OWNER){
    		List<User> usersToRemove = message.getMentionedUsers();
    		for (User user : usersToRemove){
	        	if(Match.getUserAuthority(user, DISCORD_TARGET_GUILD) == AuthorityEnum.ADMIN || 
	        			Match.getUserAuthority(user, DISCORD_TARGET_GUILD) == AuthorityEnum.OWNER){
	        		DISCORD_TARGET_CHANNEL.sendMessageFormat("Invalid Command: %s is an admin or owner", user.getName()).queue();
	        		return;
	        	}
    			String id = user.getId();
    			DISCORD_TARGET_CHANNEL.sendMessageFormat("Removing user %s from database", user.getName()).queue();
    			playerDatabase.remove(id);
	    		mainQueue.removePlayerFromQueue(user);
    		}
    	}
    }
}
