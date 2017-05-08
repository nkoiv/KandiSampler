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
import java.util.Arrays;
import java.util.HashMap;

/**
 * SalesHistory is the sales history of
 * of a single outlet and a single product
 * @author nikok
 */
public class SalesHistory {
    public int outlet_id;
    public int product_id;
    
    //Array index number works as a linking ID
    private int issue_pointer; //next free slot
    private int[] issue;
    private int[] delivered;
    private int[] returned;

    public SalesHistory(int outlet, int product) {
    	this.outlet_id = outlet;
    	this.product_id = product;
    	this.issue = new int[15];
    	this.delivered = new int[15];
    	this.returned = new int[15];

    	this.issue_pointer = 0;
    }


    public int[] getSales() {
    	int[] sales = new int[issue_pointer];
    	for (int i = 0; i < sales.length;i++) {
    		sales[i] = delivered[i] - returned[i];
    	}
    	return sales;	
    }


    public void addSale(int issueNumber, int delivery_amount, int return_amount) {
    	if (issue_pointer >= issue.length) expand();

    	this.issue[issue_pointer] = issueNumber;
    	this.delivered[issue_pointer] = delivery_amount;
    	this.returned[issue_pointer] = return_amount;

    	issue_pointer++;
    }

    /**
	* Expand the issue-arrays to make room for more
	* sales history. Existing array size is doubled
    **/
    private void expand() {
    	int nLength = issue.length * 2;

    	int[] nIssue = new int[nLength];
    	int[] nDelivered = new int[nLength];
    	int[] nReturned = new int[nLength];

    	for (int i = 0; i < issue.length; i++) {
    		nIssue[i] = issue[i];
    		nDelivered[i] = delivered[i];
    		nReturned[i] = returned[i];

    	}

    	issue = nIssue;
    	delivered = nDelivered;
    	returned = nReturned;

    }

    public static void loadHistory(HashMap<Integer, SalesHistory> sales, int product_id, String filename) {
        BufferedReader br = null;
        String line = "";
        String split = ";";
        try {
            br = new BufferedReader(new FileReader(filename));
            try {
                while ((line = br.readLine()) != null) {
                    // use comma as separator
                    String[] data = line.split(split);
                    int issue = Integer.parseInt(data[1]);
                    int outlet_id = Integer.parseInt(data[2]);
                    int delivered = Integer.parseInt(data[3]);
                    int returned = Integer.parseInt(data[4]);

                    if (!sales.containsKey(outlet_id)) sales.put(outlet_id, new SalesHistory(outlet_id, product_id));

                    sales.get(outlet_id).addSale(issue, delivered, returned);

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
    
    @Override
    public String toString() {
        return outlet_id+":"+product_id+" delivered: "+Arrays.toString(this.getSales());
    }
}
