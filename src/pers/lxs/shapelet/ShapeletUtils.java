package pers.lxs.shapelet;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import pers.lxs.shapelet.datasetconverter.ArffConverter;
import weka.core.Instance;
import weka.core.Instances;

public class ShapeletUtils {
	
   /**
    * load data set into memory
    * @param fileName	file path
    * @return
    */
   public static Instances loadData(String fileName) {
       Instances data = null;
       try {
           FileReader r;
           r = new FileReader(fileName);
           data = new Instances(r);          
       }
       catch (Exception e) {
           System.out.println(" Error =" + e + " in method loadData");
           e.printStackTrace();
       }
       return data;
   }
   
   /**
    * transfer data set into shapelet's feature
    * @param shapelets
    * @param data
    * @return
    */
   public static Feature[][] transferFeature(List<Shapelet> shapelets, Instances data) {
	   Feature[][] features = new Feature[data.numInstances()][shapelets.size()];
	   
	   for(int i = 0; i < data.numInstances(); i++) {
		   for(int j = 0; j < shapelets.size(); j++) {
			   double distance = subsequenceDistance(shapelets.get(j).content, data.get(i));
			   features[i][j] = new FeatureNode(j, distance);
		   }		   
	   }	   
	   
	   return features;
   }
   
   public static String convertToArff(String relationName, Feature[][] features) {
	   List<double[]> data = new ArrayList<double[]>();
	   
	   for(int i = 0; i < features.length; i++) {
		   double[] sample = new double[features[i].length];
		   for(int j = 0; j < features[i].length; j++) {
			   sample[features[i][j].getIndex()] = features[i][j].getValue();
		   }	
		   data.add(sample);
	   }	   
	   
	   return new ArffConverter().convertToArff(relationName, data, 0);
   }

   

   /**
    *
    * @param candidate
    * @param timeSeriesIns
    * @return
    */
   public static double subsequenceDistance(double[] candidate, Instance timeSeriesIns) {
       double[] timeSeries = timeSeriesIns.toDoubleArray();
       return subsequenceDistance(candidate, timeSeries);
   }

   public static double subsequenceDistance(double[] candidate, double[] timeSeries) {

       double bestSum = Double.MAX_VALUE;
       double sum = 0;
       double[] subseq;

       // for all possible subsequences of two
       for (int i = 0; i <= timeSeries.length - candidate.length - 1; i++) {
           sum = 0;
           // get subsequence of two that is the same lenght as one
           subseq = new double[candidate.length];

           for (int j = i; j < i + candidate.length; j++) {
               subseq[j - i] = timeSeries[j];
           }
           subseq = zNorm(subseq, false); // Z-NORM HERE
           for (int j = 0; j < candidate.length; j++) {
               sum += (candidate[j] - subseq[j]) * (candidate[j] - subseq[j]);
           }
           if (sum < bestSum) {
               bestSum = sum;
           }
       }
       return Math.sqrt((1.0 / candidate.length * bestSum));
   }

   /**
    *
    * @param input
    * @param classValOn
    * @return
    */
   public static double[] zNorm(double[] input, boolean classValOn) {
       double mean;
       double stdv;

       double classValPenalty = 0;
       if (classValOn) {
           classValPenalty = 1;
       }
       double[] output = new double[input.length];
       double seriesTotal = 0;

       for (int i = 0; i < input.length - classValPenalty; i++) {
           seriesTotal += input[i];
       }

       mean = seriesTotal / (input.length - classValPenalty);
       stdv = 0;
       for (int i = 0; i < input.length - classValPenalty; i++) {
           stdv += (input[i] - mean) * (input[i] - mean);
       }

       stdv = stdv / input.length - classValPenalty;
       stdv = Math.sqrt(stdv);

       for (int i = 0; i < input.length - classValPenalty; i++) {
           output[i] = (input[i] - mean) / stdv;
       }

       if (classValOn == true) {
           output[output.length - 1] = input[input.length - 1];
       }

       return output;
   }

   
}
