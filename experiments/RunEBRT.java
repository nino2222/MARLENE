package marlene3.experiments;

import com.yahoo.labs.samoa.instances.MultiLabelInstance;
import com.yahoo.labs.samoa.instances.Prediction;
import gooweml.Baselines.OzaBagMLISOUP;
import marlene3.evaluation.Evaluation;
import moa.classifiers.multilabel.meta.OzaBagML;
import moa.core.Utils;
import moa.streams.MultiTargetArffFileStream;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RunEBRT {
    public static void main(String args[]) throws IOException {
        String path = "/home/h/hd168/MarleneDataSet/";
        String outpath = "/home/h/hd168/MarleneOutput/";
//        String[] dataset = new String[]{"Slashdot", "Ohsumed", "Reuters", "Yeast"};
//        int[] numOfLabels = new int[]{22, 23, 103, 14};

        String[] dataset = new String[]{"Slashdot", "Ohsumed", "Reuters", "Yeast", "20NG", "TMC2007", "IMDB"};
        int[] numOfLabels = new int[]{22, 23, 103, 14, 20, 22, 28};

//        String[] dataset = new String[]{"IMDB"};
//        int[] numOfLabels = new int[]{28};
//        String[] dataset = new String[]{"Yeast"};
//        int[] numOfLabels = new int[]{14};
        double[] microGMean = new double[30];
        double[] macroGMean = new double[30];
        double[] exampleGMean = new double[30];
        double[] time = new double[30];


        for (int d = 0; d < dataset.length; d++) {
//            for (int r = 0; r < 30; r++) {
            int r = Integer.valueOf(args[0]);
                System.out.println("EBRT" + dataset[d] + " : " + r);
                MultiTargetArffFileStream stream = new MultiTargetArffFileStream(
                        path + dataset[d] + "/" + dataset[d] + "_full.arff", Integer.toString(numOfLabels[d]));
                stream.prepareForUse();

                OzaBagMLISOUP learner = new OzaBagMLISOUP();
//                learner.baseLearnerOption.setValueViaCLIString(
//                        "multilabel.MEKAClassifier -l (meka.classifiers.multilabel.incremental.PSUpdateable -I 100 -S 10 -W weka.classifiers.trees.HoeffdingTree)");
                learner.setRandomSeed(r + 1);
                learner.setModelContext(stream.getHeader());
                learner.prepareForUse();
                learner.resetLearning();

//                Evaluation eva = new Evaluation(numOfLabels[d]);
            Output output = new Output();
                long starttime, endtime;
                starttime = System.currentTimeMillis();
                while (stream.hasMoreInstances()) {
                    MultiLabelInstance inst = (MultiLabelInstance) stream.nextInstance().getData();
                    double[] labels = new double[inst.numOutputAttributes()];
                    for (int i = 0; i < labels.length; i++) {
                        labels[i] = inst.valueOutputAttribute(i);
                    }
                    Prediction pred = learner.getPredictionForInstance(inst);
                    output.addPred(pred);
//                    eva.update(pred, labels);
                    learner.trainOnInstance(inst);
                }

                endtime = System.currentTimeMillis();

            output.addTime(endtime - starttime);
            output.writeOutput(outpath + "/EBRT/EBRT_" + dataset[d] + "_" +r + ".txt");
//                microGMean[r] = eva.getMicroGMean();
//                macroGMean[r] = eva.getMarcoGMean();
//                exampleGMean[r] = eva.getLabelSetBasedGMean();
//                time[r] = endtime - starttime;
//            }

//            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path + "/EBRT/EBRT_" + dataset[d] + "_" +r + ".txt")));
//            writer.write("MicroGMean: " + Utils.sum(microGMean) + "\n");
//            writer.write("MacroGMean: " + Utils.sum(macroGMean) + "\n");
//            writer.write("ExampleGMean: " + Utils.sum(exampleGMean) + "\n");
//            writer.write("Time: " + Utils.sum(time));
//            writer.close();
//            writer.write("MicroGMean: " + Utils.sum(microGMean) / 30 + "\n");
//            writer.write("MacroGMean: " + Utils.sum(macroGMean) / 30 + "\n");
//            writer.write("ExampleGMean: " + Utils.sum(exampleGMean) / 30+ "\n");
//            writer.write("Time: " + Utils.sum(time) / 30);
//            writer.close();

        }

    }
}
