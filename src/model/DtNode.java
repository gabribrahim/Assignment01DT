package model;

import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.control.TextArea;
import prefuse.data.Node;
import prefuse.data.Tree;
import prefuse.demos.TreeView;

public class DtNode {
	ArrayList<LabelledDataInstance> containedInstances ;
	String nodeName;
	DataSetsLoader parentDataSet;
	
	boolean debug;
	ArrayList <DtNode> children					  	= new ArrayList<>();
	DtNode leftSideNode;
	DtNode rightSideNode;
	ArrayList<LabelledDataInstance> instancesTrue 	= new ArrayList<>();
	ArrayList<LabelledDataInstance> instancesFalse 	= new ArrayList<>();
	ArrayList<String> attrsList;
	ArrayList<String> originalAttrslist;
	String bestAttr     						  	= "";
	String attributeToSplitOn						= "";
	Node visualRepNode;
	public double geniImpurityThreshold					=0.0;
	int numberOfAttrsAtInitiation ;
	String baseClassifier							= "live";
	
	public void printListofInstance(ArrayList<LabelledDataInstance>instances) {
		
		for (LabelledDataInstance debuginstance :instances) {
			System.out.println(debuginstance.labelName+","+debuginstance.featureListAsValues);
		}		
	}
	public String predict(LabelledDataInstance testInstance, TextArea debug) {
		String result;
		if (isPure()) {
//			System.out.println("Passing Prediction AS"+ containedInstances.get(0).labelName);
//			printListofInstance(containedInstances);
			return containedInstances.get(0).labelName;
		}
		
		if (isEmpty() && !isPure()) {
			return baseClassifier;
		}

		int featureIndex							= originalAttrslist.indexOf(this.attributeToSplitOn);
		boolean valueOfFeature						= testInstance.featureListAsValues.get(featureIndex);

		String debugMessage =" Node>"+this.attributeToSplitOn+" "+containedInstances.size()+" "+ valueOfFeature+"\n";
		pp(debugMessage);
		
		debug.appendText(debugMessage);
		if (valueOfFeature) {
			result = leftSideNode.predict(testInstance,debug);
		}
		else {
			result = rightSideNode.predict(testInstance,debug);
		}
		return result;
	}
	public String getBestAttr() {
		
		if (isEmpty()) {
			return "BaseClassifier Live";
		}
		if (isPure() && !isEmpty()) {
			return containedInstances.get(0).labelName + "\n"+computeNodeProbabilities();
		}
		if (bestAttr.equals("")){return nodeName;}
		return  bestAttr +"\n" + instancesTrue.size() + "T<>" + instancesFalse.size()+"F\n"+computeNodeProbabilities()+
				"\n"+attrsList.size()+"/"+numberOfAttrsAtInitiation;
	}

	@Override
	public String toString() {
		return getBestAttr()+" [containedInstances=" + containedInstances.size() + ", nodeName=" + nodeName + ", children=" + children.size()
				+ ", instancesTrue=" + instancesTrue.size() + ", instancesFalse=" + instancesFalse.size() + "]";
	}

	public DtNode(ArrayList<LabelledDataInstance> containedInstances,ArrayList<String>attrsList, ArrayList<String>originalAttrslist) {
		super();
		this.containedInstances		= containedInstances;
		this.attrsList				= attrsList;
		this.debug					= true;
		this.originalAttrslist		= originalAttrslist;
		this.numberOfAttrsAtInitiation = attrsList.size();
		System.out.println(containedInstances.size()+" " + attrsList.size());
	}
	
	private void pp2(ArrayList<?> listOfthingsToPrint) {
		if (!this.debug){return;}
		for (Object toprint: listOfthingsToPrint) {
			System.out.print(toprint);
			System.out.print(",");
		}
		System.out.println("");
	}
	private void pp(String message) {
//		if (containedInstances.size()<49) {System.out.println(message);}
		if (this.debug) {System.out.println(message);}
	}
	
