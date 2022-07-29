package com.workruta.android;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.workruta.android.NotificationUtils.Notifications;
import com.workruta.android.Utils.Constants;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import kotlin.Pair;

import static com.workruta.android.PreviousRoutesActivity.setRouteActive;

public class NotificationsActivity extends SharedCompatActivity {

    TextView headText;
    ScrollView scrollView;
    LinearLayout linearLayout, whiteFade;
    RelativeLayout emptyView;
    int myId;
    JSONObject object;
    FirebaseDatabase dbInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        myId = getMyId();
        object = new JSONObject();
        dbInstance = FirebaseDatabase.getInstance();

        headText = findViewById(R.id.headText);
        scrollView = findViewById(R.id.scrollView);
        linearLayout = findViewById(R.id.linearLayout);
        whiteFade = findViewById(R.id.whiteFade);
        emptyView = findViewById(R.id.emptyView);

        headText.setOnClickListener(v -> finish());
        whiteFade.setOnClickListener(v -> {
            return;
        });

        loadNotifications();

    }

    @SuppressLint("InflateParams")
    private void loadNotifications() {
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null, false);
        whiteFade.addView(layout);
        DatabaseReference notificationsDB = dbInstance.getReference("notifications/" + myId);
        notificationsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                whiteFade.setVisibility(View.GONE);
                int dataCount = (int) snapshot.getChildrenCount();
                if(dataCount > 0) {
                    List<Notifications> lists = new ArrayList<>();
                    scrollView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String key = dataSnapshot.getKey();
                        Notifications notifications = dataSnapshot.getValue(Notifications.class);
                        Objects.requireNonNull(notifications).setKey(key);
                        lists.add(notifications);
                    }
                    Collections.reverse(lists);
                    displayNotifications(lists);
                } else {
                    scrollView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("InflateParams")
    private void displayNotifications(List<Notifications> lists) {
        if(linearLayout.getChildCount() > 0)
            linearLayout.removeAllViews();
        for(Notifications notifications : lists){
            String key = notifications.getKey();
            String dataId = notifications.getDataId();
            String dataType = notifications.getDataType();
            String date = notifications.getDate();
            String extraId = notifications.getExtraId();
            String userFrom = notifications.getUserFrom();
            Spanned text = notifications.getText();
            String dateStr = functions.minify(date);
            int image = notifications.getImage();
            int unseen = notifications.getUnseen();
            boolean seen = unseen == 0;
            RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.note_list, null, false);
            LinearLayout container = relativeLayout.findViewById(R.id.container);
            ImageView imageView = relativeLayout.findViewById(R.id.imageView);
            TextView textView = relativeLayout.findViewById(R.id.textView);
            TextView dateTextView = relativeLayout.findViewById(R.id.dateTextView);
            imageView.setBackgroundResource(image);
            textView.setText(text);
            dateTextView.setText(dateStr);
            if(!seen)
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.mainColorLight));
            if(dataType.equals("followRoute") && !seen){
                LinearLayout optionLayout = relativeLayout.findViewById(R.id.optionLayout);
                Button acceptBtn = relativeLayout.findViewById(R.id.acceptBtn);
                Button rejectBtn = relativeLayout.findViewById(R.id.rejectBtn);
                ImageView acceptBtnImg = relativeLayout.findViewById(R.id.acceptBtnImg);
                ImageView rejectBtnImg = relativeLayout.findViewById(R.id.rejectBtnImg);
                optionLayout.setVisibility(View.VISIBLE);
                acceptBtn.setOnClickListener(v -> {
                    if(!object.has(key)) {
                        try {
                            sendAction(acceptBtn, acceptBtnImg, relativeLayout, userFrom, key, dataId, extraId, "accepted");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                rejectBtn.setOnClickListener(v -> {
                    if(!object.has(key)) {
                        try {
                            sendAction(rejectBtn, rejectBtnImg, relativeLayout, userFrom, key, dataId, extraId, "rejected");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                relativeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int height = relativeLayout.getHeight();
                        int halfHeight = height / 2;
                        int newHeight = height + halfHeight;
                        container.setMinimumHeight(newHeight);
                        relativeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
            } else if(!dataType.equals("followRoute")) {
                relativeLayout.setOnClickListener(v -> {
                    Intent intent = notifications.getIntent(context);
                    startActivity(intent);
                    updateSeen(key);
                    container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                });
            }
            linearLayout.addView(relativeLayout);
        }
    }

    private void sendAction(Button button, ImageView imageView, RelativeLayout relativeLayout, String userTo,
                            String key, String dataId, String extraId, String action) throws JSONException {
        object.put(key, true);
        button.setVisibility(View.INVISIBLE);
        LinearLayout container = relativeLayout.findViewById(R.id.container);
        LinearLayout optionLayout = relativeLayout.findViewById(R.id.optionLayout);
        imageView.setVisibility(View.VISIBLE);
        new android.os.Handler().postDelayed(() -> {
            List<Pair<String, String>> parameters = new ArrayList<>();
            parameters.add(new Pair<>("key", key));
            parameters.add(new Pair<>("user", String.valueOf(myId)));
            parameters.add(new Pair<>("userTo", userTo));
            parameters.add(new Pair<>("dataId", dataId));
            parameters.add(new Pair<>("extraId", extraId));
            parameters.add(new Pair<>("todo", action));
            parameters.add(new Pair<>("action", "followAction"));
            Fuel.INSTANCE.post(Constants.actionsUrl, parameters).responseString(new Handler<String>() {
                @Override
                public void success(String s) {
                    runOnUiThread(() -> {
                        try {
                            JSONObject object = new JSONObject(s);
                            boolean noError = object.getBoolean("noError");
                            if(noError) {
                                int height = container.getHeight();
                                int newHeight = (height / 3) * 2;
                                optionLayout.setVisibility(View.GONE);
                                container.setMinimumHeight(newHeight);
                                container.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                            } else {
                                object.remove(key);
                                button.setVisibility(View.VISIBLE);
                                imageView.setVisibility(View.INVISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void failure(@NotNull FuelError fuelError) {
                    runOnUiThread(() -> {
                        object.remove(key);
                        button.setVisibility(View.VISIBLE);
                        imageView.setVisibility(View.INVISIBLE);
                    });
                }
            });
        }, 1000);
    }

    private void updateSeen(String key) {
        DatabaseReference notificationPath = dbInstance.getReference("notifications/" + myId + "/" + key + "/unseen");
        notificationPath.setValue(0);
    }
}