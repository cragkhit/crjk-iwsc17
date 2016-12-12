package org.jfree.chart.event;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.annotations.Annotation;
public class AnnotationChangeEvent extends ChartChangeEvent {
    private Annotation annotation;
    public AnnotationChangeEvent ( final Object source, final Annotation annotation ) {
        super ( source );
        ParamChecks.nullNotPermitted ( annotation, "annotation" );
        this.annotation = annotation;
    }
    public Annotation getAnnotation() {
        return this.annotation;
    }
}
