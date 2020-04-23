package core;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class CVOAUtilities {

    private static CVOAUtilities instance;
    public DecimalFormat decimalFormat;

    private CVOAUtilities() {

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        this.decimalFormat = new DecimalFormat("#.##E00", symbols);
    }

    public static CVOAUtilities getInstance() {

        CVOAUtilities res = null;

        if (instance == null) {
            res = new CVOAUtilities();
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

    public int binaryToDecimal(int[] rawData) {

        int i, res = 0;

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
