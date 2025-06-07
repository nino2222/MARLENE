package marlene4.learners;

import com.github.javacliparser.FloatOption;
import com.github.javacliparser.IntOption;
import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.AbstractClassifier;
import moa.classifiers.Classifier;
import moa.classifiers.MultiClassClassifier;
import moa.classifiers.core.driftdetection.ChangeDetector;
import moa.classifiers.core.driftdetection.DDM_OCI;
import moa.core.DoubleVector;
import moa.core.Measurement;
import moa.core.MiscUtils;
import moa.core.Utils;
import moa.options.ClassOption;

import java.util.Random;

public class BLearner extends AbstractClassifier implements MultiClassClassifier {

    public static final int DDM_INCONTROL_LEVEL = 0;
    public static final int DDM_WARNING_LEVEL = 1;
    public static final int DDM_OUTCONTROL_LEVEL = 2;
    protected int ddmLevel;

    public ClassOption baseLearnerOption = new ClassOption("baseLearner", 'c',
            "Classifier to train.", Classifier.class,"meta.OzaBoost");;
    public ClassOption driftDetectionMethodOption = new ClassOption("driftDetectionMethod", 'd',
            "Drift detection method to use.", ChangeDetector.class, "DDM_OCI");;
    public IntOption numOfLabels = new IntOption("numberOfLabels", 'l',
            "The number of target Labels", 1, 1, Integer.MAX_VALUE);



    protected Classifier classifier;
    protected ChangeDetector changeDetector;
    protected LPerformance[] lperfs;
    protected BLearner newclassifier;
    protected double[] results;
    protected double[][] results2;
    protected double nP;
    protected double nN;

//    public BLearner(ClassOption baseLearnerOption, ClassOption driftDetectionMethodOption, FloatOption forgettingFactor){
//        this.baseLearnerOption = baseLearnerOption;
//        this.driftDetectionMethodOption = driftDetectionMethodOption;
//        this.forgettingFactor = forgettingFactor;
//    }

    @Override
    public double[] getVotesForInstance(Instance inst) {
        DoubleVector vote = new DoubleVector(this.classifier.getVotesForInstance(inst));
//        System.out.println(vote);
        if (vote.numValues() < 2){
            vote.setValue(1, 0);
        }
        if (vote.sumOfValues() > 0.0) {
            vote.normalize();
        }else{
            vote.setValue(0,0);
            vote.setValue(1,0);
        }

        if (Double.isNaN(vote.getValue(0))){
            vote.setValue(0, 1);
            vote.setValue(1, 0);
        }

        if (Double.isNaN(vote.getValue(1))){
            vote.setValue(0, 0);
            vote.setValue(1, 1);
        }

        return vote.getArrayRef();
    }

    public BLearner detectDrift(Instance inst){
        boolean prediction = classifier.correctlyClassifies(inst);
        //changeDetector.input(prediction ? 0.0: 1.0);
        ddmLevel = DDM_INCONTROL_LEVEL;

        if (changeDetector instanceof DDM_OCI)
            ((DDM_OCI) changeDetector).input(prediction ? 0.0 : 1.0, inst);
        else
            changeDetector.input(prediction ? 0.0 : 1.0);

        if (changeDetector.getChange()){
            this.ddmLevel = DDM_OUTCONTROL_LEVEL;
        }else if(changeDetector.getWarningZone()){
            this.ddmLevel = DDM_WARNING_LEVEL;
        }

//        if (ddmLevel == DDM_OUTCONTROL_LEVEL)
//            System.out.println("Drift Detected");

        switch (this.ddmLevel){
            case DDM_WARNING_LEVEL:
                if (newclassifier == null){
                    newclassifier = new BLearner();
                    newclassifier.baseLearnerOption.setValueViaCLIString(this.baseLearnerOption.getValueAsCLIString());
                    newclassifier.driftDetectionMethodOption.setValueViaCLIString(this.driftDetectionMethodOption.getValueAsCLIString());
                    newclassifier.numOfLabels.setValue(this.numOfLabels.getValue());
                    newclassifier.setRandomSeed(this.randomSeed);
                    newclassifier.prepareForUse();
                    newclassifier.resetLearning();
                }
                newclassifier.trainOnInstance(inst);
                break;
            case DDM_OUTCONTROL_LEVEL:
                return newclassifier;
            case DDM_INCONTROL_LEVEL:
                newclassifier = null;
                break;
        }

        return null;
    }

    @Override
    public void resetLearningImpl() {
        classifier = (Classifier) getPreparedClassOption(this.baseLearnerOption);
        classifier.prepareForUse();
        classifier.resetLearning();
        changeDetector = (ChangeDetector) getPreparedClassOption(driftDetectionMethodOption);
        changeDetector.prepareForUse();
        changeDetector.resetLearning();
        lperfs = new LPerformance[numOfLabels.getValue()];
        for (int i = 0; i < numOfLabels.getValue(); i++){
            lperfs[i] = new LPerformance();
        }
        newclassifier = null;
        nN = 0;
        nP = 0;
    }

    @Override
    public void trainOnInstanceImpl(Instance inst) {
        double weight = inst.weight();
        if (inst.classValue() == 0){
//            nN = this.forgettingFactor.getValue() * nN + (1 - this.forgettingFactor.getValue());
//            nP = this.forgettingFactor.getValue() * nP;
            nN ++;
//            inst.setWeight((nN+nP)/2/nN);
        }else
        {
//            nP = this.forgettingFactor.getValue() * nP + (1 - this.forgettingFactor.getValue());
//            nN = this.forgettingFactor.getValue() * nN;
            nP ++;
//            inst.setWeight((nN+nP)/2/nP);
        }

        double max = nN > nP ? nN : nP;

        double k;
        if (inst.classValue() == 0){
            k = MiscUtils.poisson(max/nN, new Random(this.randomSeed));
        }else
        {
            k = MiscUtils.poisson(max/nP, new Random(this.randomSeed));
        }

        inst.setWeight(k);
//        System.out.println(" max: " + max + " nN: " + nN + " nP: "+ nP + " k: " + k);

        this.classifier.trainOnInstance(inst);
        inst.setWeight(weight);

//        while(!this.classifier.correctlyClassifies(inst)) {
//            this.classifier.trainOnInstance(inst);
//        }
    }


    public void updatePerf(double[] sw, double[] sc, double[] classValues){

        for (int i = 0; i < classValues.length; i++){
            lperfs[i].update(sw[i], sc[i], this.results, this.results2[i], classValues[i]);
        }
    }

    public LPerformance[] getLPerfs(){
        return lperfs;
    }

    public void resetPerfOnLabel(int i){
        lperfs[i] = new LPerformance();
    }

    public void saveResults(double[] results, double[][] results2){
        this.results = results;
        this.results2 = results2;
    }

    @Override
    protected Measurement[] getModelMeasurementsImpl() {
        return new Measurement[0];
    }


    @Override
    public void getModelDescription(StringBuilder out, int indent) {

    }


    @Override
    public boolean isRandomizable() {
        return false;
    }

//    public static void main(String args[]) {
//
//        int a[][] = new int[2][3];
//        System.out.println(a.length);
//    }
}
