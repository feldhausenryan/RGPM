package structures;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class MatchResult implements Serializable{
	/**
	 * An array list containing the members of the red team. 
	 */
	private final ArrayList<Player> redTeam;
	/**
	 * An array list containing the members of the blue team. 
	 */
	private final ArrayList<Player> blueTeam;
	/** 
	 * The date when the match was conducted.
	 */
	private final String date;
	/**
	 * The odds of of red team winning the match (ex 0.57). 
	 */
	private final double odds;
	/**
	 * True if red team won the match. 
	 */
	private final WinningTeam winner;
	/**
	 * The change in rating for the member's of the red team after this match. 
	 */
	private final int redChange;
	/**
	 * The change in rating for the member's of the blue team after this match. 
	 */
	private final int blueChange;
	/**
	 * 
	 */
	private final MatchResultStatusEnum recordStatus;
	
	/**
	 * Initialize a match result class that is stored in the mapdb database to record a match. The parameters of
	 * this class cannot be changed, as doing so would create bad side effects for the mapdb database. To change 
	 * parameters, generate a new match and replace the record of the old one. 
	 * @param redTeam An array list of players on the red team.
	 * @param blueTeam An array list of players on the blue team. 
	 * @param odds The odds of red team winning the match. (ex. 0.57)
	 * @param redWin A boolean that is true is red won the match
	 * @param redChange An integer representing the change in rating for members of the red team. 
	 * @param blueChange An integer representing the change in rating for memebers of the blue team. 
	 */
	public MatchResult(ArrayList<Player> redTeam, ArrayList<Player> blueTeam, double odds, WinningTeam winner, int redChange, int blueChange, MatchResultStatusEnum recordStatus){
		this.redTeam = redTeam;
		this.blueTeam = blueTeam;
		this.odds = odds;
		this.winner = winner;
		this.redChange = redChange;
		this.blueChange = blueChange;
		this.recordStatus = recordStatus;
		this.date = new Date().toString();
	}

	/**
	 * Included to allow mapdb to serialize this class. It is based off convention
	 * from the mapdb example code for custom classes. 
	 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchResult matchResult = (MatchResult) o;

        if (date != null ? !date.equals(matchResult.getDate()) : matchResult.getDate() != null) return false;
        if (winner != matchResult.getWinner()) return false;
        if (recordStatus != matchResult.recordStatus()) return false;
        if (redChange != matchResult.redChange) return false;
        if (blueChange != matchResult.blueChange) return false;
        if (redTeam != null ? !redTeam.equals(matchResult.getRedTeam()) : matchResult.getRedTeam() != null) return false;
        if (blueTeam != null ? !blueTeam.equals(matchResult.getBlueTeam()) : matchResult.getBlueTeam() != null) return false;

        return true;
    }
    
	/**
	 * @return the redTeam, an array list of players on the red team 
	 */
	public ArrayList<Player> getRedTeam() {
		return redTeam;
	}

	/**
	 * @return the blueTeam, an array list of players on the blue team
	 */
	public ArrayList<Player> getBlueTeam() {
		return blueTeam;
	}

	/**
	 * @return the odds of red team beating the blue team (ex 0.57)
	 */
	public double getOdds() {
		return odds;
	}

	/**
	 * @return the date the match was conducted represented as a string
	 */
	public String getDate() {
		return date;
	}

	/**
	 * @return true if red team won, false if blue team won
	 */
	public WinningTeam getWinner() {
		return winner;
	}

	/**
	 * @return true if the match was recorded or just placed into the database. 
	 */
	public MatchResultStatusEnum recordStatus() {
		return recordStatus;
	}

	/**
	 * @return the change in red team's ratings after the match
	 */
	public int getRedChange() {
		return redChange;
	}

	/**
	 * @return the change in blue team's ratings after the match
	 */
	public int getBlueChange() {
		return blueChange;
	}

}
