package structures;

/**
 * AuthorityEnum defines the permission level of a Discord Guild's member with respect to this bot.
 * @author feldh
 */
public enum AuthorityEnum {
	/**
	 * Normal users, no special permission granted. 
	 */
	NORMAL,
	/**
	 * Discord admins, highly trusted people. 
	 * Allowed admin actions to fix match results
	 */
	ADMIN,
	/**
	 * Bot owner, allowed all actions. 
	 */
	OWNER,
	/**
	 * This shouldn't happen
	 */
	UNKNOWN
}
