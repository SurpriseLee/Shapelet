package pers.lxs.shapelet.shapeletfilter;


/*
 * NOTE: As shapelet extraction can be time consuming, there is an option to output shapelets
 * to a text file (Default location is in the root dir of the project, file name "defaultShapeletOutput.txt").
 *
 * Default settings are TO PRODUCE OUTPUT FILE - unless file name is changed, each successive filter will
 * overwrite the output (see "setLogOutputFile(String fileName)" to change file dir and name).
 *
 * To reconstruct a filter from this output, please see the method "createFilterFromFile(String fileName)".
 */

import weka.core.*;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

import pers.lxs.shapelet.OrderLineObj;
import pers.lxs.shapelet.Shapelet;

public class ShapeletFilter {

    private int minShapeletLength;
    private int maxShapeletLength;
    private int numShapelets;
    private boolean shapeletsTrained;
    private ArrayList<Shapelet> shapelets;
    private String ouputFileLocation = "defaultShapeletOutput.txt"; // default store location
    private boolean recordShapelets = true; // default action is to write an output file

    public ShapeletFilter() {
        this.minShapeletLength = -1;
        this.maxShapeletLength = -1;
        this.numShapelets = -1;
        this.shapeletsTrained = false;
        this.shapelets = null;
    }

    /**
     *
     * @param k - the number of shapelets to be generated
     */
    public ShapeletFilter(int k) {
        this.minShapeletLength = -1;
        this.maxShapeletLength = -1;
        this.numShapelets = k;
        this.shapelets = new ArrayList<Shapelet>();
        this.shapeletsTrained = false;
    }

    /**
     *
     * @param k - the number of shapelets to be generated
     * @param minShapeletLength - minimum length of shapelets
     * @param maxShapeletLength - maximum length of shapelets
     */
    public ShapeletFilter(int k, int minShapeletLength, int maxShapeletLength) {
        this.minShapeletLength = minShapeletLength;
        this.maxShapeletLength = maxShapeletLength;
        this.numShapelets = k;
        this.shapelets = new ArrayList<Shapelet>();
        this.shapeletsTrained = false;
    }

    /**
     *
     * @param k - the number of shapelets to be generated
     */
    public void setNumberOfShapelets(int k) {
        this.numShapelets = k;
    }

    /**
     *
     * @param minShapeletLength - minimum length of shapelets
     * @param maxShapeletLength - maximum length of shapelets
     */
    public void setShapeletMinAndMax(int minShapeletLength, int maxShapeletLength) {
        this.minShapeletLength = minShapeletLength;
        this.maxShapeletLength = maxShapeletLength;
    }

    /**
     *
     * @param inputFormat - the format of the input data
     * @return a new Instances object in the desired output format
     * @throws Exception - if all required attributes of the filter are not initialised correctly
     */
    protected Instances determineOutputFormat(Instances inputFormat) throws Exception {

        if (this.numShapelets < 1) {
            throw new Exception(
                    "ShapeletFilter not initialised correctly - please specify a value of k that is greater than 1");
        }

        // Set up instances size and format.
        int length = this.numShapelets;
        FastVector<Attribute> atts = new FastVector();
        String name;
        for (int i = 0; i < length; i++) {
            name = "Shapelet_" + i;
            atts.addElement(new Attribute(name));
        }

        if (inputFormat.classIndex() >= 0) { // Classification set, set class
            // Get the class values as a fast vector
            Attribute target = inputFormat.attribute(inputFormat.classIndex());

            FastVector vals = new FastVector(target.numValues());
            for (int i = 0; i < target.numValues(); i++) {
                vals.addElement(target.value(i));
            }
            atts.addElement(new Attribute(inputFormat.attribute(inputFormat.classIndex()).name(), vals));
        }
        Instances result = new Instances("Shapelets" + inputFormat.relationName(), atts,
                inputFormat.numInstances());
        if (inputFormat.classIndex() >= 0) {
            result.setClassIndex(result.numAttributes() - 1);
        }
        return result;
    }

