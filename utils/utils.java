package marlene3.utils;

import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.Prediction;
import moa.core.Utils;
import moa.streams.ArffFileStream;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class utils {

    public static double[][] readLabels(String filePath) throws IOException {
        double[][] labels; //row, col
        BufferedReader in = new BufferedReader(new FileReader(filePath));
        String str;
        List<String> line = new ArrayList<>();
        while ((str = in.readLine()) != null) {
            line.add(str);
        }

        labels = new double[line.size()][];
        for (int i = 0; i < line.size(); i++){
            String[] tmp = line.get(i).split(",");
            labels[i] = new double[tmp.length];
            for (int j = 0; j < tmp.length; j++){
                labels[i][j] = Double.parseDouble(tmp[j]);
            }
        }

        return labels;
    }

    public static Instance[] readInstances(String filePath){
        ArffFileStream stream = new ArffFileStream(filePath, -1);
        List<Instance> inst_list = new ArrayList<>();
        while (stream.hasMoreInstances()){
            inst_list.add(stream.nextInstance().getData());
        }

        Instance[] insts = new Instance[inst_list.size()];
        for (int i = 0; i < inst_list.size(); i++){
            insts[i] = inst_list.get(i);
        }

        return insts;
    }

    public static String Pred2Str(Prediction pred){
        String output = "";
        for (int i = 0; i < pred.size(); i++){
            output += Utils.maxIndex(pred.getVotes(i));
        }
        return output;
    }

    public static String Label2Str(double[] labels){
        String output = "";
        for (int i = 0; i < labels.length; i++){
            output += (int)labels[i];
        }

        return output;
    }
}
