package marlene3.evaluation;

import com.yahoo.labs.samoa.instances.Prediction;
import moa.core.Utils;

import java.util.ArrayList;
import java.util.List;

public class Evaluation {
    double sumExamples;
    double[] tp;
    double[] fp;
    double[] fn;
    double[] tn;
    double[] tp1;
    double[] fp1;
    double[] fn1;
    double[] tn1;
    List<Double>[] accuracy;
    List<Double> gMean;
    protected double hammingLoss;
    protected double exactMatch;
    protected double exampleBasedAccuracy;
    protected double exampleBasedRecall;
    protected double exampleBasedPrecision;
    int numOfL;
    List<Double>[] labelGMeanList;
    List<Double> macroGMeanList;
    List<Double> microGMeanList;

    public Evaluation(int numOflabels){
        numOfL = numOflabels;
        sumExamples = 0;
        tp = new double[numOflabels];
        fp = new double[numOflabels];
        fn = new double[numOflabels];
        tn = new double[numOflabels];

        tp1 = new double[numOflabels];
        fp1 = new double[numOflabels];
        fn1 = new double[numOflabels];
        tn1 = new double[numOflabels];

        accuracy = new List[numOflabels];
        for (int i = 0; i < numOflabels; i++){
            accuracy[i] = new ArrayList<>();
        }

        labelGMeanList = new List[numOflabels];
        for (int i = 0; i < numOflabels; i++){
            labelGMeanList[i] = new ArrayList<>();
        }

        macroGMeanList = new ArrayList<>();
        microGMeanList = new ArrayList<>();

        gMean = new ArrayList<>();
        hammingLoss = 0.0;
        exactMatch = 0.0;
        exampleBasedAccuracy = 0.0;
        exampleBasedPrecision = 0.0;
        exampleBasedRecall = 0.0;
    }


    public void reset(){
        tp1 = new double[numOfL];
        fp1 = new double[numOfL];
        fn1 = new double[numOfL];
        tn1 = new double[numOfL];
    }

    public void update(Prediction pred, double[] labels){
//        sumExamples = 0.99*sumExamples + 1;
        sumExamples++;
        double tmpTP = 0.0;
        double tmpFP = 0.0;
        double tmpFN = 0.0;
        double tmpTN = 0.0;

        for (int i = 0; i< labels.length; i++){
            int classValue = (int) labels[i];
            int yp = Utils.maxIndex(pred.getVotes(i));

//            if (yp==1){
//                System.out.print(i+",");
//            }

            if (yp == 1 && classValue == 1){
                tp[i] ++;
                tp1[i] ++;
                tmpTP++;
            }else if (yp == 1 && classValue == 0){
                fp[i] ++;
                fp1[i] ++;
                tmpFP++;
            }else if (yp == 0 && classValue == 1){
                fn[i] ++;
                fn1[i] ++;
                tmpFN++;
            }else if (yp == 0 && classValue == 0){
                tn[i] ++;
                tn1[i] ++;
                tmpTN++;
            }
        }


        for (int i = 0; i < tp.length; i++){
            double acc = (tp[i] + tn[i])/(tp[i] + fn[i] + fp[i] + tn[i]);
            accuracy[i].add(acc);
        }

        hammingLoss += (tmpTP + tmpTN);
        exactMatch += tmpTP + tmpTN == numOfL ? 1:0;
        exampleBasedAccuracy += tmpTP == 0 ? 0 : tmpTP/(tmpTP + tmpFP + tmpFN);
        exampleBasedRecall += tmpTP == 0 ? 0 : tmpTP/(tmpTP + tmpFN);
        exampleBasedPrecision += tmpTP == 0 ? 0 : tmpTP/(tmpTP + tmpFP);

        double r = tmpTP+tmpFN == 0 ? 0: tmpTP/(tmpTP+tmpFN);
        double s = tmpTN+tmpFP == 0 ? 0: tmpTN/(tmpTN+tmpFP);
        gMean.add(Math.sqrt(r*s));
//        gMean.add(Math.sqrt(tmpTP/(tmpTP+tmpFN + tmpTN+tmpFP)*(tmpTN/(tmpTN+tmpFP + tmpTN+tmpFP))));
//        System.out.println(gMean.get(gMean.size()-1));
//        System.out.println();
        double[] gArray = this.getAverageGMean();
        for (int i = 0; i < numOfL; i++){
            labelGMeanList[i].add(gArray[i]);
        }

//        microGMeanList.add(this.getMicroGMean());
//        macroGMeanList.add(this.getMarcoGMean());

    }

    public double getAverageMicroGMean(){
        double g = 0;
        for (int i = 0; i < microGMeanList.size(); i++){
            g = 0.99*g + microGMeanList.get(i);
        }

        return g/sumExamples;

    }

    public double getAverageMacroGMean(){
        double g = 0;
        for (int i = 0; i < macroGMeanList.size(); i++){
            g = 0.99*g + macroGMeanList.get(i);
        }

        return g/sumExamples;

    }


    public List<Double>[] getLabelGMeanList() {
        return labelGMeanList;
    }

