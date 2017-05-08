/*
 * This software (code) is free to use as it is, as long as it's not used for commercial purposes
 * and as long as you credit the author accordingly. For commercial purposes please contact the author.
 * The software is provided "as is" with absolutely no warranty of any kind.
 * Using this software is entirely up to you, and the author is in no way responsible for anything you do with it.
 */
package kandisampler;

/**
 * A rule to sales estimates, changing
 * the basic sales estimate of a selected group
 * by given amount
 * @author nikok
 */
public class SalesRule {
    public final static int ALL = 0;
    public final static int CHAIN = 1;
    public final static int BRANCH = 2;
    public final static int LANGUAGE = 1;
    public int rule_type; //consider changing ENUM
    public int outlets_id;
    public double change_amount;
    
    public SalesRule(int rule_type, int outlets_id, double change_amount) {
        if (rule_type < 0 || rule_type > 3) this.rule_type = 0;
        else this.rule_type = rule_type;
        this.change_amount = change_amount;
        this.outlets_id = outlets_id;
    }
}
