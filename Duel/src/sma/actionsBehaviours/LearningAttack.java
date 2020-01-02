package sma.actionsBehaviours;

import org.jpl7.Query;

import com.jme3.math.Vector3f;

import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.agents.FinalAgent;

import weka.classifiers.trees.J48;

public class LearningAttack extends TickerBehaviour {

	private static final long serialVersionUID = 4340498260100499547L;

	public static long FORGET_TIME = 35;;

	FinalAgent agent;

	String enemy;
	long lastTimeSeen;
	Vector3f lastPosition;

	public static boolean openFire = false;
	public static LearningProlog b;

	public LearningAttack(Agent a, long period, String enemy, LearningProlog b) {
		super(a, period);
		this.enemy = enemy;
		agent = (FinalAgent) ((AbstractAgent) a);
		lastPosition = agent.getEnemyLocation(enemy);
		lastTimeSeen = System.currentTimeMillis();
		openFire = false;
		System.out.println("Player Attack");
		this.b = b;
	}

	@Override
	protected void onTick() {
		Vector3f pos = b.moveAttack();
		if (pos != null) {
			agent.moveTo(pos);
		}
		askForFirePermission();

		agent.goTo(lastPosition);

		if (agent.isVisible(enemy, AbstractAgent.VISION_DISTANCE)) {
			lastTimeSeen = System.currentTimeMillis();
			lastPosition = agent.getEnemyLocation(enemy);
			agent.lookAt(lastPosition);

			if (openFire) {
				System.out.println("Enemy visible, FIRE !");
				agent.lastAction = Situation.SHOOT;
				agent.shoot(enemy);

			}

		} else {

			if (System.currentTimeMillis() - lastTimeSeen > FORGET_TIME * getPeriod()) {
				System.out.println("The enemy ran away");
				agent.removeBehaviour(this);
				agent.currentBehavior = null;
			}
			agent.lastAction = Situation.FOLLOW;

		}
	}

	public static void askForFirePermission() {
		String query = "toOpenFire(" + LearningProlog.sit.enemyInSight + "," + LearningProlog.sit.impactProba + ")";

		openFire = Query.hasSolution(query);
	}

}