	public String computeNodeProbabilities() {
		int liveCount 				= 0;
		int dieCount				= 0;
		for (LabelledDataInstance instance : containedInstances) {
			if (instance.labelName.equals("live")) {liveCount++;}
			if (instance.labelName.equals("die")) {dieCount++;}
		}
		double plive				= (double)liveCount / (double)containedInstances.size();
		double pdie					= (double)dieCount / (double)containedInstances.size();
		String probs				="L"+String.format("%.2f", plive)+" D"+String.format("%.2f", pdie)+"\n";
		probs						+= "#L"+ liveCount +" #D" + dieCount+" #Total"+ containedInstances.size();
		return probs;
	}
	public double computeGeniImpurity(String attribute){
		int indexOfAttribute = originalAttrslist.indexOf(attribute);
		instancesTrue.clear();
		instancesFalse.clear();
		
		pp("Checking Attr:. "+attribute+" "+containedInstances.size());
		
		for (LabelledDataInstance instance :containedInstances) {
			boolean attributeBoolean			= instance.featureListAsValues.get(indexOfAttribute);
//			System.out.println(attribute + "____"+String.valueOf(attributeBoolean)+instance.labelName+
//					indexOfAttribute);
			if (attributeBoolean==true) {
				instancesTrue.add(instance);
			}
			else {
				instancesFalse.add(instance);
			}
		}
		
		pp("True Instances = "+instancesTrue.size());
		if (instancesTrue.size()==2) {
			pp(instancesTrue.get(0).labelName+","+instancesTrue.get(0).featureListAsValues);
			pp(instancesTrue.get(0).labelName+","+instancesTrue.get(0).featureListAsValues);
			}
		pp("False Instances = "+instancesFalse.size());
		if (instancesFalse.size()==2) {
			pp(instancesFalse.get(0).labelName+","+instancesFalse.get(0).featureListAsValues);
			pp(instancesFalse.get(0).labelName+","+instancesFalse.get(0).featureListAsValues);
			}				
		int liveCount 				= 0;
		int dieCount				= 0;
		double trueImpurity		    = 0;
		double falseImpurity		= 0;
		for (LabelledDataInstance instance:instancesTrue) {
			if (instance.labelName.equals("live")) {liveCount ++;}
			if (instance.labelName.equals("die")) {dieCount ++;}
		}
		if (instancesTrue.size()>0) {
			double weightOfTrueInstances		= (double) instancesTrue.size() /(double)containedInstances.size();
			trueImpurity						= weightOfTrueInstances*((2*(double)liveCount *(double)dieCount)
													/Math.pow((liveCount+dieCount),2)) ;
			}
		
//		System.out.print(weightOfTrueInstances + " ");
		pp((instancesTrue.size()>0) + " --True Impurity = "+trueImpurity + "LiveCount:"+liveCount+" DieCount:"+dieCount);

		liveCount 							= 0;
		dieCount							= 0;
		for (LabelledDataInstance instance:instancesFalse) {
			if (instance.labelName.equals("live")) {liveCount ++;}
			if (instance.labelName.equals("die")) {dieCount ++;}
		}
		
		if (instancesFalse.size()>0) {
			double weightOfFalseInstances		= (double) instancesFalse.size() /(double)containedInstances.size();
			falseImpurity						= weightOfFalseInstances*(2*(double)liveCount *(double)dieCount)
													/Math.pow((liveCount+dieCount),2) ;
			
			}
		pp((instancesFalse.size()>0)+" --False Impurity = "+falseImpurity+ "LiveCount:"+liveCount+" DieCount:"+dieCount);
		
		double nodeImpurity			= (trueImpurity + falseImpurity)/2.0;
		pp("Avg Impurity = "+nodeImpurity);
		return nodeImpurity;
				
	}
	
	public boolean isEmpty() {
		if (containedInstances.size()==0) {return true;}
		return false;
		
	}
	
