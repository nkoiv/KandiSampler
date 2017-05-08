/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 */
package kandisampler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 * A simple representation of discrete probability distribution
 * Keys are all stored as integers, probabilities as (integer) occurances,
 * divided by total amount of values when probability is demanded.
 * @author nikok
 */
public class ProbabilityDistribution { 
    
   public TreeMap<String, Integer> distribution;
   public int valueCount;
   
   public ProbabilityDistribution() {
       this.distribution = new TreeMap<>();
       this.valueCount = 0;
   }
   
   public ProbabilityDistribution(Object[] values) {
       this();
       calculateDistribution(values);
   }
   
   public ProbabilityDistribution(int[] values) {
       this();
       Integer[] v = new Integer[values.length];
       for (int i = 0; i < values.length; i++) {
           v[i] = values[i];
       }
       calculateDistribution(v);
   }
   
   private void calculateDistribution(Object[] values) {
       valueCount = values.length;
       for (int i = 0; i < values.length; i++) {
           if (distribution.containsKey(values[i].toString())) {
               distribution.put(values[i].toString(), distribution.get(values[i].toString())+1);
           } else {
               distribution.put(values[i].toString(), 1);
           }
       }
   }
   
   public double relativeFrequency(String event) {
       if (this.distribution.containsKey(event)) {
           return ((double)this.distribution.get(event) / this.valueCount);
       } else {
           return 0.0;
       }
   }
   
   /**
    * For two discrete probability distributions P = (p1,...pk) and Q = (q1,...,qk),
    * their Hellinger distance is defined as:
    * H(P,Q) = (1 / sqrt(2)) * sqrt( sum from i = 1 to k (sqrt(p_i) - sqrt(q_i))^2)
    * @param other The other discrete probability distribution to calc distance from
    * @return 0 for maximally similiar distributions, 1 for maximally different distributions.
    */
   public double HellingerDistance(ProbabilityDistribution other) {
       //System.out.println("Calculating Hellinger distance...");
       Set<String> keys = mergedKeys(this, other);
       double sum = 0.0;
       for (String key : keys) {
           sum += Math.pow(Math.sqrt(this.relativeFrequency(key))-Math.sqrt(other.relativeFrequency(key)), 2);
       }
       return Math.sqrt(sum) *  ( 1/Math.sqrt(2) );
   }
   
   public double BhattacharyyaDistance(ProbabilityDistribution other) {
       Set<String> keys = mergedKeys(this, other);
       double sum = 0.0;
       for (String key : keys) {
           sum += Math.sqrt(this.relativeFrequency(key) * other.relativeFrequency(key));
       }
       return sum;
   }
   
   private static Set<String> mergedKeys(ProbabilityDistribution a, ProbabilityDistribution b) {
       Set<String> allKeys = new HashSet<>();
       allKeys.addAll(a.distribution.keySet());
       allKeys.addAll(b.distribution.keySet());
       
       return allKeys;
   }
}
