package org.apache.catalina.tribes.transport;
import org.apache.catalina.tribes.io.XByteBuffer;
public class Constants {
    public static final String Package = "org.apache.catalina.tribes.transport";
    public static final byte[] ACK_DATA;
    public static final byte[] FAIL_ACK_DATA;
    public static final byte[] ACK_COMMAND;
    public static final byte[] FAIL_ACK_COMMAND;
    static {
        ACK_DATA = new byte[] { 6, 2, 3 };
        FAIL_ACK_DATA = new byte[] { 11, 0, 5 };
        ACK_COMMAND = XByteBuffer.createDataPackage ( Constants.ACK_DATA );
        FAIL_ACK_COMMAND = XByteBuffer.createDataPackage ( Constants.FAIL_ACK_DATA );
    }
}
