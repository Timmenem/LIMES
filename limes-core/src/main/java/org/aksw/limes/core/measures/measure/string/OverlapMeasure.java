/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.aksw.limes.core.measures.measure.string;

import java.util.TreeSet;

import org.aksw.limes.core.io.cache.Instance;

/**
 * @author Axel-C. Ngonga Ngomo (ngonga@informatik.uni-leipzig.de)
 */
public class OverlapMeasure extends StringMeasure {

    public int getPrefixLength(int tokensNumber, double threshold) {
        return (int) (tokensNumber - threshold + 1);
    }

    // positional filtering does not help for overlap
    public int getMidLength(int tokensNumber, double threshold) {
        return Integer.MAX_VALUE;
    }

    public double getSizeFilteringThreshold(int tokensNumber, double threshold) {
        return threshold;
    }

    public int getAlpha(int xTokensNumber, int yTokensNumber, double threshold) {
        return (int) threshold;
    }

    public double getSimilarity(int overlap, int lengthA, int lengthB) {
        return overlap;
    }

    public double getSimilarity(Object object1, Object object2) {
        double counter = 0;

        TreeSet<String> tokens1 = new TreeSet<String>();
        TreeSet<String> tokens2 = new TreeSet<String>();

        String split1[] = ((String) object1).split(" ");
        for (int i = 0; i < split1.length; i++)
            tokens1.add(split1[i]);

        String split2[] = ((String) object2).split(" ");
        for (int i = 0; i < split2.length; i++)
            tokens2.add(split2[i]);

        for (String s : tokens2) {
            if (tokens1.contains(s))
                counter++;
        }
        return counter;
    }

    public String getType() {
        return "string";
    }

    public double getSimilarity(Instance instance1, Instance instance2, String property1, String property2) {

        double max = 0, sim;
        for (String p1 : instance1.getProperty(property1)) {
            for (String p2 : instance2.getProperty(property2)) {
                sim = getSimilarity(p1, p2);
                if (sim > max)
                    max = sim;
            }
        }
        return max;
    }

    public String getName() {
        return "overlap";
    }

    public boolean computableViaOverlap() {
        return true;
    }

    public double getRuntimeApproximation(double mappingSize) {
        return mappingSize / 1000d;
    }

}
