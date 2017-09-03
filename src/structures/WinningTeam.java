package structures;

/**
 * Winning team specifies that a match was won by red or blue, or was contested. 
 * @author feldh
 *
 */
public enum WinningTeam {
	/**
	 * Red team won the match
	 */
	REDTEAM,
	/**
	 * Blue team won the match
	 */
	BLUETEAM,
	/**
	 * Contested or unknown match result
	 */
	UNKNOWN
}
