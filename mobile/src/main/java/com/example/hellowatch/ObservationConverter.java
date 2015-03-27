package com.example.hellowatch;

//this class replaces observation symbols with their corresponding int assignments

import java.util.ArrayList;

public class ObservationConverter {

    ArrayList <Integer> emission; //will be returned by processObservation()
    ArrayList <String> emissionSet;

    public ObservationConverter(){
        emission = new ArrayList <Integer>();
    }

    public ArrayList <Integer> processObservation(String seq, ArrayList <String> es){
        emissionSet = es;
        char[] c = seq.toCharArray();
        String[] s = new String[c.length];
        for(int i = 0; i< c.length; i++){
            s[i] = Character.toString(c[i]);
        }
		/*for(int i = 0; i < s.length; i++){
			System.out.print(s[i]);
		}
		System.out.println();
		*/
        for(int i = 0; i < s.length; i++){
            for(int j = 0; j < emissionSet.size(); j++){
                if(s[i].equals(emissionSet.get(j))){
                    s[i] = Integer.toString(j);
                    //System.out.println("true");
                }//else
                //System.out.println("false");
            }
        }
        //for(int i = 0; i < s.length; i++){
        //System.out.print(s[i]);
        //}
        //System.out.println();
        for(int i = 0; i < s.length; i++){
            emission.add(Integer.parseInt(s[i]));
        }
        //for(int i =0; i< emission.size(); i++){
        //System.out.print(emission.get(i));
        //}
        //System.out.println();
        return emission;
    }//end processObservation()

}
