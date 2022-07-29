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

import static com.workruta.android.ForgotPassActivity.changePassword;
import static com.workruta.android.ForgotPassActivity.getVerificationCode;

public class FPChangePasswordFragment extends Fragment {

    Context context;
    RelativeLayout mainView;
    EditText passwordET, conPasswordET;
    TextView submitBtn;
    @SuppressLint("StaticFieldLeak")
    static TextView errorLog;

    public FPChangePasswordFragment(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fpchange_password, container, false);

        mainView = view.findViewById(R.id.mainView);
        passwordET = view.findViewById(R.id.passwordET);
        conPasswordET = view.findViewById(R.id.conPasswordET);
        submitBtn = view.findViewById(R.id.submit);
        errorLog = view.findViewById(R.id.errorLog);

        submitBtn.setOnClickListener((v) -> serializeData());
        setupUI(mainView);

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void serializeData(){
        errorLog.setText("");
        String password = passwordET.getText().toString(),
                conPassword = conPasswordET.getText().toString();
        if(StringUtils.isEmpty(password) || StringUtils.isEmpty(conPassword)){
            errorLog.setText("Please fill in all fields");
            return;
        }
        changePassword(context, password, conPassword);
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
                passwordET.clearFocus();
                conPasswordET.clearFocus();
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