package tv.present.android.viewgroups;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import tv.present.android.util.PLog;

public class PAspectFrameLayout extends FrameLayout {

    private static final String TAG = "tv.present.android.viewgroups.PAspectFrameLayout";

    private double targetAspect;

    public PAspectFrameLayout(Context context) {
        super(context);
        this.targetAspect = -1;
    }

    public PAspectFrameLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.targetAspect = -1;
    }

    /**
     * Sets the desired aspect ratio.  The value is <code>width / height</code>.
     */
    public void setAspectRatio(final double aspectRatio) {

        if (aspectRatio < 0) {
            throw new IllegalArgumentException("The aspect ratio provided was less than 0!");
        }

        PLog.logDebug(TAG, "setAspectRatio() -> Setting aspect ratio to " + aspectRatio + " (was " + this.targetAspect + ")");
        if (this.targetAspect != aspectRatio) {
            this.targetAspect = aspectRatio;
            requestLayout();
        }

    }

    @Override
    protected void onMeasure (int widthSpec, int heightSpec) {

//        PLog.logDebug(TAG, "onMeasure -> Target: " + this.targetAspect + " with height = " + MeasureSpec.toString(heightSpec) + " and width = " + MeasureSpec.toString(widthSpec) + ".");

        // Check if the target aspect ratio is less than 0.  If it is, we can conclude that it was
        // never actually set.
        if (this.targetAspect < 0) {

            // Get initial properties
            int initialHeight = MeasureSpec.getSize(heightSpec);
            int initialWidth = MeasureSpec.getMode(widthSpec);

            // Ignore the padding
            // factor the padding out
            final int horizontalPadding = this.getPaddingLeft() + this.getPaddingRight();
            final int verticalPadding = this.getPaddingTop() + this.getPaddingBottom();
            initialWidth -= horizontalPadding;
            initialHeight -= verticalPadding;

            double viewAspectRatio = (double) initialWidth / initialHeight;
            double aspectDiff = this.targetAspect / viewAspectRatio - 1;

            /*
             * Check to see if we're already really close to the target aspect ratio.  In effort to
             * not risk switching from a non-scaled resolution to a scaled resolution due
             */
            if (Math.abs(aspectDiff) < 0.01) {
                PLog.logDebug(TAG, "aspect ratio is good (target=" + targetAspect + ", view=" + initialWidth + "x" + initialHeight + ")");
            }
            else {

                if (aspectDiff > 0) {
                    // limited by narrow width; restrict height
                    initialHeight = (int) (initialWidth / targetAspect);
                }
                else {
                    // limited by short height; restrict width
                    initialWidth = (int) (initialHeight * targetAspect);
                }

                PLog.logDebug(TAG, "New size = " + initialWidth + "x" + initialHeight + " + padding " + horizontalPadding + "x" + verticalPadding);
                initialWidth += horizontalPadding;
                initialHeight += verticalPadding;
                widthSpec = MeasureSpec.makeMeasureSpec(initialWidth, MeasureSpec.EXACTLY);
                heightSpec = MeasureSpec.makeMeasureSpec(initialHeight, MeasureSpec.EXACTLY);

            }

            super.onMeasure(widthSpec, heightSpec);

        }

    }




}