    /**
     *
     * @param data - the input data to be transformed (and to find the shapelets if this is the first
     * run)
     * @return the transformed Instances in terms of the distance from each shapelet
     * @throws Exception - if the number of shapelets or the length parameters specified are incorrect
     */
    public ArrayList<Shapelet> process(Instances data) throws Exception {
        if (this.numShapelets < 1) {
            throw new Exception(
                    "Number of shapelets initialised incorrectly - please select value of k (Usage: setNumberOfShapelets");
        }

        int maxPossibleLength;
        if (data.classIndex() < 0)
            maxPossibleLength = data.instance(0).numAttributes();
        else
            maxPossibleLength = data.instance(0).numAttributes() - 1;

        if (this.minShapeletLength < 1 || this.maxShapeletLength < 1
                || this.maxShapeletLength < this.minShapeletLength
                || this.maxShapeletLength > maxPossibleLength) {
            throw new Exception("Shapelet length parameters initialised incorrectly");
        }

        if (this.shapeletsTrained == false) { // shapelets discovery has not yet been carried out, so do
            // so
            this.shapelets = findBestKShapeletsCache(this.numShapelets, data, this.minShapeletLength,
                    this.maxShapeletLength); // get k shapelets ATTENTION
            this.shapeletsTrained = true;
            System.out.println("\n"+shapelets.size() + " Shapelets have been generated");
        }

//        Instances output = determineOutputFormat(data);
//
//        // for each data, get distance to each shapelet and create new instance
//        for (int i = 0; i < data.numInstances(); i++) { // for each data
//            Instance toAdd = new SparseInstance(this.shapelets.size() + 1);
//            int shapeletNum = 0;
//            for (Shapelet s : this.shapelets) {
//                double dist = subsequenceDistance(s.content, data.instance(i));
//                toAdd.setValue(shapeletNum++, dist);
//            }
//            toAdd.setValue(this.shapelets.size(), data.instance(i).classValue());
//            output.add(toAdd);
 //       }
        return shapelets;
    }

    public void setLogOutputFile(String fileName) {
        this.recordShapelets = true;
        this.ouputFileLocation = fileName;
    }

    public void turnOffLog() {
        this.recordShapelets = false;
    }

    /**
     *
     * @param numShapelets - the target number of shapelets to generate
     * @param data - the data that the shapelets will be taken from
     * @param minShapeletLength - the minimum length of possible shapelets
     * @param maxShapeletLength - the maximum length of possible shapelets
     * @return an ArrayList of Shapelet objects in order of their fitness (by infoGain, seperationGap
     * then shortest length)
     */
    private ArrayList<Shapelet> findBestKShapeletsCache(int numShapelets, Instances data,
                                                        int minShapeletLength, int maxShapeletLength) throws Exception {

        long startTime = System.nanoTime();
        Double[] rawContent;

        ArrayList<Shapelet> kShapelets = new ArrayList<Shapelet>(); // store (upto) the best k shapelets
        // overall
        ArrayList<Shapelet> seriesShapelets = new ArrayList<Shapelet>(); // temp store of all shapelets
        // for each time series

    /*
     * new version to allow caching: - for all time series, calculate the gain of all candidates of
     * all possible lengths - insert into a strucutre in order of fitness - arraylist with
     * comparable implementation of shapelets - once all candidates for a series are established,
     * integrate into store of k best
     */

        TreeMap<Double, Integer> classDistributions = getClassDistributions(data); // used to calc info
        // gain

        // for all time series
        System.out.println("Processing data: ");
        int numInstances = data.numInstances();
        for (int i = 0; i < numInstances; i++) {

            if (i == 0 || i % (numInstances / 4) == 0) {
                System.out.println("Currently processing instance " + (i + 1) + " of " + numInstances);
            }

            double[] wholeCandidate = data.instance(i).toDoubleArray();
            seriesShapelets = new ArrayList<Shapelet>();

            for (int length = minShapeletLength; length <= maxShapeletLength; length++) {

                // for all possible starting positions of that length
                for (int start = 0; start <= wholeCandidate.length - length - 1; start++) { // -1 = avoid
                    // classVal -
                    // handle later
                    // for series
                    // with no class
                    // val
                    // CANDIDATE ESTABLISHED - got original series, length and starting position
                    // extract relevant part into a double[] for processing
                    double[] candidate = new double[length];
                    rawContent = new Double[length +1];
                    for (int m = start; m < start + length; m++) {
                        candidate[m - start] = wholeCandidate[m];
                        rawContent[m - start] = wholeCandidate[m];
                    }

                    // znorm candidate here so it's only done once, rather than in each distance calculation
                    rawContent[length] = data.instance(i).classValue();
                    candidate = zNorm(candidate, false);
                    Shapelet candidateShapelet = checkCandidate(candidate, data, i, start, classDistributions,rawContent);
                    seriesShapelets.add(candidateShapelet);
                }
            }
            // now that we have all shapelets, self similarity can be fairly assessed without fear of
            // removing potentially
            // good shapelets
            Collections.sort(seriesShapelets);
            seriesShapelets = removeSelfSimilar(seriesShapelets);
            kShapelets = combine(numShapelets, kShapelets, seriesShapelets);
        }

        if (this.recordShapelets) {
            FileWriter out = new FileWriter(this.ouputFileLocation);
            for (int i = 0; i < kShapelets.size(); i++) {
                out.append(kShapelets.get(i).informationGain + "," + kShapelets.get(i).seriesId + ","
                        + kShapelets.get(i).startPos + "\n");
                /*Uncomment this code block to write information gain to the file */
//                double[] shapeletContent = kShapelets.get(i).content;
//
//                for (int j = 0; j < shapeletContent.length; j++) {
//                    out.append(shapeletContent[j] + ",");
//                }

                /*Uncomment this code block to write raw content of the shapelet to the file */
                Double[] shapeletRawContent = kShapelets.get(i).rawContent;

                for (int j = 0; j < shapeletRawContent.length; j++) {
                    out.append(shapeletRawContent[j] + ",");
                }
                out.append("\n");
            }
            out.close();
        }

        return kShapelets;
    }

