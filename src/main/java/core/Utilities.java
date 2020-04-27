package core;

/**
 *
 * @author Data Science & Big Data Lab, Pablo de Olavide University
 *
 * Parallel Coronavirus Optimization Algorithm
 * 
 * Version 3.0 Academic version for a binary codification
 *
 * April 2020
 *
 */

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Random;
import fitness.FitnessFunction;



// Utilities singleton class composed of methods for handling numeric issues (conversion to binary, format...)
public class Utilities {

    private static Utilities instance;
    public DecimalFormat decimalFormat;
    private Random rnd;

    private Utilities() {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        this.decimalFormat = new DecimalFormat("#.##E00", symbols);
        this.rnd = new Random(System.currentTimeMillis());
    }

    public static Utilities getInstance() {

        Utilities res = null;

        if (instance == null) {
            res = new Utilities();
        } else {
            res = instance;
        }

        return res;
    }

    public int binaryToDecimal(Individual binaryIndividual) {

        int i, res = 0;
        int[] data = binaryIndividual.getData();

        if (data != null) {
            for (i = 0; i < data.length; i++) {
                res += data[i] * Math.pow(2, i);
            }
        }
        return res;
    }

    public double binaryToDecimal(int[] rawData) {

        int i;
        double res = 0;

        if (rawData != null) {
            for (i = 0; i < rawData.length; i++) {
                res += rawData[i] * Math.pow(2, i);
            }
        }
        
        return res;
        
    }

    public DecimalFormat getDecimalFormat() {
        return decimalFormat;
    }

    public DecimalFormat getDecimalFormat(String pattern) {
        decimalFormat.applyPattern(pattern);
        return decimalFormat;
    }

    public Individual randomInfection(int nBits, FitnessFunction function) {
        Individual pz = null;

        int[] data = new int[nBits];

        for (int i = 0; i < nBits; i++) {
            data[i] = rnd.nextInt(2);
        }

        pz = buildIndividual(data, function);

        return pz;
    }
    
    public Individual buildIndividual(int[] data, FitnessFunction function) {

        Individual res = new Individual();
        res.setData(data);
        res.setFitness(function.fitness(res));
        return res;

    }
    
        public Individual getExtremeIndividual(boolean best) {

        Individual res = new Individual();

        res.setFitness(Long.MAX_VALUE);

        if (best) {
            res.setFitness(0);
        }

        return res;
    }
    
}
