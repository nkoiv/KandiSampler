/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 */
package kandisampler;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Calculate the sales estimate for a product given the sales history.
 * Weighted base estimate is adjusted by seasonal variation index.
 * @author nikok
 */
public class SalesPrediction {
    
    //calculation array stores the various steps of the sales prediction
    private int week;
    private HashMap<Integer, Integer> weighted_basic_sales;
    private HashMap<Integer, Double> sales_estimates;
    private OutletGroup outlets;
    private HashMap<Integer, SalesHistory> sales;
    private HashMap<Integer, SeasonalData> seasonal;
    
    private ArrayList<SalesRule> sales_rules;
    
    public SalesPrediction(HashMap<Integer, SalesHistory> sales, HashMap<Integer, SeasonalData> seasonal, OutletGroup outlets) {
        this.outlets = outlets;
        this.sales = sales;
        this.seasonal = seasonal;
        this.weighted_basic_sales = new HashMap<>();
        this.sales_estimates = new HashMap<>();
        this.sales_rules = new ArrayList<>();
    }
    
    public void setOutlets(OutletGroup outlets) {
        this.outlets = outlets;
    }
    
    public void calculateWeightedBasicSales() {
        for (int outlet : outlets.outlet_id) {
            weighted_basic_sales.put(outlet, (int)getWeightedEstimate(sales.get(outlet).getSales()));
        }
    }
    
    public double calculatePrediction(int outlet_id, int week) {
        double prediction;
        int[] sold_copies = sales.get(outlet_id).getSales();
        double weekly_multi = 1.0;
        if (week > 0 && seasonal.containsKey(outlet_id)) {
            weekly_multi = seasonal.get(outlet_id).weekly_data[week];
        }
        prediction = getWeightedEstimate(sold_copies) * weekly_multi;
        
        prediction = calculateSalesRules(outlet_id, prediction);
        
        return prediction;
    }
    
    private double calculateSalesRules(int outlet_id, double estimate) {
        double new_estimate = estimate;
        int id = -1;
        for (int i = 0; i < this.outlets.outlet_id.length; i++) {
            if (this.outlets.outlet_id[i] == outlet_id) {
                id = i;
                break;
            }
        }
        if (id == -1) {
            System.out.println("Couldnt find outlet");
            return new_estimate;
        }
        for (SalesRule rule : this.sales_rules) {
            switch (rule.rule_type) {
                case 0: 
                    new_estimate = new_estimate * rule.change_amount;
                    break;
                case 1: 
                    if (this.outlets.outlet_chain[id] == rule.outlets_id) new_estimate = new_estimate * rule.change_amount;
                    break;
                case 2: 
                    if (this.outlets.outlet_branch[id] == rule.outlets_id) new_estimate = new_estimate * rule.change_amount;
                    break;
                case 3: 
                    if (this.outlets.outlet_language[id] == rule.outlets_id) new_estimate = new_estimate * rule.change_amount;
                    break;
                default: break;
            }
        }
        return new_estimate;
    }
    
    public double getTotalSalesEstimate() {
        double total = 0;
        for (Integer outlet : sales_estimates.keySet()) {
            total = total+sales_estimates.get(outlet);
        }
        return total;
    }
    
    public double getAvgSalesEstimate() {
        double total = getTotalSalesEstimate();
        return total/(double)sales_estimates.keySet().size();
    }
    
    public int getEstimateCount() {
        return sales_estimates.keySet().size();
    }
    
    private double getWeightedEstimate(int[] sold_copies) {
        double estimate = 0;
        double weight_sum = 0;
        double n = 1;
        for (int i = sold_copies.length-1; i >= 0; i--) {
            //Start from the latest, go through all the sales;
            double pow = -n/3;
            double weight = Math.pow(Math.E, pow);
            weight_sum = weight_sum + weight;
            estimate += ((double)sold_copies[i] * weight);
            n++;
        }
        
        estimate = estimate / weight_sum;
        return estimate;
    }
    
    public void calculatePredictions(int week) {
        this.week = week;
        for (int outlet : outlets.outlet_id) {
            if (sales.containsKey(outlet)) sales_estimates.put(outlet, calculatePrediction(outlet, week));
        }
    }
    
    /**
     * print out sales per outletgroup
     * @param group 
     */
    public void printSalesPerOutletChain(OutletGroup group) {
        int[] chains = group.getOutletChains();
        for (int i = 0; i < chains.length; i++) {
            System.out.println("Sales for chain " + chains[i] + ": " + getChainSumSales(chains[i], group));
        }   
    }
    
    public double getChainSumSales(int outletChain, OutletGroup group) {
        double sum = 0;
        for (int i = 0; i < group.outlet_id.length; i++) {
            if (group.outlet_chain[i] == outletChain && this.sales_estimates.containsKey(group.outlet_id[i])) {
                sum += this.sales_estimates.get(group.outlet_id[i]);
            }
        }
        return sum;
    }
    
    public void clearSalesRules() {
        this.sales_rules = new ArrayList<>();
    }
    
    public void addSalesRule(SalesRule rule) {
        this.sales_rules.add(rule);
    }
    public int getWeek() {
        return this.week;
    }
    
    public HashMap<Integer, Double> getPredictions() {
        return this.sales_estimates;
    }
    
    public Integer[] getOutletIDs() {
        return (Integer[])sales_estimates.keySet().toArray(new Integer[0]);
    }
    
}
