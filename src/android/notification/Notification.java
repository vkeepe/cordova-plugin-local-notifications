/*
 * Apache 2.0 License
 *
 * Copyright (c) Sebastian Katzer 2017
 *
 * This file contains Original Code and/or Modifications of Original Code
 * as defined in and that are subject to the Apache License
 * Version 2.0 (the 'License'). You may not use this file except in
 * compliance with the License. Please obtain a copy of the License at
 * http://opensource.org/licenses/Apache-2.0/ and read it before using this
 * file.
 *
 * The Original Code and all software distributed under the License are
 * distributed on an 'AS IS' basis, WITHOUT WARRANTY OF ANY KIND, EITHER
 * EXPRESS OR IMPLIED, AND APPLE HEREBY DISCLAIMS ALL SUCH WARRANTIES,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, QUIET ENJOYMENT OR NON-INFRINGEMENT.
 * Please see the License for the specific language governing rights and
 * limitations under the License.
 */

package de.appplant.cordova.plugin.notification;


import android.app.AlarmManager;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import de.appplant.cordova.plugin.notification.receiver.TriggerReceiver;

/**
 * Wrapper class around OS notification class. Handles basic operations
 * like show, delete, cancel for a single local notification instance.
 */
public final class Notification {

    // Used to differ notifications by their life cycle state
    public enum Type {
        ALL, SCHEDULED, TRIGGERED
    }

    // Key for private preferences
    static final String PREF_KEY = "LocalNotification";

    // Application context passed by constructor
    private final Context context;

    // Notification options passed by JS
    private final Options options;

    // Builder with full configuration
    private final NotificationCompat.Builder builder;

    /**
     * Constructor
     *
     * @param context Application context.
     * @param options Parsed notification options.
     * @param builder Pre-configured notification builder.
     */
    Notification (Context context, Options options, NotificationCompat.Builder builder) {
        this.context  = context;
        this.options  = options;
        this.builder  = builder;
    }

    /**
     * Constructor
     *
     * @param context Application context.
     * @param options Parsed notification options.
     */
    public Notification(Context context, Options options) {
        this.context  = context;
        this.options  = options;
        this.builder  = null;
    }

    /**
     * Get application context.
     */
    public Context getContext () {
        return context;
    }

    /**
     * Get notification options.
     */
    public Options getOptions () {
        return options;
    }

    /**
     * Get notification ID.
     */
    public int getId () {
        return options.getId();
    }

    /**
     * If it's a repeating notification.
     */
    public boolean isRepeating () {
        return getOptions().getTrigger().has("every");
    }

    // /**
    //  * If the notification is scheduled.
    //  */
    // public boolean isScheduled () {
    //     return isRepeating() || !wasInThePast();
    // }

    // /**
    //  * If the notification is triggered.
    //  */
    // public boolean isTriggered () {
    //     return wasInThePast();
    // }

    // /**
    //  * If the notification is an update.
    //  *
    //  * @param keepFlag
    //  *      Set to false to remove the flag from the option map
    //  */
    // protected boolean isUpdate (boolean keepFlag) {
    //     boolean updated = options.getDict().optBoolean("updated", false);

    //     if (!keepFlag) {
    //         options.getDict().remove("updated");
    //     }

    //     return updated;
    // }

    // /**
    //  * Notification type can be one of pending or scheduled.
    //  */
    // public Type getType () {
    //     return isScheduled() ? Type.SCHEDULED : Type.TRIGGERED;
    // }

    /**
     * Clear the local notification without canceling repeating alarms.
     */
    public void clear () {

        // if (!isRepeating() && wasInThePast())
        //     unpersist();

        // if (!isRepeating())
        //     getNotMgr().cancel(getId());
    }

    /**
     * Cancel the local notification.
     *
     * Create an intent that looks similar, to the one that was registered
     * using schedule. Making sure the notification id in the action is the
     * same. Now we can search for such an intent using the 'getService'
     * method and cancel it.
     */
    public void cancel() {
        // Intent intent = new Intent(context, receiver)
        //         .setAction(options.getIdStr());

        // PendingIntent pi = PendingIntent.
        //         getBroadcast(context, 0, intent, 0);

        // getAlarmMgr().cancel(pi);
        // getNotMgr().cancel(options.getId());

        // unpersist();
    }

    /**
     * Present the local notification to user.
     */
    public void show () {
        // TODO Show dialog when in foreground
        showNotification();
    }

    /**
     * Show as local notification when in background.
     */
    private void showNotification () {
        if (builder != null) {
            getNotMgr().notify(getId(), builder.build());
        }
    }

    // /**
    //  * Count of triggers since schedule.
    //  */
    // public int getTriggerCountSinceSchedule() {
    //     long now = System.currentTimeMillis();
    //     long triggerTime = options.getTriggerTime();

    //     if (!wasInThePast())
    //         return 0;

    //     if (!isRepeating())
    //         return 1;

    //     return (int) ((now - triggerTime) / options.getRepeatInterval());
    // }

    /**
     * Encode options to JSON.
     */
    public String toString() {
        JSONObject dict = options.getDict();
        JSONObject json = new JSONObject();

        try {
            json = new JSONObject(dict.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }

    /**
     * Persist the information of this notification to the Android Shared
     * Preferences. This will allow the application to restore the notification
     * upon device reboot, app restart, retrieve notifications, aso.
     */
    private void persist () {
        SharedPreferences.Editor editor = getPrefs().edit();

        editor.putString(options.getIdentifier(), options.toString());
        editor.apply();
    }

    /**
     * Remove the notification from the Android shared Preferences.
     */
    private void unpersist () {
        SharedPreferences.Editor editor = getPrefs().edit();

        editor.remove(options.getIdentifier());
        editor.apply();
    }

    /**
     * Shared private preferences for the application.
     */
    private SharedPreferences getPrefs () {
        return context.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
    }

    /**
     * Notification manager for the application.
     */
    private NotificationManager getNotMgr () {
        return (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Alarm manager for the application.
     */
    private AlarmManager getAlarmMgr () {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

}
