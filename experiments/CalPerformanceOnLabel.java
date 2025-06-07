package marlene3.experiments;

import com.yahoo.labs.samoa.instances.MultiLabelPrediction;
import com.yahoo.labs.samoa.instances.Prediction;
import marlene3.evaluation.Evaluation;
import marlene3.evaluation.SWEvaluation;
import marlene3.utils.utils;
import moa.core.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CalPerformanceOnLabel {

    public static void main(String args[]) throws IOException {
        String dataPath = "/home/h/hd168/MarleneDataSet/";
        String prePath = "/home/h/hd168/MarleneOutput/";

        int numberOfLabels = 7;

        String[] methods = new String[]{"BRMarlene", "BRPWMarlene", "BRMarlene_NS", "BRPWMarlene_NS", "BRMarlene_S", "BRPWMarlene_S"};

        int z = Integer.valueOf(args[0]);

        String[] dataset = new String[]{"Case"+args[0]+"_50", "Case"+args[0]+"_500", "Case"+args[0]+"_5000"};
        int[] numOfreset = new int[3];
        if (z == 7){
            numOfreset = new int[]{250, 2500, 25000, 250, 2500, 25000};
        }else if (z == 8){
            numOfreset = new int[]{250, 2500, 25000, 1250, 12500, 125000};
        }else if (z == 9){
            numOfreset = new int[]{250, 2500, 25000, 250, 2500, 25000};
        }else if (z == 10){
            numOfreset = new int[]{250, 2500, 25000, 750, 7500, 75000};
        }else if (z == 11){
            numOfreset = new int[]{250, 2500, 25000, 250, 2500, 25000};
        }else if (z == 12){
            numOfreset = new int[]{250, 2500, 25000, 500, 5000, 50000};
        }

        for (int m = 0; m < methods.length; m++) {
            for (int d = 0; d < dataset.length; d++) {
                double[][] labels = utils.readLabels(dataPath + dataset[d] + "/" + dataset[d] + "_labels.txt");
                List<Double>[] gMeanList = new List[2];
                for (int i = 0; i < 2; i++){
                    gMeanList[i] = new ArrayList<>();
                }

                for (int r = 0; r < 30; r++) {
                    String resultsPath = prePath + "/" + methods[m] + "/" + methods[m] + "_" + dataset[d] + "_" + r + ".txt";
                    BufferedReader in = new BufferedReader(new FileReader(resultsPath));

                    String str;
                    List<String> line = new ArrayList<>();
                    while ((str = in.readLine()) != null) {
                        line.add(str);
                    }

                    Evaluation eva0 = new Evaluation(numberOfLabels);
                    Evaluation eva1 = new Evaluation(numberOfLabels);
                    for (int i = 1; i < line.size(); i++) {
                        String[] tmp = line.get(i).split(",");
                        Prediction pred = new MultiLabelPrediction(labels[0].length);
                        for (int j = 0; j < pred.size(); j++) {
                            pred.setVote(j, 0, 1);
                            pred.setVote(j, 1, 0);
                        }

                        for (int j = 0; j < tmp.length; j++) {
                            if (Integer.parseInt(tmp[j]) != -1) {
                                int labelNum = Integer.parseInt(tmp[j]);
//                        System.out.print(labelNum + ",");
                                pred.setVote(labelNum, 0, 0);
                                pred.setVote(labelNum, 1, 1);
                            }
                        }

                        eva0.update(pred, labels[i - 1]);
                        eva1.update(pred, labels[i - 1]);

                        double[] tmpGMean0 = eva0.getAverageGMean();
                        double[] tmpGMean1 = eva1.getAverageGMean();

                        if (r == 0){
                            gMeanList[0].add(tmpGMean0[0]);
                            gMeanList[1].add(tmpGMean1[1]);
                        }else {
                            gMeanList[0].set(i-1, gMeanList[0].get(i-1) + tmpGMean0[0]);
                            gMeanList[1].set(i-1, gMeanList[1].get(i-1) + tmpGMean1[1]);
                        }

                        if (i % numOfreset[d] == 0){
                            eva0 = new Evaluation(numberOfLabels);
                        }
                        if (i % numOfreset[d+3] == 0){
                            eva1 = new Evaluation(numberOfLabels);
                        }
                    }
                }


//                for (int i = 0; i < numberOfLabels; i++){
                    BufferedWriter writer1;
                    writer1 =  new BufferedWriter(new FileWriter(new File("/home/h/hd168/MarleneOutput/LabelPerformance/"
                            + methods[m] + "/" + methods[m] + "_" + dataset[d] + "_" + 0 + ".csv")));
                    System.out.println(dataset[d] + "0: " + gMeanList[0].size());
                    for (int j = 0 ; j < gMeanList[0].size(); j++){
                        writer1.write(gMeanList[0].get(j)/30 + "\n");
                    }

                    writer1.close();
                BufferedWriter writer2 =  new BufferedWriter(new FileWriter(new File("/home/h/hd168/MarleneOutput/LabelPerformance/"
                        + methods[m] + "/" + methods[m] + "_" + dataset[d] + "_" + 1 + ".csv")));
                System.out.println(dataset[d] + "1: " + gMeanList[1].size());
                for (int j = 0 ; j < gMeanList[1].size(); j++){
                    writer2.write(gMeanList[1].get(j)/30 + "\n");
                }

                    writer2.close();
//                }
            }
        }


    }

}

