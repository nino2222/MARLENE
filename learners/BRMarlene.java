package marlene4.learners;

import com.github.javacliparser.FlagOption;
import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.MultiLabelPrediction;
import com.yahoo.labs.samoa.instances.Prediction;
import marlene3.evaluation.Evaluation;
import moa.classifiers.Classifier;
import moa.classifiers.core.driftdetection.ChangeDetector;
import moa.core.DoubleVector;
import moa.core.Utils;
import moa.options.ClassOption;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;


public class BRMarlene {
    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'c',
            "Classifier to train.", Classifier.class,"meta.OzaBoost");
    public ClassOption driftDetectionMethodOption = new ClassOption("driftDetectionMethod", 'd',
            "Drift detection method to use.", ChangeDetector.class, "DDM_OCI");
    public IntOption targetDomainIndexOption = new IntOption("targetDomainIndex", 't',
            "The Index of target domain in data set", 0, 0, Integer.MAX_VALUE);
    public IntOption numOfLabels = new IntOption("numberOfLabels", 'l',
            "The number of target Labels", 1, 1, Integer.MAX_VALUE);
    public IntOption randomSeed = new IntOption("numberOfLabels", 'r',
            "The number of target Labels", 1, 1, Integer.MAX_VALUE);
    public FlagOption manualSetConceptDriftOption = new FlagOption("mCDO", 'm',
            "Choose manually set when concept drift happened");


    protected Map<Integer, List<BLearner>[]> l_clfs;
