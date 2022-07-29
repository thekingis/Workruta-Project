package com.workruta.android;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.workruta.android.ChatUtils.Conversations;
import com.workruta.android.NotificationUtils.Notifications;
import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.Functions;
import com.workruta.android.Utils.SharedPrefMngr;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DashboardAct extends SharedCompatActivity {

    Context context;
    ImageView photo, messageIcon, notificationIcon;
    String myPht, myName, locationName, myEmail, mySafeEmail;
    SharedPrefMngr sharedPrefMngr;
    Functions functions;
    TextView locationTextView, nameTextView, msgNumTextView, noteNumTextView;
    RelativeLayout mainView;
    LinearLayout menuLayout, blackFade, holderLayout;
    ScrollView menuView;
    View poppedView = null;
    boolean menuIsVisible, loggingOut, fingerEnabled;
    String[] menuLists;
    private int myId, unseenMsg, unseenNote;
    int[] menuIcons;
    Intent[] menuIntents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        context = this;
        sharedPrefMngr = new SharedPrefMngr(this);
        functions = new Functions();
        myId = sharedPrefMngr.getMyId();
        myPht = sharedPrefMngr.getMyPht();
        myName = sharedPrefMngr.getMyName();
        myEmail = sharedPrefMngr.getMyEmail();
        mySafeEmail = functions.safeEmail(myEmail);
        fingerEnabled = sharedPrefMngr.getFingerPrintEnabled();
        menuIsVisible = false;
        loggingOut = false;
        unseenMsg = 0;

        menuLists = new String[]{
                "Inbox",
                "Notifications",
                "Change Status",
                "Create a Ride",
                "Take a Ride",
                "Active Routes",
                "History",
                "Personal Data",
                "Transactions",
                "Support",
                "FAQ",
                "About Workruta",
                fingerEnabled ? "Disable Fingerprint Login" : "Enable Fingerprint Login",
                "Logout"
        };
        menuIcons = new int[]{
                R.drawable.inbox_mini,
                R.drawable.notification_mini,
                R.drawable.status_mini,
                R.drawable.create_route_mini,
                R.drawable.route_search_mini,
                R.drawable.prev_routes_mini,
                R.drawable.history_mini,
                R.drawable.data_mini,
                R.drawable.transaction_mini,
                R.drawable.support_mini,
                R.drawable.faq_mini,
                R.drawable.about_us_mini,
                R.drawable.finger_print_mini,
                R.drawable.logout_mini
        };
        menuIntents = new Intent[]{
                new Intent(context, ProfileActivity.class),
                new Intent(context, InboxActivity.class),
                new Intent(context, NotificationsActivity.class),
                new Intent(context, StatusActivity.class),
                new Intent(context, NewRouteActivity.class),
                new Intent(context, RouteSearchActivity.class),
                new Intent(context, PreviousRoutesActivity.class),
                new Intent(context, HistoryActivity.class),
                new Intent(context, PaymentsActivity.class),
                new Intent(context, TransactionsActivity.class),
                new Intent(context, SupportActivity.class),
                new Intent(context, FAQActivity.class),
                new Intent(context, AboutActivity.class)
        };

        Bundle bundle = new Bundle();
        bundle.putInt("userId", myId);
        menuIntents[0].putExtras(bundle);

        mainView = findViewById(R.id.mainView);
        blackFade = findViewById(R.id.blackFade);
        menuLayout = findViewById(R.id.menuLayout);
        photo = findViewById(R.id.photo);
        messageIcon = findViewById(R.id.messageIcon);
        notificationIcon = findViewById(R.id.notificationIcon);
        nameTextView = findViewById(R.id.nameTextView);
        msgNumTextView = findViewById(R.id.msgNumTextView);
        noteNumTextView = findViewById(R.id.noteNumTextView);
        locationTextView = findViewById(R.id.locationTextView);
        holderLayout = findViewById(R.id.holderLayout);
        menuView = findViewById(R.id.menuView);

        nameTextView.setText(myName);
        imageLoader.displayImage(myPht, photo);
        nameTextView.setOnClickListener((v) -> {
            if(poppedView != null){
                poppedView.setVisibility(View.GONE);
                return;
            }
            int mStart = 0, mStop = -menuView.getWidth();
            if(!menuIsVisible) {
                mStart = -menuView.getWidth();
                mStop = 0;
            }
            toggleMenuLayout(mStart, mStop);
            menuIsVisible = !menuIsVisible;
        });
        photo.setOnClickListener((v) -> {
            if(poppedView != null){
                poppedView.setVisibility(View.GONE);
                return;
            }
            int mStart = 0, mStop = -menuView.getWidth();
            if(!menuIsVisible) {
                mStart = -menuView.getWidth();
                mStop = 0;
            }
            toggleMenuLayout(mStart, mStop);
            menuIsVisible = !menuIsVisible;
        });
        blackFade.setOnClickListener((v) -> {
            return;
        });
        messageIcon.setOnClickListener((v) -> startActivity(new Intent(this, InboxActivity.class)));
        msgNumTextView.setOnClickListener((v) -> startActivity(new Intent(this, InboxActivity.class)));
        notificationIcon.setOnClickListener((v) -> startActivity(new Intent(this, NotificationsActivity.class)));
        noteNumTextView.setOnClickListener((v) -> startActivity(new Intent(this, NotificationsActivity.class)));

        listenForUpdates();
        setLocationName();
        setupMenu();
        setBoxClicks();
        setListenerForView(new View[] {holderLayout, messageIcon, msgNumTextView, notificationIcon, noteNumTextView});
        setOnLocationChangeListener(appCompatActivity -> setLocationName());
        setOnInfoChangeListener((key, object) -> {
            if(key.equals("profile")) {
                String name = object.getString("name");
                resetName(name);
            } else if(key.equals("photo")) {
                String photo = object.getString("photo");
                resetPhoto(photo);
            }
        });

        checkNotifiers();

    }

    private void checkNotifiers() {
        Fuel.INSTANCE.post(Constants.notifierURL, null).responseString(new Handler<String>() {
            @Override
            public void success(String s) {
                Log.e("REQUEST STATUS", "SUCCESS");
            }

            @Override
            public void failure(@NotNull FuelError fuelError) {
                Log.e("REQUEST STATUS", "FAILING ERROR");
                checkNotifiers();
            }
        });
    }

    private void listenForUpdates() {
        FirebaseDatabase dbInstance = FirebaseDatabase.getInstance();
        DatabaseReference conversationsDB = dbInstance.getReference(mySafeEmail + "/conversations");
        conversationsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                unseenMsg = 0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Conversations conversations = dataSnapshot.getValue(Conversations.class);
                    int userFrom = Integer.parseInt(Objects.requireNonNull(conversations).getLatest_message().getUserFrom());
                    if(userFrom != myId){
                        int u = conversations.getUnseen();
                        unseenMsg += u;
                    }
                }
                if(unseenMsg > 0){
                    msgNumTextView.setText(String.valueOf(unseenMsg));
                    msgNumTextView.setVisibility(View.VISIBLE);
                } else {
                    msgNumTextView.setVisibility(View.INVISIBLE);
                    msgNumTextView.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        DatabaseReference notificationsDB = dbInstance.getReference("notifications/" + myId);
        notificationsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                unseenNote = 0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Notifications notifications = dataSnapshot.getValue(Notifications.class);
                    int unseen = notifications.getUnseen();
                    unseenNote += unseen;
                }
                if(unseenNote > 0){
                    noteNumTextView.setText(String.valueOf(unseenNote));
                    noteNumTextView.setVisibility(View.VISIBLE);
                } else {
                    noteNumTextView.setVisibility(View.INVISIBLE);
                    noteNumTextView.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void resetPhoto(String photo) {
        imageLoader.displayImage(photo, this.photo);
        LinearLayout linearLayout = (LinearLayout) menuLayout.getChildAt(0);
        ImageView menuIconView = linearLayout.findViewById(R.id.menuIconView);
        imageLoader.displayImage(photo, menuIconView);
    }

    private void resetName(String name) {
        myName = name;
        nameTextView.setText(name);
    }

    private void setBoxClicks() {
        for(int i = 0; i < holderLayout.getChildCount(); i++){
            ConstraintLayout constraintLayout = (ConstraintLayout) holderLayout.getChildAt(i);
            setListenerForView(new View[] {constraintLayout});
            for (int x = 0; x < constraintLayout.getChildCount(); x++){
                LinearLayout linearLayout = (LinearLayout) constraintLayout.getChildAt(x);
                setListenerForView(new View[] {linearLayout});
                for (int a = 0; a < linearLayout.getChildCount(); a++){
                    RelativeLayout relativeLayout = (RelativeLayout) linearLayout.getChildAt(a);
                    setListenerForView(new View[] {relativeLayout});
                    relativeLayout.setOnClickListener((v) -> {
                        int tag = Integer.parseInt(v.getTag().toString());
                        if(tag == 14){
                            logoutAlert();
                            return;
                        }
                        Intent intent = menuIntents[tag];
                        startActivity(intent);
                    });
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setListenerForView(View[] views){
        for(int i = 0; i < views.length; i++) {
            View view = views[i];
            view.setOnTouchListener((v, event) -> {
                if (menuIsVisible) {
                    int mStart = 0, mStop = -menuView.getWidth();
                    toggleMenuLayout(mStart, mStop);
                    menuIsVisible = !menuIsVisible;
                    return true;
                }
                return false;
            });
        }
    }

    @SuppressLint("InflateParams")
    private void setupMenu() {
        for(int i = 0; i < menuLists.length + 1; i++){
            int index = i - 1;
            LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_list, null);
            ImageView menuIconView = linearLayout.findViewById(R.id.menuIconView);
            TextView menuTextView = linearLayout.findViewById(R.id.menuTextView);
            if(i == 0){
                imageLoader.displayImage(myPht, menuIconView);
                menuTextView.setText(myName);
            } else {
                int menuIcon = menuIcons[index];
                String menuText = menuLists[index];
                menuIconView.setVisibility(View.GONE);
                menuTextView.setText(menuText);
                menuTextView.setCompoundDrawablesWithIntrinsicBounds(menuIcon, 0, 0, 0);
            }
            int finalI = i;
            linearLayout.setOnClickListener((v) -> {
                if(menuIsVisible) {
                    int mStart = 0, mStop = -menuView.getWidth();
                    toggleMenuLayout(mStart, mStop);
                    menuIsVisible = !menuIsVisible;
                }
                if(finalI < menuLists.length) {
                    if(finalI == menuLists.length - 1){
                        setupFingerprint(menuTextView);
                        return;
                    }
                    Intent intent = menuIntents[finalI];
                    if(finalI == 0){
                        Bundle pageParams = new Bundle();
                        pageParams.putInt("userID", myId);
                        intent.putExtras(pageParams);
                    }
                    startActivity(intent);
                    return;
                }
                logoutAlert();
            });
            menuLayout.addView(linearLayout);
        }
    }

    @SuppressLint("InflateParams")
    private void setupFingerprint(TextView mTextView) {
        boolean fingerEnabled = sharedPrefMngr.getFingerPrintEnabled();
        poppedView = blackFade;
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String negTxt = getResources().getString(R.string.cancel),
                posTxt = getResources().getString(R.string.enable),
                text = "Disable Fingerprint Login",
                alertTxt = getResources().getString(R.string.enable_finger);
        if(fingerEnabled){
            text = "Enable Fingerprint Login";
            posTxt = getResources().getString(R.string.disable);
            alertTxt = getResources().getString(R.string.disable_finger);
        }
        LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
        TextView textView = alertLayout.findViewById(R.id.textView);
        Button negativeBtn = alertLayout.findViewById(R.id.negative);
        Button positiveBtn = alertLayout.findViewById(R.id.positive);
        textView.setText(alertTxt);
        negativeBtn.setText(negTxt);
        positiveBtn.setText(posTxt);
        negativeBtn.setOnClickListener((v) -> {
            blackFade.setVisibility(View.GONE);
            poppedView = null;
        });
        String finalText = text;
        String finalTxt = "Fingerprint " + posTxt + "d";
        positiveBtn.setOnClickListener((v) -> {
            sharedPrefMngr.saveFingerPrint(!fingerEnabled, myId);
            mTextView.setText(finalText);
            blackFade.setVisibility(View.GONE);
            poppedView = null;
            Toast.makeText(context, finalTxt, Toast.LENGTH_LONG).show();
        });
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint("InflateParams")
    private void logoutAlert() {
        poppedView = blackFade;
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String negTxt = getResources().getString(R.string.cancel),
                posTxt = getResources().getString(R.string.logout),
                alertTxt = getResources().getString(R.string.logout_text);
        LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
        TextView textView = alertLayout.findViewById(R.id.textView);
        Button negativeBtn = alertLayout.findViewById(R.id.negative);
        Button positiveBtn = alertLayout.findViewById(R.id.positive);
        textView.setText(alertTxt);
        negativeBtn.setText(negTxt);
        positiveBtn.setText(posTxt);
        negativeBtn.setOnClickListener((v) -> {
            blackFade.setVisibility(View.GONE);
            poppedView = null;
        });
        positiveBtn.setOnClickListener((v) -> logoutUser());
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

    @SuppressLint("InflateParams")
    private void logoutUser() {
        loggingOut = true;
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String alertTxt = getResources().getString(R.string.logging_out);
        LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.requesting_layout, null);
        TextView textView = alertLayout.findViewById(R.id.textView);
        textView.setText(alertTxt);
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
        sharedPrefMngr.logOut();
        Intent intent = new Intent(this, StartAct.class);
        new android.os.Handler().postDelayed(() -> {
            startActivity(intent);
            finish();
        }, 2000);
    }

    private void toggleMenuLayout(int mStart, int mStop) {
        ValueAnimator animator = ValueAnimator.ofInt(mStart, mStop);
        animator.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) menuView.getLayoutParams();
            params.leftMargin = (Integer) animation.getAnimatedValue();
            menuView.requestLayout();
        });
        animator.setDuration(300);
        animator.start();
        /*Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                menuView.setLayoutParams(params);
            }
        };*/
    }

    private void setLocationName() {
        locationName = getLocationName();
        String locName = locationName;
        int textColor = ContextCompat.getColor(context, R.color.black);
        if(locationName == null){
            textColor = ContextCompat.getColor(context, R.color.ash);
            locName = getResources().getString(R.string.fetching_address);
        }
        locationTextView.setTextColor(textColor);
        locationTextView.setText(locName);
    }

    @Override
    @SuppressLint("InflateParams")
    public void onBackPressed() {
        if(loggingOut)
            return;
        if(poppedView != null){
            poppedView.setVisibility(View.GONE);
            poppedView = null;
            return;
        }
        if(menuIsVisible) {
            int mStart = 0, mStop = -menuView.getWidth();
            toggleMenuLayout(mStart, mStop);
            menuIsVisible = !menuIsVisible;
            return;
        }
        poppedView = blackFade;
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        String negTxt = getResources().getString(R.string.cancel),
                posTxt = getResources().getString(R.string.quit_app),
                alertTxt = getResources().getString(R.string.quit_text);
        LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
        TextView textView = alertLayout.findViewById(R.id.textView);
        Button negativeBtn = alertLayout.findViewById(R.id.negative);
        Button positiveBtn = alertLayout.findViewById(R.id.positive);
        textView.setText(alertTxt);
        negativeBtn.setText(negTxt);
        positiveBtn.setText(posTxt);
        negativeBtn.setOnClickListener((v) -> {
            blackFade.setVisibility(View.GONE);
            poppedView = null;
        });
        positiveBtn.setOnClickListener((v) -> android.os.Process.killProcess(android.os.Process.myPid()));
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

}