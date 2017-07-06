package mainpackage;
import java.util.ArrayList;
import java.util.List;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import structures.AuthorityEnum;
import structures.MatchResult;
import structures.MatchResultStatusEnum;
import structures.Player;
import structures.WinningTeam;

public class Match {
	List<QueuedPlayer> queue;
	ArrayList<Player> matchPlayers;
	
	public Match(List<QueuedPlayer> list){
		this.queue = list;
		this.matchPlayers = new ArrayList<Player>();
		
		// Convert the 10 players in the queue to 10 players of a different class.
		// This class runs off a persistent database and contains matchmaking 
		// information for each player. 
		for(QueuedPlayer player : list){
			User user = player.getUser();
			String userId = user.getId();
			String userName = user.getName();
			Player p;
			// This player is already in the database
			if(Main.playerDatabase.containsKey(userId)){
				p = (Player) Main.playerDatabase.get(userId);
			}
			// This player is not in the database
			else{
				p = new Player(userId, userName);
				Main.playerDatabase.put(p.getId(), p);
			}
			matchPlayers.add(p);
		}
		
		// At this point matchPlayers is populated by 10 unique players 
		// with associated ratings/variances/k-constants/names/ids
		// Now we seek to find the optimal arrangement of these players
		// by passing them through a matchmaker. 
		
		// This is where the code loses some efficiency
		
		// Grab every team combination. 
		ArrayList<ArrayList<Player>> combinations = getCombinations(matchPlayers, -1, 0);
		int combSize = combinations.size();
		ArrayList<Player> bestRed = combinations.get(0);
		ArrayList<Player> bestBlue = combinations.get(combSize - 1);
		double best = 1; //Can't be zero. The goal is actually for this best to approach zero. 
		double probABeatsB = 0;
		for (int x = 0; x < combSize / 2; x++){
			ArrayList<Player> redTeam = combinations.get(x);
			ArrayList<Player> blueTeam = combinations.get(combSize - 1 - x);
			probABeatsB = probTeamABeatsTeamB(redTeam, blueTeam);
			if(Math.abs(probABeatsB - 0.5) < best){
				bestRed = redTeam;
				bestBlue = blueTeam;
				best = Math.abs(probABeatsB - 0.5);
			}
		}
		probABeatsB = probTeamABeatsTeamB(bestRed, bestBlue);

		MatchResult match = new MatchResult(bestRed, bestBlue, probABeatsB, WinningTeam.UNKNOWN, 0, 0, MatchResultStatusEnum.UNRECORDED);
		int matchId = (Integer) Main.nextMatchDatabase.getOrDefault("matchCount", 0) + 1;
		Main.nextMatchDatabase.put("matchCount", matchId);
		Main.matchDatabase.put(matchId, match);

		// Now we have the best possible match. Time to publish it.
		Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("MATCH\n"
				+ "Red Team: <@%s>(%d) - <@%s>(%d) - <@%s>(%d) - <@%s>(%d) - <@%s>(%d)\n"
				+ "Blue Team: <@%s>(%d) - <@%s>(%d) - <@%s>(%d) - <@%s>(%d) - <@%s>(%d)\n"
				+ "Estimated Prob A beats B (%.2f)\n"
				+ "Report as !redwin %d or !bluewin %d", 
				bestRed.get(0).getId(), bestRed.get(0).getU(),
				bestRed.get(1).getId(), bestRed.get(1).getU(),
				bestRed.get(2).getId(), bestRed.get(2).getU(),
				bestRed.get(3).getId(), bestRed.get(3).getU(),
				bestRed.get(4).getId(), bestRed.get(4).getU(),
				bestBlue.get(0).getId(), bestBlue.get(0).getU(),
				bestBlue.get(1).getId(), bestBlue.get(1).getU(),
				bestBlue.get(2).getId(), bestBlue.get(2).getU(),
				bestBlue.get(3).getId(), bestBlue.get(3).getU(),
				bestBlue.get(4).getId(), bestBlue.get(4).getU(), probABeatsB*100., matchId, matchId).queue();
	}
	
