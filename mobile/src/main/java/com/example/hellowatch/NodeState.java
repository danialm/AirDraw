package com.example.hellowatch;

import android.util.Log;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;


public class NodeState {

    private ArrayList <Double> transition;
    private ArrayList <Double> transitionLog;
    private ArrayList <Double> emission;
    private ArrayList <Double> emissionLog;
    private ArrayList <SequenceScores> sequences;//used to handle multiple sequences
    private boolean original = true;
    private double logAlpha;

    public NodeState(String t, String e, boolean ori){
        original = ori;
        transition = new ArrayList<Double>();
        transitionLog = new ArrayList<Double>();
        emission = new ArrayList<Double>();
        emissionLog = new ArrayList<Double>();
        sequences = new ArrayList <SequenceScores>();//list of sequences
        String [] tArray = t.split(",");//String transition list
        String [] eArray = e.split(",");//String transition list
        for(int i = 0; i<tArray.length; i++){ //convert String t into an array of doubles
            double currT = Double.parseDouble(tArray[i]);
            //System.out.println("Transition: "+currT);
            if(original){
                transition.add(currT);
                transitionLog.add(Math.log(currT));
            }else{
                transitionLog.add(currT);
            }
        }
        for(int i = 0; i<eArray.length; i++){ //convert String e into an array of doubles
            double currE = Double.parseDouble(eArray[i]);
            //System.out.println("Emission: "+currE);
            if(original){
                emission.add(currE);
                emissionLog.add(Math.log(currE));
            }else{
                emissionLog.add(currE);
            }
        }
    }//end NodeState(String t, String e)


    public double getTransition(int i){
        return transition.get(i);
    }
    public double getLogTransition(int t){
        return transitionLog.get(t);
    }
    public ArrayList<Double> getLogTransitionArray(){
        return transitionLog;
    }

    public double getEmission(int i){
        return emission.get(i);
    }

    public double getLogEmission(int i){
        return emissionLog.get(i);
    }

    public ArrayList<Double> getLogEmissionArray(){
        return emissionLog;
    }

    //instantiates SequenceScores depending on the number of sequences
    public void createSequenceScore(){
        SequenceScores ss = new SequenceScores();
        sequences.add(ss);
    }//end createSequenceScore()

    //multiple sequences version
    public double getAlpha(int seqIndex, int t){
        SequenceScores ss = sequences.get(seqIndex);
        return ss.getAlpha(t);
    }//end getAlpha(int seqIndex, int t)

    /*public void setLogAlpha(int i, double a){
        SequenceScores ss;
        if(sequences.size() <= i){
            ss= new SequenceScores();
            ss.setLogAlpha(a);
            sequences.add(i, ss);
        }else{
            ss = sequences.get(i);
            ss.setLogAlpha(a);
        }
    }//end setLogAlpha(int i, double a)*///DM

    public void setLogAlpha(int i, double a){
        logAlpha = a;
    }//end setLogAlpha(int i, double a)

    public double getLogAlpha(int seqIndex, int t){
        return logAlpha;
    }//end getLogAlpha(int seqIndex, int t)

    /*public double getLogAlpha(int seqIndex, int t){
        double out;
        SequenceScores ss = sequences.get(seqIndex);
        out = ss.getLogAlpha(t);
        return out;
    }//end getLogAlpha(int seqIndex, int t)*///DM

    public double getBeta(int seqIndex, int t){//seqIndex = sequence index; t = emmission
        SequenceScores ss = sequences.get(seqIndex);
        return ss.getBeta(t);
    }//end getBeta(int i, int j)


    public double getLogBeta(int seqIndex, int t){//i = sequence index; j = emission
        SequenceScores ss = sequences.get(seqIndex);
        return ss.getLogBeta(t);
    }//end getLogBeta(int i, int j)


    public double getGamma(int seqIndex, int t){
        SequenceScores ss = sequences.get(seqIndex);
        return ss.getGamma(t);
    }//end getGamma(int i, int j)

    public double getLogGamma(int seqIndex, int t){
        SequenceScores ss = sequences.get(seqIndex);
        return ss.getLogGamma(t);
    }


    public ArrayList<Double> getXi(int seqIndex, int t){
        SequenceScores ss = sequences.get(seqIndex);
        return ss.getXi(t);
    }

    public ArrayList<Double> getLogXi(int seqIndex, int t){
        SequenceScores ss = sequences.get(seqIndex);
        return ss.getLogXi(t);
    }

    //for multiple sequences indexed by i
    public void setAlpha(int i, double a){

        if(sequences.size() <= i){
            SequenceScores ss= new SequenceScores();
            ss.setAlpha(a);
            sequences.add(i, ss);
        }else{
            SequenceScores ss = sequences.get(i);
            ss.setAlpha(a);
        }
    }//end setAlpha(int i, double a)

    //to be used for analyzing multiple sequences
    //i is the sequence to be analyzed, size is emission length for a sequence,  t is specific emission, b is beta
    //(seqIndex, emission.size(), i, beta);
    public void setBeta(int i, int size, int t, double b){

        if(sequences.size() <= i){
            SequenceScores ss= new SequenceScores();
            ss.setBeta(size, t, b);
            sequences.add(i, ss);
        }else{
            SequenceScores ss = sequences.get(i);
            ss.setBeta(size, t, b);
        }
    }//end setBeta(int size, int index, double b)

    public void setLogBeta(int i, int size, int t, double b){

        if(sequences.size() <= i){
            SequenceScores ss= new SequenceScores();
            ss.setLogBeta(size, t, b);
            sequences.add(i, ss);
        }else{
            SequenceScores ss = sequences.get(i);
            ss.setLogBeta(size, t, b);
        }
    }//end setLogBeta(int size, int index, double b)

    public void setGamma(int i, double g){
        if(sequences.size() <= i){
            SequenceScores ss= new SequenceScores();
            ss.setGamma(g);
            sequences.add(i, ss);
        }else{
            SequenceScores ss = sequences.get(i);
            ss.setGamma(g);
        }
    }//end setGamma(int i, double g)

    public void setLogGamma(int seqIndex, double g){
        if(sequences.size() <= seqIndex){
            SequenceScores ss= new SequenceScores();
            ss.setLogGamma(g);
            sequences.add(seqIndex, ss);
        }else{
            SequenceScores ss = sequences.get(seqIndex);
            ss.setLogGamma(g);
        }
    }//end setLogGamma(int i, double g)

    public void setXi(int seqIndex, ArrayList<Double> x){
        if(sequences.size() <= seqIndex){
            SequenceScores ss= new SequenceScores();
            ss.setXi(x);
            sequences.add(seqIndex, ss);
        }else{
            SequenceScores ss = sequences.get(seqIndex);
            ss.setXi(x);
        }
    }//setXi(int seqIndex, ArrayList<Double> x)

    public void setLogXi(int seqIndex, ArrayList<Double> x){
        if(sequences.size() <= seqIndex){
            SequenceScores ss= new SequenceScores();
            ss.setLogXi(x);
            sequences.add(seqIndex, ss);
        }else{
            SequenceScores ss = sequences.get(seqIndex);
            ss.setLogXi(x);
        }
    }//setLogXi(int seqIndex, ArrayList<Double> x)
}//end class
