package com.example.sck.clockwidget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

public class ClockWidget extends AppWidgetProvider{

    private final static String DATE_FORMAT = "dd/MM/yyyy";
    private final String UPDATE_CLOCK_WIDGET = "update_clock_widget";
    private final int WIDGET_UPDATE_INTERVAL = 1000;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int i : appWidgetIds) {
            updateWidget(context, appWidgetManager, i);
        }
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager, int widgetId) {

        SharedPreferences sp = context.getSharedPreferences(ConfigurationActivity.WIDGET_PREF, Context.MODE_PRIVATE);

        // read time format and determine the current time and date
        String timeFormat = sp.getString(ConfigurationActivity.WIDGET_TIME_FORMAT + widgetId, null);
        if (timeFormat == null) return;
        final Calendar cal = Calendar.getInstance();
        String currentDay = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat, Locale.getDefault());
        String currentTime = sdf.format(cal.getTime());
        String currentDate = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(cal.getTime());

        // set data to the textViews
        RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.clock_widget);
        widgetView.setTextViewText(R.id.time, currentTime);
        widgetView.setTextViewText(R.id.day_of_the_week, currentDay);
        widgetView.setTextViewText(R.id.date, currentDate);

        // set config intent
        Intent cfgIntent = new Intent(context, ConfigurationActivity.class);
        cfgIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        cfgIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        PendingIntent pIntent = PendingIntent.getActivity(context, widgetId, cfgIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.config, pIntent);

        // set update intent
        Intent updIntent = new Intent(context, ClockWidget.class);
        updIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
        pIntent = PendingIntent.getBroadcast(context, widgetId, updIntent, 0);
        widgetView.setOnClickPendingIntent(R.id.widget_bg, pIntent);

        // update widget
        appWidgetManager.updateAppWidget(widgetId, widgetView);
    }

    /**
     * starts sending broadcast messages every minute via AlarmManager
     * @param context Context
     */
    @Override
    public void onEnabled(Context context) {

        super.onEnabled(context);
        Intent i = new Intent(context, ClockWidget.class);
        i.setAction(UPDATE_CLOCK_WIDGET);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, i, 0);
        AlarmManager alarmMan = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMan.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), WIDGET_UPDATE_INTERVAL, pIntent);
    }

    @Override
    public void onDisabled(Context context) {

        super.onDisabled(context);
        Intent i = new Intent(context, ClockWidget.class);
        i.setAction(UPDATE_CLOCK_WIDGET);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, i, 0);
        AlarmManager alarmMan = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMan.cancel(pIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);
        if (intent.getAction().equalsIgnoreCase(UPDATE_CLOCK_WIDGET)) {
            ComponentName thisWidget = new ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager widgetMan = AppWidgetManager.getInstance(context);
            int ids[] = widgetMan.getAppWidgetIds(thisWidget);
            for (int widgetId : ids) {
                updateWidget(context, widgetMan, widgetId);
            }
        }
    }

    /**
     * clear Preferences
     *
     * @param context Context
     * @param appWidgetIds array of Ids
     */
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences.Editor spEditor = context.getSharedPreferences(
                ConfigurationActivity.WIDGET_PREF, Context.MODE_PRIVATE).edit();
        for (int widgetID : appWidgetIds) {
            spEditor.remove(ConfigurationActivity.WIDGET_TIME_FORMAT + widgetID);
        }
        spEditor.apply();
    }
}