import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by chaiyong on 19/11/2016.
 */
public class MainTest {

    @Test
    public void testAnnotationCount() {
        assertEquals(3, Main.getAnnotationCountFromFile("", "code.txt", 14, 5));
        assertEquals(1, Main.getAnnotationCountFromFile("/Users/chaiyong/Documents/iwsc2017/systems/", "tomcat/test/org/apache/catalina/core/TestStandardContext.java", 538, 5));
        assertEquals(2, Main.getAnnotationCountFromFile("/Users/chaiyong/Documents/iwsc2017/systems/", "tomcat/java/javax/servlet/jsp/el/ImplicitObjectELResolver.java", 79, 5));
    }
}
