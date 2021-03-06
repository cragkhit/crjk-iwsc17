package junit.framework;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import org.junit.internal.MethodSorter;
import org.junit.internal.Throwables;
public class TestSuite implements Test {
    static public Test createTest ( Class<?> theClass, String name ) {
        Constructor<?> constructor;
        try {
            constructor = getTestConstructor ( theClass );
        } catch ( NoSuchMethodException e ) {
            return warning ( "Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()" );
        }
        Object test;
        try {
            if ( constructor.getParameterTypes().length == 0 ) {
                test = constructor.newInstance ( new Object[0] );
                if ( test instanceof TestCase ) {
                    ( ( TestCase ) test ).setName ( name );
                }
            } else {
                test = constructor.newInstance ( new Object[] {name} );
            }
        } catch ( InstantiationException e ) {
            return ( warning ( "Cannot instantiate test case: " + name + " (" + Throwables.getStacktrace ( e ) + ")" ) );
        } catch ( InvocationTargetException e ) {
            return ( warning ( "Exception in constructor: " + name + " (" + Throwables.getStacktrace ( e.getTargetException() ) + ")" ) );
        } catch ( IllegalAccessException e ) {
            return ( warning ( "Cannot access test case: " + name + " (" + Throwables.getStacktrace ( e ) + ")" ) );
        }
        return ( Test ) test;
    }
    public static Constructor<?> getTestConstructor ( Class<?> theClass ) throws NoSuchMethodException {
        try {
            return theClass.getConstructor ( String.class );
        } catch ( NoSuchMethodException e ) {
        }
        return theClass.getConstructor();
    }
    public static Test warning ( final String message ) {
        return new TestCase ( "warning" ) {
            @Override
            protected void runTest() {
                fail ( message );
            }
        };
    }
    private String fName;
    private Vector<Test> fTests = new Vector<Test> ( 10 );
    public TestSuite() {
    }
    public TestSuite ( final Class<?> theClass ) {
        addTestsFromTestCase ( theClass );
    }
    private void addTestsFromTestCase ( final Class<?> theClass ) {
        fName = theClass.getName();
        try {
            getTestConstructor ( theClass );
        } catch ( NoSuchMethodException e ) {
            addTest ( warning ( "Class " + theClass.getName() + " has no public constructor TestCase(String name) or TestCase()" ) );
            return;
        }
        if ( !Modifier.isPublic ( theClass.getModifiers() ) ) {
            addTest ( warning ( "Class " + theClass.getName() + " is not public" ) );
            return;
        }
        Class<?> superClass = theClass;
        List<String> names = new ArrayList<String>();
        while ( Test.class.isAssignableFrom ( superClass ) ) {
            for ( Method each : MethodSorter.getDeclaredMethods ( superClass ) ) {
                addTestMethod ( each, names, theClass );
            }
            superClass = superClass.getSuperclass();
        }
        if ( fTests.size() == 0 ) {
            addTest ( warning ( "No tests found in " + theClass.getName() ) );
        }
    }
    public TestSuite ( Class<? extends TestCase> theClass, String name ) {
        this ( theClass );
        setName ( name );
    }
    public TestSuite ( String name ) {
        setName ( name );
    }
    public TestSuite ( Class<?>... classes ) {
        for ( Class<?> each : classes ) {
            addTest ( testCaseForClass ( each ) );
        }
    }
    private Test testCaseForClass ( Class<?> each ) {
        if ( TestCase.class.isAssignableFrom ( each ) ) {
            return new TestSuite ( each.asSubclass ( TestCase.class ) );
        } else {
            return warning ( each.getCanonicalName() + " does not extend TestCase" );
        }
    }
    public TestSuite ( Class<? extends TestCase>[] classes, String name ) {
        this ( classes );
        setName ( name );
    }
    public void addTest ( Test test ) {
        fTests.add ( test );
    }
    public void addTestSuite ( Class<? extends TestCase> testClass ) {
        addTest ( new TestSuite ( testClass ) );
    }
    public int countTestCases() {
        int count = 0;
        for ( Test each : fTests ) {
            count += each.countTestCases();
        }
        return count;
    }
    public String getName() {
        return fName;
    }
    public void run ( TestResult result ) {
        for ( Test each : fTests ) {
            if ( result.shouldStop() ) {
                break;
            }
            runTest ( each, result );
        }
    }
    public void runTest ( Test test, TestResult result ) {
        test.run ( result );
    }
    public void setName ( String name ) {
        fName = name;
    }
    public Test testAt ( int index ) {
        return fTests.get ( index );
    }
    public int testCount() {
        return fTests.size();
    }
    public Enumeration<Test> tests() {
        return fTests.elements();
    }
    @Override
    public String toString() {
        if ( getName() != null ) {
            return getName();
        }
        return super.toString();
    }
    private void addTestMethod ( Method m, List<String> names, Class<?> theClass ) {
        String name = m.getName();
        if ( names.contains ( name ) ) {
            return;
        }
        if ( !isPublicTestMethod ( m ) ) {
            if ( isTestMethod ( m ) ) {
                addTest ( warning ( "Test method isn't public: " + m.getName() + "(" + theClass.getCanonicalName() + ")" ) );
            }
            return;
        }
        names.add ( name );
        addTest ( createTest ( theClass, name ) );
    }
    private boolean isPublicTestMethod ( Method m ) {
        return isTestMethod ( m ) && Modifier.isPublic ( m.getModifiers() );
    }
    private boolean isTestMethod ( Method m ) {
        return m.getParameterTypes().length == 0 &&
               m.getName().startsWith ( "test" ) &&
               m.getReturnType().equals ( Void.TYPE );
    }
}
