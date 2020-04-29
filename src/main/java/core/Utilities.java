package core;

/**
 *
 * @author Data Science & Big Data Lab, Pablo de Olavide University
 *
 * Parallel Coronavirus Optimization Algorithm
 * 
 * Version 4.0 Academic version for a binary codification
 *
 * April 2020
 *
 */

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

// Utilities singleton class composed of methods for handling numeric issues (conversion to binary, format...)
public class Utilities {

    private static Utilities instance;
    public DecimalFormat decimalFormat;

    private Utilities() {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        this.decimalFormat = new DecimalFormat("#.##E00", symbols);
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

  public long binaryToDecimal(Individual binaryIndividual) {

    	return binaryToDecimal(binaryIndividual.getData());
    }

    public long binaryToDecimal(int[] rawData) {

        int i;
        long res = 0;

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

}
