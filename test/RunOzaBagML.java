package marlene3.test;


import com.yahoo.labs.samoa.instances.MultiLabelInstance;
import com.yahoo.labs.samoa.instances.Prediction;
import gooweml.GOOWE.GOOWEML;
import marlene3.evaluation.Evaluation;
import marlene3.utils.utils;
import moa.classifiers.multilabel.meta.OzaBagAdwinML;
import moa.classifiers.multilabel.meta.OzaBagML;
import moa.streams.MultiTargetArffFileStream;

public class RunOzaBagML {
    public static void main(String args[]){
//        String filePaths = "/home/h/hd168/MarleneDataSet/Real1/Real1_full.arff";
//        String filePaths = "/home/h/hd168/MarleneDataSet/Case7/Case7_500_full.arff";
//        String filePaths = "/home/h/hd168/MarleneDataSet/Reuters/Reuters_full.arff";
//        String filePaths = "/home/h/hd168/MarleneDataSet/Ohsumed/Ohsumed_full.arff";
//        String filePaths = "/home/h/hd168/MarleneDataSet/Slashdot/Slashdot_full.arff";
        String filePaths = "/home/h/hd168/MarleneDataSet/Yeast/Yeast_full.arff";
        MultiTargetArffFileStream stream = new MultiTargetArffFileStream(filePaths, "14");
        stream.prepareForUse();


//        OzaBagML learner = new OzaBagML();
//        learner.baseLearnerOption.setValueViaCLIString("multilabel.MEKAClassifier -l (meka.classifiers.multilabel.incremental.BRUpdateable -W weka.classifiers.trees.HoeffdingTree)");
////        learner.baseLearnerOption.setValueViaCLIString("multilabel.MEKAClassifier -l (meka.classifiers.multilabel.incremental.CCUpdateable -W weka.classifiers.trees.HoeffdingTree)");
////        learner.setRandomSeed(3);
//        learner.prepareForUse();
//        learner.resetLearning();

        OzaBagAdwinML learner = new OzaBagAdwinML();
//        learner.baseLearnerOption.setValueViaCLIString("multilabel.MEKAClassifier -l (meka.classifiers.multilabel.incremental.BRUpdateable -W weka.classifiers.trees.HoeffdingTree)");
        learner.baseLearnerOption.setValueViaCLIString("multilabel.MEKAClassifier -l (meka.classifiers.multilabel.incremental.CCUpdateable -W weka.classifiers.trees.HoeffdingTree)");
//        learner.baseLearnerOption.setValueViaCLIString("multilabel.MEKAClassifier -l (meka.classifiers.multilabel.incremental.PSUpdateable -I 100 -S 10 -W weka.classifiers.trees.HoeffdingTree)");
//        learner.isIsoup = true;
//        learner.setWindowSize(100);
        learner.setModelContext(stream.getHeader());
        learner.prepareForUse();
        learner.resetLearning();

        Evaluation eva = new Evaluation(14);

        int index = 0;
        while (stream.hasMoreInstances()){
            MultiLabelInstance inst = (MultiLabelInstance) stream.nextInstance().getData();
            double[] labels = new double[inst.numOutputAttributes()];
            for (int i = 0; i < labels.length; i++){
                labels[i] = inst.valueOutputAttribute(i);
            }
            if (index > 100) {
                Prediction pred = learner.getPredictionForInstance(inst);
                eva.update(pred, labels);
                System.out.println(utils.Pred2Str(pred) + ":" + utils.Label2Str(labels));
            }
            learner.trainOnInstance(inst);
            index++;
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

        double[] F1 = eva.getFMeasure(1);
        System.out.println("F1");
        for (int i = 0; i < F1.length; i++){
            System.out.println(i + ": " + F1[i]);
        }

        double[] gM = eva.getAverageGMean();
        for (int i = 0; i < gM.length; i++){
            System.out.println((i+1) + ": " + gM[i]);
        }

        System.out.println("MarcoGMean: " + eva.getMarcoGMean());
        System.out.println("MicroGMean: " + eva.getMicroGMean());
        System.out.println("LabelSetBasedGMean: " + eva.getLabelSetBasedGMean());

        System.out.println("AverageMacroGMean: " + eva.getAverageMacroGMean());
        System.out.println("AverageMicroGMean: " + eva.getAverageMicroGMean());
//        System.out.println("ExampleBasedRecall: " + eva.getExampleBasedRecall());
//        System.out.println("ExampleBasedF1: " + eva.getExampleBasedFMeasure(1));
//        System.out.println("MicroRecall: " + eva.getMicroRecall());
//        System.out.println("ExactMatch: " + eva.getExactMatch());
//        System.out.println("Hamming Score: " + eva.getHammingScore());
    }

}
