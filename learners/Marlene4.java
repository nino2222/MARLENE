//package marlene4.learners;
//
//import com.github.javacliparser.FloatOption;
//import com.github.javacliparser.IntOption;
//
//import com.yahoo.labs.samoa.instances.Instance;
//import com.yahoo.labs.samoa.instances.MultiLabelPrediction;
//import com.yahoo.labs.samoa.instances.Prediction;
//import moa.classifiers.Classifier;
//import moa.classifiers.core.driftdetection.ChangeDetector;
//import moa.core.DoubleVector;
//import moa.core.Utils;
//import moa.options.ClassOption;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//public class Marlene4 {
//    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'c',
//            "Classifier to train.", Classifier.class,"meta.OzaBoost");
//    public ClassOption driftDetectionMethodOption = new ClassOption("driftDetectionMethod", 'd',
//            "Drift detection method to use.", ChangeDetector.class, "DDM");
//    public FloatOption thetaOption = new FloatOption("theta", 'f',
//            "time forgetting factor.", 0.9, 0.0, 1.0);
//    public IntOption targetDomainIndexOption = new IntOption("targetDomainIndex", 't',
//            "The Index of target domain in data set", 0, 0, Integer.MAX_VALUE);
//    public IntOption numOfLabels = new IntOption("numberOfLabels", 'l',
//            "The number of target Labels", 1, 1, Integer.MAX_VALUE);
//    public FloatOption posRatio = new FloatOption("pR", 'p',
//            "time forgetting factor.", 1, 0.0, Double.MAX_VALUE);
//    public FloatOption neiRatio = new FloatOption("nR", 'n',
//            "time forgetting factor.", 1, 0.0, Double.MAX_VALUE);
//
//    protected Map<Integer, List<BLearner>[]> l_clfs;
//    protected Map<Integer, List<DLearner>[][]> d_clfs;
//    protected double[] count;
//
//    public Marlene4(){
//        l_clfs = new HashMap<>();
//        d_clfs = new HashMap<>();
//        count = new double[14];
//        for (int i = 0; i < 14; i++){
//            count[i] = 0;
//        }
//    }
//
//    public BLearner generateLCLF(){
//        BLearner lc = new BLearner();
//        lc.baseLearnerOption.setValueViaCLIString(this.baseLearnerOption.getValueAsCLIString());
//        lc.driftDetectionMethodOption.setValueViaCLIString(this.driftDetectionMethodOption.getValueAsCLIString());
//        lc.forgettingFactor.setValue(this.thetaOption.getValue());
//        lc.numOfLabels.setValue(this.numOfLabels.getValue());
//        lc.posRatio.setValue(this.posRatio.getValue());
//        lc.neiRatio.setValue(this.neiRatio.getValue());
//        lc.prepareForUse();
//        lc.resetLearning();
//        return lc;
//    }
//
//    public DLearner generateDCLF(){
//        DLearner dc = new DLearner();
//        dc.baseLearnerOption.setValueViaCLIString(this.baseLearnerOption.getValueAsCLIString());
//        dc.driftDetectionMethodOption.setValueViaCLIString(this.driftDetectionMethodOption.getValueAsCLIString());
//        dc.forgettingFactor.setValue(this.thetaOption.getValue());
//        dc.numOfLabels.setValue(this.numOfLabels.getValue());
//        dc.posRatio.setValue(this.posRatio.getValue());
//        dc.neiRatio.setValue(this.neiRatio.getValue());
//        dc.prepareForUse();
//        dc.resetLearning();
//        return dc;
//    }
//
//    //Needed to be filled, reset all relevant performance if from source
//    public void resetAllPerfOnLabel(int index){
//        for (List<BLearner>[] lc_lists: l_clfs.values()) {
//            for (int i = 0; i < lc_lists.length; i++) {
//                for (BLearner lc : lc_lists[i]) {
//                    lc.resetPerfOnLabel(index);
//                }
//            }
//        }
//    }
//
//    public void resetAllPerfOnDependence(int ti, int tj){
//        for (List<DLearner>[][] dc_lists: d_clfs.values()){
//            for (int i = 0; i < dc_lists.length; i++){
//                for (int j = 0; j < dc_lists[i].length; j++){
//                    if (i != j) {
//                        for (DLearner dc : dc_lists[i][j]) {
//                            dc.resetAllPerfOnDependence(ti, tj);
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public void trainOnInstanceImpl(int source_ID, Instance l_inst, Instance d_inst, double[] labels) {
//        if (!l_clfs.containsKey(source_ID)){
//            //Initialize Label Classifiers
//            List<BLearner>[] lc_lists = new List[labels.length];
//
//            for (int i = 0; i < labels.length; i++){
//                lc_lists[i] = new ArrayList<>();
//                lc_lists[i].add(this.generateLCLF());
//            }
//
//            l_clfs.put(source_ID, lc_lists);
//
//            //Initialize Dependence Classifiers
//            List<DLearner>[][] dc_lists = new List[labels.length][labels.length];
//
//            for (int i = 0; i < labels.length; i++){
//                for (int j =0; j < labels.length; j++){
//                    if (i != j){
//                        dc_lists[i][j] = new ArrayList<>();
//                        dc_lists[i][j].add(this.generateDCLF());
//                    }else {
//                        dc_lists[i][j] = null;
//                    }
//                }
//            }
//
//            d_clfs.put(source_ID, dc_lists);
//        }
//
//
//        //Train Label Classifiers
//        List<BLearner>[] lclf_lists = l_clfs.get(source_ID);
//        for (int i = 0; i < labels.length; i++){
//            //Set instance's class value
//            l_inst.setClassValue(labels[i]);
//
//            //Detect Drift
//            BLearner tmp_learner = lclf_lists[i].get(lclf_lists[i].size()-1).detectDrift(l_inst);
//            if (tmp_learner != null){
//                //Needed to be filled, reset all relevant performance if from source
//                if (source_ID == targetDomainIndexOption.getValue()){
//                    this.resetAllPerfOnLabel(i);
//                }
//
//                lclf_lists[i].add(tmp_learner);
//            }
//
//            //Train
////            System.out.print("i: " + i);
//            lclf_lists[i].get(lclf_lists[i].size()-1).trainOnInstance(l_inst);
//        }
//
//        //Train Dependence Classifiers
//        List<DLearner>[][] dclf_lists = d_clfs.get(source_ID);
//
//        for (int i = 0; i < labels.length; i++){
//            d_inst.setValue(d_inst.numAttributes()-1, labels[i]);
//            for (int j = 0; j < labels.length; j++){
//                if (i != j){
//                    d_inst.setClassValue(labels[j]);
//
//                    //Detect Drift
//                    DLearner tmp_learner = dclf_lists[i][j].get(dclf_lists[i][j].size()-1).detectDrift(d_inst);
//                    if (tmp_learner != null){
//                        //Needed to be filled, reset all relevant performance if from source
//                        if (source_ID == targetDomainIndexOption.getValue()){
//                            this.resetAllPerfOnDependence(i, j);
//                        }
//
//                        dclf_lists[i][j].add(tmp_learner);
//                    }
//
//                    //Train
//                    dclf_lists[i][j].get(dclf_lists[i][j].size()-1).trainOnInstance(d_inst);
//                }
//            }
//        }
//
//        //Update Performance
//        if (source_ID == targetDomainIndexOption.getValue()){
//
//            //Update Label Classifiers' Performance on Target
//            DoubleVector[] weighted_lc_votes = new DoubleVector[numOfLabels.getValue()];
//            for (int i = 0; i < weighted_lc_votes.length; i++){
//                weighted_lc_votes[i] = new DoubleVector();
//            }
//
//            for (List<BLearner>[] lc_lists: l_clfs.values()) {
//                for (int i = 0; i < lc_lists.length; i++) {
//                    for (BLearner lc : lc_lists[i]) {
//                        DoubleVector vote = new DoubleVector(lc.getVotesForInstance(l_inst));
//                        double[][] results = new double[numOfLabels.getValue()][2];
//
//                        for (int j = 0; j < numOfLabels.getValue(); j++){
//
//                            double tempN = vote.getValue(0)*lc.getLPerfs()[j].getTNWeight() + vote.getValue(1)*lc.getLPerfs()[j].getFPWeight();
//                            double tempP = vote.getValue(0)*lc.getLPerfs()[j].getFNWeight() + vote.getValue(1)*lc.getLPerfs()[j].getTPWeight();
//
////                            double tempN = vote.getValue(0)*lc.getLPerfs()[j].getTNWeight();
////                            double tempP = vote.getValue(1)*lc.getLPerfs()[j].getTPWeight();
//
////                            System.out.println("tmpN: " + tempN);
////                            System.out.println("tmpP: " + tempP);
//
//                            results[j][0] = tempN;
//                            results[j][1] = tempP;
//
//                            weighted_lc_votes[j].addToValue(0, tempN);
//                            weighted_lc_votes[j].addToValue(1, tempP);
//                        }
//
//                        lc.saveResults(vote.getArrayRef(), results);
//                    }
//                }
//            }
//
//            double[] sw = new double[numOfLabels.getValue()];
//            double[] sc = new double[numOfLabels.getValue()];
//            for (int i = 0; i < numOfLabels.getValue(); i++){
//                sw[i] = weighted_lc_votes[i].getValue((int) (1-labels[i]));
//                sc[i] = weighted_lc_votes[i].getValue((int) labels[i]);
//
////              System.out.println("sw_i: " + i + " : " + sw[i]);
////              System.out.println("sc_i: " + i + " : " + sc[i]);
//            }
//
//
//
//            for (List<BLearner>[] lc_lists: l_clfs.values()) {
//                for (int i = 0; i < lc_lists.length; i++) {
//                    for (BLearner lc : lc_lists[i]) {
//                        lc.updatePerf(sw, sc, labels);
//                    }
//                }
//            }
//
//
//            //Update Dependence Classifiers' Performance on Target
//            DoubleVector[][] weighted_dc_votes = new DoubleVector[numOfLabels.getValue()][numOfLabels.getValue()];
//            for (int i = 0; i < numOfLabels.getValue(); i++){
//                for (int j = 0; j < numOfLabels.getValue(); j++){
//                    weighted_dc_votes[i][j] = new DoubleVector();
//                }
//            }
//
//            for (List<DLearner>[][] dc_lists: d_clfs.values()){
//                for (int i = 0; i < labels.length; i++){
//                    for (int j = 0; j < labels.length; j++){
//                        if (i != j) {
//                            for (DLearner dc : dc_lists[i][j]) {
//                                //dc.updatePerf(d_inst, labels);
//
//                                double[][] rawVote = new double[numOfLabels.getValue()][2];
//                                double[][][] vote2  = new double[numOfLabels.getValue()][numOfLabels.getValue()][2];
//
//                                for (int ti = 0; ti < numOfLabels.getValue(); ti++) {
//                                    //d_inst.setValue(d_inst.numAttributes() - 1, weighted_lc_votes[ti].maxIndex());
//                                    d_inst.setValue(d_inst.numAttributes() - 1, labels[ti]);
//                                    double[] vote = dc.getVotesForInstance(d_inst);
//                                    rawVote[ti] = vote;
//                                    for (int tj = 0; tj < numOfLabels.getValue(); tj++) {
//                                        if (ti != tj) {
//                                            double tempN = vote[0] * dc.getDPerfs()[ti][tj].getTNWeight() + vote[1] * dc.getDPerfs()[ti][tj].getFPWeight();
//                                            double tempP = vote[0] * dc.getDPerfs()[ti][tj].getFNWeight() + vote[1] * dc.getDPerfs()[ti][tj].getTPWeight();
//
//                                            vote2[ti][tj][0] = tempN;
//                                            vote2[ti][tj][0] = tempP;
//
//                                            weighted_dc_votes[ti][tj].addToValue(0, tempN);
//                                            weighted_dc_votes[ti][tj].addToValue(1, tempP);
//                                        }
//                                    }
//
//                                }
//
//                                dc.saveResults(rawVote, vote2);
//                            }
//                        }
//                    }
//                }
//            }
//
//            double[][] dsw = new double[numOfLabels.getValue()][numOfLabels.getValue()];
//            double[][] dsc = new double[numOfLabels.getValue()][numOfLabels.getValue()];
//            for (int i = 0; i < numOfLabels.getValue(); i++){
//                for (int j = 0; j < numOfLabels.getValue(); j++){
//                    if (i != j) {
//                        dsw[i][j] = weighted_dc_votes[i][j].getValue((int) (1-labels[j]));
//                        dsc[i][j] = weighted_dc_votes[i][j].getValue((int) (labels[j]));
//                    }
//                }
////              System.out.println("sw_i: " + i + " : " + sw[i]);
////              System.out.println("sc_i: " + i + " : " + sc[i]);
//            }
//
//            for (List<DLearner>[][] dc_lists: d_clfs.values()) {
//                for (int i = 0; i < labels.length; i++) {
//                    for (int j = 0; j < labels.length; j++) {
//                        if (i != j) {
//                            for (DLearner dc : dc_lists[i][j]) {
//                                dc.updatePerf(dsw, dsc, labels);
//                            }
//                        }
//                    }
//                }
//            }
//
//        }
//    }
//
//    public double[] getCount() {
//        return count;
//    }
//
//    public Prediction getPredictionForInstance(Instance l_inst, Instance d_inst) {
//        Prediction prediction = new MultiLabelPrediction(numOfLabels.getValue());
//
//        if (l_clfs.isEmpty()){
//            for (int i = 0; i < prediction.size(); i++){
//                double[] tmpV = new double[2];
//                prediction.setVotes(i, tmpV);
//            }
//
//            return prediction;
//        }
//
//
//        //Label Classifier make a prediction on new example
//        DoubleVector[] weighted_lc_votes = new DoubleVector[numOfLabels.getValue()];
//        for (int i = 0; i < weighted_lc_votes.length; i++){
//            weighted_lc_votes[i] = new DoubleVector();
//        }
//
////        List<BLearner>[] currentLc = l_clfs.get(targetDomainIndexOption.getValue());
////        for (int i = 0; i < currentLc.length; i++){
////            double[] vote = currentLc[i].get(currentLc[i].size()-1).getVotesForInstance(l_inst);
//////            double tempN = vote[0]*currentLc[i].get(currentLc[i].size()-1).getLPerfs()[i].getTNWeight()+
//////                     vote[1]*currentLc[i].get(currentLc[i].size()-1).getLPerfs()[i].getFPWeight();
//////            double tempP =
//////                    vote[0]*currentLc[i].get(currentLc[i].size()-1).getLPerfs()[i].getFNWeight()+
//////                    vote[1]*currentLc[i].get(currentLc[i].size()-1).getLPerfs()[i].getTPWeight();
//////            vote[0] = tempN;
//////            vote[1] = tempP;
////            if (Utils.maxIndex(vote) == 1){
////                count[i] ++;
////            }
//////            System.out.println("i: " + i + " TN: " + currentLc[i].get(currentLc[i].size()-1).getLPerfs()[i].getTNWeight());
//////            System.out.println("i: " + i + " FP: " + currentLc[i].get(currentLc[i].size()-1).getLPerfs()[i].getFPWeight());
//////            System.out.println("i: " + i + " FN: " + currentLc[i].get(currentLc[i].size()-1).getLPerfs()[i].getFNWeight());
//////            System.out.println("i: " + i + " TP: " + currentLc[i].get(currentLc[i].size()-1).getLPerfs()[i].getTPWeight());
////            prediction.setVotes(i, vote);
//////            weighted_lc_votes[i].addValues(vote);
////        }
//
//        for (List<BLearner>[] lc_lists: l_clfs.values()){
//            for (int i = 0; i < lc_lists.length; i++){
//                for (BLearner lc: lc_lists[i]){
//                    double[] vote = lc.getVotesForInstance(l_inst);
//
//                    for (int j = 0; j < numOfLabels.getValue(); j++){
////                        System.out.println("tp"+ ":" + "j: " + j + ": " + lc.getLPerfs()[j].getTp());
////                        System.out.println("fp"+ ":" + "j: " + j + ": " + lc.getLPerfs()[j].getFp());
////                        System.out.println("tn"+ ":" + "j: " + j + ": " + lc.getLPerfs()[j].getTn());
////                        System.out.println("fn"+ ":" + "j: " + j + ": " + lc.getLPerfs()[j].getFn());
////                        if (lc.getLPerfs()[j].getA() > 0.5) {
//                            double tempN = vote[0] * lc.getLPerfs()[j].getTNWeight()
//                                    + vote[1] * lc.getLPerfs()[j].getFPWeight();
//                            double tempP = vote[0] * lc.getLPerfs()[j].getFNWeight() +
//                                    vote[1] * lc.getLPerfs()[j].getTPWeight();
//
////                        System.out.println("tmpN: " + tempN);
////                        System.out.println("tmpP: " + tempP);
//
//                            DoubleVector tempVote = new DoubleVector();
//                            tempVote.addToValue(0, tempN);
//                            tempVote.addToValue(1, tempP);
////                            if (j == 3) {
////                                System.out.println("A: " + "i: " + i + " : " + lc.getLPerfs()[j].getA());
////                            }
//                            tempVote.scaleValues(lc.getLPerfs()[j].getA());
//                            weighted_lc_votes[j].addValues(tempVote);
////                        }else{
////                            double tempN = vote[0] * lc.getLPerfs()[j].getTNWeight() + vote[1] * lc.getLPerfs()[j].getFPWeight();
////                            double tempP = vote[0] * lc.getLPerfs()[j].getFNWeight() + vote[1] * lc.getLPerfs()[j].getTPWeight();
////
//////                        System.out.println("tmpN: " + tempN);
//////                        System.out.println("tmpP: " + tempP);
////
////                            DoubleVector tempVote = new DoubleVector();
////                            tempVote.addToValue(0, tempP);
////                            tempVote.addToValue(1, tempN);
//////                            if (j == 3) {
//////                                System.out.println("A: " + "i: " + i + " : " + lc.getLPerfs()[j].getA());
//////                            }
////                            tempVote.scaleValues((1-lc.getLPerfs()[j].getA()));
////                            weighted_lc_votes[j].addValues(tempVote);
////
////                        }
////                        if (j == 4){
////                            System.out.println("i: " + i + " TN: " + lc.getLPerfs()[j].getTNWeight());
////                            System.out.println("i: " + i + " FP: " + lc.getLPerfs()[j].getFPWeight());
////                            System.out.println("i: " + i + " FN: " + lc.getLPerfs()[j].getFNWeight());
////                            System.out.println("i: " + i + " TP: " + lc.getLPerfs()[j].getTPWeight());
////                        }
////                        double tempN = 0;
////                        double tempP = 0;
////                        if (lc.getLPerfs()[j].getTNWeight() > 0.5){
////                            tempN += vote[0]*lc.getLPerfs()[j].getTNWeight();
////                        }
////
////                        if (lc.getLPerfs()[j].getFPWeight() > 0.5){
////                            tempN += vote[1]*lc.getLPerfs()[j].getFPWeight();
////                        }
////
////                        if (lc.getLPerfs()[j].getFNWeight() > 0.5){
////                            tempP += vote[0]*lc.getLPerfs()[j].getFNWeight();
////                        }
////
////                        if (lc.getLPerfs()[j].getTPWeight() > 0.5){
////                            tempP += vote[1]*lc.getLPerfs()[j].getTPWeight();
////                        }
//
////                        if (lc.getLPerfs()[j].getTNWeight() < 0.5){
////                            tempP += vote[0]*(1-lc.getLPerfs()[j].getTNWeight());
////                        }else{
////                            tempN += vote[0]*lc.getLPerfs()[j].getTNWeight();
////                        }
////                        if (lc.getLPerfs()[j].getFPWeight() < 0.5){
////                            tempP += vote[1]*(1-lc.getLPerfs()[j].getFPWeight());
////                        }else{
////                            tempN += vote[1]*lc.getLPerfs()[j].getFPWeight();
////                        }
////                        if (lc.getLPerfs()[j].getFNWeight() < 0.5){
////                            tempN += vote[0]*(1-lc.getLPerfs()[j].getFNWeight());
////                        }else{
////                            tempP += vote[0]*lc.getLPerfs()[j].getFNWeight();
////                        }
////                        if (lc.getLPerfs()[j].getTPWeight() < 0.5){
////                            tempN += vote[1]*(1-lc.getLPerfs()[j].getTPWeight());
////                        }else{
////                            tempP += vote[1]*lc.getLPerfs()[j].getTPWeight();
////                        }
////                        weighted_lc_votes[j].addToValue(0, tempN);
////                        weighted_lc_votes[j].addToValue(1, tempP);
//
//                    }
//                }
//            }
//        }
//
//
//        for (int i = 0; i < numOfLabels.getValue(); i++){
//            weighted_lc_votes[i].normalize();
//            prediction.setVotes(i, weighted_lc_votes[i].getArrayRef());
//        }
//
//
//
//        //Dependence Classifier make a prediction on Target
////        DoubleVector[][] weighted_dc_votes = new DoubleVector[numOfLabels.getValue()][numOfLabels.getValue()];
////        for (int i = 0; i < numOfLabels.getValue(); i++){
////            for (int j = 0; j < numOfLabels.getValue(); j++){
////                weighted_dc_votes[i][j] = new DoubleVector();
////            }
////        }
////
////        for (List<DLearner>[][] dc_lists: d_clfs.values()) {
////            for (int si = 0; si < dc_lists.length; si++) {
////                for (int sj = 0; sj < dc_lists[si].length; sj++) {
////                    if (si != sj) {
////                        for (DLearner dc : dc_lists[si][sj]) {
////
////                            for (int ti = 0; ti < numOfLabels.getValue(); ti++){
////                                d_inst.setValue(d_inst.numAttributes()-1, weighted_lc_votes[ti].maxIndex());
////                                double[] vote = dc.getVotesForInstance(d_inst);
////                                for (int tj = 0; tj < numOfLabels.getValue(); tj++){
////                                    if (ti != tj){
//////                                        if (dc.getDPerfs()[ti][tj].getA() > 0.5) {
////                                            double tempN = vote[0] * dc.getDPerfs()[ti][tj].getTNWeight()
////                                                    + vote[1] * dc.getDPerfs()[ti][tj].getFPWeight();
////                                            double tempP = vote[0] * dc.getDPerfs()[ti][tj].getFNWeight()
////                                                    + vote[1] * dc.getDPerfs()[ti][tj].getTPWeight();
////
////
////                                            DoubleVector tempVote = new DoubleVector();
////                                            tempVote.addToValue(0, tempN);
////                                            tempVote.addToValue(1, tempP);
//////                                        if (j == 3) {
//////                                            System.out.println("A: " + "i: " + i + " : " + lc.getLPerfs()[j].getA());
//////                                        }
////                                            tempVote.scaleValues(dc.getDPerfs()[ti][tj].getA());
////                                            weighted_dc_votes[ti][tj].addValues(tempVote);
//////                                        weighted_dc_votes[ti][tj].addToValue(0, tempN);
//////                                        weighted_dc_votes[ti][tj].addToValue(1, tempP);
//////                                        }else{
//////                                            double tempN = vote[0] * dc.getDPerfs()[ti][tj].getTNWeight() + vote[1] * dc.getDPerfs()[ti][tj].getFPWeight();
//////                                            double tempP = vote[0] * dc.getDPerfs()[ti][tj].getFNWeight() + vote[1] * dc.getDPerfs()[ti][tj].getTPWeight();
//////
//////
//////                                            DoubleVector tempVote = new DoubleVector();
//////                                            tempVote.addToValue(0, tempP);
//////                                            tempVote.addToValue(1, tempN);
////////                                        if (j == 3) {
////////                                            System.out.println("A: " + "i: " + i + " : " + lc.getLPerfs()[j].getA());
////////                                        }
//////                                            tempVote.scaleValues(1-dc.getDPerfs()[ti][tj].getA());
//////                                            weighted_dc_votes[ti][tj].addValues(tempVote);
//////                                        }
////                                    }
////                                }
////                            }
////                        }
////                    }
////                }
////            }
////        }
////
////        for (int i = 0; i < numOfLabels.getValue(); i++){
////            for (int j = 0; j < numOfLabels.getValue(); j++){
////                if (i != j){
////                    weighted_dc_votes[i][j].normalize();
////                }
////            }
////        }
////
////        //Combine Label Vote and Dependence Vote
////
////        for (int i = 0; i < numOfLabels.getValue(); i++){
////            DoubleVector vote = new DoubleVector();
////            vote.addValues(weighted_lc_votes[i]);
////            for (int j = 0; j < numOfLabels.getValue(); j++){
////                if (i != j) {
////                    double tmpN = weighted_lc_votes[j].getValue(weighted_lc_votes[j].maxIndex()) * weighted_dc_votes[j][i].getValue(0);
////                    double tmpP = weighted_lc_votes[j].getValue(weighted_lc_votes[j].maxIndex()) * weighted_dc_votes[j][i].getValue(1);
////                    vote.addToValue(0, tmpN);
////                    vote.addToValue(1, tmpP);
////                }
////            }
////
////            prediction.setVotes(i, vote.getArrayRef());
////        }
//
//        return prediction;
//    }
//
//
//
//    public void resetLearningImpl() {
//
//    }
//
//}
