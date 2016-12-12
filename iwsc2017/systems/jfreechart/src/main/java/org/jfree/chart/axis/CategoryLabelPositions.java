

package org.jfree.chart.axis;

import java.io.Serializable;
import org.jfree.chart.util.ParamChecks;

import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;


public class CategoryLabelPositions implements Serializable {


    private static final long serialVersionUID = -8999557901920364580L;


    public static final CategoryLabelPositions
    STANDARD = new CategoryLabelPositions (
        new CategoryLabelPosition (
            RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_CENTER ),
        new CategoryLabelPosition (
            RectangleAnchor.TOP, TextBlockAnchor.TOP_CENTER ),
        new CategoryLabelPosition (
            RectangleAnchor.RIGHT, TextBlockAnchor.CENTER_RIGHT,
            CategoryLabelWidthType.RANGE, 0.30f ),
        new CategoryLabelPosition (
            RectangleAnchor.LEFT, TextBlockAnchor.CENTER_LEFT,
            CategoryLabelWidthType.RANGE, 0.30f )
    );


    public static final CategoryLabelPositions
    UP_90 = new CategoryLabelPositions (
        new CategoryLabelPosition (
            RectangleAnchor.BOTTOM, TextBlockAnchor.CENTER_LEFT,
            TextAnchor.CENTER_LEFT, -Math.PI / 2.0,
            CategoryLabelWidthType.RANGE, 0.30f ),
        new CategoryLabelPosition (
            RectangleAnchor.TOP, TextBlockAnchor.CENTER_RIGHT,
            TextAnchor.CENTER_RIGHT, -Math.PI / 2.0,
            CategoryLabelWidthType.RANGE, 0.30f ),
        new CategoryLabelPosition (
            RectangleAnchor.RIGHT, TextBlockAnchor.BOTTOM_CENTER,
            TextAnchor.BOTTOM_CENTER, -Math.PI / 2.0,
            CategoryLabelWidthType.CATEGORY, 0.9f ),
        new CategoryLabelPosition (
            RectangleAnchor.LEFT, TextBlockAnchor.TOP_CENTER,
            TextAnchor.TOP_CENTER, -Math.PI / 2.0,
            CategoryLabelWidthType.CATEGORY, 0.90f )
    );


    public static final CategoryLabelPositions
    DOWN_90 = new CategoryLabelPositions (
        new CategoryLabelPosition (
            RectangleAnchor.BOTTOM, TextBlockAnchor.CENTER_RIGHT,
            TextAnchor.CENTER_RIGHT, Math.PI / 2.0,
            CategoryLabelWidthType.RANGE, 0.30f ),
        new CategoryLabelPosition (
            RectangleAnchor.TOP, TextBlockAnchor.CENTER_LEFT,
            TextAnchor.CENTER_LEFT, Math.PI / 2.0,
            CategoryLabelWidthType.RANGE, 0.30f ),
        new CategoryLabelPosition (
            RectangleAnchor.RIGHT, TextBlockAnchor.TOP_CENTER,
            TextAnchor.TOP_CENTER, Math.PI / 2.0,
            CategoryLabelWidthType.CATEGORY, 0.90f ),
        new CategoryLabelPosition (
            RectangleAnchor.LEFT, TextBlockAnchor.BOTTOM_CENTER,
            TextAnchor.BOTTOM_CENTER, Math.PI / 2.0,
            CategoryLabelWidthType.CATEGORY, 0.90f )
    );


    public static final CategoryLabelPositions UP_45
        = createUpRotationLabelPositions ( Math.PI / 4.0 );


    public static final CategoryLabelPositions DOWN_45
        = createDownRotationLabelPositions ( Math.PI / 4.0 );


    public static CategoryLabelPositions createUpRotationLabelPositions (
        double angle ) {
        return new CategoryLabelPositions (
                   new CategoryLabelPosition (
                       RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_LEFT,
                       TextAnchor.BOTTOM_LEFT, -angle,
                       CategoryLabelWidthType.RANGE, 0.50f ),
                   new CategoryLabelPosition (
                       RectangleAnchor.TOP, TextBlockAnchor.TOP_RIGHT,
                       TextAnchor.TOP_RIGHT, -angle,
                       CategoryLabelWidthType.RANGE, 0.50f ),
                   new CategoryLabelPosition (
                       RectangleAnchor.RIGHT, TextBlockAnchor.BOTTOM_RIGHT,
                       TextAnchor.BOTTOM_RIGHT, -angle,
                       CategoryLabelWidthType.RANGE, 0.50f ),
                   new CategoryLabelPosition (
                       RectangleAnchor.LEFT, TextBlockAnchor.TOP_LEFT,
                       TextAnchor.TOP_LEFT, -angle,
                       CategoryLabelWidthType.RANGE, 0.50f )
               );
    }


    public static CategoryLabelPositions createDownRotationLabelPositions (
        double angle ) {
        return new CategoryLabelPositions (
                   new CategoryLabelPosition (
                       RectangleAnchor.BOTTOM, TextBlockAnchor.BOTTOM_RIGHT,
                       TextAnchor.BOTTOM_RIGHT, angle,
                       CategoryLabelWidthType.RANGE, 0.50f ),
                   new CategoryLabelPosition (
                       RectangleAnchor.TOP, TextBlockAnchor.TOP_LEFT,
                       TextAnchor.TOP_LEFT, angle,
                       CategoryLabelWidthType.RANGE, 0.50f ),
                   new CategoryLabelPosition (
                       RectangleAnchor.RIGHT, TextBlockAnchor.TOP_RIGHT,
                       TextAnchor.TOP_RIGHT, angle,
                       CategoryLabelWidthType.RANGE, 0.50f ),
                   new CategoryLabelPosition (
                       RectangleAnchor.LEFT, TextBlockAnchor.BOTTOM_LEFT,
                       TextAnchor.BOTTOM_LEFT, angle,
                       CategoryLabelWidthType.RANGE, 0.50f )
               );
    }