    public double getLabelSetBasedGMean(){
        double g = 0;
        for (int i= 0; i < gMean.size(); i++){
            g += gMean.get(i);
        }
        return g/sumExamples;
    }

    public double getMarcoGMean(){
        double[] recall =new double[numOfL];
        double[] specificity = new double[numOfL];
        for (int i = 0; i < numOfL; i++){
            recall[i] = tp[i]==0 ? 0: tp[i]/(tp[i] + fn[i]);
        }

        for (int i = 0; i < numOfL; i++){
            specificity[i] =  tn[i]==0 ? 0: tn[i]/(tn[i] + fp[i]);
        }

        double gMean = 0;
        for (int i = 0; i < numOfL; i++){
            gMean += Math.sqrt(recall[i]*specificity[i]);
        }

        return gMean/numOfL;
    }

    public double getMicroGMean(){
        double gtn = Utils.sum(tn);
        double gtp = Utils.sum(tp);
        double gfn = Utils.sum(fn);
        double gfp = Utils.sum(fp);

        return Math.sqrt((gtp/(gtp+gfn))*(gtn/(gtn+gfp)));

    }

    public double[] getAverageGMean(){
        double[] gMean = new double[numOfL];
        double[] recall =new double[numOfL];
        double[] specificity = new double[numOfL];
        for (int i = 0; i < numOfL; i++){
            recall[i] = tp1[i] == 0 ? 0 : tp1[i]/(tp1[i] + fn1[i]);
//            recall[i] = tp1[i]/(tp1[i] + fn1[i]);
        }

        for (int i = 0; i < numOfL; i++){
            specificity[i] = tn1[i] == 0? 0: tn1[i]/(tn1[i] + fp1[i]);
//            specificity[i] = tn1[i]/(tn1[i] + fp1[i]);
        }

        for (int i = 0; i < numOfL; i++){
            gMean[i] = Math.sqrt(recall[i]*specificity[i]);
        }

        return gMean;
    }

    public List<Double>[] getAccuracy(){
        return accuracy;
    }

    //Average Prequential Accuracy on each label
    public double[] getAverageAccuracy(){
        double[] averAcc = new double[numOfL];
        for (int i = 0; i < numOfL; i++){
            for (int j = 0; j < accuracy[i].size(); j++){
                averAcc[i] += accuracy[i].get(j);
            }
            averAcc[i] = averAcc[i]/accuracy[i].size();
        }
        return averAcc;
    }

    public double getMicroAccuracy(){
        double sumTp = Utils.sum(tp);
        double sumTn = Utils.sum(tn);
        double sumFp = Utils.sum(fp);
        double sumFn = Utils.sum(fn);

        return (sumTp + sumTn) / (sumFn + sumFp + sumTn + sumTp);
    }

    public double getMicroRecall(){
        return Utils.sum(tp)/(Utils.sum(tp) + Utils.sum(fn));
    }

    public double getMicroPrecision(){
        return Utils.sum(tp)/(Utils.sum(tp) + Utils.sum(fp));
    }

    public double getMicroFMeasure(double beta){
        double precision = getMicroPrecision();
        double recall = getMicroRecall();
        double b = Math.pow(beta, 2);
        return (1 + b) * precision * recall / (b * precision + recall);
    }

    public double getExampleBasedAccuracy(){
        return exampleBasedAccuracy/sumExamples;
    }

    public double getExampleBasedFMeasure(double beta){
        double precision = getExampleBasedPrecision();
        double recall = getExampleBasedRecall();
        double b = Math.pow(beta, 2);
        return (1 + b) * precision * recall / (b * precision + recall);
    }

    public double getExampleBasedRecall(){
        return exampleBasedRecall/sumExamples;
    }

    public double getExampleBasedPrecision(){
        return exampleBasedPrecision/sumExamples;
    }

    public double getSumExapmles(){
        return sumExamples;
    }

    public double getExactMatch(){
        return exactMatch/sumExamples;
    }

    public double getHammingScore(){
        return hammingLoss/(sumExamples*numOfL);
    }

    //Recall on each label
    public double[] getRecall(){
        double[] recall = new double[tp.length];

        for (int l = 0; l < recall.length; l++) {
            recall[l] = tp[l] == 0 ? 0 : tp[l] / (tp[l] + fn[l]);
        }
        return recall;
    }

    //Precision on each label
    public double[] getPrecision(){
        double[] precision = new double[tp.length];

        for (int l = 0; l < precision.length; l++) {
            precision[l] = tp[l] == 0 ? 0 :tp[l] / (tp[l] + fp[l]);
        }

        return precision;
    }

    //FMeasure on each Label
    public double[] getFMeasure(double beta){
        double[] fMeasure = new double[tp.length];
        double[] recall = this.getRecall();
        double[] precision = this.getPrecision();
        double b = Math.pow(beta, 2);

        for (int l = 0; l < fMeasure.length; l++) {
            if (precision[l] + recall[l] == 0)
                fMeasure[l] = 0.0;
            else
                fMeasure[l] = (1 + b) * precision[l] * recall[l] / (b * precision[l] + recall[l]);
        }

        return fMeasure;
    }

//    public List<Double[]> getGMeanList(){
//
//    }

}
