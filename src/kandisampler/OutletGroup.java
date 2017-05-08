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
import java.util.ArrayList;
import java.util.Random;

/**
 * Outlets contain the data of outlets, loaded from CSV
 * Outlets could be represented by individual objects, but
 * for calculation purposes it's faster to manipulate simple arrays of data.
 * @author nikok
 */
public class OutletGroup {
    //Line 0 == empty.
    public int outlet_count; //number of outlets
    public int[] outlet_id;
    //private String[] outlet_name;
    public int[] outlet_chain;
    public int[] outlet_branch;
    public int[] outlet_language;
    public int[] outlet_sales_level;
    
    public OutletGroup() {
        outlet_count = 0;
        outlet_id = new int[0];
        outlet_chain = new int[0];
        outlet_branch = new int[0];
        outlet_language = new int[0];
        outlet_sales_level = new int[0];
    }
    
    private void initializeOutlets(int size) {
        outlet_id = new int[size];
        outlet_chain = new int[size];
        outlet_branch = new int[size];
        outlet_language = new int[size];
        outlet_sales_level = new int[size];
        outlet_count = size;
    }
    
    public void updateSalesLevels(SalesPrediction sp) {
        for (int i = 0; i < outlet_id.length; i++) {
            if (sp.getPredictions().containsKey(outlet_id[i])) outlet_sales_level[i] = (int)Math.floor(sp.getPredictions().get(outlet_id[i]));
            else outlet_sales_level[i] = 0;
        }
    }
    
