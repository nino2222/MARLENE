package marlene3.experiments;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Prediction;
import marlene3.evaluation.Evaluation;
import marlene3.utils.utils;
import marlene4.learners.BRMarlene;
import marlene4.learners.BRPWMarlene;
import moa.core.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RunBRPWMarlene {
    public static void main(String args[]) throws IOException {
        String path = "/home/h/hd168/MarleneDataSet/";
        String outpath = "/home/h/hd168/MarleneOutput/";
//        String[] dataset = new String[]{"Slashdot", "Ohsumed", "Reuters", "Yeast"};
//        int[] numOfLabels = new int[]{22, 23, 103, 14};

//        String[] dataset = new String[]{"Case7_50", "Case7_500", "Case7_5000",
//                "Case8_50", "Case8_500", "Case8_5000",
//                "Case9_50", "Case9_500", "Case9_5000",
//                "Case10_50", "Case10_500", "Case10_5000",
//                "Case11_50", "Case11_500", "Case11_5000",
//                "Case12_50", "Case12_500", "Case12_5000"};
////        int[] numOfLabels = new int[]{7, 7, 7,
////                7, 7, 7,
////                7, 7, 7,
////                7, 7, 7,
////                7, 7, 7,
////                7, 7, 7};
//
//        int[] numOfLabels = new int[]{5, 5, 5,
//                5, 5, 5,
//                5, 5, 5,
//                5, 5, 5,
//                5, 5, 5,
//                5, 5, 5};

        String[] dataset = new String[]{"Case7_50"};
        int[] numOfLabels = new int[]{5};

//        String[] dataset = new String[]{"Case7_50", "Case7_500", "Case7_5000"};
//        int[] numOfLabels = new int[]{7, 7, 7};

//        String[] dataset = new String[]{"Yeast"};
//        int[] numOfLabels = new int[]{14};
        double[] microGMean = new double[30];
        double[] macroGMean = new double[30];
        double[] exampleGMean = new double[30];
        double[] time = new double[30];

        int r = Integer.valueOf(args[0]);

        for (int d = 0; d < dataset.length; d++) {
            System.out.println("BRPWMarlene" + dataset[d] + " : " + r);
            double[][] labels = utils.readLabels(path+dataset[d] + "/" + dataset[d] + "_labels.txt");

            Instance[] l_insts = utils.readInstances(path+dataset[d] + "/" + dataset[d] + "_l.arff");
            Instance[] d_insts = utils.readInstances(path+dataset[d] + "/" + dataset[d] + "_d.arff");
            double[] aswr = new double[l_insts.length];
//            for (int r = 0; r < 30; r++) {

                BRPWMarlene learner = new BRPWMarlene();
                learner.baseLearnerOption.setValueViaCLIString("trees.HoeffdingTree");
                learner.driftDetectionMethodOption.setValueViaCLIString("DDM_OCI");
                learner.numOfLabels.setValue(numOfLabels[d]);
                learner.targetDomainIndexOption.setValue(0);
                learner.randomSeed.setValue(r+1);

                learner.manualSetConceptDriftOption.set();

//                Evaluation eva = new Evaluation(numOfLabels[d]);
            Output output = new Output();
                long starttime, endtime;
                starttime = System.currentTimeMillis();
                for (int i = 0; i < l_insts.length; i++){
                    Prediction pred = learner.getPredictionForInstance(l_insts[i], d_insts[i]);
//                    eva.update(pred, labels[i]);
                    output.addPred(pred);

                    if (d == 0 || d == 1 || d==2){
                        learner.setNumberOfReset(false, new int[]{-1});
                    }else if ((d == 3 && i % 250 == 0 && i != 0) || (d == 4 && i % 2500 == 0 && i != 0) || (d == 5 && i%25000==0 && i != 0)){
                        learner.setNumberOfReset(true, new int[]{0});
                    }else if ((d == 6 && i % 250 == 0 && i != 0)  || (d == 7 && i % 2500 == 0 && i != 0) || (d == 8 && i%25000==0 && i != 0)){
                        learner.setNumberOfReset(true, new int[]{0, 1});
                    }else if ((d == 9 && i % 250 == 0 && i != 0)  || (d == 10 && i % 2500 == 0 && i != 0) || (d == 11 && i%25000==0 && i != 0)){
                        if ((d == 9 && i == 750) || (d == 10 && i == 7500) || (d == 11 && i == 75000))
                            learner.setNumberOfReset(true, new int[]{0, 1});
                        else
                            learner.setNumberOfReset(true, new int[]{0});
                    }else if ((d == 12 && i % 250 == 0 && i != 0)  || (d == 13 && i % 2500 == 0 && i != 0) || (d == 14 && i%25000==0 && i != 0)){
                        learner.setNumberOfReset(true, new int[]{0, 1});
                    }else if ((d == 15 && i % 250 == 0 && i != 0)  || (d == 16 && i % 2500 == 0 && i != 0) || (d == 17 && i%25000==0 && i != 0)){
                        learner.setNumberOfReset(true, new int[]{0});
                    }

                    learner.trainOnInstanceImpl(0, l_insts[i], d_insts[i], labels[i]);
                    learner.setNumberOfReset(false, new int[]{-1});
                }

                endtime = System.currentTimeMillis();

            output.addTime(endtime - starttime);
            output.writeOutput(outpath + "/BRPWMarlene/BRPWMarlene_" + dataset[d] + "_" +r + ".txt");
//                microGMean[r] = eva.getMicroGMean();
//                macroGMean[r] = eva.getMarcoGMean();
//                exampleGMean[r] = eva.getLabelSetBasedGMean();
//                time[r] = endtime - starttime;
//            }

//            BufferedWriter writer =  new BufferedWriter(new FileWriter(new File(path + "/BRPWMarlene_" + dataset[d] + ".txt")));
//            writer.write("MicroGMean: " + Utils.sum(microGMean)/30 + "\n");
//            writer.write("MacroGMean: " + Utils.sum(macroGMean)/30 + "\n");
//            writer.write("ExampleGMean: " + Utils.sum(exampleGMean)/30 + "\n");
//            writer.write("Time: " + Utils.sum(time)/30);
//            writer.close();

        }

    }
}
