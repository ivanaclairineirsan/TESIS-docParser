/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tesis;

import IndonesianNLP.IndonesianStemmer;

/**
 *
 * @author Ivana Clairine
 */
public class DataPreProcess {
    
    public static String punctuationRemover(String str) {
        String retval = (str.toLowerCase().replaceAll("[^a-zA-Z\\s]", " "));   //Removes Special Characters and Digits
        return (retval.trim().replaceAll("\\s{2,}", " "));      //Removes space, Special Characters and digits);
    }
    
      public static String doStemming(String sentence) {
        
        IndonesianStemmer stemmer = new IndonesianStemmer();
        return stemmer.stem(sentence);
    }
    
}
