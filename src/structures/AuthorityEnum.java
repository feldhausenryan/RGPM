package structures;

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
