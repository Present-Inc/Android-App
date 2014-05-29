package tv.present.android.presenters;

import tv.present.android.views.NotificationsFragment;

/**
 * Created by kbw28 on 5/28/14.
 */
public class LoginPresenter {

    private static final String TAG = "tv.present.android.controllers.LoginPresenter";
    private final int REQUEST_CODE_PROFILE_IMAGE_CAPTURE = 8;
    private NotificationsFragment subject;

    public LoginPresenter(NotificationsFragment fragment) {
        this.subject = fragment;
    }

    public void doLogin(String username, String password) {


    }


}
