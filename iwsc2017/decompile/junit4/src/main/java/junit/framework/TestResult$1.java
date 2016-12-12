package junit.framework;
class TestResult$1 implements Protectable {
    final   TestCase val$test;
    public void protect() throws Throwable {
        this.val$test.runBare();
    }
}
