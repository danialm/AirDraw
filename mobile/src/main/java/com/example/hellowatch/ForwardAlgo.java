package com.example.hellowatch;

import java.util.ArrayList;


public class ForwardAlgo {

    String currSeq;
    ArrayList <Integer> emission; //the emission converted to generic index
    ArrayList <NodeState> stateArray;
    ArrayList <Double> pi;
    ArrayList <Double> piLog;
    ArrayList <String> emissionSet; //set of observation symbols
    ArrayList <String> sequences;//list the sequences to be analyzed
    ArrayList <String> descriptions; //list the description of the sequences to be analyzed
    HMM currHMM;

    public ForwardAlgo(HMM hmm, String fileName){


        emission = new ArrayList<Integer>();
        stateArray = new ArrayList<NodeState>();
        pi = new ArrayList<Double>();
        piLog = new ArrayList<Double>();
        emissionSet = new ArrayList<String>();
        sequences = new ArrayList<String>();
        descriptions = new ArrayList<String>();

        currHMM = hmm;
        stateArray = currHMM.getStateArray();
        pi = currHMM.getPiArray();
        piLog = currHMM.getPiLogArray();
        emissionSet = currHMM.getEmissionSetArray();//list of possible emissions

        //get sequence here
        FASTAReader fr = new FASTAReader(fileName);
        sequences = fr.getSequences();
        descriptions = fr.getDescriptions();//will use later on


        for(int i = 0; i <sequences.size(); i++){//iterates through all the sequences to be analyze

            currSeq = sequences.get(i);
            ObservationConverter oc = new ObservationConverter();
            emission = oc.processObservation(currSeq, emissionSet);
            //calculateAlpha(i); // computes alpha for each state for each time point
            //calculateLogAlpha(i);
            //calculateLogAlphaTest(i);
            calculateLogAlpha(i);

        }
    }//end ForwardAlgo(HMM hmm, String fileName)

    public int getSeqListSize(){
        return sequences.size();
    }

    private void calculateLogAlpha(int seqIndex){
        NodeState currNode;
        NodeState prevNode;
        int currE;
        double currLogEP;
        double logAlpha;
        double firstPrevLogAlpha = 0;
        double prevLogAlpha;
        double firstLogTransition = 0;
        double logTransition;
        double eSum;
        int firstJ = 0;
        double first = 0;
        double canFirst;
        for(int t = 0; t < emission.size(); t++){
            currE = emission.get(t);
            if(t == 0){
                for(int i0 = 0; i0 < stateArray.size(); i0++){
                    currNode = stateArray.get(i0);
                    currLogEP = currNode.getLogEmission(currE);
                    logAlpha = currLogEP+piLog.get(i0);
                    //System.out.println("LogAlpha at t = "+t+" for i = "+i0+" is: "+logAlpha);
                    currNode.setLogAlpha(seqIndex, logAlpha);
                }
            }else{
                for(int i = 0; i < stateArray.size(); i++){
                    currNode = stateArray.get(i);
                    currLogEP = currNode.getLogEmission(currE);
                    eSum=0;
                    //start get max
                    firstJ = 0;
                    for(int jm = 0; jm < stateArray.size(); jm++){
                        prevNode = stateArray.get(jm);
                        prevLogAlpha = prevNode.getLogAlpha(seqIndex, t-1);
                        logTransition = prevNode.getLogTransition(i);
                        if(jm == 0){
                            first = prevLogAlpha + logTransition;
                        }else{
                            canFirst = prevLogAlpha + logTransition;
                            if(canFirst > first){
                                first = canFirst;
                                firstJ = jm;
                            }
                        }
                    }
                    //if(seqIndex == 2 && t < 5){System.out.println("firstJ: "+firstJ);}
                    NodeState firstNode = stateArray.get(firstJ);
                    firstPrevLogAlpha = firstNode.getLogAlpha(seqIndex, t-1);
                    firstLogTransition = firstNode.getLogTransition(i);
                    //end get max

                    for(int j = 0; j <stateArray.size(); j++){
                        prevNode = stateArray.get(j);
                        prevLogAlpha = prevNode.getLogAlpha(seqIndex, t-1);
                        logTransition = prevNode.getLogTransition(i);
                        if(j != firstJ){
                            double exponent = prevLogAlpha+logTransition-firstPrevLogAlpha-firstLogTransition;
                            //double candFirst = prevLogAlpha+logTransition;//QC code
                            //double currFirst = firstPrevLogAlpha+firstLogTransition;//QC code
							/*
							if(seqIndex == 2 && t < 5){
								System.out.println("prevLogAlpha  at t = "+t+" for i = "+i+" and j = "+j+" is: "+prevLogAlpha);
								System.out.println("logTransition at t = "+t+" for i = "+i+" and j = "+j+" is: "+logTransition);
								System.out.println("candFirst     at t = "+t+" for i = "+i+" and j = "+j+" is: "+candFirst);
								System.out.println("currFirst     at t = "+t+" for i = "+i+" and j = "+j+" is: "+currFirst);
							}*/
                            double e = Math.exp(exponent);
                            if(e == Double.NEGATIVE_INFINITY){

                                System.out.println("NEGATIVE INFINITY FROM ALPHA");
                            }
                            if(e == Double.POSITIVE_INFINITY){
                                System.out.println("POSITIVE INFINITY FROM ALPHA");
                            }
                            if(e == Double.NaN){
                                System.out.println("NOT A NUMBER");
                            }
                            eSum = eSum+e;
                        }
                    }
                    logAlpha = firstPrevLogAlpha+firstLogTransition+Math.log(1+eSum)+currLogEP;
                    //QC code
                    if(logAlpha == Double.NEGATIVE_INFINITY){
                        System.out.println("LogAlpha neg_infinity at t = "+t);
                        //System.out.println("firstPrevLogAlpha: "+firstPrevLogAlpha);
                        //System.out.println("firstLogTransition: "+firstLogTransition);
                        //System.out.println("eSum: "+eSum);
                        //System.out.println("Math.log(1+eSum): "+Math.log(1+eSum));
                        //System.out.println("currLogEP: "+currLogEP);
                        //System.out.println("currLogEP: "+currNode.getLogEmissionArray());
                        //for(int it = 0; it < stateArray.size(); it++){
                        //NodeState cnode = stateArray.get(it);
                        //System.out.println("EA for node: "+it+"is: "+cnode.getLogEmissionArray());
                        //System.out.println("TA for node: "+it+"is: "+cnode.getLogTransitionArray());
                        //}

                    }
                    //end QC code
                    currNode.setLogAlpha(seqIndex, logAlpha);
                }
            }//end of else (when t is not 0)
        }
    }//end calculateLogAlpha(int seqIndex)