//    protected  int index = 0;
//    Evaluation eva = new Evaluation(1);

    protected int[] numberOfReset;
    protected boolean reset;

    public void setNumberOfReset(boolean reset, int[] numberOfReset){
        this.numberOfReset = numberOfReset;
        this.reset = reset;
    }

    public BRMarlene(){
        l_clfs = new HashMap<>();
    }

    public BLearner generateLCLF(){
        BLearner lc = new BLearner();
        lc.baseLearnerOption.setValueViaCLIString(this.baseLearnerOption.getValueAsCLIString());
        lc.driftDetectionMethodOption.setValueViaCLIString(this.driftDetectionMethodOption.getValueAsCLIString());
        lc.numOfLabels.setValue(this.numOfLabels.getValue());
        lc.setRandomSeed(this.randomSeed.getValue());
        lc.prepareForUse();
        lc.resetLearning();
        return lc;
    }


    //Needed to be filled, reset all relevant performance if from source
    public void resetAllPerfOnLabel(int index){
        for (List<BLearner>[] lc_lists: l_clfs.values()) {
            for (int i = 0; i < lc_lists.length; i++) {
                for (BLearner lc : lc_lists[i]) {
                    lc.resetPerfOnLabel(index);
                }
            }
        }
    }


    public void trainOnInstanceImpl(int source_ID, Instance l_inst, Instance d_inst, double[] labels) {
//        index ++;
        if (!l_clfs.containsKey(source_ID)){
            //Initialize Label Classifiers
            List<BLearner>[] lc_lists = new List[labels.length];

            for (int i = 0; i < labels.length; i++){
                lc_lists[i] = new ArrayList<>();
                lc_lists[i].add(this.generateLCLF());
            }

            l_clfs.put(source_ID, lc_lists);
        }


        //Train Label Classifiers
        List<BLearner>[] lclf_lists = l_clfs.get(source_ID);
        for (int i = 0; i < labels.length; i++){
            //Set instance's class value
            l_inst.setClassValue(labels[i]);

            //Detect Drift
            if (manualSetConceptDriftOption.isSet()){
                if (reset && ArrayUtils.contains(numberOfReset, i)) {
//                    System.out.println("i: " + i);
//                    System.out.println("BeforeReset");
                    this.resetAllPerfOnLabel(i);
                    lclf_lists[i].add(this.generateLCLF());
//                    System.out.println("AfterReset");
                }
            }else {
                BLearner tmp_learner = lclf_lists[i].get(lclf_lists[i].size() - 1).detectDrift(l_inst);

                //Check Recall on Label 1
                //&***********************
//            if (i == 0 ){
//                Prediction p = new MultiLabelPrediction(1);
//                p.setVotes(0, lclf_lists[i].get(lclf_lists[i].size()-1).getVotesForInstance(l_inst));
//                double[] cv= new double[1];
//                cv[0] = labels[i];
//                eva.update(p, cv);
//                System.out.println(eva.getRecall()[0]);
//            }
                //*************************

                if (tmp_learner != null) {
//                System.out.println(i  + ": " + index);
                    //Needed to be filled, reset all relevant performance if from source
                    if (source_ID == targetDomainIndexOption.getValue()) {
                        this.resetAllPerfOnLabel(i);
                    }

                    lclf_lists[i].add(tmp_learner);
                }
            }

            //Train
//            System.out.print("i: " + i);
            lclf_lists[i].get(lclf_lists[i].size()-1).trainOnInstance(l_inst);
        }

        //Update Performance
        if (source_ID == targetDomainIndexOption.getValue()){

            //Update Label Classifiers' Performance on Target
            DoubleVector[] weighted_lc_votes = new DoubleVector[numOfLabels.getValue()];
            for (int i = 0; i < weighted_lc_votes.length; i++){
                weighted_lc_votes[i] = new DoubleVector();
            }

            for (List<BLearner>[] lc_lists: l_clfs.values()) {
                for (int i = 0; i < lc_lists.length; i++) {
                    for (BLearner lc : lc_lists[i]) {
                        DoubleVector vote = new DoubleVector(lc.getVotesForInstance(l_inst));
                        double[][] results = new double[numOfLabels.getValue()][2];

                        for (int j = 0; j < numOfLabels.getValue(); j++){

                            double tempN = vote.getValue(0)*lc.getLPerfs()[j].getTNWeight() + vote.getValue(1)*lc.getLPerfs()[j].getFPWeight();
                            double tempP = vote.getValue(0)*lc.getLPerfs()[j].getFNWeight() + vote.getValue(1)*lc.getLPerfs()[j].getTPWeight();

//                            System.out.println(lc.getLPerfs()[j].getTNWeight() + lc.getLPerfs()[j].getFPWeight());
//                            System.out.println(lc.getLPerfs()[j].getFNWeight() + lc.getLPerfs()[j].getTPWeight());
                            results[j][0] = tempN;
                            results[j][1] = tempP;

                            weighted_lc_votes[j].addToValue(0, tempN);
                            weighted_lc_votes[j].addToValue(1, tempP);
                        }

                        lc.saveResults(vote.getArrayRef(), results);
                    }
                }
            }

            double[] sw = new double[numOfLabels.getValue()];
            double[] sc = new double[numOfLabels.getValue()];
            for (int i = 0; i < numOfLabels.getValue(); i++){
                sw[i] = weighted_lc_votes[i].getValue((int) (1-labels[i]));
                sc[i] = weighted_lc_votes[i].getValue((int) labels[i]);


//              System.out.println("sw_i: " + i + " : " + sw[i]);
//              System.out.println("sc_i: " + i + " : " + sc[i]);
            }



            for (List<BLearner>[] lc_lists: l_clfs.values()) {
                for (int i = 0; i < lc_lists.length; i++) {
                    for (BLearner lc : lc_lists[i]) {
                        lc.updatePerf(sw, sc, labels);
                    }
                }
            }

        }
    }


    public Prediction getPredictionForInstance(Instance l_inst, Instance d_inst) {
        Prediction prediction = new MultiLabelPrediction(numOfLabels.getValue());

        if (l_clfs.isEmpty()){
            for (int i = 0; i < prediction.size(); i++){
                double[] tmpV = new double[2];
                prediction.setVotes(i, tmpV);
            }

            return prediction;
        }


        //Label Classifier make a prediction on new example
        DoubleVector[] weighted_lc_votes = new DoubleVector[numOfLabels.getValue()];
        for (int i = 0; i < weighted_lc_votes.length; i++){
            weighted_lc_votes[i] = new DoubleVector();
        }

        for (List<BLearner>[] lc_lists: l_clfs.values()){
            for (int i = 0; i < lc_lists.length; i++){
                for (BLearner lc: lc_lists[i]){
                    double[] vote = lc.getVotesForInstance(l_inst);

                    for (int j = 0; j < numOfLabels.getValue(); j++){

                        double tempN = vote[0] * lc.getLPerfs()[j].getTNWeight()
                                + vote[1] * lc.getLPerfs()[j].getFPWeight();
                        double tempP = vote[0] * lc.getLPerfs()[j].getFNWeight() +
                                vote[1] * lc.getLPerfs()[j].getTPWeight();


//                        System.out.println(lc.getLPerfs()[j].getTNWeight() + lc.getLPerfs()[j].getFPWeight());
                        DoubleVector tempVote = new DoubleVector();
                        tempVote.addToValue(0, tempN);
                        tempVote.addToValue(1, tempP);

                        tempVote.scaleValues(lc.getLPerfs()[j].getA());

                        weighted_lc_votes[j].addValues(tempVote);
                    }
                }
            }
        }


        for (int i = 0; i < numOfLabels.getValue(); i++){
            weighted_lc_votes[i].normalize();
            prediction.setVotes(i, weighted_lc_votes[i].getArrayRef());
        }

        return prediction;
    }

    //Get each label's contribution on a certain Label
    public double[][] getWeightRationOnLabel(int l){
        double[][] wr = new double[l_clfs.size()][];

        double totalW = 0.0;

        //k ==0 means the Target Stream
        for (int k = 0 ; k < l_clfs.size(); k++){
            wr[k] = new double[l_clfs.get(k).length];
            for (int j = 0; j < l_clfs.get(k).length; j++){
                for (BLearner lc: l_clfs.get(k)[j]){
                    totalW += lc.getLPerfs()[l].getA();
                    wr[k][j] += lc.getLPerfs()[l].getA();
                }
            }
        }

        for (int k = 0; k < wr.length; k++){
            for (int j = 0; j < wr[k].length; j++){
                wr[k][j] = wr[k][j] == 0 ? 0 : wr[k][j]/totalW;
            }
        }

        return wr;
    }

    //Get Weight Ratio on each label
    public double[] getWeightRatio(){
        double[] wr = new double[this.numOfLabels.getValue()];

        for (int i = 0; i < wr.length; i++){
            double cw = 0;
            for (List<BLearner>[] lc_lists: l_clfs.values()) {
                for (int j = 0; j < lc_lists.length; j++) {
                    for (BLearner lc : lc_lists[j]) {
                        wr[i] += lc.getLPerfs()[i].getA();
                        if (lc_lists.equals(l_clfs.get(this.targetDomainIndexOption.getValue()))
                                //l_clfs.get(this.targetDomainIndexOption.getValue()).equals(lc_lists)
                                && j == i
                                //&& lc_lists[j].get(lc_lists[j].size()-1).equals(lc)
                        ){
                            cw = cw + lc.getLPerfs()[i].getA();
                        }

//                        if (l_clfs.get(this.targetDomainIndexOption.getValue()).equals(lc_lists) && j == i){
//                            cw += lc.getLPerfs()[i].getA();
//                        }
                    }
                }
            }
            if (Double.isNaN(wr[i])){
                wr[i] = 0;
            }

            wr[i] = wr[i] == 0 ? 0 : (wr[i] - cw)/wr[i];
        }

        return wr;
    }

    public double getASWR(){
        double[] wr = this.getWeightRatio();
        return Utils.sum(wr)/wr.length;
    }


    public void resetLearningImpl() {

    }

}
