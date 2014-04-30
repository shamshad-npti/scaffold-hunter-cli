
package edu.udo.scaffoldhunter.model.treegen;

import edu.udo.scaffoldhunter.model.db.Ruleset;

/**
 * Class to stores all options for tree generation
 * 
 * @author Philipp Lewe
 */
public class GeneratorOptions {
    private String title = "";
    private String comment = "";
    private Ruleset ruleset = null;
    private boolean customrules = false;
    private boolean deglycosilate = false;

    /**
     * Creates a new <code>GeneratorOptions</code> object with default options
     */
    public GeneratorOptions() {
    }

    /**
     * Creates a new <code>GeneratorOptions</code> object with
     * 
     * @param title
     *            the title of the tree
     * @param comment
     *            the tree comment
     * @param ruleset
     *            the <code>Ruleset</code> for tree generation
     */
    public GeneratorOptions(String title, String comment, Ruleset ruleset) {
        this.title = title;
        this.comment = comment;
        this.ruleset = ruleset;
        this.customrules = true;
    }

    /**
     * Creates a new <code>GeneratorOptions</code> object with default rules
     * 
     * @param title
     *            the title of the tree
     * @param comment
     *            the tree comment
     */
    public GeneratorOptions(String title, String comment) {
        this.title = title;
        this.comment = comment;
        this.customrules = false;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the ruleset
     */
    public Ruleset getRuleset() {
        return ruleset;
    }

    /**
     * @param ruleset
     *            the ruleset to be used for tree generation
     */
    public void setRuleset(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    /**
     * Sets the boolean value indicating if the custom rules should be used to
     * generate the tree
     * 
     * @param bool
     *            true if custom rules should be used
     */
    public void setCustomRules(boolean bool) {
        this.customrules = bool;
    }

    /**
     * @return true if the custom rules should be used to generate the tree
     */
    public boolean isCustomrules() {
        return customrules;
    }

    /**
     * @return true if the moleculecules should be deglycosilated before tree generation
     */
    public boolean isDeglycosilate() {
        return deglycosilate;
    }

    /**
     * @param deglycosilate
     *            set to true if the moleculecules should be deglycosilated before tree generation
     */
    public void setDeglycosilate(boolean deglycosilate) {
        this.deglycosilate = deglycosilate;
    }
}
