package marlene3.test;

import marlene3.utils.utils;
import moa.core.Utils;

import java.io.IOException;

public class TestOthers {
    public static void main(String args[]) throws IOException {
        String path = "/home/h/hd168/MarleneDataSet/Slashdot/";
        String txt_name = "Slashdot_labels.txt";


        double[][] labels = utils.readLabels(path+txt_name);
        for (int i = 0; i < labels.length; i++){
            if (Utils.sum(labels[i]) == 0)
                System.out.println("NO POSITIVE");
//            System.out.println(Utils.sum(labels[i]));
        }

    }
}
