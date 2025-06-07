package marlene3.test;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.MultiLabelPrediction;
import com.yahoo.labs.samoa.instances.Prediction;
import marlene3.evaluation.Evaluation;
import marlene3.utils.utils;
import moa.classifiers.meta.OzaBoost;
import moa.classifiers.trees.HoeffdingTree;
import moa.core.DoubleVector;
import moa.streams.ArffFileStream;

import java.io.IOException;

public class SingleLearnerTest {
    public static void main(String args[]) throws IOException {
        //HoeffdingTree[] learner = new HoeffdingTree[5];
//        OzaBoost[] learner = new OzaBoost[5];
        HoeffdingTree[] learner = new HoeffdingTree[14];
        for (int i = 0 ; i < learner.length; i++){
            learner[i] = new HoeffdingTree();
//            learner[i] = new OzaBoost();
//            learner[i].ensembleSizeOption.setValue(25);
            learner[i].prepareForUse();
            learner[i].resetLearning();
        }

//
//        String path = "/home/h/hd168/MarleneDataSet/Case7/";
//        String larff_name = "Case7_500_l.arff";
//        String darff_name = "Case7_500_d.arff";
//        String txt_name = "Case7_500_labels.txt";

        String path = "/home/h/hd168/MarleneDataSet/Real1/";
        String larff_name = "Real1_l.arff";
        String darff_name = "Real1_d.arff";
        String txt_name = "Real1_labels.txt";

        Instance[] l_insts = utils.readInstances(path+larff_name);
        double[][] labels = utils.readLabels(path+txt_name);

        double[] count = new double[14];

        Evaluation eva = new Evaluation(14);
        for (int i = 0; i < l_insts.length; i++){
            Prediction pre = new MultiLabelPrediction(14);
            for (int j = 0; j < learner.length; j++) {
                l_insts[i].setClassValue(labels[i][j]);
                double[] vote1 = learner[j].getVotesForInstance(l_insts[i]);
                DoubleVector vote = new DoubleVector(vote1);
                vote.normalize();
                if (vote.maxIndex()==1)
                    count[j]++;
                pre.setVotes(j, vote.getArrayRef());
                learner[j].trainOnInstance(l_insts[i]);
            }
            eva.update(pre, labels[i]);
            System.out.println(utils.Pred2Str(pre) + ":" + utils.Label2Str(labels[i]));
        }

        double[] acc = eva.getAverageAccuracy();
        for (int i = 0; i < acc.length; i++){
            System.out.println(i + ": " + acc[i]);
        }
        System.out.println("Recall");
        double[] recall = eva.getRecall();
        for (int i = 0; i < recall.length; i++){
            System.out.println(i + ": " + recall[i]);
        }

        System.out.println("F1");
        double[] F1 = eva.getFMeasure(1);
        for (int i = 0; i < F1.length; i++){
            System.out.println(i + ": " + F1[i]);
        }
        System.out.println("MarcoGMean: " + eva.getMarcoGMean());
        System.out.println("MicroGMean: " + eva.getMicroGMean());
        System.out.println("LabelSetBasedGMean: " + eva.getLabelSetBasedGMean());
//        System.out.println("ExampleBasedRecall: " + eva.getExampleBasedRecall());
//        System.out.println("ExampleBasedF1: " + eva.getExampleBasedFMeasure(1));
//        System.out.println("MicroRecall: " + eva.getMicroRecall());
//        System.out.println("ExactMatch: " + eva.getExactMatch());
//        System.out.println("Hamming Score: " + eva.getHammingScore());

        for (int i = 0; i< count.length; i++){
            System.out.println("i: " +(i+1) + " : " + count[i]);
        }

//        Evaluation[] eva = new Evaluation[5];
//        for (int i = 0; i < eva.length; i++){
//            eva[i] = new Evaluation(5);
//        }
//
//        for (int i = 0; i < l_insts.length; i++){
//            Prediction[] pre = new Prediction[5];
//            for (int j = 0; j < pre.length; j++) {
//                pre[j] = new MultiLabelPrediction(5);
//            }
//            for (int j = 0; j < learner.length; j++) {
//                l_insts[i].setClassValue(labels[i][j]);
//                double[] vote1 = learner[j].getVotesForInstance(l_insts[i]);
//                DoubleVector vote = new DoubleVector(vote1);
//                vote.normalize();
//                for (int k = 0; k< 5; k++) {
//                    pre[j].setVotes(k, vote.getArrayRef());
//                }
//                learner[j].trainOnInstance(l_insts[i]);
//                eva[j].update(pre[j], labels[i]);
//                System.out.println("i: " + i + " : " + utils.Pred2Str(pre[j]) + ":" + utils.Label2Str(labels[i]));
//            }
//        }
//
//        for (int j = 0; j< 5; j++) {
//            for (int i = 0; i < 5; i++) {
//                double[] acc = eva[i].getAverageAccuracy();
//                System.out.println("Learner: " + (i+1) + " label: " + (j+1) + ": " + acc[j]);
//            }
//        }
    }
}
