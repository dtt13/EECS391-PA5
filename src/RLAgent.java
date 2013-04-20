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

import java.awt.Point;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.action.ActionType;
import edu.cwru.sepia.action.TargetedAction;
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
	private static final int NUMBER_OF_FEATURES = 4;
	private static final double EPSILON = 0.02;
	
	StateView currentState;
	private int step;
	
	private PreviousState prevState;
	private ArrayList<Integer> footmanIds;
	private ArrayList<Integer> enemyIds;
	
	private double weights[];
	
	
	public RLAgent(int playernum, String[] arguments) {
		super(playernum);
		
		weights = new double[NUMBER_OF_FEATURES];
		for(int i = 0; i < weights.length; i++) {
			weights[i] = Math.random() * 2 - 1;
		}
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
		
		HashMap<Integer, Integer> targets = new HashMap<Integer, Integer>();
		for(int footId : footmanIds) {
			double rndm = Math.random();
			int targetId = -1;
			if(rndm > 1 - EPSILON) {
				targetId = enemyIds.get((int)(Math.random() * enemyIds.size()));
			} else {
				double maxQValue = -99999;
				for(int enemyId : enemyIds) {
					double qValue = calculateQFunction(currentState, footId);
					if(qValue > maxQValue) {
						maxQValue = qValue;
						targetId = enemyId;
					}
				}
			}
			targets.put(footId, targetId);
			//TODO update prevState with the attack targets
		}
		
		prevState = new PreviousState(footmanIds, friendHP, targets, enemyIds, enemyHP, currentState); //TODO null for first attack?
		
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
		for(int footId : footmanIds) {
			double rndm = Math.random();
			int targetId = -1;
			if(rndm > 1 - EPSILON) {
				targetId = enemyIds.get((int)(Math.random() * enemyIds.size()));
			} else {
				double maxQValue = -99999;
				for(int enemyId : enemyIds) {
					double qValue = calculateQFunction(currentState, footId);
					if(qValue > maxQValue) {
						maxQValue = qValue;
						targetId = enemyId;
					}
				}
			}
			//TODO update prevState with the attack targets
			Action b = new TargetedAction(footId, ActionType.COMPOUNDATTACK, targetId);
			builder.put(footId, b);
		}
		
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
//		int enemyX = currentState.getUnit(prevState.getFootmanAttack(footmanId)).getXPosition();
//		int enemyY = currentState.getUnit(prevState.getFootmanAttack(footmanId)).getYPosition();
//		Point enemyLoc = new Point(enemyX, enemyY);
		Integer footmanTarget = prevState.getFootmanAttack(footmanId);
		Point enemyLoc = prevState.getEnemyLoc(footmanTarget);
		int enemyHP = prevState.getEnemyHP(footmanTarget);
		int numAttackers = prevState.getNumAttackers(footmanTarget);
		
//		int footX = currentState.getUnit(footmanId).getXPosition();
//		int footY = currentState.getUnit(footmanId).getYPosition();
//		Point footLoc = new Point(footX, footY);
		Point footLoc = prevState.getFootmanLoc(footmanId);
		int footHP = prevState.getFootmanHP(footmanId);
		
		double previousQ = calculateQFunction(footmanId, enemyLoc, footLoc, enemyHP, footHP, numAttackers); //TODO fix state stuff... just want the previous state
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
	
	/**
	 * 
	 * @param footmanId
	 * @param enemyLoc
	 * @param footLoc
	 * @param enemyHP
	 * @param footHP
	 * @param numAttackers
	 * @return
	 */
	private double calculateQFunction(int footmanId, Point enemyLoc, Point footLoc, int enemyHP, int footHP, int numAttackers) {
		double qValue = 0;
		
		qValue += chebychevDist(enemyLoc, footLoc) * weights[0];
		qValue += enemyHP * weights[1];
		qValue += footHP * weights[2];
		qValue += numAttackers * weights[3];
		
		return qValue;
	}
	
	private static int chebychevDist(Point enemyLoc, Point footLoc) {
		return Math.max(Math.abs(enemyLoc.x - footLoc.x), Math.abs(enemyLoc.y - footLoc.y));
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
