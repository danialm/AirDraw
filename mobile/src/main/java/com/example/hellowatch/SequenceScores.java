package com.example.hellowatch;

//holds all the scores for a state
import java.util.ArrayList;


public class SequenceScores {

    //private int sequenceIndex;
    //private String sequenceDescription;
    //private String sequenceEmission;
    private ArrayList <Double> alpha;
    private ArrayList <Double> alphaLog;
    private ArrayList <Double> beta;
    private ArrayList <Double> betaLog;
    private ArrayList <Double> gamma;
    private ArrayList <Double> gammaLog;
    private ArrayList <ArrayList<Double>> xi; //outer corresponds to t, inner are possible states
    private ArrayList <ArrayList<Double>> xiLog;
    public SequenceScores(){
        alpha = new ArrayList<Double>();
        alphaLog = new ArrayList<Double>();
        beta = new ArrayList<Double>();
        betaLog = new ArrayList<Double>();
        gamma = new ArrayList<Double>();
        gammaLog = new ArrayList<Double>();
        xi = new ArrayList<ArrayList<Double>>();
        xiLog = new ArrayList<ArrayList<Double>>();
    }

    public double getAlpha(int t){
        return alpha.get(t);
    }

    public double getLogAlpha(int t){
        return alphaLog.get(t);
    }
    public double getBeta(int t){
        return beta.get(t);
    }
    public double getLogBeta(int t){
        return betaLog.get(t);
    }
    public double getGamma(int t){
        return gamma.get(t);
    }
    public double getLogGamma(int t){
        return gammaLog.get(t);
    }
    public ArrayList<Double> getXi(int t){
        return xi.get(t);
    }
    public ArrayList<Double> getLogXi(int t){
        return xiLog.get(t);
    }

    public void setAlpha(double a){
        alpha.add(a);
    }

    public void setLogAlpha(double a){
        alphaLog.add(a);
    }//end setLogAlpha(double a)


    public void setBeta(int size, int seqIndex, double b){

        if(beta.size() == 0)
            for(int i = 0; i < size; i++)
                beta.add((double)0);

        //System.out.println("the value of b being added is: "+b);
        beta.set(seqIndex,b);
    }//end setBeta(int size, int index, double b)

    public void setLogBeta(int size, int seqIndex, double b){

        if(betaLog.size() == 0){
            for(int i = 0; i < size; i++)
                betaLog.add((double)0);
        }
        betaLog.set(seqIndex,b);
    }//end setLogBeta(int size, int index, double b)

    public void setGamma(double g){
        gamma.add(g);
    }
    public void setLogGamma(double g){
        gammaLog.add(g);
    }
    public void setXi(ArrayList<Double> e){
        for(int i = 0; i < e.size(); i++){
            //System.out.print(e.get(i)+", ");
        }
        //System.out.println();
        xi.add(e);
    }
    public void setLogXi(ArrayList<Double> e){
        for(int i = 0; i < e.size(); i++){
            //System.out.print(e.get(i)+", ");
        }
        //System.out.println();
        xiLog.add(e);
    }
}//end of class