	public double probTeamABeatsTeamB(ArrayList<Player> A, ArrayList<Player> B){
		double uA = 0;
		double uB = 0;
		double sigmaA = 0;
		double sigmaB = 0;
		
		for(Player p: A){
			uA += p.getU();
			sigmaA += p.getSigma();
		}
		
		for(Player p: B){
			uB += p.getU();
			sigmaB += p.getSigma();
		}
		
		double prob = 1 - CNDF(-1.0*(uA - uB) / (sigmaA + sigmaB));
		return prob;
	}
	
	/**
	 * Through some miracle this function actually works. It takes a
	 * list and as long as the number of elements of it is even, it 
	 * returns every combination of half the elements in efficiently 
	 * and recursively. 
	 * 
	 * ex. Input = [1, 2, 3, 4]
	 *     Output = [[1,2],[1,3],[1,4],[2,3],[2,4],[3,4]]
	 * @param a The list
	 * @param prev Set to -1
	 * @param depth Set to 0
	 * @return A list of every of every combination of the input. 
	 */
	private static ArrayList<ArrayList<Player>> getCombinations(ArrayList<Player> a, int prev, int depth){
		if (depth > ((a.size() / 2) - 1)){
			ArrayList<ArrayList<Player>> newList = new ArrayList<ArrayList<Player>>();
			ArrayList<Player> newSubList = new ArrayList<Player>();
			newList.add(newSubList);
			return newList;
		}
		ArrayList<ArrayList<Player>> retVal = new ArrayList<ArrayList<Player>>();
		for(int index = prev + 1; index < ((a.size() / 2) + 1) + depth; index++){
			ArrayList<ArrayList<Player>> combs = getCombinations(a, index, depth + 1);
			for(ArrayList<Player> ali: combs){
				ali.add(a.get(index));
				retVal.add(ali);
			}
		}
		return retVal;
	}
	
	/** 
	 * The CDF of N(0,1)
	 * @param x A double representing the point to evaluate the CDF of N(0,1) at
	 * @return The cumulative normal distribution function of N(0,1) at point x
	 */
	private double CNDF(double x)
	{
	    int neg = (x < 0d) ? 1 : 0;
	    if ( neg == 1) 
	        x *= -1d;

	    double k = (1d / ( 1d + 0.2316419 * x));
	    double y = (((( 1.330274429 * k - 1.821255978) * k + 1.781477937) *
	                   k - 0.356563782) * k + 0.319381530) * k;
	    y = 1.0 - 0.398942280401 * Math.exp(-0.5 * x * x) * y;

	    return (1d - neg) * y + neg * (1d - y);
	}
	

	public static AuthorityEnum getUserAuthority(User user, Guild guild){
		// The owner is the person defined in the constants in Main. In other words, its who is reading this ;)
		if (user.getName().equals(Main.DISCORD_BOT_OWNER_PREFIX) && 
			user.getDiscriminator().equals(Main.DISCORD_BOT_OWNER_SUFFIX))      return AuthorityEnum.OWNER;
		// Admins are defined by an identifier role specificed through an owner command. Default == @admin.
		// But first have to check to see if there is a role stated by the identifier.
		if (guild.getRolesByName(Main.DISCORD_ADMIN_IDENTIFIER, false).size() == 0){
			Main.DISCORD_TARGET_CHANNEL.sendMessage("WARNING: There is no admin role set.").queue();
		}
		if (guild.getMember(user).getRoles().contains(
		    guild.getRolesByName(Main.DISCORD_ADMIN_IDENTIFIER, false).get(0))) return AuthorityEnum.ADMIN;
		return AuthorityEnum.NORMAL;
	}
	
