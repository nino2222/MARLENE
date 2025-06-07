package marlene3.test;

import moa.core.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CreatCSV {
    public static void main(String args[]) throws IOException {
//        String[] dataset = new String[]{"Slashdot", "Ohsumed", "Reuters", "Yeast", "20NG", "TMC2007", "IMDB"};

//        String[] dataset = new String[]{"Slashdot", "Ohsumed", "Reuters", "Yeast", "20NG", "TMC2007", "IMDB"};

        String[] metrics = new String[]{"Micro", "Macro", "LS"};

//        String[] method = new String[]{"BRMarlene", "EBR", "ECC", "EPS", "EBRT", "EaBR", "EaCC", "EaPS", "GOBR", "GOCC", "GOPS", "GORT"};

        String[] dataset = new String[]{"Case7_50", "Case7_500", "Case7_5000",
                "Case8_50", "Case8_500", "Case8_5000",
                "Case9_50", "Case9_500", "Case9_5000",
                "Case10_50", "Case10_500", "Case10_5000",
                "Case11_50", "Case11_500", "Case11_5000",
                "Case12_50", "Case12_500", "Case12_5000",
//                "S_Case7_50", "S_Case7_500", "S_Case7_5000",
//                "S_Case8_50", "S_Case8_500", "S_Case8_5000",
//                "S_Case9_50", "S_Case9_500", "S_Case9_5000",
//                "S_Case10_50", "S_Case10_500", "S_Case10_5000",
//                "S_Case11_50", "S_Case11_500", "S_Case11_5000",
//                "S_Case12_50", "S_Case12_500", "S_Case12_5000"
                };

        String[] method = new String[]{"BRMarlene", "BRPWMarlene", "BRMarlene_NS", "BRPWMarlene_NS", "BRMarlene_S", "BRPWMarlene_S"};


        for (int i = 0; i < metrics.length; i++){
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
                    "/home/h/hd168/MarleneOutput/Performance/" + metrics[i] + "_Artificial.csv")));

            writer.write(metrics[i] + ",");
            for (int m = 0; m < dataset.length-1; m++){
                writer.write(dataset[m] + ",");
            }
            writer.write(dataset[dataset.length-1] + "\n");

            for (int k = 0; k < method.length; k++){
                writer.write(method[k] + ",");
                for (int j = 0; j < dataset.length; j++) {
                    List<String> results = new ArrayList<>();
                    File myObj = new File("/home/h/hd168/MarleneOutput/Performance/"
                            + method[k] + "/" + method[k] + "_" + dataset[j] + "_" + metrics[i] + ".csv");
                    Scanner myReader = new Scanner(myObj);

                    while (myReader.hasNextLine()) {
                        results.add(myReader.nextLine());
                    }

                    double[] resultsValue = new double[results.size()];
                    for (int t = 0; t < resultsValue.length; t++){
                        resultsValue[t] = Double.valueOf(results.get(t));
                    }

                    writer.write(String.valueOf(Utils.sum(resultsValue)/resultsValue.length));
                    if (j != dataset.length-1)
                        writer.write(",");
                    else
                        writer.write("\n");
                }
            }

            writer.close();

        }



        for (int i = 0; i < metrics.length; i++){
            for (int j = 0; j < dataset.length; j++) {
                BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
                        "/home/h/hd168/MarleneOutput/Performance/" + dataset[j] + "_" + metrics[i] + ".csv")));

                List<String>[] rsults = new List[method.length];
                for (int k = 0; k < method.length; k++){
                    File myObj = new File("/home/h/hd168/MarleneOutput/Performance/"
                            + method[k] + "/" + method[k] + "_" + dataset[j] + "_" + metrics[i] + ".csv");
                    Scanner myReader = new Scanner(myObj);

                    rsults[k] = new ArrayList<>();
                    while (myReader.hasNextLine()) {
                        rsults[k].add(myReader.nextLine());
                    }
                }

                for (int k = 0; k < rsults[0].size(); k++){
                    for (int t = 0; t < rsults.length; t++){
                        writer.write(rsults[t].get(k));
                        if (t != rsults.length-1)
                            writer.write(",");
                        else
                            writer.write("\n");
                    }
                }

                writer.close();
            }
        }


//        for (int i = 0; i < metrics.length; i++){
//            BufferedWriter writer =  new BufferedWriter(new FileWriter(new File("/home/h/hd168/MarleneResults/" + metrics[i] + ".csv")));
//            writer.write(metrics[i] + ",");
//            for (int m = 0; m < dataset.length-1; m++){
//                writer.write(dataset[m] + ",");
//            }
//            writer.write(dataset[dataset.length-1] + "\n");
//
//            for (int k = 0; k < method.length; k++){
//                writer.write(method[k] + ",");
//                for (int j = 0; j < dataset.length; j++){
//                    File myObj = new File("/home/h/hd168/MarleneResults/" + dataset[j] + "/" + method[k] + "_" + dataset[j] + ".txt");
//                    Scanner myReader = new Scanner(myObj);
//
//                    while (myReader.hasNextLine()) {
//                        String data = myReader.nextLine();
//                        String[] dataparts = data.split(": ");
//                        if (dataparts[0].equals(metrics[i])){
//                            writer.write(dataparts[1]);
//                            break;
//                        }
//                    }
//
//                    if (j != dataset.length-1)
//                        writer.write(",");
//                    else
//                        writer.write("\n");
//                }
//            }
//
//            writer.close();
//        }

    }
}
