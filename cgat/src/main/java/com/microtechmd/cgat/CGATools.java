package com.microtechmd.cgat;

/**
 * APP-SRC-A-104-2
 */
public class CGATools {
//    static {
//        System.loadLibrary("cgat-lib");
//    }

    private static final CGATools sInstance = new CGATools();

    private CGATools() { }

    public static synchronized CGATools getInstance() {
        return sInstance;
    }

    public native int init(double[][] sg);

    public native void destroy();

    public native double[] getDailyTrendMean();

    public native double[] getDailyTrendPrctile(double pct);

    public Event getLBGD_Obj(double hypo) {
        double[] values = getLBGD(hypo);
        return new Event(values[0], values[1]);
    }
    public native double[] getLBGD(double hypo);

    public Event getHBGD_Obj(double hyper) {
        double[] values = getHBGD(hyper);
        return new Event(values[0], values[1]);
    }
    public native double[] getHBGD(double hyper);

    public native double getMBG(double startHour, double endHour);

    public native double getMValue();

    public native double getSDBG();

    public native double getCV();

    public native double getJIndex();

    public native double getIQR();

    public native double getPT(double low, double high);

    public native double getAAC(double target, double startHour, double endHour);

    public native double getAUC(double target, double startHour, double endHour);

    public native double getLBGI();

    public native double getHBGI();

    public native double getADRR();

    public GRADE getGRADE_Obj(double hypo, double hyper) {
        double[] values = getGRADE(hypo, hyper);
        return new GRADE(values[0], values[1], values[2], values[3]);
    }
    public native double[] getGRADE(double hypo, double hyper);

    public native double getLAGE();

    public MAGE getMAGE_Obj() {
        double[] values = getMAGE();
        return new MAGE(values[0], values[1]);
    }
    public native double[] getMAGE();

    public native double getMAG(double hour);

    public native double getMODD();

    public native double getCONGA(double hour);


    public class Event {
        public Event(double times, double meanDuration) {
            this.times = times;
            this.meanDuration = meanDuration;
        }
        public double times;
        public double meanDuration;
    }

    public class GRADE {
        public GRADE(double grade, double pctHypo, double pctEu, double pctHyper) {
            this.grade = grade;
            this.pctHypo = pctHypo;
            this.pctEu = pctEu;
            this.pctHyper = pctHyper;
        }
        public double grade;
        public double pctHypo;
        public double pctEu;
        public double pctHyper;
    }

    public class MAGE {
        public MAGE(double rise, double decline) {
            this.rise = rise;
            this.decline = decline;
        }
        public double rise;
        public double decline;
    }
}
