/*
 * Copyright (C) 2014 ParanoidAndroid Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.reindeercrafts.notificationpeek.peek;

import android.app.Notification;
import android.content.Context;
import android.graphics.LightingColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class NotificationHelper {

    public final static String DELIMITER = "|";

    private TelephonyManager mTelephonyManager;
    private NotificationPeek mPeek;

    public boolean mRingingOrConnected = false;

    private Context mContext;

    public NotificationHelper(Context context, NotificationPeek peek) {
        mContext = context;
        mPeek = peek;
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyManager.listen(new CallStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    // Static methods

    public static boolean shouldDisplayNotification(StatusBarNotification oldNotif,
                                                    StatusBarNotification newNotif) {
        // First check for ticker text, if they are different, some other parameters will be
        // checked to determine if we should show the notification.
        CharSequence oldTickerText = oldNotif.getNotification().tickerText;
        CharSequence newTickerText = newNotif.getNotification().tickerText;
        if (newTickerText == null ? oldTickerText == null : newTickerText.equals(oldTickerText)) {
            // If old notification title isn't null, show notification if
            // new notification title is different. If it is null, show notification
            // if the new one isn't.
            String oldNotificationText = getNotificationTitle(oldNotif);
            String newNotificationText = getNotificationTitle(newNotif);
            if (newNotificationText == null ? oldNotificationText != null : !newNotificationText
                    .equals(oldNotificationText)) {
                return true;
            }

            // Last chance, check when the notifications were posted. If times
            // are equal, we shouldn't display the new notification.
            if (oldNotif.getNotification().when != newNotif.getNotification().when) {
                return true;
            }
            return false;
        }
        return true;
    }

    public static String getNotificationTitle(StatusBarNotification n) {
        String text = null;
        if (n != null) {
            Notification notification = n.getNotification();
            Bundle extras = notification.extras;
            text = extras.getString(Notification.EXTRA_TITLE);
        }
        return text;
    }

    public static String getContentDescription(StatusBarNotification content) {
        if (content != null) {
            String tag = content.getTag() == null ? "null" : content.getTag();
            return content.getPackageName() + DELIMITER + content.getId() + DELIMITER + tag;
        }
        return null;
    }

    public static View.OnTouchListener getHighlightTouchListener(final int color) {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Drawable drawable = ((ImageView) view).getDrawable();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        LightingColorFilter lighten = new LightingColorFilter(color, color);
                        drawable.setColorFilter(lighten);
                        break;
                    case MotionEvent.ACTION_UP:
                        drawable.clearColorFilter();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Rect rect = new Rect();
                        view.getLocalVisibleRect(rect);
                        if (!rect.contains((int) event.getX(), (int) event.getY())) {
                            drawable.clearColorFilter();
                        }
                        break;
                    case MotionEvent.ACTION_OUTSIDE:
                    case MotionEvent.ACTION_CANCEL:
                        drawable.clearColorFilter();
                        break;
                }
                return false;
            }
        };
    }

    /**
     * <!-- Peek -->
     * Call state listener
     * Telephony states booleans
     */
    private class CallStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    mRingingOrConnected = true;
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    mRingingOrConnected = false;
                    break;
            }
        }
    }

    public boolean isRingingOrConnected() {
        return mRingingOrConnected;
    }

    public boolean isSimPanelShowing() {
        int state = mTelephonyManager.getSimState();
        return state == TelephonyManager.SIM_STATE_PIN_REQUIRED
                || state == TelephonyManager.SIM_STATE_PUK_REQUIRED
                || state == TelephonyManager.SIM_STATE_NETWORK_LOCKED;
    }


}
