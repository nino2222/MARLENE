package marlene3.test;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Prediction;
import marlene3.evaluation.Evaluation;
import marlene3.learners.Marlene3;
import marlene3.utils.utils;
import marlene4.learners.BRMarlene;
import marlene4.learners.BRPWMarlene;
import meka.core.F;
import moa.core.Utils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestWR {
    public static void main(String args[]) throws IOException {
//        String path = "/home/h/hd168/MarleneDataSet/Case7/";
//        String larff_name = "Case7_500_l.arff";
//        String darff_name = "Case7_500_d.arff";
//        String txt_name = "Case7_500_labels.txt";
//
//        String path = "/home/h/hd168/MarleneDataSet/Reuters/";
//        String larff_name = "Reuters_l.arff";
//        String darff_name = "Reuters_d.arff";
//        String txt_name = "Reuters_labels.txt";

//        String path = "/home/h/hd168/MarleneDataSet/Slashdot/";
//        String larff_name = "Slashdot_l.arff";
//        String darff_name = "Slashdot_d.arff";
//        String txt_name = "Slashdot_labels.txt";

//        String path = "/home/h/hd168/MarleneDataSet/Yeast/";
//        String larff_name = "Yeast_l.arff";
//        String darff_name = "Yeast_d.arff";
//        String txt_name = "Yeast_labels.txt";

        String path = "/home/h/hd168/MarleneDataSet/";
        String larff_name = "Case11_5000/Case11_5000_l.arff";
        String darff_name = "Case11_5000/Case11_5000_d.arff";
        String txt_name = "Case11_5000/Case11_5000_labels.txt";

//        String path = "/home/h/hd168/MarleneDataSet/";
//        String larff_name = "DBData/DBTarget_500_l.arff";
//        String darff_name = "DBData/DBTarget_500_d.arff";
//        String txt_name = "DBData/DBTarget_500_labels.txt";

//        String path = "/home/h/hda_labels.txt";


//        String path = "/home/h/hd168/MarleneDataSet/Ohsumed/";
//        String larff_name = "Ohsumed_l.arff";
//        String darff_name = "Ohsumed_d.arff";
//        String txt_name = "Ohsumed_labels.txt";

        double[][] labels = utils.readLabels(path+txt_name);

        Instance[] l_insts = utils.readInstances(path+larff_name);
        Instance[] d_insts = utils.readInstances(path+darff_name);

//        Marlene4 marlene = new Marlene4();
        List<Double> aswrList = new ArrayList<>();
        for (int r = 1; r<=30; r++) {
            BRMarlene marlene = new BRMarlene();
//        BRPWMarlene marlene = new BRPWMarlene();
            //marlene.baseLearnerOption.setValueViaCLIString("meta.OzaBoost -s 25");
            marlene.baseLearnerOption.setValueViaCLIString("trees.HoeffdingTree");
            marlene.driftDetectionMethodOption.setValueViaCLIString("DDM");
            marlene.numOfLabels.setValue(5);
            marlene.targetDomainIndexOption.setValue(0);
            marlene.randomSeed.setValue(r);
            marlene.manualSetConceptDriftOption.set();


            Evaluation eva = new Evaluation(5);
//            List<double[][]> wc = new ArrayList<>();


            //train source
        double[][] slabels = utils.readLabels(path  + "Case11_Source/Source1_5000_labels.txt");

        Instance[] sl_insts = utils.readInstances(path + "Case11_Source/Source1_5000_l.arff");
        Instance[] sd_insts = utils.readInstances(path + "Case11_Source/Source1_5000_d.arff");

        for (int i = 0; i < sl_insts.length; i++){
            marlene.trainOnInstanceImpl(1, sl_insts[i], sd_insts[i], slabels[i]);
        }

            slabels = utils.readLabels(path  + "Case11_Source/Source2_5000_labels.txt");

            sl_insts = utils.readInstances(path + "Case11_Source/Source2_5000_l.arff");
            sd_insts = utils.readInstances(path + "Case11_Source/Source2_5000_d.arff");

            for (int i = 0; i < sl_insts.length; i++){
                marlene.trainOnInstanceImpl(2, sl_insts[i], sd_insts[i], slabels[i]);
            }


            //train target
            for (int i = 0; i < l_insts.length; i++) {
//            if (i == l_insts.length-1){
//                System.out.println("");
//            }
                Prediction pred = marlene.getPredictionForInstance(l_insts[i], d_insts[i]);
                //double[] wr = marlene.getWeightRatio();
                double aswr = marlene.getASWR();
//                for (int j = 0; j < wr.length; j++) {
//                    System.out.println("wr " + j + ": " + wr[j]);
//                }
//            System.out.println("aswr: " + aswr);

//            System.out.println(utils.Pred2Str(pred) + ":" +utils.Label2Str(labels[i]));

//                if (i != 0)
//                    wc.add(marlene.getWeightRationOnLabel(0));
                if (r == 1){
                    aswrList.add(marlene.getASWR());
                }else {
                    aswrList.set(i, aswrList.get(i) + marlene.getASWR());
                }

                //eva.update(pred, labels[i]);
                if (i % 25000 == 0 && i != 0) {
                    System.out.println(i);
                    marlene.setNumberOfReset(true, new int[]{0});
                }
                marlene.trainOnInstanceImpl(0, l_insts[i], d_insts[i], labels[i]);
                marlene.setNumberOfReset(false, new int[]{-1});
            }
        }

        for (int i = 0; i < aswrList.size(); i++){
            aswrList.set(i, aswrList.get(i)/30);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/h/hd168/MarleneOutput/Performance/" +
                "Label_Contribution1.csv")));
