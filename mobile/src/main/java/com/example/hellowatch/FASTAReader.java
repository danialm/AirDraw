package com.example.hellowatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class FASTAReader {

    private ArrayList <String> sequence;
    private ArrayList <String> description;

    public FASTAReader(String file){

        sequence = new ArrayList<String>();

        description = new ArrayList<String>();

        processFASTA(file);

    }//end FASTAReader()

    private void processFASTA(String file){

        File f = new File(file);
        //System.out.println("Reading "+file+" from file");
        try {
            Scanner fasta = new Scanner(f);
            while(fasta.hasNextLine()) {
                String line = fasta.nextLine();

                if(line.charAt(0) == '>'){
                    description.add(line);
                }else
                    sequence.add(line);// the description on even, observation on odd
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }//end processFASTA(String file)

    public ArrayList <String> getSequences(){
        return sequence;
    }
    public ArrayList <String> getDescriptions(){
        return description;
    }
}//end class
