package tv.present.android;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import tv.present.android.util.PLog;
import tv.present.api.PUserContext;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LoginFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class LoginFragment extends Fragment implements OnClickListener, AsyncTaskResponse {

    private static String TAG = "tv.present.Present.LoginFragment";
    private final PLog logger = PLog.getInstance();
    private OnFragmentInteractionListener mListener;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
        this.logger.logDebug(this.TAG, "Called constructor of LoginFragment()");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button submit = (Button) view.findViewById(R.id.button);
        submit.setOnClickListener(this);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {

        // Get the entered username and password
        EditText username = (EditText) this.getActivity().findViewById(R.id.usernameField);
        EditText password = (EditText) this.getActivity().findViewById(R.id.passwordField);

        // Call the login controller
        LoginController loginController = new LoginController();
        loginController.delegate = this;
        loginController.execute(username.getText().toString(), password.getText().toString());
        Log.i("LEXMFJ", "Working");

    }

    @Override
    public void processAsyncResponse(PUserContext userContext) {

        if (userContext != null) {
            Toast toast = Toast.makeText(this.getActivity(), "Full username is : " + userContext.getUser().getFullName(), Toast.LENGTH_LONG);
            toast.show();
        }
        else {
            Toast toast = Toast.makeText(this.getActivity(), "You fucked up", Toast.LENGTH_LONG);
            toast.show();
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
