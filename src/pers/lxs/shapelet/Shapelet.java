package pers.lxs.shapelet;


import java.util.ArrayList;
import java.util.TreeMap;

public  class Shapelet implements Comparable<Shapelet> {
    public Double[] rawContent;
    public double[] content;
    protected ArrayList<ArrayList<Double>> contentInMergedShapelets;
    public int seriesId;
    public int startPos;
    protected double splitThreshold;
    public double informationGain;
    protected double separationGap;


    public Shapelet(double[] content, int seriesId, int startPos) {
        this.content = content;
        this.seriesId = seriesId;
        this.startPos = startPos;
    }

    protected Shapelet(ArrayList<ArrayList<Double>> content){
        this.contentInMergedShapelets = content;
    }
    // TEMPORARY - for testing
    protected Shapelet(double[] content, int seriesId, int startPos, double splitThreshold,
                     double gain, double gap) {
        this.content = content;
        this.seriesId = seriesId;
        this.startPos = startPos;
        this.splitThreshold = splitThreshold;
        this.informationGain = gain;
        this.separationGap = gap;
    }

    // TEMP - used when processing has been carried out in initial stage, then the shapelets read in
    // via csv later
    public Shapelet(double[] content) {
        this.content = content;
    }

    /*
     * note: we calculate the threshold as this is used for finding the best split point in the data
     * however, as this implementation of shapelets is as a filter, we do not actually use the
     * threshold in the transformation.
     */

    public void calcInfoGainAndThreshold(ArrayList<OrderLineObj> orderline,
                                          TreeMap<Double, Integer> classDistribution) {
        // for each split point, starting between 0 and 1, ending between end-1 and end
        // addition: track the last threshold that was used, don't bother if it's the same as the last
        // one
        double lastDist = orderline.get(0).getDistance(); // must be initialised as not visited(no point
        // breaking before any data!)
        double thisDist = -1;

        double bsfGain = -1;
        double threshold = -1;

        // check that there is actually a split point
        // for example, if all

        for (int i = 1; i < orderline.size(); i++) {
            thisDist = orderline.get(i).getDistance();
            if (i == 1 || thisDist != lastDist) { // check that threshold has moved(no point in sampling
                // identical thresholds)- special case - if 0 and 1
                // are the same dist

                // count class instances below and above threshold
                TreeMap<Double, Integer> lessClasses = new TreeMap<Double, Integer>();
                TreeMap<Double, Integer> greaterClasses = new TreeMap<Double, Integer>();

                for (double j : classDistribution.keySet()) {
                    lessClasses.put(j, 0);
                    greaterClasses.put(j, 0);
                }

                int sumOfLessClasses = 0;
                int sumOfGreaterClasses = 0;

                // visit those below threshold
                for (int j = 0; j < i; j++) {
                    double thisClassVal = orderline.get(j).getClassVal();
                    int storedTotal = lessClasses.get(thisClassVal);
                    storedTotal++;
                    lessClasses.put(thisClassVal, storedTotal);
                    sumOfLessClasses++;
                }

                // visit those above threshold
                for (int j = i; j < orderline.size(); j++) {
                    double thisClassVal = orderline.get(j).getClassVal();
                    int storedTotal = greaterClasses.get(thisClassVal);
                    storedTotal++;
                    greaterClasses.put(thisClassVal, storedTotal);
                    sumOfGreaterClasses++;
                }

                int sumOfAllClasses = sumOfLessClasses + sumOfGreaterClasses;

                double parentEntropy = entropy(classDistribution);

                // calculate the info gain below the threshold
                double lessFrac = (double) sumOfLessClasses / sumOfAllClasses;
                double entropyLess = entropy(lessClasses);
                // calculate the info gain above the threshold
                double greaterFrac = (double) sumOfGreaterClasses / sumOfAllClasses;
                double entropyGreater = entropy(greaterClasses);

                double gain = parentEntropy - lessFrac * entropyLess - greaterFrac * entropyGreater;
                if (gain > bsfGain) {
                    bsfGain = gain;
                    threshold = (thisDist - lastDist) / 2 + lastDist;
                }
            }
            lastDist = thisDist;
        }
        if (bsfGain >= 0) {
            this.informationGain = bsfGain;
            this.splitThreshold = threshold;
            this.separationGap = calculateSeparationGap(orderline, threshold);
        }
    }

    protected double calculateSeparationGap(ArrayList<OrderLineObj> orderline,
                                          double distanceThreshold) {

        double sumLeft = 0;
        double leftSize = 0;
        double sumRight = 0;
        double rightSize = 0;

        for (int i = 0; i < orderline.size(); i++) {
            if (orderline.get(i).getDistance() < distanceThreshold) {
                sumLeft += orderline.get(i).getDistance();
                leftSize++;
            }
            else {
                sumRight += orderline.get(i).getDistance();
                rightSize++;
            }
        }

        double thisSeparationGap = 1 / rightSize * sumRight - 1 / leftSize * sumLeft;

        if (rightSize == 0 || leftSize == 0) {
            return -1; // obviously there was no seperation, which is likely to be very rare but i still
            // caused it!
        } // e.g if all data starts with 0, first shapelet length =1, there will be no seperation as
        // all time series are same dist
        // equally true if all data contains the shapelet candidate, which is a more realistic example

        return thisSeparationGap;
    }

    // comparison 1: to determine order of shapelets in terms of info gain, then separation gap,
    // then shortness
    public int compareTo(Shapelet shapelet) {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (this.informationGain != shapelet.informationGain) {
            if (this.informationGain > shapelet.informationGain) {
                return BEFORE;
            }
            else {
                return AFTER;
            }
        }
        else {
            if (this.separationGap != shapelet.separationGap) {
                if (this.separationGap > shapelet.separationGap) {
                    return BEFORE;
                }
                else {
                    return AFTER;
                }
            }
            else if (this.content.length != shapelet.content.length) {
                if (this.content.length < shapelet.content.length) {
                    return BEFORE;
                }
                else {
                    return AFTER;
                }
            }
            else {
                return EQUAL;
            }
        }

    }

    private static double entropy(TreeMap<Double, Integer> classDistributions) {
        if (classDistributions.size() == 1) {
            return 0;
        }

        double thisPart;
        double toAdd;
        int total = 0;
        for (Double d : classDistributions.keySet()) {
            total += classDistributions.get(d);
        }
        // to avoid NaN calculations, the individual parts of the entropy are calculated and summed.
        // i.e. if there is 0 of a class, then that part would calculate as NaN, but this can be caught
        // and
        // set to 0.
        ArrayList<Double> entropyParts = new ArrayList<Double>();
        for (Double d : classDistributions.keySet()) {
            thisPart = (double) classDistributions.get(d) / total;
            toAdd = -thisPart * Math.log10(thisPart) / Math.log10(2);
            if (Double.isNaN(toAdd))
                toAdd = 0;
            entropyParts.add(toAdd);
        }

        double entropy = 0;
        for (int i = 0; i < entropyParts.size(); i++) {
            entropy += entropyParts.get(i);
        }
        return entropy;
    }
}