package org.apache.catalina.ssi;
class SSIConditionalState {
    boolean branchTaken = false;
    int nestingCount = 0;
    boolean processConditionalCommandsOnly = false;
}