//        for (int i = 0; i < wc.size(); i++){
//            for (int j = 0; j < wc.get(i).length; j ++){
//                for (int k = 0; k < wc.get(i)[j].length; k++){
//                    if (j == wc.get(i).length -1 && k == wc.get(i)[j].length-1)
//                        writer.write(wc.get(i)[j][k] + "\n");
//                    else
//                        writer.write(wc.get(i)[j][k] + ",");
//                }
//            }
//        }

        for (int i = 0; i < aswrList.size(); i++){
            writer.write(aswrList.get(i) + "\n");
        }

        writer.close();


//        double[] acc = eva.getAverageAccuracy();
//        for (int i = 0; i < acc.length; i++){
//            System.out.println((i+1) + ": " + acc[i]);
//        }
//
//        System.out.println("Recall");
//        double[] recall = eva.getRecall();
//        for (int i = 0; i < recall.length; i++){
//            System.out.println((i+1) + ": " + recall[i]);
//        }
//
//        System.out.println("Precision");
//        double[] precision = eva.getPrecision();
//        for (int i = 0; i < precision.length; i++){
//            System.out.println((i+1) + ": " + precision[i]);
//        }
//
//        System.out.println("F1");
//        double[] F1 = eva.getFMeasure(1);
//        for (int i = 0; i < F1.length; i++){
//            System.out.println((i+1) + ": " + F1[i]);
//        }
//
//        System.out.println("GMean");
//        double[] gM = eva.getAverageGMean();
//        for (int i = 0; i < gM.length; i++){
//            System.out.println((i+1) + ": " + gM[i]);
//        }
//
//
//        System.out.println("LSRecall: " + eva.getExampleBasedRecall());
//        System.out.println("LSF1: " + eva.getExampleBasedFMeasure(1));
//        System.out.println("MacroGMean: " + eva.getMarcoGMean());
//        System.out.println("MicroGMean: " + eva.getMicroGMean());
//        System.out.println("LabelSetBasedGMean: " + eva.getLabelSetBasedGMean());
//
//        System.out.println("AverageMacroGMean: " + eva.getAverageMacroGMean());
//        System.out.println("AverageMicroGMean: " + eva.getAverageMicroGMean());
//
//        double[] a = new double[]{0.0, 2.0, 3.0};
//
//        System.out.println(ArrayUtils.contains(a, 5));

//        System.out.println("ExampleBasedRecall: " + eva.getExampleBasedRecall());
//        System.out.println("ExampleBasedF1: " + eva.getExampleBasedFMeasure(1));
//        System.out.println("MicroRecall: " + eva.getMicroRecall());
//        System.out.println("ExactMatch: " + eva.getExactMatch());
//        System.out.println("Hamming Score: " + eva.getHammingScore());

//        double[] count = marlene.getCount();
//        for (int i = 0; i< count.length; i++){
//            System.out.println("i: " +(i+1) + " : " + count[i]);
//        }

    }
}