    /**
     *
     * @param shapelets the input Shapelets to remove self similar Shapelet objects from
     * @return a copy of the input ArrayList with self-similar shapelets removed
     */
    private static ArrayList<Shapelet> removeSelfSimilar(ArrayList<Shapelet> shapelets) {
        // return a new pruned array list - more efficient than removing
        // self-similar entries on the fly and constantly reindexing
        ArrayList<Shapelet> outputShapelets = new ArrayList<Shapelet>();
        boolean[] selfSimilar = new boolean[shapelets.size()];

        // to keep track of self similarity - assume nothing is similar to begin with
        for (int i = 0; i < shapelets.size(); i++) {
            selfSimilar[i] = false;
        }

        for (int i = 0; i < shapelets.size(); i++) {
            if (selfSimilar[i] == false) {
                outputShapelets.add(shapelets.get(i));
                for (int j = i + 1; j < shapelets.size(); j++) {
                    if (selfSimilar[j] == false && selfSimilarity(shapelets.get(i), shapelets.get(j))) { // no
                        // point
                        // recalc'ing
                        // if
                        // already
                        // self
                        // similar
                        // to
                        // something
                        selfSimilar[j] = true;
                    }
                }
            }
        }
        return outputShapelets;
    }

    /**
     *
     * @param k the maximum number of shapelets to be returned after combining the two lists
     * @param kBestSoFar the (up to) k best shapelets that have been observed so far, passed in to
     * combine with shapelets from a new series
     * @param timeSeriesShapelets the shapelets taken from a new series that are to be merged in
     * descending order of fitness with the kBestSoFar
     * @return an ordered ArrayList of the best k (or less) Shapelet objects from the union of the
     * input ArrayLists
     */

    // NOTE: could be more efficient here
    private ArrayList<Shapelet> combine(int k, ArrayList<Shapelet> kBestSoFar,
                                        ArrayList<Shapelet> timeSeriesShapelets) {

        ArrayList<Shapelet> newBestSoFar = new ArrayList<Shapelet>();
        for (int i = 0; i < timeSeriesShapelets.size(); i++) {
            kBestSoFar.add(timeSeriesShapelets.get(i));
        }
        Collections.sort(kBestSoFar);
        if (kBestSoFar.size() < k)
            return kBestSoFar; // no need to return up to k, as there are not k shapelets yet

        for (int i = 0; i < k; i++) {
            newBestSoFar.add(kBestSoFar.get(i));
        }

        return newBestSoFar;
    }

