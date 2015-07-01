package com.example.sck.clockwidget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class ConfigurationActivity extends Activity{

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDGET_TIME_FORMAT = "widget_time_format";

    private int mWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Intent mResVal;
    private SharedPreferences mSpref;
    private EditText mTimeFormat;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get ID of configurable widget
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // forming response intent
        mResVal = new Intent();
        mResVal.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);

        // negative response
        setResult(RESULT_CANCELED, mResVal);

        setContentView(R.layout.configuration);

        mSpref = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        mTimeFormat = (EditText) findViewById(R.id.timeFormat);
        mTimeFormat.setText(mSpref.getString(WIDGET_TIME_FORMAT + mWidgetId, "HH:mm:ss"));
    }

    /**
     * save timeFormat to Preferences, update widget, set positive response
     * @param v clicked view
     */
    public void onClick(View v){
        mSpref.edit().putString(WIDGET_TIME_FORMAT + mWidgetId, mTimeFormat.getText().toString()).apply();
        ClockWidget.updateWidget(this, AppWidgetManager.getInstance(this), mWidgetId);
        setResult(RESULT_OK, mResVal);
        finish();
    }
}