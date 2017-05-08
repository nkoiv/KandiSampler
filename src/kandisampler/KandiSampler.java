/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 */
package kandisampler;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Sales estimator calculating estimates by sample groups,
 * utilizing similiarity by Hellinger distance
 * @author nikok
 */
public class KandiSampler {
    private static int product_id = 100069; //seasonal model product id
    private static HashMap<Integer, SalesHistory> sales = new HashMap<>();
    private static HashMap<Integer, SeasonalData> seasonal = new HashMap<>();
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Initialize and load data
        OutletGroup all_outlets = new OutletGroup(); //create outlet group for all available outlets
        all_outlets.loadCsv("data/outlet_data.csv"); //load outlet data from CSV
        loadSales(sales); //load sales data
        loadSeasonalData(seasonal); //load seasonal data
        SalesPrediction sp = new SalesPrediction(sales, seasonal, all_outlets); //create prediction base for all outlets
        
        sp.calculatePredictions(0); //Calculate basic sales levels for 0 seasonal variance
        System.out.println("Sales calculations initialized for " + sp.getEstimateCount() + " outlets");
        all_outlets = all_outlets.trimByOutletSet(sp.getOutletIDs());
        all_outlets.updateSalesLevels(sp); //initialize the outlet sales to predicted levels
        
        /*
        //Generate probability distributions
        OutletGroup sample1_outlets = all_outlets.getSampleGroup(1, fraction); //First samplegroup is the first 10% of outlets
        OutletGroup sample2_outlets = all_outlets.getSampleGroup(2, fraction); //Second samplegroup is every 10th outlet
        
        ProbabilityDistribution sample1_sales = new ProbabilityDistribution(sample1_outlets.outlet_sales_level);
        ProbabilityDistribution sample1_chain = new ProbabilityDistribution(sample1_outlets.outlet_chain);
        ProbabilityDistribution sample2_sales = new ProbabilityDistribution(sample2_outlets.outlet_sales_level);
        ProbabilityDistribution sample2_chain = new ProbabilityDistribution(sample2_outlets.outlet_chain);
        
        //Calculate predictions and Hellinger distances
        System.out.println("Starting predictions for large group");
        sp = new SalesPrediction(sales, seasonal, all_outlets);
        sp.addSalesRule(sr); //Add the given rule        
        double pred = calculatePredictions(sp, all_outlets, all_outlets.outlet_count);
        
        System.out.println(0+";"+pred);
        System.out.println("Starting predictions for sample groups (size "+all_outlets.outlet_count/50+")");
        //---------
        System.out.println("Starting predictions for first 10th group");
        System.out.println("Hellinger distance of sales for sample 1 (first 10th): "+all_sales.HellingerDistance(sample1_sales));
        System.out.println("Hellinger distance of chain for sample 1 (first 10th): "+all_chain.HellingerDistance(sample1_chain));
        sp = new SalesPrediction(sales, seasonal, sample1_outlets);
        sp.addSalesRule(sr); //Add the given rule 
        calculatePredictions(sp, all_outlets, all_outlets.outlet_count);
        //---------
        System.out.println("Starting predictions for every 10th group");
        System.out.println("Hellinger distance of sales for sample 2 (every 10th): "+all_sales.HellingerDistance(sample2_sales));
        System.out.println("Hellinger distance of chain for sample 2 (every 10th): "+all_chain.HellingerDistance(sample2_chain));
        sp = new SalesPrediction(sales, seasonal, sample2_outlets);
        sp.addSalesRule(sr); //Add the given rule 
        calculatePredictions(sp, all_outlets, all_outlets.outlet_count);
        
        
        */
        String filename = "\\calculations.csv";
        FileWriter fw = null;
        BufferedWriter bw = null;
        
        int[] fractions = new int[]{4, 5, 6, 7, 8, 10, 12, 15, 20, 25, 35, 50};
        try {
            fw = new FileWriter(filename);
            bw = new BufferedWriter(fw);
            
            for (int fraction : fractions) {
                doPredictions(all_outlets, sp, bw, fraction);
            }
        
            
        } catch (IOException e) {
            System.out.println("filewriter or buffered writer failed - problems with saving?");
        } 
        