    /**
     *
     * @param data the input data set that the class distributions are to be derived from
     * @return a TreeMap<Double, Integer> in the form of <Class Value, Frequency>
     */
    public static TreeMap<Double, Integer> getClassDistributions(Instances data) {
        TreeMap<Double, Integer> classDistribution = new TreeMap<Double, Integer>();
        double classValue;
        for (int i = 0; i < data.numInstances(); i++) {
            classValue = data.instance(i).classValue();
            boolean classExists = false;
            for (Double d : classDistribution.keySet()) {
                if (d == classValue) {
                    int temp = classDistribution.get(d);
                    temp++;
                    classDistribution.put(classValue, temp);
                    classExists = true;
                }
            }
            if (classExists == false) {
                classDistribution.put(classValue, 1);
            }
        }
        return classDistribution;
    }

    /**
     *
     * @param candidate the data from the candidate Shapelet
     * @param data the entire data set to compare the candidate to
     * @param data the entire data set to compare the candidate to
     * @return a TreeMap<Double, Integer> in the form of <Class Value, Frequency>
     */
    private static Shapelet checkCandidate(double[] candidate, Instances data, int seriesId,
                                           int startPos, TreeMap classDistribution,Double[] rawContent) {

        // create orderline by looping through data set and calculating the subsequence
        // distance from candidate to all data, inserting in order.
        ArrayList<OrderLineObj> orderline = new ArrayList<OrderLineObj>();

        for (int i = 0; i < data.numInstances(); i++) {
            double distance = subsequenceDistance(candidate, data.instance(i));
            double classVal = data.instance(i).classValue();

            // without early abandon, it is faster to just add and sort at the end
            orderline.add(new OrderLineObj(distance, classVal));
        }
        Collections.sort(orderline, new orderLineComparator());

        // create a shapelet object to store all necessary info, i.e.
        // content, seriesId, then calc info gain, plit threshold and separation gap
        Shapelet shapelet = new Shapelet(candidate, seriesId, startPos);
        shapelet.rawContent = rawContent;
        shapelet.calcInfoGainAndThreshold(orderline, classDistribution);

        // note: early abandon entropy pruning would appear here, but has been ommitted
        // in favour of a clear multi-class information gain calculation. Could be added in
        // this method in the future for speed up, but distance early abandon is more important

        return shapelet;
    }

    // for sorting the orderline
    private static class orderLineComparator implements Comparator<OrderLineObj> {
        public int compare(OrderLineObj o1, OrderLineObj o2) {
            if (o1.distance < o2.distance)
                return -1;
            else if (o1.distance > o2.distance)
                return 1;
            else
                return 0;
        }
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
 
    private static boolean selfSimilarity(Shapelet shapelet, Shapelet candidate) {
        if (candidate.seriesId == shapelet.seriesId) {
            if (candidate.startPos >= shapelet.startPos
                    && candidate.startPos < shapelet.startPos + shapelet.content.length) { // candidate starts
                // within exisiting
                // shapelet
                return true;
            }
            if (shapelet.startPos >= candidate.startPos
                    && shapelet.startPos < candidate.startPos + candidate.content.length) {
                return true;
            }
        }
        return false;
    }




    // /**
    // *
    // * @param args
    // * @throws Exception
    // */
    // public static void main(String[] args) throws Exception{
    // ShapeletFilter sf = new ShapeletFilter(10, 5, 5);
    // Instances data = loadData("example.arff");
    //
    // sf.process(data);
    // }

    public static ShapeletFilter createFilterFromFile(String fileName) throws Exception {

        File input = new File(fileName);
        Scanner scan = new Scanner(input);
        scan.useDelimiter("\n");

        ShapeletFilter sf = new ShapeletFilter();
        ArrayList<Shapelet> shapelets = new ArrayList<Shapelet>();

        String shapeletContentString;
        ArrayList<Double> content;
        double[] contentArray;
        Scanner lineScan;

        while (scan.hasNext()) {
            scan.next();
            shapeletContentString = scan.next();

            lineScan = new Scanner(shapeletContentString);
            lineScan.useDelimiter(",");

            content = new ArrayList<Double>();
            while (lineScan.hasNext()) {
                content.add(Double.parseDouble(lineScan.next().trim()));
            }

            contentArray = new double[content.size()];
            for (int i = 0; i < content.size(); i++) {
                contentArray[i] = content.get(i);
            }

            Shapelet s = new Shapelet(contentArray);
            shapelets.add(s);
        }

        sf.shapelets = shapelets;
        sf.shapeletsTrained = true;
        sf.numShapelets = shapelets.size();
        sf.setShapeletMinAndMax(1, 1);

        return sf;
    }

}
