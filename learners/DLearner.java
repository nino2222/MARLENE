package marlene4.learners;

import com.yahoo.labs.samoa.instances.Instance;
import moa.classifiers.Classifier;
import moa.classifiers.core.driftdetection.ChangeDetector;
import moa.classifiers.core.driftdetection.DDM_OCI;
import moa.core.DoubleVector;

public class DLearner extends BLearner {
    protected LPerformance[][] dperfs;
    protected DLearner newclassifier;
    protected double[][] results;
    protected double[][][] results2;

    public void saveResults(double[][] results, double[][][] results2){
        this.results = results;
        this.results2 = results2;
    }

    @Override
    public void resetLearningImpl() {
        classifier = (Classifier) getPreparedClassOption(this.baseLearnerOption);
        classifier.prepareForUse();
        classifier.resetLearning();
        changeDetector = (ChangeDetector) getPreparedClassOption(driftDetectionMethodOption);
        changeDetector.prepareForUse();
        changeDetector.resetLearning();
        dperfs = new LPerformance[this.numOfLabels.getValue()][this.numOfLabels.getValue()];

        for (int i = 0; i < this.numOfLabels.getValue(); i++){
            for (int j = 0; j < this.numOfLabels.getValue(); j++){
                if (i != j)
                    dperfs[i][j] = new LPerformance();
                else
                    dperfs[i][j] = null;
            }
        }
        newclassifier = null;
    }

    public DLearner() {
        super();
    }

    public DLearner detectDrift(Instance inst){
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

        switch (this.ddmLevel){
            case DDM_WARNING_LEVEL:
                if (newclassifier == null){
                    newclassifier = new DLearner();
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


    public void updatePerf(double[][] sw, double[][] sc, double[] classValues){
//        if (this.dperfs == null){
//            dperfs = new LPerformance[classValues.length][classValues.length];
//
//            for (int i = 0; i < classValues.length; i++){
//                for (int j = 0; j < classValues.length; j++){
//                    if (i != j)
//                        dperfs[i][j] = new LPerformance(this.forgettingFactor.getValue());
//                    else
//                        dperfs[i][j] = null;
//                }
//            }
//        }

        //i is the condition, j is the target
        for (int i = 0; i < classValues.length; i++){
            for (int j = 0; j < classValues.length; j++){
                if (i != j){
                    dperfs[i][j].update(sw[i][j], sc[i][j], this.results[i], this.results2[i][j], classValues[j]);
                }
            }
        }
    }

    public LPerformance[][] getDPerfs(){
        return dperfs;
    }

    public void resetAllPerfOnDependence(int i, int j){
        dperfs[i][j] = new LPerformance();
    }
}
