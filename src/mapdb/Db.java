package mapdb;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Db wraps over the mapdb database and essentially provides some
 * basic hackjob lockout functionality so the database won't blow up.
 * @author feldh
 *
 */
public class Db {
	/**
	 * The mapdb database, decalred static because it is shared among
	 * all maps within this database. 
	 */
	private static DB db;
	/**
	 * The specific map used by this class. 
	 */
	private ConcurrentMap data;
	/**
	 * An instance of ReentrantLock created OUTSIDE of the class. The
	 * lock put into the constructor should be shared among all
	 * instances of this class. 
	 */
	private final ReentrantLock lock;
	/**
	 * The mapDb database name.
	 */
	private final String dbName;
	/**
	 * The mapDb map name. 
	 */
	private final String dataName;
	
	/**
	 * Constructs a database specifically giving control over one map.
	 * @param dbName The database name 
	 * @param dataName The map's name
	 * @param lock The lock used and shared among all maps. 
	 */
	public Db(String dbName, String dataName, ReentrantLock lock){
		this.dbName = dbName;
		this.dataName = dataName;
		this.lock = lock;
	}
	
	/**
	 * Used internally. Initializes the database/map if it hasn't been done so
	 * the database is NOT CLOSED so use .transactionEnable() when initializing
	 * the database to ensure crash-security. 
	 */
	private void acquire(){
		lock.lock();
		if (db == null) db = DBMaker.fileDB(dbName).transactionEnable().make();
		if (data == null) data = db.hashMap(dataName).createOrOpen();
	}
	
	/**
	 * Releases the lock for other threads
	 */
	private void release(){
		lock.unlock();
	}
	
	/**
	 * Save the changes from memory to a file. This should be called periodically
	 * outside of the main thread. Calling it frequently will slow the program down.
	 * 
	 */
	public void commit(){
		acquire();
		db.commit();
		release();
	}
	
	/**
	 * Returns the value to which the specified key is mapped, or null if this map contains no mapping for the key. 
     * More formally, if this map contains a mapping from a key k to a value v such that (key==null ? k==null : key.equals(k)), then this method returns v; otherwise it returns null. (There can be at most one such mapping.) 
     * 
     * If this map permits null values, then a return value of null does not necessarily indicate that the map contains no mapping for the key; it's also possible that the map explicitly maps the key to null. The containsKey operation may be used to distinguish these two cases.
	 * 
     * @parameters key - the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
     * @throws ClassCastException - if the key is of an inappropriate type for this map (optional)
     * @throws NullPointerException - if the specified key is null and this map does not permit null keys (optional)
	 */
	public Object get(Object key){
		acquire();
		Object retVal = data.get(key);
		release();
		return retVal;
	}
	
	/**
	 * Creates or updates the field associated with a given key.
	 * @param The key 
	 * @param The field to be created or updated
	 * @return The old value of the field or null
	 */
	public Object put(Object key, Object value){
		acquire();
		@SuppressWarnings("unchecked")
		Object retVal = data.put(key, value);
		release();
		return retVal;
	}
	
	/**
	 * Returns the value associated with the key or the default value
	 * if nothing is found. This should be followed up with a put.
	 * @param The key
	 * @param The default value returned if there was no key
	 * @return Either the value associated with the key or the argument defaultValue
	 */
	public Object getOrDefault(Object key, Object defaultValue){
		acquire();
		@SuppressWarnings("unchecked")
		Object retVal = data.getOrDefault(key, defaultValue);
		release();
		return retVal;
	}
	
	/**
	 * Returns true if this map contains a mapping for the specified key.
	 * More formally, returns true if and only if this map contains a
	 * mapping for a key k such that (key==null ? k==null : key.equals(k)). (There can be at most one such mapping.)
	 * @param The key
	 * @return true if yes / false if no
	 */
	public boolean containsKey(Object key){
		acquire();
		boolean retVal = data.containsKey(key);
		release();
		return retVal;
	}
	
	public boolean remove(Object key){
		acquire();
		boolean retVal;
		if(data.containsKey(key)){
			data.remove(key);
			retVal = true;
		}
		else{
			retVal = false;
		}
		release();
		return retVal;
	}
}