	public boolean isPure() {
		String label						= containedInstances.get(0).labelName;
		for (LabelledDataInstance instance: containedInstances) {
			if (!label.equals(instance.labelName)) {return false;}
		}
		return true;
	}
		
	public void decideBestAttribute() {
		double bestImpurity = 1.0 ;
		
		double impurtiyForAttribute=2;
		HashMap<String, Double> impurities  = new HashMap<String, Double>();
		for (String attribute : attrsList) {
			impurtiyForAttribute					= computeGeniImpurity(attribute);
			if (impurtiyForAttribute<geniImpurityThreshold) {
				bestImpurity						= impurtiyForAttribute;
				bestAttr							= attribute;
				impurities.put(attribute, impurtiyForAttribute);
				break;
			}			
			if (impurtiyForAttribute==0) {
				bestImpurity						= impurtiyForAttribute;
				bestAttr							= attribute;
				impurities.put(attribute, impurtiyForAttribute);
				break;
			}
			
			if (impurtiyForAttribute<bestImpurity) {
				bestImpurity						= impurtiyForAttribute;
				bestAttr							= attribute;
				impurities.put(attribute, impurtiyForAttribute);
			}
			
		}
		System.out.println("Removing"+bestAttr+ "Attrs List Len = "+attrsList.size()+"____"+containedInstances.size()+" "+impurtiyForAttribute);			
		attrsList.remove(attrsList.indexOf(bestAttr));
//		System.out.println("Removed "+bestAttr+ "Attrs List Len = "+attrsList.size()+"____"+containedInstances.size());
		computeGeniImpurity(bestAttr);
		attributeToSplitOn							=(String)bestAttr;
		nodeName									= bestAttr;
//		System.out.println("!!!"+ bestAttr + " " + containedInstances.size() +
//							" Split into True: "+ instancesTrue.size() + " & False: "+ instancesFalse.size() 
//							+ " Imp:"+bestImpurity);
//		System.out.println("\t"+impurities);
		
			
	}
	public String report() {
		String reportString = "";
//		System.out.println(this);
		reportString += toString()+"\n";
			
		for (DtNode child : this.children) {
			reportString += child.report();
		}
		
		return reportString;
				
	}
	public void visualNode(Node startNode, Tree decisionTreeModel, TreeView decisionTreeView) {
		startNode.setString("label", getBestAttr());
//		visualRepNode							= startNode;
		for (DtNode child :this.children) {
			Node childVisualNode				= decisionTreeModel.addChild(startNode);
			childVisualNode.setString("label", child.getBestAttr());
			child.visualNode(childVisualNode, decisionTreeModel,decisionTreeView);

		
//		this.leftSideNode
//		Node childVisualNode				= decisionTreeModel.addChild(startNode);
//		childVisualNode.setString("label", leftSideNode.getBestAttr());	
//		this.leftSideNode.visualNode(childVisualNode, decisionTreeModel,decisionTreeView);
		}
		

		
	}
	
	public DtNode branchNode() {
		if (isEmpty()) {return this;}
		
		if(isPure()) {return this;}
		
		if (attrsList.size()==0) {return this;}
		
		decideBestAttribute();
		
		ArrayList<String> attrsClone= new ArrayList<String>(attrsList) ;
//		System.out.println(attributeToSplitOn+" List size" + attrsList.size() +" "+ attrsClone.size());
		leftSideNode				= new DtNode(instancesTrue,attrsClone,originalAttrslist);
		rightSideNode				= new DtNode(instancesFalse,attrsClone,originalAttrslist);
		leftSideNode.geniImpurityThreshold	= this.geniImpurityThreshold;
		rightSideNode.geniImpurityThreshold	=this.geniImpurityThreshold;
		leftSideNode.branchNode();
		rightSideNode.branchNode();
		
		
		
		this.children.add(leftSideNode);
		this.children.add(rightSideNode);
		leftSideNode.nodeName		= this.nodeName+"_TRUE";
		rightSideNode.nodeName		= this.nodeName+"_FALSE";
		return this;
		
	}

}
