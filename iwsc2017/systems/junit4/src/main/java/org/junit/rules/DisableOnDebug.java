package org.junit.rules;
import java.util.List;
import org.junit.internal.management.ManagementFactory;
import org.junit.internal.management.RuntimeMXBean;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
public class DisableOnDebug implements TestRule {
    private final TestRule rule;
    private final boolean debugging;
    public DisableOnDebug ( TestRule rule ) {
        this ( rule, ManagementFactory.getRuntimeMXBean()
               .getInputArguments() );
    }
    DisableOnDebug ( TestRule rule, List<String> inputArguments ) {
        this.rule = rule;
        debugging = isDebugging ( inputArguments );
    }
    public Statement apply ( Statement base, Description description ) {
        if ( debugging ) {
            return base;
        } else {
            return rule.apply ( base, description );
        }
    }
    private static boolean isDebugging ( List<String> arguments ) {
        for ( final String argument : arguments ) {
            if ( "-Xdebug".equals ( argument ) ) {
                return true;
            } else if ( argument.startsWith ( "-agentlib:jdwp" ) ) {
                return true;
            }
        }
        return false;
    }
    public boolean isDebugging() {
        return debugging;
    }
}
