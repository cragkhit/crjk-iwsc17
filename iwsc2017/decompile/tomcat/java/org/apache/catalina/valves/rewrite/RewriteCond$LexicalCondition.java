package org.apache.catalina.valves.rewrite;
public static class LexicalCondition extends Condition {
    public int type;
    public String condition;
    public LexicalCondition() {
        this.type = 0;
    }
    @Override
    public boolean evaluate ( final String value, final Resolver resolver ) {
        final int result = value.compareTo ( this.condition );
        switch ( this.type ) {
        case -1: {
            return result < 0;
        }
        case 0: {
            return result == 0;
        }
        case 1: {
            return result > 0;
        }
        default: {
            return false;
        }
        }
    }
}
