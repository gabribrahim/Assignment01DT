package application;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.DataSetsLoader;
import model.DtNode;
import model.LabelledDataInstance;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Node;
import prefuse.data.Tree;
import prefuse.demos.TreeView;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

public class MainController {
	// UI ELEMENTS
	@FXML TextArea StatusTA;
	@FXML Label MainLabel;	
	@FXML SwingNode TreeP;
	@FXML HBox ChartBox;
	@FXML TextField GeniThresholdTF;
	@FXML Label StatusLB;
	@FXML VBox TreeSnapShot;
	private Main main;
	private DataSetsLoader myDataLoader = new DataSetsLoader();
	public List<String> featuresList 	= new ArrayList<String>();
	public Node treeRootVisualNode;
	public Tree decisionTreeModel;
	public TreeView decisionTreeView;
	public DtNode rootNode;
	public void setMain(Main main) {
		this.main = main;
//		loadDataSet();		
//		buildTree();
//		expandAllNode();
//		zoomToFitTree();
//		TestSN.setContent(graphComponent);

		
//		System.out.println(myDataLoader);
		

	}
	
	public void LoadTestRunN() {
		TextInputDialog dialog = new TextInputDialog("1");
		dialog.setTitle("Load Split Data Set");
		dialog.setHeaderText("There are 10 Test Runs as per Assignment handout\n"
				+"Please kindly provide a number between 1 & 10");
		dialog.setContentText("Test Run Number = ");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			int fileNumber				= Integer.parseInt(result.get());
			String trainingFileName		= "hepatitis-training-run"+String.format("%02d",fileNumber )+".dat";
			String testFileName			= "hepatitis-test-run"+String.format("%02d",fileNumber )+".dat";
			
			System.out.println("Loading Hep DataSet");
			myDataLoader.dataSetName = "Hepatitis TestRun "+ String.valueOf(fileNumber);
			myDataLoader.clear();

			String trainingFilePath		= System.getProperty("user.dir").replace('\\', '/') + "/"+trainingFileName;
			String testFilePath			= System.getProperty("user.dir").replace('\\', '/') + "/"+testFileName;
			myDataLoader.loadHepDataSet(trainingFilePath, myDataLoader.trainingDataSetList);
			StatusTA.setText(myDataLoader.toString());
			myDataLoader.loadHepDataSet(testFilePath, myDataLoader.testDataSetList);
			StatusTA.appendText("Test Data Set Size = " + myDataLoader.testDataSetList.size());			
		}			
	}
	public void loadDataSet() {
		System.out.println("Loading Hep DataSet");
		myDataLoader.dataSetName = "Hepatitis Dataset";
		myDataLoader.clear();

		String trainingFilePath		= System.getProperty("user.dir").replace('\\', '/') + "/hepatitis-training.dat";
		String testFilePath			= System.getProperty("user.dir").replace('\\', '/') + "/hepatitis-test.dat";
		myDataLoader.loadHepDataSet(trainingFilePath, myDataLoader.trainingDataSetList);
		StatusTA.setText(myDataLoader.toString());
		myDataLoader.loadHepDataSet(testFilePath, myDataLoader.testDataSetList);
		StatusTA.appendText("Test Data Set Size = " + myDataLoader.testDataSetList.size());

	
	}
	public void zoomToFitTree() {
    	
		Display display = (Display)decisionTreeView;
        Visualization vis = display.getVisualization();
        Rectangle2D bounds = vis.getBounds("_all_");
        GraphicsLib.expand(bounds, 10 + (int)(1/display.getScale()));
        DisplayLib.fitViewToBounds(display, bounds, 100);
//        System.out.println(display);

		decisionTreeView.getVisualization().run("fontAction");
	}
	public void expandAllNode() {
		decisionTreeView.getVisualization().setValue("_all_", null, VisualItem.EXPANDED, true);
		decisionTreeView.setOrientation(2);
		decisionTreeView.getVisualization().run("treeLayout");
		decisionTreeView.getVisualization().run("repaint");
		decisionTreeView.getVisualization().run("color");
		decisionTreeView.getVisualization().run("layout");
		decisionTreeView.getVisualization().run("fullPaint");
		decisionTreeView.getVisualization().run("subLayout");
		decisionTreeView.getVisualization().run("textColor");
		decisionTreeView.getVisualization().run("animatePaint");
		decisionTreeView.getVisualization().run("animate");
		decisionTreeView.getVisualization().run("edgeColor");
		
	}
	public void debug() {
		LabelledDataInstance testInstance 	= myDataLoader.testDataSetList.get(26);
		String prediction 					= rootNode.predict(testInstance,StatusTA);
		StatusTA.insertText(0,"Prediction="+prediction+"\nClass="+testInstance.labelName+"\n");
	}
	public void showBaslineClassifierPerformance() {
		int liveCount						= 0;
		int dieCount						= 0;
		double accuracy						= 0.0;
		String baselineLabel				= "";
		int hit								= 0;
		for (LabelledDataInstance testInstance: myDataLoader.trainingDataSetList) {
		if (testInstance.labelName.equals("live")) {liveCount++;}
		if (testInstance.labelName.equals("die")) {dieCount++;}
		}
		if (liveCount>dieCount) {baselineLabel="live";}
		if (liveCount<dieCount) {baselineLabel="die";}
		
		for (LabelledDataInstance testInstance : myDataLoader.testDataSetList) {
			if (testInstance.labelName.equals(baselineLabel)) {hit++;}
			}
		accuracy							= (double)hit / myDataLoader.testDataSetList.size();
		StatusTA.setText("Basline Classifier Accuracy="+accuracy+"SuccessFul Predictions="+hit+"\n"
						+"Training Set Break Down\nClasses Labelled Live:"+liveCount+"\nClasses Labelled Die:"+dieCount);
		
	}
	public void measurePerformanceOnTestDataSet() {
		int hit								= 0;
		StatusTA.setText("");
		for (LabelledDataInstance testInstance : myDataLoader.testDataSetList) {
			int testInstanceIndex = myDataLoader.testDataSetList.indexOf(testInstance);
			StatusTA.appendText("==TestInstance[ "+testInstanceIndex+" ]======\n");
			String prediction 				= rootNode.predict(testInstance,StatusTA);			
			StatusTA.appendText("Prediction:"+ prediction +"\nTrueLabel:"+testInstance.labelName+"\n");
			StatusTA.appendText("========================\n");
			if (prediction.equals(testInstance.labelName)) {
				hit++;
			}
		double accuracy						= (double)hit /(double)myDataLoader.testDataSetList.size();
		StatusLB.setText("Decision Tree @ Geni Impurity Threshold : "+
		GeniThresholdTF.getText() + "Has Accuracy = "+accuracy
		+"\nSize of TestSet"+myDataLoader.testDataSetList.size() + " Correct Predictions="+hit);
		}
	}
	
	public void buildTree() {
		decisionTreeModel					= new Tree();
		
		treeRootVisualNode					= decisionTreeModel.addRoot();
		decisionTreeModel.addColumn("label",String.class,"Root");
		decisionTreeView           			= new TreeView(decisionTreeModel,"label");
		JPanel panel 						= new JPanel(new BorderLayout());		
		panel.add(decisionTreeView);
		decisionTreeView.setBackground(Color.darkGray);
		TreeP.setContent(panel);
		
		decisionTreeView.getVisualization().run("repaint");
		decisionTreeView.getVisualization().run("color");
		decisionTreeView.getVisualization().run("layout");
		decisionTreeView.getVisualization().run("fullPaint");
		
		ArrayList<String> attrs						= new ArrayList<String>(myDataLoader.dataSetAttrsLabels);
		ArrayList<String> originalAttrs				= new ArrayList<String>(myDataLoader.dataSetAttrsLabels);
		System.out.println(originalAttrs);
		System.out.println(myDataLoader.dataSetAttrsLabels);
		rootNode									= new DtNode(myDataLoader.trainingDataSetList,attrs,originalAttrs);
		rootNode.geniImpurityThreshold				= Double.parseDouble(GeniThresholdTF.getText());
		rootNode.branchNode();
		rootNode.visualNode(treeRootVisualNode,decisionTreeModel,decisionTreeView);
		expandAllNode();
//		zoomToFitTree();
//		decisionTreeView.setOrientation(2);

//		StatusTA.setText(rootNode.report());
		StatusLB.setText("Decision Tree @ Geni Impurity Threshold : "+ GeniThresholdTF.getText());
//		saveSnapShot();
		
		
	}	
	
	private String promptUserForChoice(List<String> dialogData, String message) {
		ChoiceDialog<String> dialog = new ChoiceDialog<String>(dialogData.get(0), dialogData);
		dialog.setTitle("");
		dialog.setHeaderText(message);
		Optional<String> result = dialog.showAndWait();
		String selected = "cancelled.";

		if (result.isPresent()) {

			selected = result.get();
		}

		return selected;
	}
	
	public void saveAsPng(String fileName) {
		double scale							= 5;
		Bounds bounds 							= TreeSnapShot.getLayoutBounds();
		WritableImage image 					= new WritableImage(
	            (int) Math.round(bounds.getWidth() * scale),
	            (int) Math.round(bounds.getHeight() * scale));
		
		SnapshotParameters snapshotParams   	= new SnapshotParameters();
		snapshotParams.setFill(javafx.scene.paint.Color.rgb(40, 40, 40, 1));
		snapshotParams.setTransform(javafx.scene.transform.Transform.scale(scale, scale));
		
//	    WritableImage image2 					= TreeP.snapshot(snapshotParams,null);
    	
	    ImageView view 							= new ImageView(TreeSnapShot.snapshot(snapshotParams, image));
	    File file = new File(fileName+".png");
	    
	    try {
	        ImageIO.write(SwingFXUtils.fromFXImage(view.getImage(), null), "png", file);
	    } catch (IOException e) {
	        
	    }
	}	
	public void saveSnapShot() {
		saveAsPng("DTree_" + GeniThresholdTF.getText()+"_geniThreshold");
	}
		
}
