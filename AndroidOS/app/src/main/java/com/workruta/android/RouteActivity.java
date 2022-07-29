package com.workruta.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.github.kittinunf.fuel.core.requests.CancellableRequest;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;
import com.workruta.android.Utils.Constants;
import com.workruta.android.Utils.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import kotlin.Pair;
import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.workruta.android.PreviousRoutesActivity.setRouteActive;
import static com.workruta.android.Utils.Constants.www;
import static com.workruta.android.Utils.Functions.formatPhoneNumber;
import static com.workruta.android.Utils.Functions.getTripCost;
import static com.workruta.android.Utils.Util.convertDateToString;

public class RouteActivity extends SharedCompatActivity {

    LinearLayout resultLayout, whiteFade, blackFade, menuBtn, menuLayout, dummyLayout, bottomSheet;
    TextView headText, nameTextView, sendMessage, viewProfile, textView;
    ImageView imgView;
    ImageButton imageBtn;
    String routeIdFrom, routeIdTo, userId, routerId, pathKey, stripeCost;
    boolean requesting, notStarted, notRated;
    int[] icons;
    JSONObject optionsObject;

    PaymentSheet paymentSheet;
    PaymentSheet.CustomerConfiguration customerConfiguration;
    String customerId, ephemeralKey, clientSecretKey, paymentId;
    List<Pair<String, String>> parameters;
    CancellableRequest request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);
        optionsObject = new JSONObject();
        requesting = false;
        pathKey = "";

        Bundle bundle = getIntent().getExtras();
        routeIdTo = bundle.getString("routeIdTo");
        routeIdFrom = bundle.getString("routeIdFrom");

        PaymentConfiguration.init(context, Constants.stripePKTestAPIKey);
        paymentSheet = new PaymentSheet(this, this::onPaymentSheetResult);

        try {
            optionsObject.put("viewOwnRoute", new int[]{R.drawable.ic_carm, R.string.viewOwnRoute});
            optionsObject.put("isFollowingRide", new int[]{R.drawable.ic_follow});
            optionsObject.put("canPay", new int[]{R.drawable.ic_credit_card, R.string.makePayment});
            optionsObject.put("canRate", new int[]{R.drawable.ic_star, R.string.rateRide});
        } catch (Exception e) {
            e.printStackTrace();
        }

        icons = new int[]{
                R.drawable.ic_phone,
                R.drawable.ic_distance,
                R.drawable.ic_location_from,
                R.drawable.ic_location_to,
                R.drawable.ic_length,
                R.drawable.ic_time,
                R.drawable.ic_money,
                R.drawable.ic_group,
                R.drawable.ic_group_add,
                R.drawable.ic_date
        };

        resultLayout = findViewById(R.id.resultLayout);
        whiteFade = findViewById(R.id.whiteFade);
        blackFade = findViewById(R.id.blackFade);
        headText = findViewById(R.id.headText);
        nameTextView = findViewById(R.id.nameTextView);
        sendMessage = findViewById(R.id.sendMessage);
        viewProfile = findViewById(R.id.viewProfile);
        menuBtn = findViewById(R.id.menuBtn);
        menuLayout = findViewById(R.id.menuLayout);
        bottomSheet = findViewById(R.id.bottomSheet);
        dummyLayout = findViewById(R.id.dummyLayout);
        imgView = findViewById(R.id.imgView);
        imageBtn = findViewById(R.id.imageBtn);

        headText.setOnClickListener((v) -> finish());
        menuBtn.setOnClickListener((v) -> {
            if(menuLayout.getVisibility() == View.VISIBLE) {
                menuLayout.setVisibility(View.GONE);
                dummyLayout.setVisibility(View.GONE);
            } else {
                menuLayout.setVisibility(View.VISIBLE);
                dummyLayout.setVisibility(View.VISIBLE);
            }
        });
        imageBtn.setOnClickListener((v) -> {
            if(menuLayout.getVisibility() == View.VISIBLE) {
                menuLayout.setVisibility(View.GONE);
                dummyLayout.setVisibility(View.GONE);
            } else {
                menuLayout.setVisibility(View.VISIBLE);
                dummyLayout.setVisibility(View.VISIBLE);
            }
        });
        dummyLayout.setOnClickListener((v) -> hideMenuBox());
        whiteFade.setOnClickListener((v) -> {
            return;
        });
        blackFade.setOnClickListener((v) -> {
            return;
        });
        bottomSheet.setOnClickListener((v) -> {
            return;
        });
        viewProfile.setOnClickListener((v) -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            Bundle bundle1 = new Bundle();
            bundle1.putInt("userId", Integer.parseInt(userId));
            intent.putExtras(bundle1);
            startActivity(intent);
        });

        getRouteContents();

    }

    private void hideMenuBox() {
        menuLayout.setVisibility(View.GONE);
        dummyLayout.setVisibility(View.GONE);
    }

    private void followRoute(TextView textView) {
        whiteFade.setVisibility(View.VISIBLE);
        requesting = true;
        boolean isTrue = Boolean.parseBoolean(textView.getTag().toString());
        new android.os.Handler().postDelayed(() -> {
            List<Pair<String, String>> parameters = new ArrayList<>();
            parameters.add(new Pair<>("userTo", userId));
            parameters.add(new Pair<>("userFrom", String.valueOf(getMyId())));
            parameters.add(new Pair<>("pathKey", pathKey));
            parameters.add(new Pair<>("routerId", routerId));
            parameters.add(new Pair<>("routeIdTo", routeIdTo));
            parameters.add(new Pair<>("routeIdFrom", routeIdFrom));
            parameters.add(new Pair<>("toFollow", String.valueOf(!isTrue)));
            Fuel.INSTANCE.post(Constants.followRouteURL, parameters).responseString(new Handler<String>() {
                @Override
                public void success(String s) {
                    runOnUiThread(() -> {
                        try {
                            requesting = false;
                            whiteFade.setVisibility(View.GONE);
                            JSONObject object = new JSONObject(s);
                            boolean noError = object.getBoolean("noError");
                            if(noError) {
                                int text = isTrue ? R.string.follow_ride : R.string.unfollow_ride;
                                textView.setText(text);
                                textView.setTag(!isTrue);
                                pathKey = object.getString("pathKey");
                                routerId = object.getString("routerId");
                                String cluded = !isTrue ? "included" : "excluded";
                                setRouteActive(routeIdFrom, routerId, routeIdTo, userId, pathKey, !isTrue);
                                showMessageError("You have been " + cluded + " as a passenger to this ride");
                                if(!isTrue)
                                    showRequestBox("pending");
                                else
                                    hideRequestBox();
                            } else {
                                String errorMsg = object.getString("errorMsg");
                                showMessageError(errorMsg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void failure(@NotNull FuelError fuelError) {
                    runOnUiThread(() -> {
                        requesting = false;
                        whiteFade.setVisibility(View.GONE);
                        showMessageError("An error occurred. Please try again");
                    });
                }
            });
        }, 2000);
    }

    private void hideRequestBox() {
        View view = resultLayout.getChildAt(0);
        if(view instanceof TextView)
            resultLayout.removeViewAt(0);
    }

    @SuppressLint("InflateParams")
    private void getRouteContents() {
        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.slide_loader, null);
        whiteFade.addView(layout);
        new android.os.Handler().postDelayed(() -> {
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("routeIdFrom", routeIdFrom)
                    .addFormDataPart("routeIdTo", routeIdTo)
                    .addFormDataPart("user", String.valueOf(getMyId()))
                    .build();
            Request request = new Request.Builder()
                    .url(Constants.getRouteUrl)
                    .post(requestBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Call call = okHttpClient.newCall(request);
            try(Response response = call.execute()) {
                if (response.isSuccessful()) {
                    whiteFade.setVisibility(View.GONE);
                    bottomSheet.setVisibility(View.VISIBLE);
                    String responseString = Objects.requireNonNull(response.body()).string();
                    JSONObject object = new JSONObject(responseString);
                    pathKey = object.getString("pathKey");
                    userId = object.getString("userTo");
                    routerId = object.getString("routerId");
                    notRated = object.getBoolean("notRated");
                    String name = object.getString("name");
                    String photo = www + object.getString("photo");
                    String userEmail = functions.safeEmail(object.getString("email"));
                    String phone = object.getString("phone");
                    String locationFrom = object.getString("locationFrom");
                    String locationTo = object.getString("locationTo");
                    String passengers = object.getString("passengers");
                    String passNum = object.getString("passNum");
                    String routeDate = object.getString("routeDate");
                    String status = object.getString("status");
                    JSONObject options = object.getJSONObject("options");
                    notStarted = object.getBoolean("notStarted");
                    boolean freeRide = object.getBoolean("freeRide");
                    double fromLatitudeFrom = object.getDouble("fromLatitudeFrom");
                    double fromLongitudeFrom = object.getDouble("fromLongitudeFrom");
                    double toLatitudeFrom = object.getDouble("toLatitudeFrom");
                    double toLongitudeFrom = object.getDouble("toLongitudeFrom");
                    double toLatitudeTo = object.getDouble("toLatitudeTo");
                    double toLongitudeTo = object.getDouble("toLongitudeTo");
                    double dist = functions.getDistance(fromLatitudeFrom, fromLongitudeFrom, toLatitudeFrom, toLongitudeFrom, null);
                    double len = functions.getDistance(toLatitudeFrom, toLongitudeFrom, toLatitudeTo, toLongitudeTo, null);
                    String time = functions.getRouteTime(toLatitudeFrom, toLongitudeFrom, toLatitudeTo, toLongitudeTo);
                    stripeCost = functions.getStripeCost(len);
                    String cost = getTripCost(len);
                    if(freeRide)
                        cost = "Free";
                    String[] strings = new String[]{
                            formatPhoneNumber(phone),
                            dist + "mi away from you",
                            locationFrom,
                            locationTo,
                            len + "mi",
                            time,
                            cost,
                            passNum,
                            passengers,
                            convertDateToString(routeDate, true)
                    };
                    sendMessage.setOnClickListener((v) -> {
                        Intent intent = new Intent(context, MessageActivity.class);
                        Bundle bundle1 = new Bundle();
                        bundle1.putInt("userId", Integer.parseInt(userId));
                        bundle1.putString("photoUrl", photo);
                        bundle1.putString("name", name);
                        bundle1.putString("userEmail", userEmail);
                        intent.putExtras(bundle1);
                        startActivity(intent);
                    });
                    imageLoader.displayImage(photo, imgView);
                    nameTextView.setText(name);
                    for(int i = 0; i < icons.length; i++){
                        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.option_text, null);
                        TextView textView = linearLayout.findViewById(R.id.textView);
                        String text = strings[i];
                        int icon = icons[i];
                        textView.setElegantTextHeight(true);
                        textView.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                        textView.setSingleLine(false);
                        textView.setText(text);
                        textView.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
                        if(i == 0)
                            textView.setOnClickListener((v) -> dialPhoneNumber(phone));
                        resultLayout.addView(linearLayout, i);
                    }
                    for(int x = 0; x < options.length(); x++){
                        String key = Objects.requireNonNull(options.names()).getString(x);
                        boolean isTrue = options.getBoolean(key);
                        boolean canFollow = options.optBoolean("canFollow");
                        if((isTrue && !key.equals("canFollow") && !key.equals("isFollowingRide"))
                                || (key.equals("isFollowingRide") && notStarted && canFollow)){
                            int menuText, menuIcon = ((int[]) optionsObject.get(key))[0];
                            if(key.equals("isFollowingRide")){
                                menuText = isTrue ? R.string.unfollow_ride : R.string.follow_ride;
                            } else {
                                menuText = ((int[]) optionsObject.get(key))[1];
                            }
                            LinearLayout optionText = (LinearLayout) getLayoutInflater().inflate(R.layout.option_text, null);
                            TextView textView = optionText.findViewById(R.id.textView);
                            textView.setTag(isTrue);
                            textView.setText(menuText);
                            textView.setTextColor(ContextCompat.getColor(context, R.color.mainColor));
                            textView.setCompoundDrawablesWithIntrinsicBounds(menuIcon, 0, 0, 0);
                            textView.setOnClickListener(v -> listenToMenuClick(optionText, textView, key));
                            menuLayout.addView(optionText);
                        }
                    }
                    if(!StringUtils.isEmpty(status))
                        showRequestBox(status);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2000);
    }

    private void listenToMenuClick(LinearLayout linearLayout, TextView textView, String key) {
        hideMenuBox();
        switch (key){
            case "viewOwnRoute":
                Bundle bundle = new Bundle();
                bundle.putString("routeId", routeIdFrom);
                Intent intent = new Intent(context, OpenRouteActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case "isFollowingRide":
                followRoute(textView);
                break;
            case "canPay":
                makePayment(textView);
                break;
            case "canRate":
                showRatingBar(linearLayout);
                break;
        }
    }

    @SuppressLint("InflateParams")
    private void showRatingBar(LinearLayout linearLayout) {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        RelativeLayout relativeLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.rating_view, null);
        RatingBar ratingBar = relativeLayout.findViewById(R.id.ratingBar);
        Button button = relativeLayout.findViewById(R.id.rateBtn);
        ImageView cupertino = relativeLayout.findViewById(R.id.cupertino);
        ImageView checkIcon = relativeLayout.findViewById(R.id.checkIcon);
        blackFade.addView(relativeLayout);
        blackFade.setVisibility(View.VISIBLE);
        button.setOnClickListener(v -> {
            double rate = ratingBar.getRating();
            if(rate == 0){
                Toast.makeText(context, "Please make a rating", Toast.LENGTH_SHORT).show();
                return;
            }
            requesting = true;
            button.setText("");
            cupertino.setVisibility(View.VISIBLE);
            String rating = String.valueOf(rate);
            new android.os.Handler().postDelayed(() -> {
                parameters = new ArrayList<>();
                parameters.add(new Pair<>("user", String.valueOf(getMyId())));
                parameters.add(new Pair<>("userTo", userId));
                parameters.add(new Pair<>("rating", rating));
                parameters.add(new Pair<>("routeId", routeIdTo));
                parameters.add(new Pair<>("routerId", routerId));
                parameters.add(new Pair<>("action", "rateRide"));
                request = Fuel.INSTANCE.post(Constants.actionsUrl, parameters).responseString(new Handler<String>() {
                    @Override
                    public void success(String s) {
                        runOnUiThread(() -> {
                            cupertino.setVisibility(View.GONE);
                            checkIcon.setVisibility(View.VISIBLE);
                            ((Animatable) checkIcon.getDrawable()).start();
                            new android.os.Handler().postDelayed(() -> {
                                runOnUiThread(() -> {
                                    requesting = false;
                                    blackFade.setVisibility(View.GONE);
                                    linearLayout.setVisibility(View.GONE);
                                });
                            }, 1000);
                        });
                    }

                    @Override
                    public void failure(@NotNull FuelError fuelError) {
                        runOnUiThread(() -> {
                            requesting = false;
                            button.setText(R.string.rate);
                            cupertino.setVisibility(View.GONE);
                            Toast.makeText(context, "An error occurred. Please try again", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }, 1000);
        });
    }

    private void makePayment(TextView textView) {
        whiteFade.setVisibility(View.VISIBLE);
        requesting = true;
        this.textView = textView;
        new android.os.Handler().postDelayed(() -> {
            parameters = new ArrayList<>();
            parameters.add(new Pair<>("userFrom", String.valueOf(getMyId())));
            parameters.add(new Pair<>("userTo", userId));
            parameters.add(new Pair<>("amount", stripeCost));
            parameters.add(new Pair<>("routerId", routerId));
            parameters.add(new Pair<>("routeIdTo", routeIdTo));
            parameters.add(new Pair<>("routeIdFrom", routeIdFrom));
            request = Fuel.INSTANCE.post(Constants.stripeAPIUrl, parameters).responseString(new Handler<String>() {
                @Override
                public void success(String s) {
                    runOnUiThread(() -> {
                        try {
                            requesting = false;
                            whiteFade.setVisibility(View.GONE);
                            JSONObject object = new JSONObject(s);
                            boolean noError = object.getBoolean("noError");
                            if(noError) {
                                paymentId = object.getString("paymentId");
                                customerId = object.getString("customerId");
                                ephemeralKey = object.getString("ephemeralKey");
                                clientSecretKey = object.getString("clientSecretKey");
                                customerConfiguration = new PaymentSheet.CustomerConfiguration(customerId, ephemeralKey);
                                presentPaymentSheet();
                            } else {
                                String errorMsg = object.getString("errorMsg");
                                showMessageError(errorMsg);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                }

                @Override
                public void failure(@NotNull FuelError fuelError) {
                    runOnUiThread(() -> {
                        requesting = false;
                        whiteFade.setVisibility(View.GONE);
                    });
                }
            });
        }, 1000);
    }

    private void showRequestBox(String status) {
        String text = status.equals("pending") ? "Request Pending Approval" :
                status.equals("rejected") ? "Request Rejected" : "Request Accepted";
        int background = status.equals("pending") ? R.drawable.yellow_box :
                status.equals("rejected") ? R.drawable.red_box : R.drawable.green_box;
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(3, 3, 3, 3);
        textView.setLayoutParams(params);
        textView.setBackgroundResource(background);
        textView.setTextColor(ContextCompat.getColor(context, R.color.black));
        textView.setTextSize(17f);
        textView.setText(text);
        resultLayout.addView(textView, 0);
    }

    private void dialPhoneNumber(String phone) {
        Uri uri = Uri.parse("tel:+1" + phone);
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(uri);
        startActivity(intent);
    }

    @SuppressLint("InflateParams")
    private void showMessageError(String errorMsg) {
        if(blackFade.getChildCount() > 0)
            blackFade.removeAllViews();
        LinearLayout alertLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.alert_layout, null);
        TextView textView = alertLayout.findViewById(R.id.textView);
        Button negativeBtn = alertLayout.findViewById(R.id.negative);
        Button positiveBtn = alertLayout.findViewById(R.id.positive);
        textView.setText(errorMsg);
        positiveBtn.setText(R.string.ok);
        negativeBtn.setVisibility(View.GONE);
        positiveBtn.setOnClickListener((v) -> blackFade.setVisibility(View.INVISIBLE));
        blackFade.addView(alertLayout);
        blackFade.setVisibility(View.VISIBLE);
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if(paymentSheetResult instanceof PaymentSheetResult.Completed) {
            whiteFade.setVisibility(View.VISIBLE);
            requesting = true;
            List<Pair<String, String>> params = new ArrayList<>();
            params.add(new Pair<>("action", "confirmPayment"));
            params.add(new Pair<>("user", String.valueOf(getMyId())));
            params.add(new Pair<>("userTo", userId));
            params.add(new Pair<>("name", myName));
            params.add(new Pair<>("routeId", routeIdTo));
            params.add(new Pair<>("amount", stripeCost));
            params.add(new Pair<>("paymentId", paymentId));
            Fuel.INSTANCE.post(Constants.actionsUrl, params).responseString(new Handler<String>() {
                @Override
                public void success(String s) {
                    runOnUiThread(() -> {
                        requesting = false;
                        whiteFade.setVisibility(View.GONE);
                        showMessageError("Payment Successful");
                        textView.setVisibility(View.GONE);
                        if(notRated) {
                            int lastChild = menuLayout.getChildCount() - 1;
                            LinearLayout linearLayout = (LinearLayout) menuLayout.getChildAt(lastChild);
                            showRatingBar(linearLayout);
                        }
                    });
                }

                @Override
                public void failure(@NotNull FuelError fuelError) {
                    runOnUiThread(() -> {
                        requesting = false;
                        whiteFade.setVisibility(View.GONE);
                    });
                }
            });
        }
        if(paymentSheetResult instanceof PaymentSheetResult.Failed) {
            showMessageError("Failed to complete payment. Please try again");
        }
    }

    private void presentPaymentSheet() {
        PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder(Constants.NAME)
                .customer(customerConfiguration)
                // Set `allowsDelayedPaymentMethods` to true if your business can handle payment methods
                // that complete payment after a delay, like SEPA Debit and Sofort.
                //.allowsDelayedPaymentMethods(true);
                .build();
        paymentSheet.presentWithPaymentIntent(clientSecretKey, configuration);
    }

    @Override
    public void onBackPressed() {
        if(menuLayout.getVisibility() == View.VISIBLE){
            hideMenuBox();
            return;
        }
        if(!requesting)
            finish();
    }
}