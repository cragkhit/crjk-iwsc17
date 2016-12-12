package org.junit.rules;
import java.util.Collections;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
public class RuleChain implements TestRule {
    private static final RuleChain EMPTY_CHAIN;
    private List<TestRule> rulesStartingWithInnerMost;
    public static RuleChain emptyRuleChain() {
        return RuleChain.EMPTY_CHAIN;
    }
    public static RuleChain outerRule ( final TestRule outerRule ) {
        return emptyRuleChain().around ( outerRule );
    }
    private RuleChain ( final List<TestRule> rules ) {
        this.rulesStartingWithInnerMost = rules;
    }
    public RuleChain around ( final TestRule enclosedRule ) {
        if ( enclosedRule == null ) {
            throw new NullPointerException ( "The enclosed rule must not be null" );
        }
        final List<TestRule> rulesOfNewChain = new ArrayList<TestRule>();
        rulesOfNewChain.add ( enclosedRule );
        rulesOfNewChain.addAll ( this.rulesStartingWithInnerMost );
        return new RuleChain ( rulesOfNewChain );
    }
    public Statement apply ( final Statement base, final Description description ) {
        return new RunRules ( base, this.rulesStartingWithInnerMost, description );
    }
    static {
        EMPTY_CHAIN = new RuleChain ( Collections.emptyList() );
    }
}
