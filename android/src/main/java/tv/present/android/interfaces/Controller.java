package tv.present.android.interfaces;

import android.view.MotionEvent;
import android.view.View;

/**
 *  This Controller is an abstract class that all Controllers will inherit from.
 *  @author Kyle Weisel (kyle@present.tv)
 */
public interface Controller {
    public void handleClick(View view);
    public void handleTouch(View view, MotionEvent event);
}