    public void displayScores(){
        //String scores = "";
        String logScores = "";
        for(int seqIndex = 0; seqIndex < sequences.size(); seqIndex++){
            //System.out.println(getLogScore(seqIndex));
            logScores = logScores+getLogScore(seqIndex)+" ";
        }

        System.out.println(logScores);

    }//end displayScores()

    public double getLogScore(int seqIndex){ // index is for the sequence being analyzed
        double logScore = 0;
        double firstLogAlpha = 0;
        int firstIndex = 0;
        double currLogAlpha;
        double currE;
        double sumE = 0;
        double exponent;
        NodeState currNode;
        //find firstLogAlpha
        for(int stateIndex = 0; stateIndex < stateArray.size(); stateIndex++){
            currNode = stateArray.get(stateIndex);
            String currSeq = sequences.get(seqIndex);
            currLogAlpha = currNode.getLogAlpha(seqIndex, currSeq.length()-1);//seqLength-1 = T
            if(stateIndex == 0){
                firstLogAlpha = currLogAlpha;
                firstIndex = stateIndex;
                //System.out.println("the firstLogAlpha is "+firstLogAlpha);
            }else{
                if(currLogAlpha > firstLogAlpha){
                    firstLogAlpha = currLogAlpha;
                    firstIndex = stateIndex;
                }
            }
        }

        //end find firstLogAlpha

        for(int stateIndex = 0; stateIndex < stateArray.size(); stateIndex++){
            currNode = stateArray.get(stateIndex);
            String currSeq = sequences.get(seqIndex);
            currLogAlpha = currNode.getLogAlpha(seqIndex, currSeq.length()-1);//seqLength-1 = T
            if(stateIndex != firstIndex){
                //calculate esum
                //System.out.println("the currLogAlph is "+currLogAlpha);
                exponent = currLogAlpha - firstLogAlpha;
                currE = Math.exp(exponent);
                //System.out.println("the currE is "+currE);
                sumE = sumE+currE;
            }
        }
        //System.out.println("The sumE is "+sumE);
        //System.out.println("The firstLogAlpha is "+firstLogAlpha);
        logScore = firstLogAlpha+Math.log(1+sumE);
        //System.out.println(logScore);
        return logScore;
    }//end of getLogScore()

    public ArrayList<String> getStateSeq(){//the ViterbiAlgo
        ArrayList<String>stateSeqArray = new ArrayList<String>();
        String stateSeq = "";
        NodeState currNode;
        double maxAlpha;
        int maxState;
        double currAlpha;

        //System.out.println("The number of sequences being analyzed is "+sequences.size());
        for(int seqIndex = 0; seqIndex < sequences.size(); seqIndex++){//iterates through sequence list
            //System.out.println("the length of the sequence being analyzed is "+emission.size());
            stateSeq = "";
            String currSeq = sequences.get(seqIndex);
            int currLength = currSeq.length();
            for(int t = 0; t < currLength; t++){
                maxAlpha = 0;
                maxState = 0;
                for(int i = 0; i < stateArray.size(); i++){
                    currNode = stateArray.get(i);
                    currAlpha = currNode.getLogAlpha(seqIndex, t);
                    if(i == 0){
                        maxAlpha = currAlpha;
                    }
                    if(currAlpha > maxAlpha){
                        maxAlpha = currAlpha;
                        maxState = i;
                    }
                }//end of state iteration
                stateSeq = stateSeq+maxState;
            }//end of emission iteration
            //System.out.println("From Viterbi : "+stateSeq);
            stateSeqArray.add(stateSeq);
        }//end of sequence list iteration
        return stateSeqArray;
    }//end ArrayList<String> getStateSeq()
}//end of class
