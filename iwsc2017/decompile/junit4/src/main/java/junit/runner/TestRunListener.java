package junit.runner;
public interface TestRunListener {
    public static final int STATUS_ERROR = 1;
    public static final int STATUS_FAILURE = 2;
    void testRunStarted ( String p0, int p1 );
    void testRunEnded ( long p0 );
    void testRunStopped ( long p0 );
    void testStarted ( String p0 );
    void testEnded ( String p0 );
    void testFailed ( int p0, String p1, String p2 );
}
