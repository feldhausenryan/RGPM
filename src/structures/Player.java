package structures;
import java.io.Serializable;

import mainpackage.Main;

public class Player implements Serializable{
	/**
	 * id is a unique player identifier created by discord.
	 * It is typically a large string of numbers. It could be represented
	 * by a numeric format, but is formatted as a string to prevent 
	 * the number from potentially overflowing. 
	 */
	private final String id;
	/**
	 * name is a non-unique player name used by discord. This is the display
	 * name user's see in the discord server (guild)
	 */
	private final String name;
	/**
	 * u is the mean of the normal distribution of a player's rating.
	 * This can be considered the ELO or MMR of the player.
	 */
	private final int u;
	/**
	 * sigma is the variance of the player's rating.
	 * Currently, sigma does not change from game to game.
	 */
	private final int sigma;
	
	/**
	 * Initializes a player, which is a data strucutre used to 
	 * record information about a player's stats (rank etc).
	 * This constructor fills in the player without default values
	 * for u and sigma.
	 * @param id The ID of the player. User.getId()
	 * @param name The name of the player. User.getName()
	 */
	public Player(String id, String name){
		this(id, name, Main.STARTING_U, Main.STARTING_SIGMA);
	}
	
	/**
	 * Initializes a player, which is basically a data structure used to 
	 * record information about a player's stats (rank etc)
	 * @param id The ID of the player. User.getId()
	 * @param name The name of the player. User.getName()
	 * @param u The mean distribution of a player's rating. 
	 * @param sigma The variance of a player's rating. 
	 */
	public Player(String id, String name, int u, int sigma){
		this.id = id;
		this.name = name;
		this.u = u;
		this.sigma = sigma;
	}

	/**
	 * This function is included to make the mapdb calls work. This is based
	 * directly from the convention used in the mapdb example code for custom
	 * classes.
	 */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (id != null ? !id.equals(player.getId()) : player.getId() != null) return false;
        if (name != null ? !name.equals(player.getName()) : player.getName() != null) return false;
        if (!(u==(player.getU()))) return false;
        if (!(sigma==(player.getSigma()))) return false;

        return true;
    }
    
	/**
	 * @return the id. A long string of numbers representing the user's
	 * discord-wide unique id. 
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name. A non-unique name (ex. Ryan)
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the u. The mean rating of the player.
	 */
	public int getU() {
		return u;
	}

	/**
	 * @return the sigma. The variance of the player's skill. 
	 */
	public int getSigma() {
		return sigma;
	}
}
