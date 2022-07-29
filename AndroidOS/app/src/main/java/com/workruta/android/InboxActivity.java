package com.workruta.android;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.workruta.android.ChatUtils.Conversations;
import com.workruta.android.Utils.CollectionLists;
import com.workruta.android.Utils.Constants;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class InboxActivity extends SharedCompatActivity {

    TextView headText;
    ScrollView scrollView;
    LinearLayout linearLayout, whiteFade;
    RelativeLayout emptyView;
    String myEmail, mySafeEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        myEmail = sharedPrefMngr.getMyEmail();
        mySafeEmail = functions.safeEmail(myEmail);

        headText = findViewById(R.id.headText);
        scrollView = findViewById(R.id.scrollView);
        linearLayout = findViewById(R.id.linearLayout);
        whiteFade = findViewById(R.id.whiteFade);
        emptyView = findViewById(R.id.emptyView);

        headText.setOnClickListener(v -> finish());
        whiteFade.setOnClickListener(v -> {
            return;
        });

        loadConversations();

    }

    @SuppressLint("InflateParams")
    private void loadConversations() {
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null, false);
        whiteFade.addView(layout);
        FirebaseDatabase dbInstance = FirebaseDatabase.getInstance();
        DatabaseReference conversationsDB = dbInstance.getReference(mySafeEmail + "/conversations");
        conversationsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                final int[] count = {0};
                whiteFade.setVisibility(View.GONE);
                final List<Conversations>[] list = new List[]{new ArrayList<>()};
                int dataCount = (int) snapshot.getChildrenCount();
                if(dataCount > 0){
                    scrollView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                        Conversations conversations = dataSnapshot.getValue(Conversations.class);
                        String userEmail = Objects.requireNonNull(conversations).getOther_user_email();
                        DatabaseReference userDB = dbInstance.getReference(userEmail + "/photoUrl");
                        userDB.get().addOnCompleteListener(task -> {
                            count[0]++;
                            String photoUrl = task.getResult().getValue(String.class);
                            conversations.setPhotoUrl(photoUrl);
                            list[0].add(conversations);
                            int last = count[0];
                            if(last == dataCount){
                                List<Conversations> list1 = sortConversations(list[0]);
                                displayConversations(list1);
                            }
                        });
                    }
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

    @SuppressLint({"InflateParams", "SimpleDateFormat"})
    private void displayConversations(List<Conversations> lists) {
        if(linearLayout.getChildCount() > 0)
            linearLayout.removeAllViews();
        for (Conversations conversations: lists){
            boolean fromMe = getMyId() == Integer.parseInt(conversations.getLatest_message().getUserFrom());
            boolean isRead = fromMe || conversations.getLatest_message().isIs_read();
            int drawableLeft = fromMe ? R.drawable.ic_square_up : R.drawable.ic_square_down;
            int userId = fromMe ? Integer.parseInt(conversations.getUserTo()) : Integer.parseInt(conversations.getUserFrom());
            int unseen = conversations.getUnseen();
            String userEmail = conversations.getOther_user_email();
            String name = conversations.getName();
            String photoUrl = Constants.www + functions.rawUrl(conversations.getPhotoUrl());
            String dateStr = functions.minify(conversations.getLatest_message().getDate());
            String message = conversations.getLatest_message().getMessage();
            RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.message_list, null, false);
            ImageView imageView = relativeLayout.findViewById(R.id.imageView);
            TextView nameTextView = relativeLayout.findViewById(R.id.nameTextView);
            TextView dateTextView = relativeLayout.findViewById(R.id.dateTextView);
            TextView messageTextView = relativeLayout.findViewById(R.id.messageTextView);
            TextView unseenTV = relativeLayout.findViewById(R.id.unseenTV);
            messageTextView.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, 0, 0, 0);
            imageLoader.displayImage(photoUrl, imageView);
            nameTextView.setText(name);
            dateTextView.setText(dateStr);
            messageTextView.setText(message);
            if(!isRead) {
                messageTextView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                if(unseen > 0) {
                    unseenTV.setText(String.valueOf(unseen));
                    unseenTV.setVisibility(View.VISIBLE);
                }
            }
            relativeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(context, MessageActivity.class);
                Bundle bundle1 = new Bundle();
                bundle1.putString("photoUrl", photoUrl);
                bundle1.putString("name", name);
                bundle1.putString("userEmail", userEmail);
                bundle1.putInt("userId", userId);
                intent.putExtras(bundle1);
                startActivity(intent);
            });
            linearLayout.addView(relativeLayout);
        }
    }

    private List<Conversations> sortConversations(List<Conversations> list){
        Collections.sort(list, (o1, o2) -> {
            Date a = o1.getLatest_message().convertDate();
            Date b = o2.getLatest_message().convertDate();
            boolean b1 = b.after(a);
            boolean b2 = a.after(b);
            if(b1 != b2){
                if(b1)
                    return 1;
                else
                    return -1;
            }
            return 0;
        });
        return list;
    }

}