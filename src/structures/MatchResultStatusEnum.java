package structures;

/**
 * An enum that tracks the status of a matchResult recording. 
 * @author feldh
 *
 */
public enum MatchResultStatusEnum {
	/**
	 * If the match hasn't been recorded yet. No rating change. 
	 */
	UNRECORDED,
	/**
	 * If the match was recorded by a general user.
	 */
	NORMAL_RECORDED,
	/**
	 * If the match was contested by a general user. Admins can change this result. 
	 * Ratings are held until this is resolved.
	 */
	NORMAL_CONTESTED,
	/**
	 * If an admin has recorded the match. Only the owner can change this.
	 */
	ADMIN_RECORDED,
	/**
	 * If the owner records this match, nothing can change it but him.
	 */
	OWNER_RECORDED,
	/**
	 * This state happens in the middle of some match recording function calls. 
	 */
	UNKNOWN
}
