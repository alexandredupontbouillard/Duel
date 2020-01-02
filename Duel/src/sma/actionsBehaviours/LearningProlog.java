package sma.actionsBehaviours;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import weka.classifiers.trees.J48;
import weka.core.Instances;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jpl7.Query;

import com.jme3.math.Vector3f;

import env.jme.NewEnv;
import env.jme.Situation;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.agents.FinalAgent;

public class LearningProlog extends TickerBehaviour{
	private static final long serialVersionUID = 5739600674796316846L;

	public static FinalAgent agent;
	public static Class nextBehavior;

	public static Situation sit;
	public static J48 tree;

	public LearningProlog(Agent a, long period, J48 tree) {
		super(a, period);
		agent = (FinalAgent)((AbstractAgent)a);
		this.tree=tree;
	}



	@Override
	protected void onTick() {
		try {
			String prolog = "consult('./ressources/prolog/duel/requete_learning.pl')";

			if (!Query.hasSolution(prolog)) {
				System.out.println("Cannot open file " + prolog);
			}
			else {
				sit = Situation.getCurrentSituation(agent);
				moveAttack();

				List<String> behavior = Arrays.asList("explore", "hunt", "attack");
				ArrayList<Object> terms = new ArrayList<Object>();

				for (String b : behavior) {
					terms.clear();
					// Get parameters 
					if (b.equals("explore")) {
						terms.add(sit.timeSinceLastShot);
						terms.add(((LearningExplore.prlNextOffend)?sit.offSize:sit.defSize ));
						terms.add(InterestPoint.INFLUENCE_ZONE);
						terms.add(NewEnv.MAX_DISTANCE);
					}
					else if (b.equals("hunt")) {
						terms.add(sit.life);
						terms.add(sit.timeSinceLastShot);
						terms.add(sit.offSize);
						terms.add(sit.defSize);
						terms.add(InterestPoint.INFLUENCE_ZONE);
						terms.add(NewEnv.MAX_DISTANCE);
						terms.add(sit.enemyInSight);
					}else if(b.equals("attack")){
						//terms.add(sit.life);
						terms.add(sit.enemyInSight);
						//terms.add(sit.impactProba);
						
					}
					else { // RETREAT
						terms.add(sit.life);
						terms.add(sit.timeSinceLastShot);
					}

					String query = prologQuery(b, terms);
					if (Query.hasSolution(query)) {
						//System.out.println("has solution");
						setNextBehavior();

					}
				}
			}
			
		}catch(Exception e) {
			System.err.println("Behaviour file for Prolog agent not found");
			System.exit(0);
		}
	}

	public void setNextBehavior(){

		if(agent.currentBehavior != null && nextBehavior == agent.currentBehavior.getClass()){
			return;
		}
		if (agent.currentBehavior != null){
			agent.removeBehaviour(agent.currentBehavior);
		}

		if (nextBehavior == LearningExplore.class){
			LearningExplore ex = new LearningExplore(agent, FinalAgent.PERIOD);
			agent.addBehaviour(ex);
			agent.currentBehavior = ex;

		}else if(nextBehavior == HuntBehavior.class){
			HuntBehavior h = new HuntBehavior(agent, FinalAgent.PERIOD);
			agent.currentBehavior = h;
			agent.addBehaviour(h);

		}else if(nextBehavior == LearningAttack.class){

			LearningAttack a = new LearningAttack(agent, FinalAgent.PERIOD, sit.enemy,this);
			agent.currentBehavior = a;
			agent.addBehaviour(a);

		}


	}


	public String prologQuery(String behavior, ArrayList<Object> terms) {
		String query = behavior + "(";
		for (Object t: terms) {
			query += t + ",";
		}
		return query.substring(0,query.length() - 1) + ")";
	}

	public static void executeExplore() {
		//System.out.println("explore");
		nextBehavior = LearningExplore.class;
	}


	public static void executeHunt() {
		//System.out.println("hunt");
		nextBehavior = HuntBehavior.class;
	}

	public static void executeAttack() {
		//System.out.println("attack");
		nextBehavior = LearningAttack.class;
	}


	public static void executeRetreat() {
		//System.out.println("retreat");
		//nextBehavior = RetreatBehavior.class;
	}
	public static Vector3f moveAttack()  {
		ArrayList<Vector3f> points = agent.sphereCast(agent.getSpatial(), AbstractAgent.NEIGHBORHOOD_DISTANCE, AbstractAgent.CLOSE_PRECISION, AbstractAgent.VISION_ANGLE);
		
		String res = "";//getCSVColumns()+"\n";
		
		
		ArrayList<String> listP = new ArrayList<String>();
		String s;
		for(int i = 0 ; i < points.size();i++) {
			s = sit.minAltitude+","+sit.maxAltitude+","+points.get(i).z+","+sit.fovValue+","+sit.lastAction+","+sit.life+",?,";

			listP.add(s);
		}
		
		try{
		    File file = new File(System.getProperty("user.dir")+"/ressources/simushoot/classify.arff");
		    FileWriter fr = new FileWriter(file);
		    BufferedWriter br = new BufferedWriter(fr);
		    PrintWriter pr = new PrintWriter(br);
		    pr.println("@relation shoot ");
		    pr.println("@attribute minAltitude REAL");
		    pr.println("@attribute maxAltitude REAL");
		    pr.println("@attribute currentAltitude REAL");
		    pr.println("@attribute fovValue REAL");
		    pr.println("@attribute lastAction {explore_off,explore_def,follow,shoot,idle}");
		    pr.println("@attribute life numeric");
		    pr.println("@attribute result {DEFEAT,VICTORY}");
		    pr.println("");
		    pr.println("@data");
		    pr.println("");
		    pr.println("");
		    for(int i = 0 ; i < listP.size();i++){
		    	pr.println(listP.get(i));
		    }
		    pr.close();
		    br.close();
		    fr.close();
		}
		catch (IOException e) {
			  System.out.println(e);
			  System.out.println("saving points failed");
			}
		 Instances unlabeled;
		try {
			unlabeled = new Instances(
			         new BufferedReader(
			           new FileReader(System.getProperty("user.dir")+"/ressources/simushoot/classify.arff")));
			// set class attribute
			unlabeled.setClassIndex(unlabeled.numAttributes() - 1);			
			// create copy
			Instances labeled = new Instances(unlabeled);
			double clsLabel;
			// label instances
			
			for (int i = 0; i < unlabeled.numInstances(); i++) {
				
				try {
					//System.out.println(unlabeled.instance(i));
					clsLabel = tree.classifyInstance(unlabeled.instance(i));
					labeled.instance(i).setClassValue(clsLabel);
					//System.out.println(clsLabel + " -> " + unlabeled.classAttribute().value((int) clsLabel));
					if(unlabeled.classAttribute().value((int) clsLabel).equals("VICTORY")) {
						return points.get(i);
					}
					
	
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
			
		} 
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	return null; 

		
		
	}
}
