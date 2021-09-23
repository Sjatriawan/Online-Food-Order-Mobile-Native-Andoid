package com.lentera.silaq.Common;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.lentera.silaq.Model.AddonModel;
import com.lentera.silaq.Model.CategoryModel;
import com.lentera.silaq.Model.CommentModel;
import com.lentera.silaq.Model.FoodModel;
import com.lentera.silaq.Model.SizeModel;
import com.lentera.silaq.Model.TokenModel;
import com.lentera.silaq.Model.UserModel;
import com.lentera.silaq.R;
import com.lentera.silaq.services.MyFCMServices;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class Common {
    public static final String USER_REFERENCES = "Users";
    public static final String POPULAR_CATEGORY_REF = "MostPopular";
    public static final String BEST_DEAL_REF = "BestDeals";
    public static final int DEFAULT_COLLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLLUMN = 1;
    public static final String CATEGORY_REF = "Category";
    public static final String COMMENT_REF = "Comments";
    public static final String ORDER_REF = "Order";
    public static final String NOTI_TITLE ="title" ;
    public static final String NOTI_CONTENT = "content";
    public static final String PLUS_REF = "Plus";
    private static final String TOKEN_REF = "Tokens";
    //    public static CommentModel currentUser;
    public static UserModel currentUser;
    public static CategoryModel categorySelected;
    public static FoodModel selectedFood;
//
    public static String formatPrice(double price) {
        if(price != 0){
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(df.format(price)).toString();
            return finalPrice.replace(".",",");
        }
        else
            return "0,00";
    }

    public static Double calculateExtraPrice(SizeModel userSelectedSize, List<AddonModel> userSelectedAddon) {
        Double result = 0.0;
        if(userSelectedSize == null && userSelectedAddon == null)
            return 0.0;
        else if (userSelectedSize == null){
            for (AddonModel addonModel: userSelectedAddon)
                result+=addonModel.getPrice();
            return result;
        }
        else if (userSelectedAddon==null){
            return userSelectedSize.getPrice()*1.0;
        }
        else{
            result= userSelectedSize.getPrice()*1.0;
            for (AddonModel addonModel:userSelectedAddon)
                result+=addonModel.getPrice();
            return result;
        }

    }

    public static void setSpanString(String s, String name, TextView txt_user) {
        SpannableStringBuilder builder= new SpannableStringBuilder();
        builder.append(s);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan,0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        txt_user.setText(builder, TextView.BufferType.SPANNABLE);

    }

    public static String createOrderNumber() {
        return new StringBuilder()
                .append(System.currentTimeMillis())
                .append(Math.abs(new Random().nextInt()))
                .toString();
    }

    public static String getDateOfWeek(int i) {

        switch (i){
            case 1:
                return "Minggu";
            case 2:
                return "Senin";
            case 3:
                return "Selasa";
            case 4:
                return "Rabu";
            case 5:
                return "Kamis";
            case 6:
                return "Juma'at";
            case 7:
                return "Sabtu";
            default:
                return "Tidak diketahui";

        }
    }

    public static String convertStatusToText(int o) {
        switch (o)
        {
            case 0:
                return "Sedang diproses";
            case 1:
                return "Dikirim";
            case 2:
                return "Terkirim";
            case -1:
                return "Dibatalkan";
            default:
                return "Tidak diketahui";

        }

    }

    public static void showNotification(Context context, int id, String title, String content, Intent intent){
        PendingIntent pendingIntent = null;
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "Silaq_v2";
        NotificationManager notificationManager =(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Silaq", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Silaq Pesen");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_room_service_black_24dp));
        if(pendingIntent != null){
            builder.setContentIntent(pendingIntent);
            Notification notification = builder.build();
            notificationManager.notify(id, notification);
        }
    }

    public static void updateToken(Context context, String newToken) {
        if(Common.currentUser != null) {
            FirebaseDatabase.getInstance()
                    .getReference(Common.TOKEN_REF)
                    .child(Common.currentUser.getUid())
                    .setValue(new TokenModel(Common.currentUser.getPhone(), newToken))
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    });
        }
    }

    public static String createTopicOrder() {
        return  new StringBuilder("/topics/new_order").toString();
    }
}

