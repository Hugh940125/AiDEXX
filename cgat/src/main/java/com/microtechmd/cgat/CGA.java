package com.microtechmd.cgat;

public class CGA {
	static public class Event {
		public Event(double[] event) {
			if (event == null) {
				return;
			}
			if (event.length > 0) {
				this.times = (int)event[0];
			}
			if (event.length > 1) {
				this.meanDuration = event[1];
			}
		}
        public double times = 0;
        public double meanDuration = 0;
    }
	static public Event[] toEvents(double[][] value) {
		if (value == null) {
			return null;
		}
		int size = value.length;
		if (size == 0) {
			return null;
		}
		Event[] events = new Event[size];
		for (int i = 0; i < size; i++) {
			events[i] = new Event(value[i]);
    	}
		return events;
	}

	static public class GRADE {
        public GRADE(double[] grade) {
        	if (grade == null) {
        		return;
        	}
            this.grade = grade[0];
            this.pctHypo = grade[1];
            this.pctEu = grade[2];
            this.pctHyper = grade[3];
        }
        public double grade = Double.NaN;
        public double pctHypo = Double.NaN;
        public double pctEu = Double.NaN;
        public double pctHyper = Double.NaN;
    }

	static public class TIR {
        public TIR(double[] range, double[][] tir) {
        	this.range = range;
        	this.tir = tir;
        }
        public double[] range = null;
        public double[][] tir = null;
	}
	
	static public class MAGE {
        public MAGE(double[] mage) {
        	if (mage == null) {
        		return;
        	}
        	this.average = mage[0];
        	this.rise = mage[1];
        	this.decline = mage[2];
        }
        public double average = Double.NaN;
        public double rise = Double.NaN;
        public double decline = Double.NaN;
    }
	static public MAGE[] toMages(double[][] value) {
		if (value == null) {
			return null;
		}
		int size = value.length;
		if (size == 0) {
			return null;
		}
		MAGE[] mages = new MAGE[size];
		for (int i = 0; i < size; i++) {
			mages[i] = new MAGE(value[i]);
    	}
		return mages;
	}
	
	static public class Pentagon {
		public Pentagon(double[] pentagon) {
        	if (pentagon == null) {
        		return;
        	}
        	this.tor = pentagon[0];
        	this.cv = pentagon[1];
        	this.intHypo = pentagon[2];
        	this.intHyper = pentagon[3];
        	this.mbg = pentagon[4];
        	this.tor_mm = pentagon[5];
        	this.cv_mm = pentagon[6];
        	this.intHypo_mm = pentagon[7];
        	this.intHyper_mm = pentagon[8];
        	this.mbg_mm = pentagon[9];
        	this.area = pentagon[10];
        }
        public double tor = Double.NaN;
        public double cv = Double.NaN;
        public double intHypo = Double.NaN;
        public double intHyper = Double.NaN;
        public double mbg = Double.NaN;
        public double tor_mm = Double.NaN;
        public double cv_mm = Double.NaN;
        public double intHypo_mm = Double.NaN;
        public double intHyper_mm = Double.NaN;
        public double mbg_mm = Double.NaN;
        public double area = Double.NaN;
	}
	static public Pentagon[] toPentagons(double[][] value) {
		if (value == null) {
			return null;
		}
		int size = value.length;
		if (size == 0) {
			return null;
		}
		Pentagon[] pentagons = new Pentagon[size];
		for (int i = 0; i < size; i++) {
			pentagons[i] = new Pentagon(value[i]);
    	}
		return pentagons;
	}
	
	private int colSize;
	private int rowSize;
	private long dataPtr;
	private long selectedDataPtr;
	private long mbgPtr;
	private long sdbgPtr;
	private long cvPtr;
	
    public CGA(double[][] sg) {
        constructor(sg);
    }

    @Override
    protected void finalize() throws Throwable {
        destructor();
        super.finalize();
    }

    private native void constructor(double[][] sg);
    private native void destructor();
    
	public native double[] selectPeriod(double startHour, double endHour);
	
    public native double[] getDailyTrendMean();
    public native double[] getDailyTrendPrctile(double pct);

    public native double[][] getPeriodLBGD(double hypo);
    public native double[][] getPeriodHBGD(double hyper);
    
    public native double[] getPeriodNUM();
    public native double[] getPeriodMAXBG();
    public native double[] getPeriodMINBG();
    public native double[] getPeriodMBG();
    public native double[] getPeriodMValue(double target);
    public native double[] getPeriodSDBG();
    public native double[] getPeriodCV();
    public native double[] getPeriodJIndex();
    public native double[] getPeriodIQR();
    public native double[][] getPeriodPT(double[] v);
    public native double[] getPeriodAAC(double target);
    public native double[] getPeriodAUC(double target);

    public native double getHBA1C();
    public native double getLBGI();
    public native double getHBGI();
    public native double getADRR();
    public native double[] getGRADE(double hypo, double hyper);
    public native double[] getLAGE();
    public native double[][] getMAGE(double nv);
    public native double[] getMAG(double hour);
    public native double[] getMODD();
    public native double[] getCONGA(double hour);
    
    public native double[][] getPentagon();
}
