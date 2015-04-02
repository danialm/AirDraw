package com.example.hellowatch;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class HMM {
    private ArrayList <Double> piArray;
    private ArrayList <Double> piLogArray;
    private ArrayList <String> transition;
    private ArrayList <String> emission;
    private ArrayList <NodeState> stateArray;
    private ArrayList <String> emissionSet; //list of possible emissions with corresponding index
    private boolean original = true; //flag for whether it is an original (PROBABILITY) hmm from text or an adjusted (LOG) hmm
    public HMM()
    {
        //empty contructor
    }
    public HMM(String json){

        piArray = new ArrayList<Double>();
        piLogArray = new ArrayList<Double>();
        stateArray = new ArrayList<NodeState>();
        transition = new ArrayList<String>();
        emission = new ArrayList<String>();
        emissionSet = new ArrayList<String>();
        parseJson(json);
        setEmissionSet(json);
        loadHMM();//creates statearray
    }

    public HMM(String json, boolean ori){//ori is always false

        original = ori;
        piArray = new ArrayList<Double>();
        piLogArray = new ArrayList<Double>();
        stateArray = new ArrayList<NodeState>();
        transition = new ArrayList<String>();
        emission = new ArrayList<String>();
        emissionSet = new ArrayList<String>();
        parseJsonAdj(json);//does not take the log
        setEmissionSet(json);
        loadHMM();//creates statearray
    }

    //true if hmm is from text, false if it has been adjusted
    //determines whether the parameters loaded are probabilities or logProbabilities
    //addresses log 0 = NaN



    public int getLambda(){//used in BIC calc
        int lambda = stateArray.size()*stateArray.size();
        return lambda;
    }
    public boolean getStatus(){
        return original;
    }
    public ArrayList<Double> getPiArray(){
        return piArray;
    }
    public ArrayList<Double> getPiLogArray(){
        return piLogArray;
    }
    public ArrayList<String> getEmissionSetArray(){
        return emissionSet;
    }
    public ArrayList<NodeState> getStateArray(){
        return stateArray;
    }
    public NodeState getNodeState(int i){
        return stateArray.get(i);
    }
    public int sizeHMM(){//returns the number of states in the HMM
        return stateArray.size();
    }
    public String getEmissionSet(){

        String set = "";
        for(int i = 0; i < emissionSet.size() ; i++){
            set = set+emissionSet.get(i)+" ";
        }
        return set;
    }

    public String getPI(){
        String pi = "";
        if(original){
            for(int i = 0; i < piArray.size() ; i++){
                pi = pi+piArray.get(i)+" ";
            }
        }else{//adjusted hmm
            for(int i = 0; i < piLogArray.size() ; i++){
                pi = pi+piLogArray.get(i)+" ";
            }
        }

        return pi;
    }

    public String getA(){
        String a = "";
        for(int i = 0; i < transition.size() ; i++){
            a = a+"["+transition.get(i)+"] ";
        }

        return a;
    }

    public String getB(){

        String b = "";
        for(int i = 0; i < emission.size() ; i++){
            b = b+"["+emission.get(i)+"] ";
        }
        return b;
    }

    private void loadHMM(){
        for(int i = 0; i< transition.size(); i++ ){
            String t = transition.get(i);
            String e = emission.get(i);
            //System.out.println("Node Transitions: "+t+" Node Emissions: "+e);
            NodeState curr = new NodeState(t, e, original);
            stateArray.add(curr);
        }

    }//end of loadHMM()

    private void setEmissionSet(String json){

        Pattern be = Pattern.compile("\"BE\":(.*?)]");
        Matcher bematcher = be.matcher(json);
        if (bematcher.find()){
            String beString = bematcher.group(0);
            //System.out.println("The Emission Set is: "+beString);
            String[] beArray = beString.split("[\\[\\],]" );
            for(int i = 1; i< beArray.length; i++ ){
                //System.out.println(beArray[i]);
                emissionSet.add(beArray[i]);
                //System.out.println(emissionSet.get(i-1));
            }
        }
    }//end setEmissionSet()

    //use to parse adjusted HMM
    private void parseJsonAdj(String json){
        String piString;
        String aString;
        String bString;
        String[] aaArray;
        String[] bbArray;

        Pattern pi = Pattern.compile("\"pi\":(.*?)]");
        Pattern a = Pattern.compile("\"A\":(.*?)]]");
        Pattern b = Pattern.compile("\"B\":(.*?)]]");
        Matcher pimatcher = pi.matcher(json);
        Matcher amatcher = a.matcher(json);
        Matcher bmatcher = b.matcher(json);
        if (pimatcher.find()){
            piString = pimatcher.group(0);
            //System.out.println(piString);
            String[] p = piString.split("[\\[\\],]" );
            for(int i = 1; i< p.length; i++ ){
                Double d = Double.parseDouble(p[i]);
                piLogArray.add(d);
                //piLogArray.add(Math.log(d));
                //System.out.println(piArray.get(i-1));
            }
        }
        if (amatcher.find()){
            aString = amatcher.group(0);
            //System.out.println(aString);
            Pattern aa = Pattern.compile("\\[\\[(.*?)]]");
            Matcher aamatcher = aa.matcher(aString);
            if(aamatcher.find()){
                String aaString = aamatcher.group(0);
                //System.out.println(aaString);
                aaArray = aaString.split("],\\[");
                for(int i = 0; i< aaArray.length; i++ ){
                    aaArray[i] = aaArray[i].replaceAll("\\[","");
                    aaArray[i] = aaArray[i].replaceAll("]","");
                    //System.out.println(aaArray[i]);
                    transition.add(aaArray[i]);
                }
            }
        }
        if (bmatcher.find()){
            bString = bmatcher.group(0);
            //System.out.println(bString);
            Pattern bb = Pattern.compile("\\[\\[(.*?)]]");
            Matcher bbmatcher = bb.matcher(bString);
            if(bbmatcher.find()){
                String bbString = bbmatcher.group(0);
                //System.out.println(bbString);
                bbArray = bbString.split("],\\[");
                for(int i = 0; i< bbArray.length; i++ ){
                    bbArray[i] = bbArray[i].replaceAll("\\[","");
                    bbArray[i] = bbArray[i].replaceAll("]","");
                    //System.out.println(bbArray[i]);
                    emission.add(bbArray[i]);

                }
            }
        }
    }

    private void parseJson(String json){
        String piString;
        String aString;
        String bString;
        String[] aaArray;
        String[] bbArray;

        Pattern pi = Pattern.compile("\"pi\":(.*?)]");
        Pattern a = Pattern.compile("\"A\":(.*?)]]");
        Pattern b = Pattern.compile("\"B\":(.*?)]]");
        Matcher pimatcher = pi.matcher(json);
        Matcher amatcher = a.matcher(json);
        Matcher bmatcher = b.matcher(json);
        if (pimatcher.find()){
            piString = pimatcher.group(0);
            //System.out.println(piString);
            String[] p = piString.split("[\\[\\],]" );
            for(int i = 1; i< p.length; i++ ){
                Double d = Double.parseDouble(p[i]);
                piArray.add(d);
                piLogArray.add(Math.log(d));
                //System.out.println(piArray.get(i-1));
                Log.v("piArray inside HMM.parseJson", Double.toString(piArray.get(i - 1)));
            }
        }
        if (amatcher.find()){
            aString = amatcher.group(0);
            //System.out.println(aString);
            Pattern aa = Pattern.compile("\\[\\[(.*?)]]");
            Matcher aamatcher = aa.matcher(aString);
            if(aamatcher.find()){
                String aaString = aamatcher.group(0);
                //System.out.println(aaString);
                aaArray = aaString.split("],\\[");
                for(int i = 0; i< aaArray.length; i++ ){
                    aaArray[i] = aaArray[i].replaceAll("\\[","");
                    aaArray[i] = aaArray[i].replaceAll("]","");
                    //System.out.println(aaArray[i]);
                    transition.add(aaArray[i]);
                }
            }
        }
        if (bmatcher.find()){
            bString = bmatcher.group(0);
            //System.out.println(bString);
            Pattern bb = Pattern.compile("\\[\\[(.*?)]]");
            Matcher bbmatcher = bb.matcher(bString);
            if(bbmatcher.find()){
                String bbString = bbmatcher.group(0);
                //System.out.println(bbString);
                bbArray = bbString.split("],\\[");
                for(int i = 0; i< bbArray.length; i++ ){
                    bbArray[i] = bbArray[i].replaceAll("\\[","");
                    bbArray[i] = bbArray[i].replaceAll("]","");
                    //System.out.println(bbArray[i]);
                    emission.add(bbArray[i]);

                }
            }
        }
    }
}