public class Test {
    public static void main ( String[] args ) {
        String s = "org.apache.catalina.Context context";
        String[] ss = s.split("\\.");
        for (int i=0; i<ss.length; i++) {
            System.out.println(ss[i]);
        }
    }
}
