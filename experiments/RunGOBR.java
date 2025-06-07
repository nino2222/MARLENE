package marlene3.experiments;

import com.yahoo.labs.samoa.instances.MultiLabelInstance;
import com.yahoo.labs.samoa.instances.Prediction;
import gooweml.GOOWE.GOOWEML;
import marlene3.evaluation.Evaluation;
import moa.classifiers.multilabel.meta.OzaBagML;
import moa.core.Utils;
import moa.streams.MultiTargetArffFileStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RunGOBR {
    public static void main(String args[]) throws IOException {
        String path = "/home/h/hd168/MarleneDataSet/";
        String outpath = "/home/h/hd168/MarleneOutput/";
//        String[] dataset = new String[]{"Slashdot", "Ohsumed", "Reuters", "Yeast"};
//        int[] numOfLabels = new int[]{22, 23, 103, 14};


        String[] dataset = new String[]{"Slashdot", "Ohsumed", "Reuters", "Yeast", "20NG", "TMC2007", "IMDB"};
        int[] numOfLabels = new int[]{22, 23, 103, 14, 20, 22, 28};

//        String[] dataset = new String[]{ "IMDB"};
//        int[] numOfLabels = new int[]{28};

//        String[] dataset = new String[]{"Yeast"};
//        int[] numOfLabels = new int[]{14};
        int[] numOfInstance = new int[]{3782, 13529, 6000, 2417, 19300, 28596, 120919};
//        int[] numOfInstance = new int[]{120919};
        double microGMean = 0;
        double macroGMean = 0;
        double exampleGMean = 0;
        double time = 0;


        for (int d = 0; d < dataset.length; d++) {
            System.out.println("GOBR" + dataset[d]);
            MultiTargetArffFileStream stream = new MultiTargetArffFileStream(
                    path + dataset[d] + "/" + dataset[d] + "_full.arff", Integer.toString(numOfLabels[d]));
            stream.prepareForUse();

            GOOWEML learner = new GOOWEML();
            learner.baseLearnerOption.setValueViaCLIString(
                    "multilabel.MEKAClassifier -l (meka.classifiers.multilabel.incremental.BRUpdateable -W weka.classifiers.trees.HoeffdingTree)");
            learner.setWindowSize(numOfInstance[d] / 20);
            learner.setModelContext(stream.getHeader());
            learner.prepareForUse();
            learner.resetLearning();

//            Evaluation eva = new Evaluation(numOfLabels[d]);
            Output output = new Output();
            long starttime, endtime;
            starttime = System.currentTimeMillis();
            int index = 0;
            while (stream.hasMoreInstances()) {
                MultiLabelInstance inst = (MultiLabelInstance) stream.nextInstance().getData();
                double[] labels = new double[inst.numOutputAttributes()];
                for (int i = 0; i < labels.length; i++) {
                    labels[i] = inst.valueOutputAttribute(i);
                }

                if (index > (numOfInstance[d] / 20)) {
                    Prediction pred = learner.getPredictionForInstance(inst);
//                    eva.update(pred, labels);
                    output.addPred(pred);
                }
                learner.trainOnInstance(inst);
                index++;
            }

            endtime = System.currentTimeMillis();

            output.addTime(endtime - starttime);
            output.writeOutput(outpath + "/GOBR/GOBR_" + dataset[d] + "_" +"0" + ".txt");
//            microGMean = eva.getMicroGMean();
//            macroGMean = eva.getMarcoGMean();
//            exampleGMean = eva.getLabelSetBasedGMean();
//            time = endtime - starttime;
//
//
//            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "/GOBR_" + dataset[d] + ".txt")));
//            writer.write("MicroGMean: " + microGMean + "\n");
//            writer.write("MacroGMean: " + macroGMean + "\n");
//            writer.write("ExampleGMean: " + exampleGMean + "\n");
//            writer.write("Time: " + time);
//            writer.close();
        }

    }
}
