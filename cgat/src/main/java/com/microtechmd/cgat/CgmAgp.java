package com.microtechmd.cgat;

import java.util.Date;

public class CgmAgp {

    private Integer glucoseCount;

    private Date beginDate;

    private Date endDate;

    private Double hyper;

    private Double hypo;

    private double[][] glucose;

    private double[] dailyMean;

    private double[] dailyPrctile10;

    private double[] dailyPrctile25;

    private double[] dailyPrctile50;

    private double[] dailyPrctile75;

    private double[] dailyPrctile90;

    private CGA.Event[] lbgd;

    private CGA.Event[] hbgd;

    private double[] num;

    private double[] maxbg;

    private double[] minbg;

    private double[] mbg;

    private double ehba1c = Double.NaN;

    private double[] mvalue;

    private double[] sdbg;

    private double[] cv;

    private double[] jindex;

    private double[] iqr;

    private CGA.TIR tir;

    private double[] aac;

    private double[] auc;

    private double lbgi = Double.NaN;

    private double hbgi = Double.NaN;

    private double adrr = Double.NaN;

    private CGA.GRADE grade;

    private double[] lage;

    private double meanLage = Double.NaN;

    private double maxLage = Double.NaN;


    private CGA.MAGE[] mage;

    private double[] mag;

    private double[] conga1;

    private double[] conga4;

    private double[] modd;

    private CGA.Pentagon[] pentagon;

    public Integer getGlucoseCount() {
        return glucoseCount;
    }

    public void setGlucoseCount(Integer glucoseCount) {
        this.glucoseCount = glucoseCount;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Double getHyper() {
        return hyper;
    }

    public void setHyper(Double hyper) {
        this.hyper = hyper;
    }

    public Double getHypo() {
        return hypo;
    }

    public void setHypo(Double hypo) {
        this.hypo = hypo;
    }

    public double[][] getGlucose() {
        return glucose;
    }

    public void setGlucose(double[][] glucose) {
        this.glucose = glucose;
    }

    public double[] getDailyMean() {
        return dailyMean;
    }

    public void setDailyMean(double[] dailyMean) {
        this.dailyMean = dailyMean;
    }

    public double[] getDailyPrctile10() {
        return dailyPrctile10;
    }

    public void setDailyPrctile10(double[] dailyPrctile10) {
        this.dailyPrctile10 = dailyPrctile10;
    }

    public double[] getDailyPrctile25() {
        return dailyPrctile25;
    }

    public void setDailyPrctile25(double[] dailyPrctile25) {
        this.dailyPrctile25 = dailyPrctile25;
    }

    public double[] getDailyPrctile50() {
        return dailyPrctile50;
    }

    public void setDailyPrctile50(double[] dailyPrctile50) {
        this.dailyPrctile50 = dailyPrctile50;
    }

    public double[] getDailyPrctile75() {
        return dailyPrctile75;
    }

    public void setDailyPrctile75(double[] dailyPrctile75) {
        this.dailyPrctile75 = dailyPrctile75;
    }

    public double[] getDailyPrctile90() {
        return dailyPrctile90;
    }

    public void setDailyPrctile90(double[] dailyPrctile90) {
        this.dailyPrctile90 = dailyPrctile90;
    }

    public CGA.Event[] getLbgd() {
        return lbgd;
    }

    public void setLbgd(CGA.Event[] lbgd) {
        this.lbgd = lbgd;
    }

    public CGA.Event[] getHbgd() {
        return hbgd;
    }

    public void setHbgd(CGA.Event[] hbgd) {
        this.hbgd = hbgd;
    }

    public double[] getNum() {
        return num;
    }

    public void setNum(double[] num) {
        this.num = num;
    }

    public double[] getMaxbg() {
        return maxbg;
    }

    public void setMaxbg(double[] maxbg) {
        this.maxbg = maxbg;
    }

    public double[] getMinbg() {
        return minbg;
    }

    public void setMinbg(double[] minbg) {
        this.minbg = minbg;
    }

    public double[] getMbg() {
        return mbg;
    }

    public void setMbg(double[] mbg) {
        this.mbg = mbg;
    }

    public double getEhba1c() {
        return ehba1c;
    }

    public void setEhba1c(double ehba1c) {
        this.ehba1c = ehba1c;
    }

    public double[] getMvalue() {
        return mvalue;
    }

    public void setMvalue(double[] mvalue) {
        this.mvalue = mvalue;
    }

    public double[] getSdbg() {
        return sdbg;
    }

    public void setSdbg(double[] sdbg) {
        this.sdbg = sdbg;
    }

    public double[] getCv() {
        return cv;
    }

    public void setCv(double[] cv) {
        this.cv = cv;
    }

    public double[] getJindex() {
        return jindex;
    }

    public void setJindex(double[] jindex) {
        this.jindex = jindex;
    }

    public double[] getIqr() {
        return iqr;
    }

    public void setIqr(double[] iqr) {
        this.iqr = iqr;
    }

    public CGA.TIR getTir() {
        return tir;
    }

    public void setTir(CGA.TIR tir) {
        this.tir = tir;
    }

    public double[] getAac() {
        return aac;
    }

    public void setAac(double[] aac) {
        this.aac = aac;
    }

    public double[] getAuc() {
        return auc;
    }

    public void setAuc(double[] auc) {
        this.auc = auc;
    }

    public double getLbgi() {
        return lbgi;
    }

    public void setLbgi(double lbgi) {
        this.lbgi = lbgi;
    }

    public double getHbgi() {
        return hbgi;
    }

    public void setHbgi(double hbgi) {
        this.hbgi = hbgi;
    }

    public double getAdrr() {
        return adrr;
    }

    public void setAdrr(double adrr) {
        this.adrr = adrr;
    }

    public CGA.GRADE getGrade() {
        return grade;
    }

    public void setGrade(CGA.GRADE grade) {
        this.grade = grade;
    }

    public double[] getLage() {
        return lage;
    }

    public void setLage(double[] lage) {
        this.lage = lage;
    }

    public double getMeanLage() {
        return meanLage;
    }

    public void setMeanLage(double meanLage) {
        this.meanLage = meanLage;
    }

    public double getMaxLage() {
        return maxLage;
    }

    public void setMaxLage(double maxLage) {
        this.maxLage = maxLage;
    }

    public CGA.MAGE[] getMage() {
        return mage;
    }

    public void setMage(CGA.MAGE[] mage) {
        this.mage = mage;
    }

    public double[] getMag() {
        return mag;
    }

    public void setMag(double[] mag) {
        this.mag = mag;
    }

    public double[] getConga1() {
        return conga1;
    }

    public void setConga1(double[] conga1) {
        this.conga1 = conga1;
    }

    public double[] getConga4() {
        return conga4;
    }

    public void setConga4(double[] conga4) {
        this.conga4 = conga4;
    }

    public double[] getModd() {
        return modd;
    }

    public void setModd(double[] modd) {
        this.modd = modd;
    }

    public CGA.Pentagon[] getPentagon() {
        return pentagon;
    }

    public void setPentagon(CGA.Pentagon[] pentagon) {
        this.pentagon = pentagon;
    }
}
