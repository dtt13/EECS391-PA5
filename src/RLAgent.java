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

	StateView currentState;
	private int step;
	
	private PreviousState prevState;
	private ArrayList<Integer> footmanIds;
	private ArrayList<Integer> enemyIds;
	
	
	public RLAgent(int playernum, String[] arguments) {
		super(playernum);
		
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
		
		prevState = new PreviousState(footmanIds, friendHP, enemyIds, enemyHP);
		
		
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
		
		//ANALYZE PHASE
		
		
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

	/**
	 * @param footmanId - id of attacker
	 * @return enemy's target id
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
