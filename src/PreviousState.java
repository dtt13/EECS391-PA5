import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

public class PreviousState {

	private ArrayList<Integer> footmanIds = new ArrayList<Integer>();
	private HashMap<Integer, Integer> footmanHP = new HashMap<Integer, Integer>();
	private ArrayList<Integer> enemyIds = new ArrayList<Integer>();
	private HashMap<Integer, Integer> enemyHP = new HashMap<Integer, Integer>();
	private ArrayList<Integer> toRemoveFootman = new ArrayList<Integer>();
	private ArrayList<Integer> toRemoveEnemy = new ArrayList<Integer>();

	
	public PreviousState(ArrayList<Integer> footmanIds, HashMap<Integer, Integer> footmanHP,
			ArrayList<Integer> enemyIds, HashMap<Integer, Integer> enemyHP) {
		for(Integer id : footmanIds) {
			this.footmanIds.add(id);
			this.footmanHP.put(id, footmanHP.get(id));
		}
		for(Integer id : enemyIds) {
			this.enemyIds.add(id);
			this.enemyHP.put(id, enemyHP.get(id));
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
		toRemoveFootman.add(id);
	}
	
	public void removeMarkedFootman() {
		for(Integer id : toRemoveFootman) {
			if(footmanIds.contains(id)) {
				footmanIds.remove(id);
			}
			if(footmanHP.containsKey(id)) {
				footmanHP.remove(id);
			}
		}
		toRemoveFootman = new ArrayList<Integer>();
	}
	
	public void markEnemyForRemoval(int id) {
		toRemoveEnemy.add(id);
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
	
	public void addFootman(int footmanId, int footmanHP) {
		this.footmanIds.add(footmanId);
		this.footmanHP.put(footmanId, footmanHP);
	}
	
	public void addEnemy(int enemyId, int enemyHP) {
		this.footmanIds.add(enemyId);
		this.footmanHP.put(enemyId, enemyHP);
	}

}
