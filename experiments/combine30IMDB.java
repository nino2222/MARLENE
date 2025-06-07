package marlene3.experiments;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class combine30IMDB {
    public static void main(String args[]) throws IOException {
        String[] metrics = new String[]{"Micro", "Macro", "LS"};

        String method = "EaPS";

        for (int j = 0; j < metrics.length; j++){
            List<String>[] results = new List[30];

            for (int i = 0; i < 30; i++) {
                BufferedReader in = new BufferedReader(new FileReader("/home/h/hd168/MarleneOutput/Performance/"
                        + method + "/" + method + "_IMDB" + "_"+ metrics[j] + "_" + i + ".csv"));

                String str;
                results[i] = new ArrayList<>();
                while ((str = in.readLine()) != null) {
                    results[i].add(str);
                }
            }

            double[] sumResult = new double[results[0].size()];

            for (int i = 0; i < results[0].size(); i++){
                for (int k = 0; k < results.length; k++){
                    sumResult[i] += Double.valueOf(results[k].get(i));
                }
            }

            BufferedWriter writer =  new BufferedWriter(new FileWriter(new File("/home/h/hd168/MarleneOutput/Performance/"
                    + method + "/" + method + "_IMDB" + "_"+ metrics[j] + ".csv")));
            for (int i = 0; i < sumResult.length; i++){
                writer.write(String.valueOf(sumResult[i]/results.length) + "\n");
            }

            writer.close();
        }

    }
}
