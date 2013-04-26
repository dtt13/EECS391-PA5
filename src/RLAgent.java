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
	private static int targetEpisodes;
	private static int numEpisodes;
	
	StateView currentState;
	private int step;
	
	private PreviousState prevState;
	private ArrayList<Integer> footmanIds;
	private ArrayList<Integer> enemyIds;
	
	private double weights[];
	private boolean firstStep;
	
	
	public RLAgent(int playernum, String[] arguments) {
		super(playernum);
		
		targetEpisodes = Integer.parseInt(arguments[0]);
		numEpisodes = 0;
		
		weights = new double[NUMBER_OF_FEATURES];
		for(int i = 0; i < weights.length; i++) {
			weights[i] = Math.random() * 2 - 1;
		}
	}

	
	@Override
	public Map<Integer, Action> initialStep(StateView newState, History.HistoryView statehistory) {
		step = 0;
		currentState = newState;
		
		//initialize previous state information
		//friendly info
		List<Integer> friendUnitIds = currentState.getUnitIds(0);
		HashMap<Integer, Integer> friendHP = new HashMap<Integer, Integer>();
		HashMap<Integer, Point> friendLocs = new HashMap<Integer, Point>();
		footmanIds = new ArrayList<Integer>();
		for(int i = 0; i < friendUnitIds.size(); i++) {
			int id = friendUnitIds.get(i);
			UnitView unit = currentState.getUnit(id);
			footmanIds.add(id);
			friendHP.put(id, unit.getHP());
			Point footLoc = new Point(unit.getXPosition(), unit.getYPosition());
			friendLocs.put(id, footLoc);
		}
		
		//enemy info
		List<Integer> enemyUnitIds = currentState.getUnitIds(1);
		HashMap<Integer, Integer> enemyHP = new HashMap<Integer, Integer>();
		HashMap<Integer, Point> enemyLocs = new HashMap<Integer, Point>();
		enemyIds = new ArrayList<Integer>();
		for(int i = 0; i < enemyUnitIds.size(); i++) {
			int id = enemyUnitIds.get(i);
			UnitView unit = currentState.getUnit(id);
			enemyIds.add(id);
			enemyHP.put(id, unit.getHP());
			Point foeLoc = new Point(unit.getXPosition(),unit.getYPosition());
			enemyLocs.put(id, foeLoc);
		}
		
		//initializing targets
		HashMap<Integer, Integer> targets = new HashMap<Integer, Integer>();
		for(int footId : footmanIds) {
			double rndm = Math.random();
			int targetId = -1;
			if(rndm > 1 - EPSILON) {
				targetId = enemyIds.get((int)(Math.random() * enemyIds.size()));
			} else {
				double maxQValue = -99999;
				Point friendLoc = friendLocs.get(footId);
				int footHP = friendHP.get(footId);
				for(int enemyId : enemyIds) {
					int numAttackers = 0; //TODO how to calculate
					Point enemyLoc = enemyLocs.get(enemyId);
					int foeHP = enemyHP.get(enemyId);
					double qValue = calculateQFunction(footId, enemyLoc, friendLoc, foeHP, footHP, numAttackers);
					if(qValue > maxQValue) {
						maxQValue = qValue;
						targetId = enemyId;
					}
				}
			}
			targets.put(footId, targetId);
			firstStep = true;
		}
		
		prevState = new PreviousState(footmanIds, friendHP, friendLocs, targets, enemyIds, enemyHP, enemyLocs);
		
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
		
		printWeights();
		
		if(firstStep) {
			firstStep = false;
			for(int footId : footmanIds) {
				int targetId = prevState.getFootmanAttack(footId);
				Action b = new TargetedAction(footId, ActionType.COMPOUNDATTACK, targetId);
				builder.put(footId, b);
			}
			return builder;
		}
		
		List<Integer> friendUnitIds = currentState.getUnitIds(0);
		footmanIds = new ArrayList<Integer>();
		for(int i = 0; i < friendUnitIds.size(); i++) {
			int id = friendUnitIds.get(i);
			footmanIds.add(id);
		}
		
		//enemy info
		List<Integer> enemyUnitIds = currentState.getUnitIds(1);
		enemyIds = new ArrayList<Integer>();
		for(int i = 0; i < enemyUnitIds.size(); i++) {
			int id = enemyUnitIds.get(i);
			enemyIds.add(id);
		}
		
		boolean noInjuries = true;
		for(int id : prevState.getFootmanIds()) {
			if(!currentState.getAllUnitIds().contains(id)
					|| prevState.getFootmanHP(id) != currentState.getUnit(id).getHP()) {
				noInjuries = false;
				break;
			}
			
			int enemyId = prevState.getFootmanAttack(id);
			if(!currentState.getAllUnitIds().contains(enemyId)
					|| prevState.getEnemyHP(enemyId) != currentState.getUnit(enemyId).getHP()) {
				noInjuries = false;
				break;
			}
		}
		
		if(noInjuries) {
			return builder;
		}
		
		//ANALYZE PHASE
		for(int id : prevState.getFootmanIds()) {
			double reward = 0.0;
			if(!currentState.getAllUnitIds().contains(id)) {
				//footman died
				reward -= 100;
				prevState.markFootmanForRemoval(id);
			} else {
				//footman was injured
				reward -= prevState.getFootmanHP(id) - currentState.getUnit(id).getHP();
			}
			
			int enemyId = prevState.getFootmanAttack(id);
			if(!currentState.getAllUnitIds().contains(enemyId)) {
				//enemy footman died
				reward += 100;
				prevState.markEnemyForRemoval(enemyId);
			} else {
				//enemy was injured
				reward += prevState.getEnemyHP(enemyId) - currentState.getUnit(enemyId).getHP();
			}
			reward -= 0.1;
			if(numEpisodes % 10 < 5) {
				updateQFunction(reward, id);
			}
		}
		updateStatusInfo();
		
		//DECIDE PHASE
		for(int footId : footmanIds) {
			double rndm = Math.random();
			int targetId = -1;
			if(rndm > 1 - EPSILON && numEpisodes % 10 < 5) {
				targetId = enemyIds.get((int)(Math.random() * enemyIds.size()));
			} else {
				UnitView friendUnit = currentState.getUnit(footId);
				Point friendLoc = new Point(friendUnit.getXPosition(), friendUnit.getYPosition());
				int friendHP = friendUnit.getHP();
				double maxQValue = -99999;
				for(int enemyId : enemyIds) {
					int numAttackers = 0; //TODO how to calculate
					UnitView enemyUnit = currentState.getUnit(enemyId);
					Point enemyLoc = new Point(enemyUnit.getXPosition(), enemyUnit.getYPosition());
					int enemyHP = enemyUnit.getHP();
					double qValue = calculateQFunction(footId, enemyLoc, friendLoc, enemyHP, friendHP, numAttackers);
					if(qValue > maxQValue) {
						maxQValue = qValue;
						targetId = enemyId;
					}
				}
			}
			Action b = new TargetedAction(footId, ActionType.COMPOUNDATTACK, targetId);
			builder.put(footId, b);
			
			prevState.setFootmanAttack(footId, targetId);
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
		
		numEpisodes++;
		
//		int numEnemies = enemyIds.size();
//		int numFriendlies = footmanIds.size();
//		System.out.println("Enemies: " + numEnemies);
//		System.out.println("Friendlies: " + numFriendlies);
//		System.out.println();
		
		if(logger.isLoggable(Level.FINE)) {
			logger.fine("Congratulations! You have finished the task!");
		}
	}
	
	/**
	 * updates all units' death, health, and location information in the previous state
	 */
	private void updateStatusInfo() {
		prevState.removeMarkedEnemy();
		prevState.removeMarkedFootman();
		for(int enemy : prevState.getEnemyIds()) {
			UnitView unit = currentState.getUnit(enemy);
			int currentHP = unit.getHP();
			if(currentHP < prevState.getEnemyHP(enemy)) {
				prevState.setEnemyHP(enemy, currentHP);
			}
			int xPosition = unit.getXPosition();
			int yPosition = unit.getYPosition();
			Point location = new Point(xPosition, yPosition);
			prevState.setEnemyLoc(enemy, location);
		}
		for(int footman : prevState.getFootmanIds()) {
			UnitView unit = currentState.getUnit(footman);
			int currentHP = unit.getHP();
			if(currentHP < prevState.getFootmanHP(footman)) {
				prevState.setFootmanHP(footman, currentHP);
			}
			int xPosition = unit.getXPosition();
			int yPosition = unit.getYPosition();
			Point location = new Point(xPosition, yPosition);
			prevState.setEnemyLoc(footman, location);
		}
	}

	/**
	 * updates the weights of the Q function
	 * @param reward
	 * @param footmanId
	 */
	private void updateQFunction(double reward, int footmanId) {
		//calculate previous Q function
		int footmanTarget = prevState.getFootmanAttack(footmanId);
		Point prevEnemyLoc = prevState.getEnemyLoc(footmanTarget);
		int prevEnemyHP = prevState.getEnemyHP(footmanTarget);
		
		Point prevFootLoc = prevState.getFootmanLoc(footmanId);
		int prevFootHP = prevState.getFootmanHP(footmanId);
		
		int prevNumAttackers = prevState.getNumAttackers(footmanTarget);

		double previousQ = calculateQFunction(footmanId, prevEnemyLoc, prevFootLoc, prevEnemyHP, prevFootHP, prevNumAttackers);

		//calculate current Q function
		UnitView friendUnit = currentState.getUnit(footmanId);
		Point newFriendLoc;
		int newFriendHP;
		if(friendUnit != null) {
			newFriendHP = friendUnit.getHP();
			newFriendLoc = new Point(friendUnit.getXPosition(),friendUnit.getYPosition());
		} else {
//			for(int enemyId : enemyIds) {
//				UnitView enemy = currentState.getUnit(enemyId);
//				//TODO check if this actually is COMPOUNDATTACK
//				if(enemy.getCurrentDurativeAction().getClass().toString().equals("COMPOUNDATTACK")) {
////					(ActionType.COMPOUNDATTACK)(enemy.getCurrentDurativeAction()).
//				}
//			}
			//TODO maybe find a more accurate way to do this
			newFriendHP = 0;
			Point footmanLoc = prevState.getFootmanLoc(footmanId);
			newFriendLoc = new Point(footmanLoc.x, footmanLoc.y);
		}
		
		double maxQ = -99999;
		Point maxLoc = null;
		int maxHP = 0;
		int maxNumAttackers = -1;
		for(int enemyId : enemyIds) {
			int numAttackers = 0; //TODO how to calculate
			UnitView enemyUnit = currentState.getUnit(enemyId);
			Point newEnemyLoc = new Point(enemyUnit.getXPosition(), enemyUnit.getYPosition());
			int newEnemyHP = enemyUnit.getHP();
			double currentQ = calculateQFunction(footmanId, newEnemyLoc, newFriendLoc, newEnemyHP, newFriendHP, numAttackers);
			if(currentQ > maxQ) {
				maxQ = currentQ;
				maxLoc = newEnemyLoc;
				maxHP = newEnemyHP;
				maxNumAttackers = numAttackers;
			}
		}
		
		//update Q function weights
		double updateFactor = reward + DISCOUNTING_FACTOR * maxQ - previousQ;
		
		weights[0] = weights[0] + LEARNING_RATE * updateFactor * chebychevDist(maxLoc, newFriendLoc);
		weights[1] = weights[1] + LEARNING_RATE * updateFactor * maxHP;
		weights[2] = weights[2] + LEARNING_RATE * updateFactor * newFriendHP;
		weights[3] = weights[3] + LEARNING_RATE * updateFactor * maxNumAttackers;
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
		//qValue += numAttackers * weights[3];
		
		return qValue;
	}
	
	private int chebychevDist(Point enemyLoc, Point footLoc) {
		return Math.max(Math.abs(enemyLoc.x - footLoc.x), Math.abs(enemyLoc.y - footLoc.y));
	}
	
	public void printWeights() {
		for(int i = 0; i < weights.length; i++) {
			System.out.println(i + " " +  weights[i]);
		}
		System.out.println();
		System.out.println();
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
