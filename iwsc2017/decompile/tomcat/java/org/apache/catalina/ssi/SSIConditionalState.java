package org.apache.catalina.ssi;
class SSIConditionalState {
    boolean branchTaken;
    int nestingCount;
    boolean processConditionalCommandsOnly;
    SSIConditionalState() {
        this.branchTaken = false;
        this.nestingCount = 0;
        this.processConditionalCommandsOnly = false;
    }
}
