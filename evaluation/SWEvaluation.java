package marlene3.evaluation;

import com.yahoo.labs.samoa.instances.Prediction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SWEvaluation {

    List<double[]> sub_labels;
    List<Prediction> sub_pred;

    List<Double> macroGMean;
    List<Double> microGMean;
    List<Double> exampleGMean;

    int windowSize;

    public SWEvaluation(int ws){
        sub_labels = new ArrayList<>();
        sub_pred = new ArrayList<>();
        macroGMean = new ArrayList<>();
        microGMean = new ArrayList<>();
        exampleGMean = new ArrayList<>();
        windowSize = ws;
    }

    public void update(Prediction pre, double[] label){
        if (sub_pred.size() < windowSize){
            sub_pred.add(pre);
            sub_labels.add(label);
        }else{
            sub_pred.remove(0);
            sub_pred.add(pre);
            sub_labels.remove(0);
            sub_labels.add(label);
        }

        Evaluation eva = new Evaluation(label.length);
        for (int i = 0; i < sub_pred.size(); i++){
            eva.update(sub_pred.get(i), sub_labels.get(i));
        }

        macroGMean.add(eva.getMarcoGMean());
        microGMean.add(eva.getMicroGMean());
        exampleGMean.add(eva.getLabelSetBasedGMean());
    }

    public List<Double> getMicroGMeanList() {
        return microGMean;
    }

    public List<Double> getMacroGMeanList() {
        return macroGMean;
    }

    public List<Double> getExampleGMeanList() {
        return exampleGMean;
    }

    private double getAverageValue(List<Double> perform){
        double sumValue = 0;
        for (double p: perform){
            sumValue += p;
        }

        return sumValue/perform.size();
    }

    public double getAverageMicroGMean(){
        return this.getAverageValue(microGMean);
    }

    public double getAverageMacroGMean(){
        return this.getAverageValue(macroGMean);
    }

    public double getAverageLSGMean(){
        return this.getAverageValue(exampleGMean);
    }

}
