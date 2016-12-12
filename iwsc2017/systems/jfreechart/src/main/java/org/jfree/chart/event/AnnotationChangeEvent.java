package org.jfree.chart.event;
import org.jfree.chart.annotations.Annotation;
import org.jfree.chart.util.ParamChecks;
public class AnnotationChangeEvent extends ChartChangeEvent {
    private Annotation annotation;
    public AnnotationChangeEvent ( Object source, Annotation annotation ) {
        super ( source );
        ParamChecks.nullNotPermitted ( annotation, "annotation" );
        this.annotation = annotation;
    }
    public Annotation getAnnotation() {
        return this.annotation;
    }
}
