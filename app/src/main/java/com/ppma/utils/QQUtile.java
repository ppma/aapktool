package com.ppma.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class QQUtile {
    public static void addFriend(Context context, String qqNumber) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqwpa://im/chat?chat_type=wpa&uin=" + qqNumber));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }

    }

    public static void addGroup(Context context, String qqGroupNumber) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&card_type=group&source=qrcode&uin=" + qqGroupNumber));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (Exception e) {
        }


    }
}