	/**
	 * Compares two authorities. Checks to see if newAuthority is greater than oldAuthority.
	 * @param newAuthority
	 * @param oldAuthority
	 * @return
	 */
	public static boolean greaterAuthority(AuthorityEnum newAuthority, AuthorityEnum oldAuthority){
		switch(oldAuthority){
		case OWNER:
			if(newAuthority == AuthorityEnum.OWNER) return true;
			else return false;
		case ADMIN:
			if(newAuthority == AuthorityEnum.OWNER) return true;
			else return false;
		case NORMAL:
			if(newAuthority == AuthorityEnum.OWNER || newAuthority == AuthorityEnum.ADMIN) return true;
			else return false;
		case UNKNOWN:
			if(oldAuthority == AuthorityEnum.UNKNOWN) return false;
			else return true;
		}
		return false;
	}
	
	
	private static MatchResult recordMatchResult(int matchID, WinningTeam winner, MatchResult match, MatchResultStatusEnum recordingStatus){
		int redChange  = 0;
		int blueChange = 0;
		double odds = match.getOdds();
		
		switch(winner){
		case REDTEAM:
			redChange  = Main.K_VALUE;
			blueChange = -1*((int) (((1.0 - odds) / odds) * Main.K_VALUE));
			break;
		case BLUETEAM:
			redChange  = -1*((int) ((odds / (1.0 - odds)) * Main.K_VALUE));
			blueChange = Main.K_VALUE;
			break;
		case UNKNOWN:
			break;
		}

		ArrayList<Player> redTeam = match.getRedTeam();
		ArrayList<Player> blueTeam = match.getBlueTeam();
		
		for(Player redPlayer : redTeam){
			Player newRating = new Player(redPlayer.getId(), redPlayer.getName(), redPlayer.getU() + redChange, redPlayer.getSigma());
			Main.playerDatabase.put(redPlayer.getId(), newRating);
		}
		for(Player bluePlayer : blueTeam){
			Player newRating = new Player(bluePlayer.getId(), bluePlayer.getName(), bluePlayer.getU() + blueChange, bluePlayer.getSigma());
			Main.playerDatabase.put(bluePlayer.getId(), newRating);
		}
		
		MatchResult matchFinal = new MatchResult(redTeam, blueTeam, odds, winner, redChange, blueChange, recordingStatus);
		Main.matchDatabase.put(matchID, matchFinal);
		return matchFinal;
	}
	
	private static MatchResult revertRatingChange(int matchID, MatchResult match){
		int revertedRedOffset  = -match.getRedChange();
		int revertedBlueOffset = -match.getBlueChange();

		ArrayList<Player> redTeam = match.getRedTeam();
		ArrayList<Player> blueTeam = match.getBlueTeam();
		
		for(Player redPlayer : redTeam){
			Player newRating = new Player(redPlayer.getId(), redPlayer.getName(), redPlayer.getU() + revertedRedOffset, redPlayer.getSigma());
			Main.playerDatabase.put(redPlayer.getId(), newRating);
		}
		for(Player bluePlayer : blueTeam){
			Player newRating = new Player(bluePlayer.getId(), bluePlayer.getName(), bluePlayer.getU() + revertedBlueOffset, bluePlayer.getSigma());
			Main.playerDatabase.put(bluePlayer.getId(), newRating);
		}

		MatchResult matchFinal = new MatchResult(redTeam, blueTeam, match.getOdds(), match.getWinner(), match.getRedChange(), match.getBlueChange(), MatchResultStatusEnum.UNKNOWN);
		Main.matchDatabase.put(matchID, matchFinal);
		return matchFinal;
	}
	
