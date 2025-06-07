package marlene4.learners;


import moa.core.Utils;

public class LPerformance {
    protected double tp;
    protected double fp;
    protected double fn;
    protected double tn;
    protected double ptp;
    protected double pfp;
    protected double pfn;
    protected double ptn;
    protected double a;
    protected double lambda_sw;
    protected double lambda_sc;


    public LPerformance(){
        this.reset();
    }

    public void reset(){
        tp = 0;
        fp = 0;
        fn = 0;
        tn = 0;
        ptp = 0;
        pfp = 0;
        pfn = 0;
        ptn = 0;
        lambda_sc = 0;
        lambda_sw = 0;
        a = 0;
    }

//    public void update(double sw, double sc, double classValue, double[] vote){
//        if (classValue == 1){
//            tp = theta * tp + (sw/sc) * (vote[1]/sc);
//            fn = theta * fn + (sw/sc) * (vote[0]/sw);
//        }else{
//            tn = theta * tn + (sw/sc) * (vote[0]/sc);
//            fp = theta * fp + (sw/sc) * (vote[1]/sw);
//        }
//    }

    public void update(double sw, double sc, double[] vote1, double[] vote2, double classValue){
        int yp = Utils.maxIndex(vote1);
//        tp = theta * tp;
//        fp = theta * fp;
//        fn = theta * fn;
//        tn = theta * tn;


//        double v = sw/sc;
//        double q = vote2[(int) classValue]/sc;
//        double r = vote2[(int) (1-classValue)]/sw;
//
//        double e = v*q*r;
        double examplW = sc == 0 ? 0 : sw/sc;
        double x = sc == 0 ? 0 : vote2[(int) classValue]/sc;

        double y = sw == 0 ? 0 : vote2[(int) (1-classValue)]/sw;


//        lambda_sc = theta * lambda_sc + (1-theta) * examplW * x;
//        lambda_sw = theta * lambda_sw + (1-theta) * examplW * y;

        lambda_sc = lambda_sc + examplW * x;
        lambda_sw = lambda_sw + examplW * y;

//        double[] cor = this.getCorrs();
//        if (classValue == 1) {
//            lambda_sc = lambda_sc + cor[1]*examplW * x;
//            lambda_sw = lambda_sw + cor[1]*examplW * y;
//        }else{
//            lambda_sc = lambda_sc + cor[0]*examplW * x;
//            lambda_sw = lambda_sw + cor[0]*examplW * y;
//        }


//        System.out.println("sw: " + lambda_sw);
//        System.out.println("sc: " + lambda_sc);
        a = lambda_sc+lambda_sw == 0 ? 0 : (lambda_sc)/(lambda_sw+lambda_sc);
//        if (Double.isNaN(a)){
//            System.out.println("");
//        }

        if (yp == 1 && classValue == 1){
            tp = tp + 1;
            //tp = theta * tp + (vote1[1] - vote1[0]);
            //tp = theta * tp + (1-theta)*1;
//            ptp = theta * tp + (1-theta)*(vote1[1] - vote1[0]);
            //fn = theta * fn + (1-theta)*(vote1[0] - vote1[1]);
            //tp += (1-theta)*(vote[1] - vote[0]);
        }else if (yp == 1 && classValue == 0){
            fp = fp + 1;
            //fp = theta * fp + (vote1[1] - vote1[0]);
            //fp = theta * fp + (1-theta)*1;
//            pfp = theta * fp + (1-theta)*(vote1[1] - vote1[0]);
            //tn = theta * tn + (1-theta)*(vote1[0] - vote1[1]);;
            //fp += (1-theta)*(vote[1] - vote[0]);
        }else if (yp == 0 && classValue == 1){
            fn = fn + 1;
            //fn = theta * fn + (vote1[0] - vote1[1]);
            //fn = theta * fn + (1-theta)*1;
//            pfn = fn + (1-theta)*(vote1[0] - vote1[1]);
            //tp = theta * tp + (1-theta)*(vote1[1] - vote1[0]);;
            //fn += (1-theta)*(vote[0] - vote[1]);
        }else if (yp == 0 && classValue == 0){
            tn = tn + 1;
            //tn = theta * tn + (vote1[0] - vote1[1]);
            //tn = theta * tn + (1-theta)*1;
//            ptn = theta * tn + (1-theta)*(vote1[0] - vote1[1]);
            //fp = theta * fp + (1-theta)*(vote1[1] - vote1[0]);;
            //tn += (1-theta)*(vote[0] - vote[1]);
        }

    }

    protected double[] getCorrs(){
        double posCorr;
        double neiCorr;
//        if ((tp+fn) < (fp+tn)){
//            majCorr = (fp + tn) == 0 ? 1 : (tp+fp+tn+fn)/(fp+tn);
//            minCorr = (tp + fn) == 0 ? 1 : (tp+fp+tn+fn)/(tp+fn);
//        }else{
//            majCorr = (fp + tn) == 0 ? 1 : (tp+fp+tn+fn)/(fp+tn);
//            minCorr = (tp + fn) == 0 ? 1 : (tp+fp+tn+fn)/(tp+fn);
//        }

        neiCorr = (fp + tn) == 0 ? 1 : (tp+fp+tn+fn)/(fp+tn);
        posCorr = (tp + fn) == 0 ? 1 : (tp+fp+tn+fn)/(tp+fn);

        double[] corrs = new double[2];
        corrs[0] = neiCorr;
        corrs[1] = posCorr;
        return corrs;
    }

    public double getTNWeight(){
        double corrs[] = this.getCorrs();
        double negCorr = corrs[0];
        double posCorr = corrs[1];

//        return ptn*negCorr == 0 ? 0 : ptn*negCorr/(ptn*negCorr + pfn*posCorr);
//        return tn == 0 ? 0 : tn/(tn + fn);
        return tn*negCorr == 0 ? 0 : tn*negCorr/(tn*negCorr + fn*posCorr);
    }

    public double getFNWeight(){
        double corrs[] = this.getCorrs();
        double negCorr = corrs[0];
        double posCorr = corrs[1];

//        return pfn*posCorr == 0 ? 0 : pfn*posCorr/(ptn*negCorr + pfn*posCorr);
        return fn*posCorr == 0 ? 0 : fn*posCorr/(tn*negCorr + fn*posCorr);
//        return fn == 0 ? 0 : fn/(tn + fn);
    }

    public double getTPWeight(){
        double corrs[] = this.getCorrs();
        double negCorr = corrs[0];
        double posCorr = corrs[1];

//        return ptp*posCorr == 0 ? 0 : ptp*posCorr/(ptp*posCorr + pfp*negCorr);
        return tp *posCorr== 0 ? 0 : tp*posCorr/(tp*posCorr + fp*negCorr);
//        return tp == 0 ? 0 : tp/(tp + fp);

    }

    public double getFPWeight(){
        double corrs[] = this.getCorrs();
        double negCorr = corrs[0];
        double posCorr = corrs[1];

//        return pfp*negCorr == 0 ? 0 : pfp*negCorr/(ptp*posCorr + pfp*negCorr);
        return fp*negCorr == 0 ? 0 : fp*negCorr/(tp*posCorr + fp*negCorr);
//        return fp == 0 ? 0 : fp/(tp + fp);
    }

    public double getFn() {
        return fn;
    }

    public double getFp() {
        return fp;
    }

    public double getTn() {
        return tn;
    }

    public double getTp() {
        return tp;
    }

    public boolean ready(){
        if (tp + fn > 0 && fp + tn > 0)
            return true;
        else
            return false;
    }

    public double getA() {
        return a;
    }
}
