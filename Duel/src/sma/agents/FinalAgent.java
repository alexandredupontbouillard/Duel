package sma.agents;
import java.util.ArrayList;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import env.jme.Environment;
import env.jme.NewEnv;
import jade.core.behaviours.Behaviour;
import sma.AbstractAgent;
import sma.InterestPoint;
import sma.actionsBehaviours.DumbBehavior;
import sma.actionsBehaviours.ExploreBehavior;
import sma.actionsBehaviours.HuntBehavior;
import sma.actionsBehaviours.PrologBehavior;
import sma.actionsBehaviours.PrologHeightRandomBehavior;
import sma.actionsBehaviours.TempSphereCast;

public class FinalAgent extends AbstractAgent{

	
	private static final long serialVersionUID = 5215165765928961044L;
	
	public static final long PERIOD = 1000;
	
	public enum MoveMode {
		
		NORMAL,
		LEGAL;
		
	}
	
	public boolean friendorFoe;// ?
	
	public ArrayList<InterestPoint> offPoints;
	public ArrayList<InterestPoint> defPoints;
	
	public ExploreBehavior explore;
	public HuntBehavior hunt;
	
	public boolean useProlog;
	
	public int life;
	public long lastHit;
	public boolean dead;
	
	public String lastAction = "idle";
	
	MoveMode mode = MoveMode.NORMAL;
	
	public Behaviour currentBehavior;
	
	protected void setup(){
		super.setup();
		
		
		
		deploiment();
		
		offPoints = new ArrayList<>();
		defPoints = new ArrayList<>();
		
		this.life = AbstractAgent.MAX_LIFE;
		this.dead = false;
		this.lastHit = 0;
		
		
		addToAgents(this);
		
		currentBehavior = null;
		
		
		
		
		teleport(getRandomPosition());
		
	}
	
	public void goTo(Vector3f target){
		if (mode == MoveMode.NORMAL){
			if (getDestination() != null && getDestination().equals(target)){
				return; 
			}
			moveTo(target);
		}else{
			moveTo(target); 
		}
	}
	
	public void lookAt(Vector3f target){
		if (mode == MoveMode.NORMAL){
			((Camera)getSpatial().getUserData("cam")).lookAt(target, Vector3f.UNIT_Y); 
		}else{
			((Camera)getSpatial().getUserData("cam")).lookAt(target, Vector3f.UNIT_Y);
		}
	}
	
	void deploiment(){
		final Object[] args = getArguments();
		if(args[0]!=null && args[1]!=null){
			
			
			useProlog = ((boolean)args[1]);
			
			if(useProlog){
				
				//addBehaviour(new PrologBehavior(this,PERIOD));
				addBehaviour(new PrologHeightRandomBehavior(this,PERIOD));
				//addBehaviour(new LearningProlog(this,PERIOD,getClassifierJ48()));
			}else{
				addBehaviour(new DumbBehavior(this, PERIOD));
			}
			
			
			deployAgent((NewEnv) args[0], useProlog);
			
			System.out.println("Agent "+getLocalName()+" deployed !");
			
			
		}else{
			System.err.println("Malfunction during parameter's loading of agent"+ this.getClass().getName());
			System.exit(-1);
		}
	}
	J48 getClassifierJ48() {
		J48 tree = new J48();
		try {
			DataSource source = new DataSource(System.getProperty("user.dir")+"/ressources/simus/HeightRandom.arff");
			Instances data = source.getDataSet();
			if(data.classIndex() == -1)
				data.setClassIndex(data.numAttributes()-1);
			tree.buildClassifier(data);
			//Evaluation eval = new Evaluation(data);
			//eval.crossValideModel(tree,data,10,new Random(1));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return tree;
	}
	
	
}