	public static boolean setMatchResult(int matchID, WinningTeam winner, User user) throws Exception{
		if(Main.matchDatabase.containsKey(matchID)){
			MatchResult match = (MatchResult) Main.matchDatabase.get(matchID);
			AuthorityEnum userAuthority = getUserAuthority(user, Main.DISCORD_TARGET_GUILD);
			switch (match.recordStatus()){
			case UNRECORDED:
				switch(userAuthority){
				case OWNER:
					match = recordMatchResult(matchID, winner, match, MatchResultStatusEnum.OWNER_RECORDED);
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("RedTeam %d | BlueTeam %d", match.getRedChange(), match.getBlueChange()).queue();
					return true;
				case ADMIN:
					match = recordMatchResult(matchID, winner, match, MatchResultStatusEnum.ADMIN_RECORDED);
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("RedTeam %d | BlueTeam %d", match.getRedChange(), match.getBlueChange()).queue();
					return true;
				case NORMAL:
					match = recordMatchResult(matchID, winner, match, MatchResultStatusEnum.NORMAL_RECORDED);
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("RedTeam %d | BlueTeam %d", match.getRedChange(), match.getBlueChange()).queue();
					return true;
				case UNKNOWN:
					Exception e = new Exception("setMatchResult Failed: userAuthority == UNKOWN");
					throw e;
				}
			case NORMAL_RECORDED:
				switch(userAuthority){
				case OWNER:
					match = revertRatingChange(matchID, match);
					match = recordMatchResult(matchID, winner, match, MatchResultStatusEnum.OWNER_RECORDED);
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%d Changed by Owner. RedTeam %d | BlueTeam %d", matchID, match.getRedChange(), match.getBlueChange()).queue();
					return true;
				case ADMIN:
					match = revertRatingChange(matchID, match);
					match = recordMatchResult(matchID, winner, match, MatchResultStatusEnum.ADMIN_RECORDED);
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%d Changed by Admin. RedTeam %d | BlueTeam %d", matchID, match.getRedChange(), match.getBlueChange()).queue();
					return true;
				case NORMAL:
					match = revertRatingChange(matchID, match);
					match = recordMatchResult(matchID, WinningTeam.UNKNOWN, match, MatchResultStatusEnum.NORMAL_CONTESTED);
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%d Contested by %s", matchID, user.getName()).queue();
					return true;
				case UNKNOWN:
					Exception e = new Exception("setMatchResult Failed: userAuthority == UNKOWN");
					throw e;
				}
			case NORMAL_CONTESTED:
				switch(userAuthority){
				case OWNER:
					match = revertRatingChange(matchID, match);
					match = recordMatchResult(matchID, winner, match, MatchResultStatusEnum.OWNER_RECORDED);
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%d Resolved by Owner. RedTeam %d | BlueTeam %d", matchID, match.getRedChange(), match.getBlueChange()).queue();
					return true;
				case ADMIN:
					match = revertRatingChange(matchID, match);
					match = recordMatchResult(matchID, winner, match, MatchResultStatusEnum.ADMIN_RECORDED);
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%d Resolved by Admin. RedTeam %d | BlueTeam %d", matchID, match.getRedChange(), match.getBlueChange()).queue();
					return true;
				case NORMAL:
					Exception e = new Exception("setMatchResult Failed: match already contested");
					throw e;
				case UNKNOWN:
					Exception e1 = new Exception("setMatchResult Failed: userAuthority == UNKOWN");
					throw e1;
				}
			case ADMIN_RECORDED:
				switch(userAuthority){
				case OWNER:
					match = revertRatingChange(matchID, match);
					match = recordMatchResult(matchID, winner, match, MatchResultStatusEnum.OWNER_RECORDED);
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%d Changed by Owner. RedTeam %d | BlueTeam %d", matchID, match.getRedChange(), match.getBlueChange()).queue();
					return true;
				case ADMIN:
					Exception e = new Exception("setMatchResult Failed: userAuthority ADMIN cannot supersede ADMIN");
					throw e;
				case NORMAL:
					Exception e1 = new Exception("setMatchResult Failed: userAuthority ADMIN supersedes NORMAL");
					throw e1;
				case UNKNOWN:
					Exception e2 = new Exception("setMatchResult Failed: userAuthority == UNKOWN");
					throw e2;
				}
			case OWNER_RECORDED:
				switch(userAuthority){
				case OWNER:
					match = revertRatingChange(matchID, match);
					match = recordMatchResult(matchID, winner, match, MatchResultStatusEnum.OWNER_RECORDED);
					Main.DISCORD_TARGET_CHANNEL.sendMessageFormat("%d Changed by Owner. RedTeam %d | BlueTeam %d", matchID, match.getRedChange(), match.getBlueChange()).queue();
					return true;
				case ADMIN:
					Exception e = new Exception("setMatchResult Failed: userAuthority OWNER supersedes ADMIN");
					throw e;
				case NORMAL:
					Exception e1 = new Exception("setMatchResult Failed: userAuthority OWNER supersedes NORMAL");
					throw e1;
				case UNKNOWN:
					Exception e2 = new Exception("setMatchResult Failed: userAuthority == UNKOWN");
					throw e2;
				}
			case UNKNOWN:
				Exception e = new Exception("setMatchResult Failed: recordStatus == UNKOWN");
				throw e;
			}
		}
		else{
			Exception e = new Exception("setMatchResult Failed: match not found in database");
			throw e;
		}
		Exception e = new Exception("setMatchResult ERROR: CONTACT THE DEVELOPER IF YOU SEE THIS");
		throw e;
	}
}
