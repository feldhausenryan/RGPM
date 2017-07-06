package mainpackage;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import structures.AuthorityEnum;
import structures.WinningTeam;
import javax.security.auth.login.LoginException;

import command.CommandAddFakePlayers;
import command.CommandBan;
import command.CommandCommands;
import command.CommandHelp;
import command.CommandLeave;
import command.CommandMatchWin;
import command.CommandQueue;
import command.CommandRank;
import command.CommandRemove;
import command.CommandSetAdminIdentifier;
import command.CommandStatus;
import command.CommandTargetChannel;
import command.CommandUnban;

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
	public static final String STRING_DISCORD_TARGET_GUILD_ID   = "DISCORD_TARGET_GUILD_ID";
	public static final String STRING_DISCORD_TARGET_CHANNEL_ID = "DISCORD_TARGET_CHANNEL_ID";
	public static final String STRING_DISCORD_ADMIN_IDENTIFIER  = "DISCORD_ADMIN_IDENTIFIER";
	private static ReentrantLock lock = new ReentrantLock();
	public static Db settingsDatabase  = new Db(DB_NAME, "settingsDatabase", lock);
	public static Db playerDatabase    = new Db(DB_NAME, "playerDatabase", lock);
	public static Db matchDatabase     = new Db(DB_NAME, "matchDatabase", lock);
	public static Db nextMatchDatabase = new Db(DB_NAME, "nextMatchDatabase", lock);
	public static Db banDatabase       = new Db(DB_NAME, "banDatabase", lock);
	
	private static Queue mainQueue = new Queue();
	public static JDA jda;
	
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
            String discordTargetGuildId   = (String) settingsDatabase.getOrDefault(STRING_DISCORD_TARGET_GUILD_ID, "1");
            String discordTargetChannelId = (String) settingsDatabase.getOrDefault(STRING_DISCORD_TARGET_CHANNEL_ID, "1");
            DISCORD_ADMIN_IDENTIFIER      = (String) settingsDatabase.getOrDefault(STRING_DISCORD_ADMIN_IDENTIFIER, "@admin");
            
            // These might be null. Have to add checks in later in the process of getting messages to make sure 
            // These don't cause any problems. 
            DISCORD_TARGET_GUILD   = jda.getGuildById(discordTargetGuildId);
            if (DISCORD_TARGET_GUILD == null) DISCORD_TARGET_CHANNEL = null;
            else DISCORD_TARGET_CHANNEL = DISCORD_TARGET_GUILD.getTextChannelById(discordTargetChannelId);
            
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
        		new CommandTargetChannel(     event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD).execute();
    			break;
        	case("!rgpm_fake"):
        		new CommandAddFakePlayers(    event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD, mainQueue).execute();
        		break;
        	case("rgpm_admin_identifier"):
        		new CommandSetAdminIdentifier(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD).execute();
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
    		new CommandQueue(   event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD, mainQueue).execute();
    		break;
    	case("!leave"):
    		new CommandLeave(   event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD, mainQueue).execute();
    		break;
    	case("!status"):
    		new CommandStatus(  event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD, mainQueue).execute();
    		break;
    	case("!redwin"):
    		new CommandMatchWin(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD, WinningTeam.REDTEAM).execute();
    		break;
    	case("!bluewin"):
    		new CommandMatchWin(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD, WinningTeam.BLUETEAM).execute();
    		break;
    	case("!rank"):
    		new CommandRank(    event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD).execute();
    		break;
    	case("!commands"):
    		new CommandCommands(event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD).execute();
    		break;
    	case("!help"):
    		new CommandHelp(    event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD).execute();
    		break;
    	case("!ban"):
    		new CommandBan(     event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD, mainQueue).execute();
    		break;
    	case("!unban"):
    		new CommandUnban(   event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD).execute();
    		break;
    	case("!remove"):
    		new CommandRemove(  event, DISCORD_TARGET_CHANNEL, DISCORD_TARGET_GUILD, mainQueue).execute();
    		break;
    	}
    }
}
