package javax.el;
public class ELClass {
    private final Class<?> clazz;
    public ELClass ( final Class<?> clazz ) {
        this.clazz = clazz;
    }
    public Class<?> getKlass() {
        return this.clazz;
    }
}
