package tv.present.android.views;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import tv.present.android.R;
import tv.present.android.controllers.PController;
import tv.present.android.controllers.PEntryController;
import tv.present.android.models.PView;
import tv.present.android.util.PLog;

/**
 * Created by kbw28 on 6/10/14.
 */
public class PCreateAccountView extends PView implements View.OnFocusChangeListener, View.OnClickListener {

    private static final String TAG = "tv.present.android.views.CreateAccountView";
    private final int REQUEST_CODE_PROFILE_IMAGE_CAPTURE = 8;
    private final int REQUEST_CODE_PROFILE_IMAGE_GALLERY = 1001;

    private transient ImageButton choosePhotoButton;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PCreateAccountView newInstance(PController controller) {
        Bundle arguments = new Bundle();
        arguments.putSerializable("controller", controller);
        PLog.logDebug(TAG, "newInstance -> the controller put into fragment arguments was " + (controller == null ? "in fact" : "not") + " null");
        PLog.logDebug(TAG, "newInstance -> the controller got out of the fragment arguments was " + (arguments.get("controller") == null ? "in fact" : "not") + " null");
        PCreateAccountView createAccountView = new PCreateAccountView();
        createAccountView.setController(controller);
        createAccountView.setArguments(arguments);
        return createAccountView;
    }

    public PCreateAccountView() {
    }

    /**
     * Inflates and prepares this fragment view.  Within this method, the root layout element is
     * given focus so that the username field doesn't steal it.  OnFocusChange listeners are also
     * registered to both the username and password fields to toggle the keyboard when a field gains
     * or loses focus.
     * @param inflater is the LayoutInflater to use to inflate this layout.
     * @param container is a ViewGroup container that will display this inflated layout.
     * @param savedInstanceState is a Bundle of data that represents a previous instance state to
     *                           restore this view to.
     * @return a View that is the LoginFragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        PLog.logDebug(TAG, "Creating and configuring fragment view.");

        View rootView = inflater.inflate(R.layout.fragment_create_account, container, false);

        // This app is not compatible with API levels that require the SupportActionBar, therefore,
        // we will not use it.
        @SuppressLint("AppCompatMethod")
        ActionBar actionBar = this.getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        this.registerTouchListeners(rootView);

        // Set focus change listeners for the text fields
        final EditText emailAddressField = (EditText) rootView.findViewById(R.id.createEmailAddressField);
        final EditText usernameField = (EditText) rootView.findViewById(R.id.createUsernameField);
        final EditText passwordField = (EditText) rootView.findViewById(R.id.createPasswordField);
        final EditText fullNameField = (EditText) rootView.findViewById(R.id.createFullNameField);
        final EditText phoneNumberField = (EditText) rootView.findViewById(R.id.createPhoneNumberField);
        emailAddressField.setOnFocusChangeListener(this);
        usernameField.setOnFocusChangeListener(this);
        passwordField.setOnFocusChangeListener(this);
        fullNameField.setOnFocusChangeListener(this);
        phoneNumberField.setOnFocusChangeListener(this);

        // Set click listener for the profile photo button
        this.choosePhotoButton = (ImageButton) rootView.findViewById(R.id.selectProfilePictureButton);
        this.choosePhotoButton.setOnClickListener(this);

        Button nextButton = (Button) rootView.findViewById(R.id.createContinueButton);
        nextButton.setOnClickListener(this);

        // Don't initially focus on a field, give it to the RelativeLayout -- do we want this?
        emailAddressField.clearFocus();
        final RelativeLayout rootLayout = (RelativeLayout) rootView.findViewById(R.id.createAccountRootLayout);
        rootLayout.requestFocus();

        return rootView;

    }

    /**
     * This method is called when focus changes on either of the two input textboxes.
     * @param view is the view that called this method (ie: the EditText box).
     * @param hasFocus is a boolean value which specifies whether the view has focus or no longer does (ie: lost it).
     */
    public void onFocusChange(final View view, final boolean hasFocus) {

        PLog.logDebug(TAG, "Focus has changed to " + hasFocus + " on the " + view.toString() + " view.");

        final InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Shows the keyboard when a login field gets focus.
        if(hasFocus) {
            final EditText field = (EditText) view;
            inputMethodManager.showSoftInput(field, InputMethodManager.SHOW_FORCED);
        }
        /* Hides it on lost focus. - Don't do this
        else {
            EditText field = (EditText) view;
            view.setFocusable(false);
            view.setFocusableInTouchMode(false);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }*/

    }

