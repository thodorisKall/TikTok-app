package com.tiktok.tiktok;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Network;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import org.xml.sax.SAXException;

import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

import Main.TikTokController;
import config.NetworkConfiguration;
import config.StorageConfiguration;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MAIN_ACTIVITY";

//    private static int VIDEO_REQUEST = 101;
//    private Uri videoUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NetworkConfiguration.loadIP();

        String ip = NetworkConfiguration.INITIAL_PUBLISHER_IP;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString("INITIAL_PUBLISHER_IP", ip);
        edit.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //    public void captureVideo(View view){
//        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//
//        if(videoIntent.resolveActivity(getPackageManager())!=null)
//        {
//            startActivityForResult(videoIntent,VIDEO_REQUEST);
//        }
//    }
//
//
//    @Override
//    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == VIDEO_REQUEST && resultCode == RESULT_OK) {
//            videoUri = data.getData();
//        }
//
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        TikTokController.shutdown();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (NetworkConfiguration.INITIAL_PUBLISHER_IP == "unknown") {
            View parentLayout = findViewById(android.R.id.content);
            Snackbar.make(parentLayout, "IP unknown please enable network access", Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_variables) {
            startActivity(new Intent(MainActivity.this, VariablesActivity.class));
            return true;
        }

        if (id == R.id.action_exit) {
            finish();
        }


        return super.onOptionsItemSelected(item);
    }

}