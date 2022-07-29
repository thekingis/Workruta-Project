package com.workruta.android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.workruta.android.ChatUtils.Chats;
import com.workruta.android.ChatUtils.Conversations;
import com.workruta.android.ChatUtils.LatestMessage;
import com.workruta.android.Utils.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MessageActivity extends SharedCompatActivity {

    TextView headText, textView, scrollToView;
    ImageView imageView;
    ScrollView scrollView;
    LinearLayout linearLayout, whiteFade;
    EditText editText;
    RelativeLayout mainView, sendBtn;
    String name, myName, userSafeEmail, photoUrl, myEmail, mySafeEmail, conversationId, databasePath;
    int myId, userId, extraCount;
    boolean chatExists, scrolledToBottom, firstScroll, toUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        myId = getMyId();
        myEmail = sharedPrefMngr.getMyEmail();
        myName = sharedPrefMngr.getMyName();
        mySafeEmail = functions.safeEmail(myEmail);
        scrolledToBottom = true;
		firstScroll = true;
        toUpdate = true;
        extraCount = 0;

        Bundle bundle = getIntent().getExtras();
        name = bundle.getString("name");
        userSafeEmail = bundle.getString("userEmail");
        photoUrl = bundle.getString("photoUrl");
        userId = bundle.getInt("userId");

        mainView = findViewById(R.id.mainView);
        headText = findViewById(R.id.headText);
        textView = findViewById(R.id.textView);
        scrollToView = findViewById(R.id.scrollToView);
        imageView = findViewById(R.id.imageView);
        scrollView = findViewById(R.id.scrollView);
        linearLayout = findViewById(R.id.linearLayout);
        whiteFade = findViewById(R.id.whiteFade);
        editText = findViewById(R.id.editText);
        sendBtn = findViewById(R.id.sendBtn);

        imageLoader.displayImage(photoUrl, imageView);
        textView.setText(name);

        headText.setOnClickListener(v -> finish());
        imageView.setOnClickListener(v -> visitProfile());
        textView.setOnClickListener(v -> visitProfile());
        sendBtn.setOnClickListener(v -> sendMessage());
        whiteFade.setOnClickListener(v -> {
            return;
        });
        scrollToView.setOnClickListener(v -> scrollToViewBottom());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                int scrllH = scrollView.getHeight(), viewH = linearLayout.getHeight();
                int scrllTo = viewH - scrllH;
                scrolledToBottom = scrllTo == scrollY;
                if(scrolledToBottom) {
                    extraCount = 0;
                    scrollToView.setVisibility(View.GONE);
                    if(databasePath != null)
                        updateRead();
                }
            });
        }

        checkChatExists();
        setupUI(mainView);

    }

    private void scrollToViewBottom() {
        extraCount = 0;
        scrollToView.setVisibility(View.GONE);
        int lastChild = linearLayout.getChildCount();
        if(lastChild != 0){
            lastChild -= 1;
            RelativeLayout relativeLayout = (RelativeLayout) linearLayout.getChildAt(lastChild);
            int scrollTo = relativeLayout.getBottom();
            scrollView.smoothScrollTo(0, scrollTo);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void sendMessage() {
        String message = editText.getText().toString();
        if(StringUtils.isEmpty(message))
            return;
        if(!chatExists){
            sendNewMessage();
            return;
        }
        editText.setText("");
        String date  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Chats chat = new Chats(message, date, conversationId, false, myName, mySafeEmail, String.valueOf(myId), String.valueOf(userId));
        RelativeLayout relativeLayout = setChatBubble(chat, false);
        setScrollTo(relativeLayout);
        linearLayout.addView(relativeLayout);
        setupUI(mainView);
        DatabaseReference messagesDB = FirebaseDatabase.getInstance().getReference(conversationId + "/messages");
        messagesDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Map<String, Chats> messages = new HashMap<>();
                int indexM = 0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Chats chats = dataSnapshot.getValue(Chats.class);
                    messages.put(String.valueOf(indexM), chats);
                    indexM++;
                }
                messages.put(String.valueOf(indexM), chat);
                messagesDB.setValue(messages);
                LatestMessage latestMessage = new LatestMessage(date, false, message, String.valueOf(myId), String.valueOf(userId));
                DatabaseReference fromEmailDB = FirebaseDatabase.getInstance().getReference(mySafeEmail + "/conversations");
                fromEmailDB.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        int index = 0;
                        int indexC = 0;
                        boolean gottenIndex = false;
                        Conversations conversations = null;
                        Map<String, Conversations> allConversations = new HashMap<>();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String conversationID = dataSnapshot.child("id").getValue(String.class);
                            Conversations conversations1 = dataSnapshot.getValue(Conversations.class);
                            allConversations.put(String.valueOf(indexC), conversations1);
                            if (Objects.equals(conversationID, conversationId)) {
                                gottenIndex = true;
                                conversations = conversations1;
                            }
                            indexC++;
                            if (!gottenIndex)
                                index++;
                        }
                        if (!(conversations == null)) {
                            conversations.setLatest_message(latestMessage);
                            allConversations.put(String.valueOf(index), conversations);
                        } else {
                            conversations = new Conversations(conversationId, latestMessage, name, userSafeEmail, String.valueOf(myId), String.valueOf(userId), 0);
                            allConversations.put(String.valueOf(indexC), conversations);
                        }
                        fromEmailDB.setValue(allConversations);
                        DatabaseReference toEmailDB = FirebaseDatabase.getInstance().getReference( userSafeEmail+ "/conversations");
                        toEmailDB.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                int index = 0;
                                int indexC = 0;
                                boolean gottenIndex = false;
                                Conversations conversations = null;
                                Map<String, Conversations> allConversations = new HashMap<>();
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String conversationID = dataSnapshot.child("id").getValue(String.class);
                                    Conversations conversations1 = dataSnapshot.getValue(Conversations.class);
                                    allConversations.put(String.valueOf(indexC), conversations1);
                                    if (Objects.equals(conversationID, conversationId)) {
                                        gottenIndex = true;
                                        conversations = conversations1;
                                    }
                                    indexC++;
                                    if (!gottenIndex)
                                        index++;
                                }
                                if (!(conversations == null)) {
                                    int unseen = conversations.getUnseen();
                                    unseen++;
                                    conversations.setUnseen(unseen);
                                    conversations.setLatest_message(latestMessage);
                                    allConversations.put(String.valueOf(index), conversations);
                                } else {
                                    conversations = new Conversations(conversationId, latestMessage, myName, mySafeEmail, String.valueOf(myId), String.valueOf(userId), 1);
                                    allConversations.put(String.valueOf(indexC), conversations);
                                }
                                toEmailDB.setValue(allConversations);
                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void sendNewMessage() {
        chatExists = true;
        conversationId = createNewId();
        String message = editText.getText().toString();
        editText.setText("");
        String date  = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        Chats chat = new Chats(message, date, conversationId, false, myName, mySafeEmail, String.valueOf(myId), String.valueOf(userId));
        RelativeLayout relativeLayout = setChatBubble(chat, false);
        setScrollTo(relativeLayout);
        linearLayout.addView(relativeLayout);
        setupUI(mainView);
        LatestMessage latestMessage = new LatestMessage(date, false, message, String.valueOf(myId), String.valueOf(userId));
        Conversations conversationsFrom = new Conversations(conversationId, latestMessage, name, userSafeEmail, String.valueOf(myId), String.valueOf(userId), 0);
        Conversations conversationsTo = new Conversations(conversationId, latestMessage, myName, mySafeEmail, String.valueOf(myId), String.valueOf(userId), 1);
        DatabaseReference cToDB = FirebaseDatabase.getInstance().getReference(userSafeEmail + "/conversations");
        DatabaseReference cFromDB = FirebaseDatabase.getInstance().getReference(mySafeEmail + "/conversations");
        cToDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Map<String, Conversations> conversationsMap = new HashMap<>();
                int indexM = 0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Conversations conversations = dataSnapshot.getValue(Conversations.class);
                    conversationsMap.put(String.valueOf(indexM), conversations);
                    indexM++;
                }
                conversationsMap.put(String.valueOf(indexM), conversationsTo);
                cToDB.setValue(conversationsMap);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        cFromDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                Map<String, Conversations> conversationsMap = new HashMap<>();
                int indexM = 0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Conversations conversations = dataSnapshot.getValue(Conversations.class);
                    conversationsMap.put(String.valueOf(indexM), conversations);
                    indexM++;
                }
                databasePath = mySafeEmail + "/conversations/" + indexM;
                conversationsMap.put(String.valueOf(indexM), conversationsFrom);
                cFromDB.setValue(conversationsMap);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        DatabaseReference messagesDB = FirebaseDatabase.getInstance().getReference(conversationId + "/messages");
        Map<String, Chats> chatsMap = new HashMap<>();
        chatsMap.put("0", chat);
        messagesDB.setValue(chatsMap);
        getConversations();
    }

    private void setScrollTo(RelativeLayout relativeLayout){
        relativeLayout.post(() -> {
            int scrollTo = relativeLayout.getBottom();
			if(firstScroll)
				scrollView.scrollTo(0, scrollTo);
			else
				scrollView.smoothScrollTo(0, scrollTo);
			firstScroll = false;
        });
    }

    private String createNewId() {
        long numOfSecs = TimeUnit.MILLISECONDS.toSeconds(new Date().getTime());
        return "conversation_" + myId + "_" + userId + "_" + numOfSecs;
    }

    private void visitProfile() {
        Intent intent = new Intent(context, ProfileActivity.class);
        Bundle bundle1 = new Bundle();
        bundle1.putInt("userId", userId);
        intent.putExtras(bundle1);
        startActivity(intent);
    }

    @SuppressLint("InflateParams")
    private void checkChatExists() {
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null, false);
        whiteFade.addView(layout);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                int index = 0;
                DataSnapshot dataSnapshot = snapshot.child(mySafeEmail + "/conversations");
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    Conversations conversations = data.getValue(Conversations.class);
                    String otherUserEmail = Objects.requireNonNull(conversations).getOther_user_email();
                    if(otherUserEmail.equals(userSafeEmail)){
                        chatExists = true;
                        conversationId = Objects.requireNonNull(conversations).getId();
                        databasePath = mySafeEmail + "/conversations/" + index;
                        updateRead();
                        getConversations();
                        break;
                    }
                    index++;
                }
                if(!chatExists)
                    whiteFade.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void updateRead() {
        if(toUpdate) {
            String unseenPath = databasePath + "/unseen";
            String path = databasePath + "/latest_message/is_read";
            DatabaseReference unseenReference = FirebaseDatabase.getInstance().getReference(unseenPath);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
            unseenReference.setValue(0);
            reference.setValue(true);
        }
    }

    private void getConversations() {
        DatabaseReference messagesDB = FirebaseDatabase.getInstance().getReference(conversationId + "/messages");
        messagesDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                whiteFade.setVisibility(View.GONE);
                if(linearLayout.getChildCount() > 0)
                    linearLayout.removeAllViews();
                int count = 0;
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    count++;
                    Chats chats = dataSnapshot.getValue(Chats.class);
                    RelativeLayout relativeLayout = setChatBubble(Objects.requireNonNull(chats), true);
                    if(snapshot.getChildrenCount() == count) {
                        int userFrom = Integer.parseInt(chats.getUserFrom());
                        boolean fromMe = userFrom == myId;
                        if(scrolledToBottom || fromMe) {
                            setScrollTo(relativeLayout);
                            if(!fromMe)
                                updateRead();
                        } else {
                            extraCount++;
                            scrollToView.setText(String.valueOf(extraCount));
                            scrollToView.setVisibility(View.VISIBLE);
                        }
                    }
                    linearLayout.addView(relativeLayout);
                    setupUI(mainView);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private RelativeLayout setChatBubble(Chats chats, boolean sent) {
        String message = chats.getContent();
        String date = chats.getDate();
        int userFrom = Integer.parseInt(chats.getUserFrom());
        boolean fromMe = userFrom == myId;
        date = functions.miniDate(date);
        RelativeLayout relativeLayout;
        relativeLayout = (RelativeLayout) LayoutInflater.from(context).inflate(fromMe ? sent ? R.layout.blue_message_bubble : R.layout.blue_message_bubble_fade : R.layout.ash_message_bubble, null, false);
        RelativeLayout messageHolder = relativeLayout.findViewById(R.id.messageHolder);
        TextView messageTxt = relativeLayout.findViewById(R.id.message);
        TextView dateTxt = relativeLayout.findViewById(R.id.date);
        messageTxt.setText(message);
        dateTxt.setText(date);
        messageHolder.setOnClickListener(v -> {
            if(dateTxt.getVisibility() == View.VISIBLE)
                dateTxt.setVisibility(View.GONE);
            else
                dateTxt.setVisibility(View.VISIBLE);
        });
        return relativeLayout;
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            if(!(view == sendBtn)) {
                view.setOnTouchListener((v, event) -> {
                    hideSoftKeyboard(v);
                    editText.clearFocus();
                    return false;
                });
            } else
                sendMessage();
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        toUpdate = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        toUpdate = false;
    }
}