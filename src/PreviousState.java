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
	
	/**
	 * 
	 * @param id - The id of the footman you are concerned with.
	 * @return null if the footman doesn't exist. The footman's HP if it does exist.
	 */
	public Integer getFootmanHP(int id) {
		if(!footmanHP.containsKey(id)) {
			return null;
		}
		return footmanHP.get(id);
	}
	
	/**
	 * 
	 * @param id - The id of the footman you are concerned with.
	 * @return null if the footman doesn't exist. The id of the enemy that that footman was attacking if it does exist.
	 */
	public Integer getFootmanAttack(int id) {
		if(!footmanAttack.containsKey(id)) {
			return null;
		}
		return footmanAttack.get(id);
	}
	
	/**
	 * 
	 * @param id - The id of the enemy you are concerned with.
	 * @return null if the enemy doesn't exist. The enemy's HP if it does exist.
	 */
	public Integer getEnemyHP(int id) {
		if(!enemyHP.containsKey(id)) {
			return null;
		}
		return enemyHP.get(id);
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
	 * @param footmanId - ID of the footman who is attacking
	 * @param enemyId - ID of the enemy being attacked
	 */
	public void setFootmanAttack(int footmanId, int enemyId) {
		footmanAttack.remove(footmanId);
		footmanAttack.put(footmanId, enemyId);
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
	
	public ArrayList<Integer> getFootmanIds() {
		return footmanIds;
	}
	
	public ArrayList<Integer> getEnemyIds() {
		return enemyIds;
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
		}
		toRemoveEnemy = new ArrayList<Integer>();
	}
	
	public Point getEnemyLoc(int id) {
		return enemyLocs.get(id);
	}
	
	public void setEnemyLoc(int id, Point location) {
		enemyLocs.remove(id);
		enemyLocs.put(id, location);
	}
	
	public Point getFootmanLoc(int id) {
		return footmanLocs.get(id);
	}
	
	public void setFootmanLoc(int id, Point location) {
		footmanLocs.remove(id);
		footmanLocs.put(id, location);
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
