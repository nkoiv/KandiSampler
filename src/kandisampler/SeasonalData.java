/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 */
package kandisampler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author nikok
 */
public class SeasonalData {
    int outlet_id;
    int product_id;
    double[] weekly_data;
    
    public SeasonalData(int product_id, int outlet_id) {
        this.outlet_id = outlet_id;
        this.product_id = product_id;
        this.weekly_data = new double[53];
    }
    
    /**
     * Load seasonal data for given product into a hashmap of SeasonalData objects
     * @param sd Seasonal Data hashmap for the given product
     * @param product_id ProductID to look for in the CSV file
     * @param filename path to the CSV file
     */
    public static void loadSeasonalData(HashMap<Integer, SeasonalData> sd, int product_id, String filename) {
        BufferedReader br = null;
        String line = "";
        String split = ";";
        try {
            br = new BufferedReader(new FileReader(filename));
            try {
                while ((line = br.readLine()) != null) {
                    // use comma as separator
                    String[] data = line.split(split);
                    if (data.length < 5) continue;
                    int outlet = Integer.parseInt(data[1]);
                    int product = Integer.parseInt(data[2]);
                    int week = Integer.parseInt(data[3]);
                    
                    double multi = Double.parseDouble(data[4].replace(",","."));
                    
                    
                    if (product != product_id) continue;
                    if (!sd.containsKey(outlet)) sd.put(outlet, new SeasonalData(product_id, outlet));
                    if (week > 0 && week <= 52 ) sd.get(outlet).weekly_data[week] = multi;
                }
            } catch (Exception e) {
                System.out.println("Failed to parse line: "+e);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        
    }
}
