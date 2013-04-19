/**
 *  Strategy Engine for Programming Intelligent Agents (SEPIA)
    Copyright (C) 2012 Case Western Reserve University

    This file is part of SEPIA.

    SEPIA is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SEPIA is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SEPIA.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.history.History;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;

/**
 * @author Derrick Tilsner
 * @author Sam Fleckenstein
 *
 */
public class RLAgent extends Agent {
	private static final long serialVersionUID = -4047208702628325380L;
	private static final Logger logger = Logger.getLogger(RLAgent.class.getCanonicalName());

	private static final double DISCOUNTING_FACTOR = 0.9;
	private static final double LEARNING_RATE = 0.0001;
	private static final int NUMBER_OF_FEATURES = 5;
	
	StateView currentState;
	private int step;
	
	private PreviousState prevState;
	private ArrayList<Integer> footmanIds;
	private ArrayList<Integer> enemyIds;
	
	private double weights[];
	
	
	public RLAgent(int playernum, String[] arguments) {
		super(playernum);
		
		weights = new double[NUMBER_OF_FEATURES];
	}

	
	@Override
	public Map<Integer, Action> initialStep(StateView newState, History.HistoryView statehistory) {
		step = 0;
		currentState = newState;
		
		List<Integer> friendUnitIds = currentState.getUnitIds(0);
		HashMap<Integer, Integer> friendHP = new HashMap<Integer, Integer>();
		footmanIds = new ArrayList<Integer>();
		for(int i = 0; i < friendUnitIds.size(); i++) {
			int id = friendUnitIds.get(i);
			UnitView unit = currentState.getUnit(id);
			footmanIds.add(id);
			friendHP.put(id, unit.getHP());
		}
		
		List<Integer> enemyUnitIds = currentState.getUnitIds(1);
		HashMap<Integer, Integer> enemyHP = new HashMap<Integer, Integer>();
		enemyIds = new ArrayList<Integer>();
		for(int i = 0; i < enemyUnitIds.size(); i++) {
			int id = enemyUnitIds.get(i);
			UnitView unit = currentState.getUnit(id);
			enemyIds.add(id);
			enemyHP.put(id, unit.getHP());
		}
		
		prevState = new PreviousState(footmanIds, friendHP, null, enemyIds, enemyHP, currentState); //TODO null for first attack?
		
		return middleStep(newState, statehistory);
	}

	@Override
	public Map<Integer, Action> middleStep(StateView newState, History.HistoryView statehistory) {
		step++;
		currentState = newState;
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("=> Step: " + step);
		}
		Map<Integer, Action> builder = new HashMap<Integer, Action>();
		
		//TODO make sure that when someone dies, the ids get updated
		
		//ANALYZE PHASE
		for(int id : prevState.getFootmanIds()) {
			double reward = 0.0;
			//TODO calculate rewards for each footman
			if(!currentState.getAllUnitIds().contains(id)) {
				reward -= 100; //footman died
				prevState.markFootmanForRemoval(id);
			} else {
				reward -= prevState.getFootmanHP(id) - currentState.getUnit(id).getHP(); //footman was injured
				prevState.setFootmanHP(id, currentState.getUnit(id).getHP());
			}
			int enemyId = prevState.getFootmanAttack(id);
			if(!currentState.getAllUnitIds().contains(enemyId)) {
				reward += 100; //enemy footman died
				prevState.markEnemyForRemoval(enemyId);
			} else {
				reward += prevState.getEnemyHP(enemyId) - currentState.getUnit(enemyId).getHP(); //enemy was injured
			}
			reward -= 0.1;
			updateQFunction(reward, id);
		}
		prevState.removeMarkedEnemy();
		prevState.removeMarkedFootman();
		updateEnemyHPs();
		
		//DECIDE PHASE
		
		
		//EXECUTE PHASE
		return builder;
	}

	@Override
	public void terminalStep(StateView newstate, History.HistoryView statehistory) {
		step++;
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("=> Step: " + step);
		}

		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Congratulations! You have finished the task!");
		}
	}
	
//	public double calculateRewardValue() {
//		double reward = 0;
//		
//		for(int id : prevState.getEnemyIds()) {
//			if(!currentState.getAllUnitIds().contains(id)) {
//				//killed an enemy
//				reward += 100;
//				prevState.markEnemyForRemoval(id);
//			} else {
//				//injured an enemy
//				reward += prevState.getEnemyHP(id) - currentState.getUnit(id).getHP();
//				prevState.setEnemyHP(id, currentState.getUnit(id).getHP());
//			}
//		}
//		
//		for(int id : prevState.getFootmanIds()) {
//			if(!currentState.getAllUnitIds().contains(id)) {
//				//got killed
//				reward -= 100;
//				prevState.markFootmanForRemoval(id);
//			} else {
//				//got injured
//				reward -= prevState.getEnemyHP(id) - currentState.getUnit(id).getHP();
//				prevState.setFootmanHP(id, currentState.getUnit(id).getHP());
//			}
//			//subtract 0.1 for each action taken
//			reward -= 0.1;
//		}
//		
//		prevState.removeMarkedEnemy();
//		prevState.removeMarkedFootman();
//		
//		return reward;
//	}
	
	private void updateEnemyHPs() {
		for(int enemy : prevState.getEnemyIds()) {
			int currentHP = currentState.getUnit(enemy).getHP();
			if(currentHP < prevState.getEnemyHP(enemy)) {
				prevState.setEnemyHP(enemy, currentHP);
			}
		}
	}

	private void updateQFunction(double reward, int footmanId) {
		double previousQ = calculateQFunction(prevState.getState(), footmanId); //TODO fix state stuff... just want the previous state
		double maxQ = -99999;
		for(int enemy : enemyIds) {
			//TODO edit currentState so that footman is attacking that enemy
			double currentQ = calculateQFunction(currentState, footmanId);
			if(currentQ > maxQ) {
				maxQ = currentQ;
			}
		}
		double updateFactor = reward + DISCOUNTING_FACTOR * maxQ - previousQ;
		for(int i = 0; i < weights.length; i++) {
			weights[i] = weights[i] + LEARNING_RATE * updateFactor; //*feature[i];
		}
	}
	
	private static double calculateQFunction(StateView state, int footmanId) {
		double qValue = 0;
		
		//TODO calculate qValue += (feature[i])(weights[i]);
		
		return qValue;
	}
	
	/**
	 * @param footmanId - id of attacker
	 * @return target's id
	 */
	private int findNextTarget(int footmanId) {
		//incorporate a global or previous state parameter
		//that keeps track of which footmen are going to be attacking who
		
		//this will be called in loop of middleStep DECIDE PHASE
		return 0;
	}
	
	@Override
	public void savePlayerData(OutputStream os) {
		//this agent lacks learning and so has nothing to persist.
	}
	@Override
	public void loadPlayerData(InputStream is) {
		//this agent lacks learning and so has nothing to persist.
	}
}
