package com.workruta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.workruta.android.Utils.StringUtils;

import static com.workruta.android.ForgotPassActivity.changeViewPage;
import static com.workruta.android.ForgotPassActivity.getVerificationCode;

public class FPEmailFragment extends Fragment {

    Context context;
    RelativeLayout mainView;
    EditText emailET;
    TextView switchPage, submitBtn;
    @SuppressLint("StaticFieldLeak")
    static TextView errorLog;

    public FPEmailFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fpemail, container, false);

        mainView = view.findViewById(R.id.mainView);
        emailET = view.findViewById(R.id.emailET);
        errorLog = view.findViewById(R.id.errorLog);
        switchPage = view.findViewById(R.id.switchPage);
        submitBtn = view.findViewById(R.id.submit);

        switchPage.setOnClickListener((v) -> changeViewPage(2, true));
        submitBtn.setOnClickListener((v) -> serializeData());

        setupUI(mainView);

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void serializeData(){
        errorLog.setText("");
        String email = emailET.getText().toString();
        if(StringUtils.isEmpty(email)){
            errorLog.setText("Please fill in Your Email address");
            return;
        }
        getVerificationCode(context, email, "email");
    }

    public static void displayError(String errorMsg){
        errorLog.setText(errorMsg);
    }

    public void toggleSoftKeyboard(View view, boolean hide) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(hide)
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        else
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener((v, event) -> {
                toggleSoftKeyboard(v, true);
                emailET.clearFocus();
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }
}