    /**
     * Handles touch events.  If the calling View is not a textbox, we hide the keyboard.
     * @param view is the View that was touched.
     * @param event is the MotionEvent that occured.
     * @return false
     */
    public boolean onTouch(View view, MotionEvent event) {

        if (view instanceof EditText) {
            final InputMethodManager inputMethodManager = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_FORCED);
            return false;
        }
        else {
            this.hideKeyboard(this.controller);
            return false;
        }

    }

    /**
     * Handles clicks on buttons.
     * @param view is the View (Button) that was clicked.
     */
    public void onClick(View view) {

        View rootView = this.getView();

        // Remember: there is only one ImageButton on this view
        if (view instanceof ImageButton) {
            final Context theC = this.getActivity();

            //Dialog for either taking profile picture or choosing from gallery
            AlertDialog theChoice = new AlertDialog.Builder(theC)
                    .setTitle("Select Profile Photo")
                    .setPositiveButton("From Camera", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            //For cropping
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                            takePictureIntent.putExtra("crop", "true");
                            takePictureIntent.putExtra("aspectX", 0);
                            takePictureIntent.putExtra("aspectY", 0);
                            takePictureIntent.putExtra("outputX", 960);
                            takePictureIntent.putExtra("outputY", 960);
                            try { takePictureIntent.putExtra("return-data", true); }
                            catch (Exception e) { }

                            if (takePictureIntent.resolveActivity(theC.getPackageManager()) != null)
                                startActivityForResult(takePictureIntent, REQUEST_CODE_PROFILE_IMAGE_CAPTURE);
                        }
                    })
                    .setNegativeButton("From Gallery", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);

                            //For cropping
                            intent.putExtra("crop", "true");
                            intent.putExtra("aspectX", 1);
                            intent.putExtra("aspectY", 1);
                            intent.putExtra("outputX", 960);
                            intent.putExtra("outputY", 960);
                            try { intent.putExtra("return-data", true); }
                            catch (Exception e) { }

                            startActivityForResult(Intent.createChooser(intent, "Select Photo"),
                                    REQUEST_CODE_PROFILE_IMAGE_GALLERY);
                        }
                    })
                    .create();
            theChoice.show();
        }

        if (view instanceof Button) {

            // Get all of the data from the view
            String emailAddress = ((EditText) rootView.findViewById(R.id.createEmailAddressField)).getText().toString();
            String username = ((EditText) rootView.findViewById(R.id.createUsernameField)).getText().toString();
            String password = ((EditText) rootView.findViewById(R.id.createPasswordField)).getText().toString();
            String fullName = ((EditText) rootView.findViewById(R.id.createFullNameField)).getText().toString();
            String phoneNumber = ((EditText) rootView.findViewById(R.id.createPhoneNumberField)).getText().toString();

            ((PEntryController) this.controller).executeCreateAccount(username, password, emailAddress, fullName, phoneNumber);
        }
    }

    /**
     * Handles long clicks.
     * @param view is a View that was long clicked.
     * @return a boolean value for which I am unsure of the meaning.
     */
    public boolean onLongClick(View view) {
        return false;
    }

    /**
     * Handles the callback from another activity that was started for a result.
     * @param requestCode is the integer code of the requesting Activity.
     * @param resultCode is the integer result code from the Activity.
     * @param data is any returned Intent data from the finished Activity.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        PLog.logDebug(TAG, "Made it back to the activity result!");

        //Take Profile Picture with camera
        if (requestCode == REQUEST_CODE_PROFILE_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK)
        {
            final Bundle extras = data.getExtras();
            Bitmap photo = extras.getParcelable("data");
            photo = Bitmap.createScaledBitmap(photo, this.choosePhotoButton.getWidth(), this.choosePhotoButton.getHeight(), true);
            this.choosePhotoButton.setImageBitmap(photo);
        }

        //Choose Profile Photo from gallery. Same as above, but important to keep it separate
        else if (requestCode == REQUEST_CODE_PROFILE_IMAGE_GALLERY && resultCode == Activity.RESULT_OK)
        {
            final Bundle extras = data.getExtras();
            Bitmap photo = extras.getParcelable("data");
            photo = Bitmap.createScaledBitmap(photo, this.choosePhotoButton.getWidth(), this.choosePhotoButton.getHeight(), true);
            this.choosePhotoButton.setImageBitmap(photo);
        }

    }
}
