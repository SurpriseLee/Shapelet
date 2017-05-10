package pers.lxs.shapelet.test;

import java.util.ArrayList;

import de.bwaldvogel.liblinear.Feature;
import pers.lxs.shapelet.Shapelet;
import pers.lxs.shapelet.ShapeletUtils;
import pers.lxs.shapelet.Utils;
import pers.lxs.shapelet.shapeletfilter.ShapeletFilter;
import weka.core.Instances;

public class ShapeFilterTest {

	public static void main(String[] args) {
	    long startTime = System.currentTimeMillis();
        try {

            String ARFFName = "dataset/Coffee_TRAIN.arff";
            Instances data = ShapeletUtils.loadData(ARFFName);
            data.setClassIndex(0);

            int k = Integer.MAX_VALUE; // number of shapelets
            int minLength = 2;
            int maxLength = data.get(1).numValues()-1;

            String outPutFile = "dataset/generatedShapelets.txt";
            ShapeletFilter sf = new ShapeletFilter(k, minLength, maxLength);
            sf.setLogOutputFile(outPutFile); // log file stores shapelet output
            ArrayList<Shapelet> filteredShapelets = sf.process(data);
      
            Feature[][] features = ShapeletUtils.transferFeature(filteredShapelets, data);
            
            String transferedData = ShapeletUtils.convertToArff("iris", features);
            
            System.out.println("Arff: " + transferedData);
            
            Utils.writeFile(transferedData, "dataset/transferedData.txt");
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("\nExecution time in milli seconds: "+totalTime);

	}

}