    private int countLines(String filename) {
        BufferedReader br = null;
        String line = "";
        int lines = 0;
        try {
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                lines++;
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
        return lines;
    }
    
    public int[] getOutletChains() {
        ArrayList<Integer> chains = new ArrayList<>();
        for (int chain_id : this.outlet_chain) {
            if (!chains.contains(chain_id)) chains.add(chain_id);
        }
        int[] ret = new int[chains.size()];
        for (int i = 0; i < chains.size(); i++) {
            ret[i] = chains.get(i);
        }
        return ret;
    }
    
    public void loadCsv(String filename) {
        int lines = countLines(filename);
        initializeOutlets(lines);
        BufferedReader br = null;
        String line = "";
        String split = ";";
        int currentLine = 0;
        try {
            br = new BufferedReader(new FileReader(filename));
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] outlet = line.split(split);
                addOutlet(outlet, currentLine);
                currentLine++;
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
    
    private void addOutlet(String[] outlet, int line) {
        //CSV: OUTLET_ID;MAIN_CHAIN_ID;ASSRTMNT_ASSRTMNT_LANGUAGE_ID;ASS_ASSRTMNT_OTLT_BRNCH_ID
        if (line == 0) return;
        try {
            outlet_id[line] = Integer.parseInt(outlet[0]);
            outlet_chain[line] = Integer.parseInt(outlet[1]);
            outlet_branch[line] = Integer.parseInt(outlet[2]);
            outlet_language[line] = Integer.parseInt(outlet[3]);
        } catch (Exception e) {
            System.out.println("Failed to parse line from CSV: "+outlet.toString());
        }
        
    }
    
    /**
     * Return a quarter of the base group, selected
     * with a method of choice.
     * 1: First N% (N=10) of outlets
     * 2: Every Nth (N=10) outlet
     * 3: Randomly selected outlets
     * 4: Markovian chain selection by sale_level
     * @param method
     * @param fraction how many outlets are included in sample (as 1/fraction of base group)
     * @return 
     */
    public OutletGroup getSampleGroup(int method, int fraction) {
        int fraction_size =  outlet_count/fraction;
        OutletGroup sample = new OutletGroup();
        sample.initializeOutlets(fraction_size);
        switch (method) {
            case 1: copyHead(sample); break;
            case 2: copyEveryNth(sample, fraction); break;
            case 3: copyRandom(sample);
            default: 
        }
        
        return sample;
    }
    
    private void copyHead(OutletGroup newGroup) {
        for (int i = 0; i < newGroup.outlet_count; i ++) {
            newGroup.outlet_id[i] = outlet_id[i];
            newGroup.outlet_chain[i] = outlet_chain[i];
            newGroup.outlet_branch[i] = outlet_branch[i];
            newGroup.outlet_language[i] = outlet_language[i];
            newGroup.outlet_sales_level[i] = outlet_sales_level[i];
        }
        newGroup.outlet_count = newGroup.outlet_id.length;
    }
       
    private void copyEveryNth(OutletGroup newGroup, int n) {
        for (int i = 0; i < newGroup.outlet_count; i ++) {
            int pointer = i*n;
            //if (pointer > this.outlet_id.length) pointer = this.outlet_id.length-1;
            newGroup.outlet_id[i] = outlet_id[pointer];
            newGroup.outlet_chain[i] = outlet_chain[pointer];
            newGroup.outlet_branch[i] = outlet_branch[pointer];
            newGroup.outlet_language[i] = outlet_language[pointer];
            newGroup.outlet_sales_level[i] = outlet_sales_level[pointer];
        }
        newGroup.outlet_count = newGroup.outlet_id.length;
    }
    
    private void copyRandom(OutletGroup newGroup) {
        if (newGroup.outlet_count > this.outlet_count) System.out.println("Trying to fill a big group with randoms from smaller one!");
        Random rand = new Random();
        int pointer = 0;
        while (pointer < newGroup.outlet_count) {
            int rid = rand.nextInt(this.outlet_count);
            if (newGroup.getPosition(outlet_id[rid]) < 0) {
                newGroup.outlet_id[pointer] = outlet_id[rid];
                newGroup.outlet_chain[pointer] = outlet_chain[rid];
                newGroup.outlet_branch[pointer] = outlet_branch[rid];
                newGroup.outlet_language[pointer] = outlet_language[rid];
                newGroup.outlet_sales_level[pointer] = outlet_sales_level[rid];
                pointer++;
            }
        }
        newGroup.outlet_count = newGroup.outlet_id.length;
    }    
    
    public OutletGroup trimByOutletSet(Integer[] outletIDs) {
        System.out.println("Total outlets: "+this.outlet_count+", trimmed target: "+outletIDs.length);
        OutletGroup newGroup = new OutletGroup();
        newGroup.initializeOutlets(outletIDs.length);
        for (int i = 0; i < outletIDs.length; i++) {
            int pos = this.getPosition(outletIDs[i]);
            if (pos >= 0) {
                newGroup.outlet_id[i] = outlet_id[pos];
                newGroup.outlet_chain[i] = outlet_chain[pos];
                newGroup.outlet_branch[i] = outlet_branch[pos];
                newGroup.outlet_language[i] = outlet_language[pos];
                newGroup.outlet_sales_level[i] = outlet_sales_level[pos];
            }
        }
        newGroup.outlet_count = newGroup.outlet_id.length;
        return newGroup;
    }
 
    
    /*
    public OutletGroup trimByOutletArray(int[] outletIDs) {
        int count = 0;
        for (int i = 0; i < outletIDs.length; i++) {
            if (this.getPosition(outletIDs[i]) >= 0) count++;
        }
        OutletGroup newGroup = new OutletGroup();
        newGroup.initializeOutlets(count);
        for (int i = 0; i < outletIDs.length; i++) {
            int pos = this.getPosition(outletIDs[i]);
            if (pos >= 0) {
                newGroup.outlet_id[i] = outlet_id[pos];
                newGroup.outlet_chain[i] = outlet_chain[pos];
                newGroup.outlet_branch[i] = outlet_branch[pos];
                newGroup.outlet_language[i] = outlet_language[pos];
                newGroup.outlet_sales_level[i] = outlet_sales_level[pos];
            }
        }
        
        return newGroup;
    }
    */
    
    
    /**
     * Search the OutletGroup for given ID.
     * @param id OutletID to search for
     * @return True if given OutletID is within this outletGroup
     */
    private int getPosition(int id) {
        for (int i = 0; i < this.outlet_count; i++) {
            if (this.outlet_id[i] == id) return i;
        }
        return -1;
    }
}
