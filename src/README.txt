Derrick Tilsner 	- 	dtt13
Sam Fleckenstein 	- 	sef44


To compile:
	Navigate to the src folder and compile using the command:
		javac -cp "Sepia.jar;" PreviousState.java RLAgent.java

To run:
	Navigate to the src folder and use the command:
		java -cp "Sepia.jar;." edu.cwru.sepia.Main2 CombatConfig.xml


To change the number of episodes to play:
	Open the CombatConfig.xml and edit the Argument line under the RLAgent to be the desired
	number of episodes.

Notes:
	We found that the agent runs better when the DISCOUNTING_FACTOR is changed from 0.9 to 0.7
	and the EPSILON value is changed from 0.02 to 0.01.