        try {
            if (bw != null)
                bw.close();
            if (fw != null)
                fw.close();
            } catch (IOException e) {
                System.out.println("closing writers failed");
        }

    }
    
    private static void doPredictions(OutletGroup all_outlets, SalesPrediction sp, BufferedWriter bw, int fraction) {
        int randomCount = 100;
        //Generate the 20 randomly selected groups
        OutletGroup[] random_groups = new OutletGroup[randomCount];
        for (int i = 0; i < random_groups.length; i++) {
            random_groups[i] = all_outlets.getSampleGroup(3, fraction); //3 for random selection
        }
        
        
        //Calculate the propability distributions
        ProbabilityDistribution all_sales = new ProbabilityDistribution(all_outlets.outlet_sales_level);
        ProbabilityDistribution all_chain = new ProbabilityDistribution(all_outlets.outlet_chain);
        ProbabilityDistribution all_branch = new ProbabilityDistribution(all_outlets.outlet_branch);
        
        ProbabilityDistribution[] random_sales_prob = new ProbabilityDistribution[randomCount];
        ProbabilityDistribution[] random_chain_prob = new ProbabilityDistribution[randomCount];
        ProbabilityDistribution[] random_branch_prob = new ProbabilityDistribution[randomCount];
        for (int i = 0; i < randomCount; i++) {
            random_sales_prob[i] = new ProbabilityDistribution(random_groups[i].outlet_sales_level);
            random_chain_prob[i] = new ProbabilityDistribution(random_groups[i].outlet_chain);
            random_branch_prob[i] = new ProbabilityDistribution(random_groups[i].outlet_branch);
        }
    
        
        //Create a sales rule for +100% sales estimate on outlet_branch 2
        SalesRule sr = new SalesRule(1, 100006, 2);
        
        
        //Random selected samples
        System.out.println("---Predictions for random selections---");
        System.out.println("Hellinger Distance for... sales ; chain ; group; average ; Estimated sales ; sample group size%");
        double pred;
        for (int i = 0; i < random_groups.length; i++) {
            //System.out.println("Starting predictions for #"+i+" random group");
            //System.out.println("Hellinger distance of sales for sample #"+i+ "(random selection): "+all_sales.HellingerDistance(random_sales_prob[i]));
            //System.out.println("Hellinger distance of chain for sample #"+i+ "(random selection): "+all_chain.HellingerDistance(random_chain_prob[i]));
            //System.out.println("Bhattacharyya distance of sales for sample #"+i+ "(random selection): "+all_sales.BhattacharyyaDistance(random_sales_prob[i]));
            sp = new SalesPrediction(sales, seasonal, random_groups[i]);
            sp.addSalesRule(sr); //Add the given rule 
            pred = calculatePredictions(sp, all_outlets, all_outlets.outlet_count);
            //Print out the prediction line to console as csv separated by ";":
            //Hellinger distance of sales ; sales estimate ; sample size compared to full group (%)
            String s = all_sales.HellingerDistance(random_sales_prob[i])+";"
                    +all_chain.HellingerDistance(random_chain_prob[i])+";"
                    +all_branch.HellingerDistance(random_branch_prob[i])+";"
                    +(all_sales.HellingerDistance(random_sales_prob[i])+all_chain.HellingerDistance(random_chain_prob[i])+all_branch.HellingerDistance(random_branch_prob[i]) / 3.0)+";"
                    +pred+";"
                    +(double)((double)100/(double)fraction);
            System.out.println(s);
            try {
                bw.write(s);
                bw.newLine();
            } catch (IOException e) {
                System.out.println("writing failed");
            }
        }
    }
    
    private static double calculatePredictions(SalesPrediction sp, OutletGroup og, int totalOutletCount) {
        long start_time = System.currentTimeMillis();
        sp.calculatePredictions(15);
        long end_time = System.currentTimeMillis();
        //System.out.println("Predictions calculated in "+(end_time - start_time)+"ms for "+sp.getEstimateCount()+" outlets");
        //System.out.println("Total sales estimated: "+sp.getTotalSalesEstimate() + " (adjusted to total outlets "+ totalOutletCount +" = " + (sp.getTotalSalesEstimate() * (totalOutletCount / sp.getEstimateCount())) + ")" );
        //System.out.println("Avg sales estimated: "+sp.getAvgSalesEstimate());
        //sp.printSalesPerOutletChain(og);
        return (sp.getTotalSalesEstimate() * (totalOutletCount / sp.getEstimateCount()));
    }
    
    private static void loadSeasonalData(HashMap<Integer, SeasonalData> seasonal) {
        SeasonalData.loadSeasonalData(seasonal, product_id, "data/seasonal_data.csv");
        
        //int outlet = 213770;
        //System.out.println("Weekly data for "+outlet+" : "+Arrays.toString(seasonal.get(outlet).weekly_data));
        
        System.out.println("Weekly seasonal data loaded for "+seasonal.keySet().size()+" outlets");
    }
    
    private static void loadSales(HashMap<Integer, SalesHistory> sales) {
        SalesHistory.loadHistory(sales, 1, "data/sales_history.csv");
        /*
        for (int key : sales.keySet()) {
            System.out.println(sales.get(key).toString());
        }
        */
        System.out.println("Total outlets in sales history: "+sales.keySet().size());
        
    }
    
    
    public static void testSalesHistory() {
        SalesHistory sh = new SalesHistory(10016, 641400);
        sh.addSale(1, 15, 7);
        sh.addSale(3, 4, 2);
        System.out.println(Arrays.toString(sh.getSales()));
    }
    
    public static void testHellinger() {
        Integer[] foo = {15, 15, 15, 12, 52, 3, 4, 123, 24, 5, 4, 3, 1, 1, 1, 1, 1,};
        Integer[] bar = {39, 15, 15, 15, 1, 1, 1, 1, 1};
        ProbabilityDistribution a = new ProbabilityDistribution(foo);
        System.out.println("Valuecount: "+a.valueCount+" values: " +a.distribution.toString());
        ProbabilityDistribution b = new ProbabilityDistribution(bar);
        System.out.println("Valuecount: "+b.valueCount+" values: " +b.distribution.toString());
        System.out.println(a.HellingerDistance(b));
    }
    
    public static void testOutletGroup(OutletGroup og) {
        og.loadCsv("data/outlet_data.csv");
        System.out.println("Outlets: "+og.outlet_count);
        int n = 15;
        System.out.println("Data on row "+n+": "+og.outlet_id[n]+", "+og.outlet_chain[n]+", "+og.outlet_language[n]+", "+og.outlet_branch[n]);
    }
    
}
