import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

public class PreviousState {

	private ArrayList<Integer> footmanIds = new ArrayList<Integer>();
	private HashMap<Integer, Integer> footmanHP = new HashMap<Integer, Integer>();
	private HashMap<Integer, Point> footmanLocs = new HashMap<Integer, Point>();
	private HashMap<Integer, Integer> footmanAttack = new HashMap<Integer, Integer>();
	private ArrayList<Integer> toRemoveFootman = new ArrayList<Integer>();
	
	private ArrayList<Integer> enemyIds = new ArrayList<Integer>();
	private HashMap<Integer, Integer> enemyHP = new HashMap<Integer, Integer>();
	private HashMap<Integer, Point> enemyLocs = new HashMap<Integer, Point>();
	private ArrayList<Integer> toRemoveEnemy = new ArrayList<Integer>();
	
	public PreviousState(ArrayList<Integer> footmanIds, HashMap<Integer, Integer> footmanHP,
			HashMap<Integer, Point> footmanLocs, HashMap<Integer, Integer> footmanAttack,
			ArrayList<Integer> enemyIds, HashMap<Integer, Integer> enemyHP, HashMap<Integer, Point> enemyLocs) {
		for(Integer id : footmanIds) {
			this.footmanIds.add(id);
			this.footmanHP.put(id, footmanHP.get(id));
			this.footmanLocs.put(id, footmanLocs.get(id));
			this.footmanAttack.put(id, footmanAttack.get(id));
		}
		for(Integer id : enemyIds) {
			this.enemyIds.add(id);
			this.enemyHP.put(id, enemyHP.get(id));
			this.enemyLocs.put(id, enemyLocs.get(id));
		}
	}
	
	public ArrayList<Integer> getFootmanIds() {
		return footmanIds;
	}
	
	public ArrayList<Integer> getEnemyIds() {
		return enemyIds;
	}

	/**
	 * 
	 * @param id - ID of the footman whose HP has changed.
	 * @param HP - The new HP.
	 */
	public void setFootmanHP(int id, int HP) {
		footmanHP.remove(id);
		footmanHP.put(id, HP);
	}
	
	/**
	 * 
	 * @param id - The id of the footman you are concerned with.
	 * @return null if the footman doesn't exist. The footman's HP if it does exist.
	 */
	public Integer getFootmanHP(int id) {
		return footmanHP.get(id);
	}
	
	/**
	 * 
	 * @param id - ID of the enemy whose HP has changed.
	 * @param HP - The new HP.
	 */
	public void setEnemyHP(int id, int HP) {
		enemyHP.remove(id);
		enemyHP.put(id, HP);
	}

	/**
	 * 
	 * @param id - ID of the enemy you are concerned with.
	 * @return null if the enemy doesn't exist. The enemy's HP if it does exist.
	 */
	public Integer getEnemyHP(int id) {
		return enemyHP.get(id);
	}
	
	/**
	 * 
	 * @param id - ID of the footman whose location has changed.
	 * @param location - the new location
	 */
	public void setFootmanLoc(int id, Point location) {
		footmanLocs.remove(id);
		footmanLocs.put(id, location);
	}
	
	/**
	 * 
	 * @param id - ID of the footman you are concerned with.
	 * @return null if the footman doesn't exist. The footman's location if it does exist.
	 */
	public Point getFootmanLoc(int id) {
		return footmanLocs.get(id);
	}
	
	/**
	 * 
	 * @param id - ID of the enemy whose location has changed.
	 * @param location - the new location
	 */
	public void setEnemyLoc(int id, Point location) {
		enemyLocs.remove(id);
		enemyLocs.put(id, location);
	}
	/**
	 * 
	 * @param id - ID of the enemy you are concerned with.
	 * @return null if the enemy doesn't exist. The enemy's location if it does exist.
	 */
	public Point getEnemyLoc(int id) {
		return enemyLocs.get(id);
	}

	/**
	 * 
	 * @param footmanId - ID of the footman who is attacking
	 * @param enemyId - ID of the enemy being attacked
	 */
	public void setFootmanAttack(int footmanId, int enemyId) {
		footmanAttack.remove(footmanId);
		footmanAttack.put(footmanId, enemyId);
	}

	/**
	 * 
	 * @param id - The id of the footman you are concerned with.
	 * @return null if the footman doesn't exist. The id of the enemy that that footman was attacking if it does exist.
	 */
	public Integer getFootmanAttack(int id) {
		return footmanAttack.get(id);
	}
	
	public void markFootmanForRemoval(int id) {
		if(!toRemoveFootman.contains(id)) {
			toRemoveFootman.add(id);
		}
	}
	
	public void removeMarkedFootman() {
		for(Integer id : toRemoveFootman) {
			if(footmanIds.contains(id)) {
				footmanIds.remove(id);
			}
			if(footmanHP.containsKey(id)) {
				footmanHP.remove(id);
			}
			if(footmanAttack.containsKey(id)) {
				footmanAttack.remove(id);
			}
			if(footmanLocs.containsKey(id)) {
				footmanLocs.remove(id);
			}
		}
		toRemoveFootman = new ArrayList<Integer>();
	}
	
	public void markEnemyForRemoval(int id) {
		if(!toRemoveEnemy.contains(id)) {
			toRemoveEnemy.add(id);			
		}
	}
	
	public void removeMarkedEnemy() {
		for(Integer id : toRemoveEnemy) {
			if(enemyIds.contains(id)) {
				enemyIds.remove(id);
			}
			if(enemyHP.containsKey(id)) {
				enemyHP.remove(id);
			}
			if(enemyLocs.containsKey(id)) {
				enemyLocs.remove(id);
			}
		}
		toRemoveEnemy = new ArrayList<Integer>();
	}

	public int getNumAttackers(int enemyId) {
		int numAttackers = 0;
		for(int id : footmanAttack.values()) {
			if(id == enemyId) {
				numAttackers++;
			}
		}
		return numAttackers;
	}
}