    private CategoryLabelPosition positionForAxisAtTop;


    private CategoryLabelPosition positionForAxisAtBottom;


    private CategoryLabelPosition positionForAxisAtLeft;


    private CategoryLabelPosition positionForAxisAtRight;


    public CategoryLabelPositions() {
        this.positionForAxisAtTop = new CategoryLabelPosition();
        this.positionForAxisAtBottom = new CategoryLabelPosition();
        this.positionForAxisAtLeft = new CategoryLabelPosition();
        this.positionForAxisAtRight = new CategoryLabelPosition();
    }


    public CategoryLabelPositions ( CategoryLabelPosition top,
                                    CategoryLabelPosition bottom, CategoryLabelPosition left,
                                    CategoryLabelPosition right ) {

        ParamChecks.nullNotPermitted ( top, "top" );
        ParamChecks.nullNotPermitted ( bottom, "bottom" );
        ParamChecks.nullNotPermitted ( left, "left" );
        ParamChecks.nullNotPermitted ( right, "right" );

        this.positionForAxisAtTop = top;
        this.positionForAxisAtBottom = bottom;
        this.positionForAxisAtLeft = left;
        this.positionForAxisAtRight = right;
    }


    public CategoryLabelPosition getLabelPosition ( RectangleEdge edge ) {
        CategoryLabelPosition result = null;
        if ( edge == RectangleEdge.TOP ) {
            result = this.positionForAxisAtTop;
        } else if ( edge == RectangleEdge.BOTTOM ) {
            result = this.positionForAxisAtBottom;
        } else if ( edge == RectangleEdge.LEFT ) {
            result = this.positionForAxisAtLeft;
        } else if ( edge == RectangleEdge.RIGHT ) {
            result = this.positionForAxisAtRight;
        }
        return result;
    }


    public static CategoryLabelPositions replaceTopPosition (
        CategoryLabelPositions base, CategoryLabelPosition top ) {

        ParamChecks.nullNotPermitted ( base, "base" );
        ParamChecks.nullNotPermitted ( top, "top" );

        return new CategoryLabelPositions ( top,
                                            base.getLabelPosition ( RectangleEdge.BOTTOM ),
                                            base.getLabelPosition ( RectangleEdge.LEFT ),
                                            base.getLabelPosition ( RectangleEdge.RIGHT ) );
    }


    public static CategoryLabelPositions replaceBottomPosition (
        CategoryLabelPositions base, CategoryLabelPosition bottom ) {

        ParamChecks.nullNotPermitted ( base, "base" );
        ParamChecks.nullNotPermitted ( bottom, "bottom" );

        return new CategoryLabelPositions (
                   base.getLabelPosition ( RectangleEdge.TOP ),
                   bottom,
                   base.getLabelPosition ( RectangleEdge.LEFT ),
                   base.getLabelPosition ( RectangleEdge.RIGHT ) );
    }


    public static CategoryLabelPositions replaceLeftPosition (
        CategoryLabelPositions base, CategoryLabelPosition left ) {

        ParamChecks.nullNotPermitted ( base, "base" );
        ParamChecks.nullNotPermitted ( left, "left" );

        return new CategoryLabelPositions (
                   base.getLabelPosition ( RectangleEdge.TOP ),
                   base.getLabelPosition ( RectangleEdge.BOTTOM ),
                   left,
                   base.getLabelPosition ( RectangleEdge.RIGHT ) );
    }


    public static CategoryLabelPositions replaceRightPosition (
        CategoryLabelPositions base, CategoryLabelPosition right ) {

        ParamChecks.nullNotPermitted ( base, "base" );
        ParamChecks.nullNotPermitted ( right, "right" );
        return new CategoryLabelPositions (
                   base.getLabelPosition ( RectangleEdge.TOP ),
                   base.getLabelPosition ( RectangleEdge.BOTTOM ),
                   base.getLabelPosition ( RectangleEdge.LEFT ),
                   right );
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof CategoryLabelPositions ) ) {
            return false;
        }

        CategoryLabelPositions that = ( CategoryLabelPositions ) obj;
        if ( !this.positionForAxisAtTop.equals ( that.positionForAxisAtTop ) ) {
            return false;
        }
        if ( !this.positionForAxisAtBottom.equals (
                    that.positionForAxisAtBottom ) ) {
            return false;
        }
        if ( !this.positionForAxisAtLeft.equals ( that.positionForAxisAtLeft ) ) {
            return false;
        }
        if ( !this.positionForAxisAtRight.equals ( that.positionForAxisAtRight ) ) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        int result = 19;
        result = 37 * result + this.positionForAxisAtTop.hashCode();
        result = 37 * result + this.positionForAxisAtBottom.hashCode();
        result = 37 * result + this.positionForAxisAtLeft.hashCode();
        result = 37 * result + this.positionForAxisAtRight.hashCode();
        return result;
    }
}
