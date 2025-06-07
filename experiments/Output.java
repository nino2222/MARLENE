package marlene3.experiments;

import com.yahoo.labs.samoa.instances.Prediction;
import moa.core.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Output {
    List<List<Integer>> output;
    double time;

    public Output(){
        output = new ArrayList<>();
        time = 0;
    }

    public void addTime(double t){
        time = t;
    }

    public void addPred(Prediction pred){
        List<Integer> prediction = new ArrayList<>();

        int count0 = 0;
        for (int i = 0; i < pred.size(); i++){
            if (Utils.maxIndex(pred.getVotes(i)) == 1){
                prediction.add(i);
            }else
                count0++;
        }

        if (count0 == pred.size())
            prediction.add(-1);

        output.add(prediction);
    }

    public void writeOutput(String filepath) throws IOException {
        BufferedWriter writer =  new BufferedWriter(new FileWriter(new File(filepath)));

        writer.write("Time: " + time + "\n");

        for (int i = 0; i < output.size(); i++){
            for (int j = 0; j < output.get(i).size(); j++) {
                writer.write(Integer.toString(output.get(i).get(j)));
                if (j != output.get(i).size() - 1)
                    writer.write(",");
                else
                    writer.write("\n");

            }
        }
        writer.close();

    }

    public List<List<Integer>> getOutput() {
        return output;
    }
}
