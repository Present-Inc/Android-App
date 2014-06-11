package tv.present.android.controllers;

import android.app.Activity;

import java.io.Serializable;

/**
 * Present Controller Abstract Object
 * June 04, 2014
 * @author Kyle Weisel (kyle@present.tv)
 */

/**
 * Present Controller Abstract Object
 *
 * This object is an abstract superclass of all of the controllers for this application.  A
 * {@link tv.present.android.controllers.PController} extends an {@link android.app.Activity},
 * so that all controllers are also Activities.
 *
 * June 10, 2014
 *
 * @author  Kyle Weisel (kyle@present.tv)
 */
public abstract class PController extends Activity implements Serializable {

}