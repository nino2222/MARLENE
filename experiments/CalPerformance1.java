package marlene3.experiments;

import com.yahoo.labs.samoa.instances.MultiLabelPrediction;
import com.yahoo.labs.samoa.instances.Prediction;
import marlene3.evaluation.Evaluation;
import marlene3.evaluation.SWEvaluation;
import marlene3.utils.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CalPerformance1 {
    public static List<Double> getAvgList(List<Double>[] lists){
        List<Double> results = new ArrayList<>();
        for (int i = 0; i < lists.length; i++){
            for (int j = 0; j < lists[i].size(); j++){
                if (i == 0){
                    results.add(j, lists[i].get(j));
                }else{
                    results.set(j, results.get(j) + lists[i].get(j));
                }
            }
        }

        for (int i = 0; i < results.size(); i++){
            results.set(i, results.get(i)/lists.length);
        }
        return results;
    }

    public static void main(String args[]) throws IOException {
        String dataPath = "/home/h/hd168/MarleneDataSet/";
        String prePath = "/home/h/hd168/MarleneOutput/";

//        String[] methods = new String[]{"BRMarlene", "EBR", "ECC", "EPS", "EBRT", "EaBR", "EaCC", "EaPS"};
        String[] methods = new String[]{"GOBR", "GOCC", "GOPS", "GORT"};
//        String[] methods = new String[]{"BRMarlene"};

        String[] dataset = new String[]{"Slashdot", "Ohsumed", "Reuters", "Yeast", "20NG", "TMC2007", "IMDB"};
        int[] numOfInstance = new int[]{3782, 13529, 6000, 2417, 19300, 28596, 120919};

//        String[] dataset = new String[]{"Yeast"};

        SWEvaluation[] eva;
        for (int m = 0; m < methods.length; m++) {
            for (int d = 0; d < dataset.length; d++) {
                double[][] labels = utils.readLabels(dataPath + dataset[d] + "/" + dataset[d] + "_labels.txt");
                eva = new SWEvaluation[1];
                for (int r = 0; r < 1; r++) {
//            int r = 0;
                    String resultsPath = prePath + "/" + methods[m] + "/" + methods[m] + "_" + dataset[d] + "_" + r + ".txt";
                    BufferedReader in = new BufferedReader(new FileReader(resultsPath));

                    String str;
                    List<String> line = new ArrayList<>();
                    while ((str = in.readLine()) != null) {
                        line.add(str);
                    }

                    eva[r] = new SWEvaluation(numOfInstance[d]/10);

                    for (int i = 1; i < line.size(); i++) {
//                        if (i-1 > (numOfInstance[d] / 20)) {
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
//                            System.out.println();

                            eva[r].update(pred, labels[i - 1]);
//                        }
                    }

                }

                BufferedWriter writer;

                List<Double>[] macroListArray = new List[eva.length];
                for (int i = 0; i< eva.length; i++){
                    macroListArray[i] = eva[i].getMacroGMeanList();
                }
                List<Double> macroList = getAvgList(macroListArray);
                writer =  new BufferedWriter(new FileWriter(new File("/home/h/hd168/MarleneOutput/Performance/"
                        + methods[m] + "/" + methods[m] + "_" + dataset[d] + "_Macro" + ".csv")));
                for (int i = 0; i < macroList.size(); i++){
                    writer.write(macroList.get(i) + "\n");
                }
                writer.close();

                List<Double>[] microListArray = new List[eva.length];
                for (int i = 0; i< eva.length; i++){
                    macroListArray[i] = eva[i].getMicroGMeanList();
                }
                List<Double> microList = getAvgList(macroListArray);
                writer =  new BufferedWriter(new FileWriter(new File("/home/h/hd168/MarleneOutput/Performance/"
                        + methods[m] + "/" + methods[m] + "_" + dataset[d] + "_Micro" + ".csv")));
                for (int i = 0; i < microList.size(); i++){
                    writer.write(microList.get(i) + "\n");
                }
                writer.close();

                List<Double>[] exampleGMArray = new List[eva.length];
                for (int i = 0; i< eva.length; i++){
                    exampleGMArray[i] = eva[i].getExampleGMeanList();
                }
                List<Double> exampleGMList = getAvgList(exampleGMArray);
                writer =  new BufferedWriter(new FileWriter(new File("/home/h/hd168/MarleneOutput/Performance/"
                        + methods[m] + "/" + methods[m] + "_" + dataset[d] + "_LS" + ".csv")));
                for (int i = 0; i < exampleGMList.size(); i++){
                    writer.write(exampleGMList.get(i) + "\n");
                }
                writer.close();

                double macro = 0;
                double micro = 0;
                double example = 0;
//
                for (int i = 0; i < eva.length; i++) {
                    micro += eva[i].getAverageMicroGMean();
                    macro += eva[i].getAverageMacroGMean();
                    example += eva[i].getAverageLSGMean();
                }

                System.out.println(dataset[d]);
//            System.out.println("Marco: " + eva[0].getMarcoGMean());
//            System.out.println("Mirco: " + eva[0].getMicroGMean());
//            System.out.println("Example: " + eva[0].getLabelSetBasedGMean());

                System.out.println("Marco: " + macro / 1);
                System.out.println("Mirco: " + micro / 1);
                System.out.println("Example: " + example / 1);
            }
        }
    }
}
