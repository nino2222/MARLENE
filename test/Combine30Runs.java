package marlene3.test;

import moa.core.Utils;

import java.io.*;
import java.util.Scanner;

public class Combine30Runs {
    public static void main(String args[]) throws IOException {
        String method = "ECC";
        String dataset = "IMDB";

        String outputPath = "/home/h/hd168/MarleneResults/";
        double microGMean = 0;
        double macroGMean = 0;
        double exampleGMean = 0;
        double time = 0;


        for (int i = 0; i < 30; i++){
            File myObj = new File("/home/h/hd168/MarleneDataSet/"+method + "/" + method + "_" + dataset + "_" + String.valueOf(i) + ".txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] dataparts = data.split(": ");
                switch (dataparts[0]){
                    case "MicroGMean":
                        microGMean += Double.valueOf(dataparts[1]);
                        break;
                    case "MacroGMean":
                        macroGMean += Double.valueOf(dataparts[1]);
                        break;
                    case "ExampleGMean":
                        exampleGMean += Double.valueOf(dataparts[1]);
                        break;
                    case "Time":
                        time += Double.valueOf(dataparts[1]);
                        break;
                }
            }
            myReader.close();
        }

        microGMean /= 30;
        macroGMean /= 30;
        exampleGMean /= 30;
        time /= 30;
        System.out.println(microGMean);
        System.out.println(macroGMean);
        System.out.println(exampleGMean);
        System.out.println(time);

        BufferedWriter writer =  new BufferedWriter(new FileWriter(new File(outputPath + "/" + dataset + "/" + method + "_" + dataset + ".txt")));
        writer.write("MicroGMean: " + microGMean + "\n");
        writer.write("MacroGMean: " + macroGMean + "\n");
        writer.write("ExampleGMean: " + exampleGMean + "\n");
        writer.write("Time: " + time);
        writer.close();
    }
}
