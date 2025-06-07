package marlene3.test;

import com.yahoo.labs.samoa.instances.Instance;
import marlene3.utils.utils;
import moa.core.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnalysisData {
    double numInstance;
    double numFeature;
    double numLabels;
    Instance[] insts;
    double[][] labels;
    double lc;

    public AnalysisData(){
        numFeature = 0;
        numInstance = 0;
        numLabels = 0;
    }

    public AnalysisData(Instance[] i, double[][] l){
        addData(i, l);
    }

    public void addData(Instance[] i, double[][] l){
        this.insts = i;
        this.labels = l;
        numInstance = i.length;
        numLabels = l[0].length;
        numFeature = i[0].numInputAttributes();
    }

    public double getNumInstance() {
        return numInstance;
    }

    public double getNumFeature() {
        return numFeature;
    }

    public double getNumLabels() {
        return numLabels;
    }

    public double getLCard(){
        lc = 0;
        for (int i = 0; i < labels.length; i++){
            for (int  j = 0; j < labels[i].length; j++){
                if (labels[i][j] == 1){
                    lc++;
                }
            }
        }

        lc = lc/insts.length;
        return lc;
    }

    public double getLDen(){
        return lc/labels[0].length;
    }

    public double getLIR(){
        double[] pos = new double[labels[0].length];
        double[] nei = new double[labels[0].length];
        double[] ir = new double[labels[0].length];

        for (int i = 0; i < labels.length; i++){
            for (int j = 0; j < labels[i].length; j++){
                if (labels[i][j] == 1)
                    pos[j] ++;
                else
                    nei[j] ++;
            }
        }

        for (int i = 0; i < labels[0].length; i++){
            if (pos[i] > nei[i])
                ir[i] = pos[i] == 0 ? 0 : nei[i]/labels.length;
            else
                ir[i] = nei[i] == 0 ? 0 : pos[i]/labels.length;
        }
        return Utils.sum(ir)/labels[0].length;
    }

    public double getEIR(){
        double eir = 0;
        for (int i = 0; i < labels.length; i++){
            double pos = 0;
            double nei = 0;
            for (int j = 0; j < labels[i].length; j++){
                if (labels[i][j] == 1)
                    pos ++;
                else
                    nei ++;
            }

            eir += pos > nei ? nei/labels[0].length : pos/labels[0].length;
        }

        return eir/labels.length;
    }

    public double getLDiv(){
        List<double[]> labelSet = new ArrayList<>();
        labelSet.add(labels[0]);

        for (int i = 0; i < labels.length; i++){
            for (int j = 0; j < labelSet.size(); j++){
                if (!Arrays.equals(labelSet.get(j), labels[i]))
                    labelSet.add(labels[i]);
            }
        }

        return labelSet.size();
    }

    public static void main(String args[]) throws IOException {
        String path = "/home/h/hd168/MarleneDataSet/";
        String[] dataset = new String[]{"Slashdot", "Ohsumed", "Reuters", "Yeast", "20NG", "TMC2007", "IMDB"};
//        String[] dataset = new String[]{"Yeast"};
//        String[] dataset = new String[]{"IMDB"};

        BufferedWriter writer =  new BufferedWriter(new FileWriter(new File("/home/h/hd168/MarleneResults/AnalysisData.csv")));
        writer.write("Dataset,NI,NF,NL,LCard,LDen,LIR,EIR,LDiv,PLDiv" + "\n");

        AnalysisData ad;
        for (int d = 0; d < dataset.length; d++) {
            double[][] labels = utils.readLabels(path + dataset[d] + "/" + dataset[d] + "_labels.txt");
            Instance[] l_insts = utils.readInstances(path + dataset[d] + "/" + dataset[d] + "_l.arff");

            ad = new AnalysisData(l_insts, labels);
            double lc = ad.getLCard();
//            double ldiv = ad.getLDiv();
            writer.write(dataset[d] + "," + ad.getNumInstance()
                    + "," + ad.getNumFeature()
                    + "," + ad.getNumLabels()
                    + "," + lc
                    + "," + lc/labels[0].length
                    + "," + ad.getLIR()
                    + "," + ad.getEIR()
//                    + "," + ldiv
//                    + "," + ldiv/l_insts.length
                    + "," + "\n");
//            System.out.println(dataset[d] + " LCard: " + ad.getLCard());
//            System.out.println(dataset[d] + " LDen: " + ad.getLCard()/labels[0].length);
//            System.out.println(dataset[d] + " LIR: " + ad.getLIR());
//            System.out.println(dataset[d] + " EIR: " + ad.getEIR());
//            System.out.println(dataset[d] + " LDiv: " + ad.getLDiv());
        }

        writer.close();
    }
}
