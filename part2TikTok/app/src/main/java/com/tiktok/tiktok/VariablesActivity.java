package com.tiktok.tiktok;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import config.StorageConfiguration;

public class VariablesActivity extends AppCompatActivity {

    private TextView mCurrentDirectory;
    private TextView mDownloadsDirectory;
    private TextView mProfileDirectory;
    private TextView mResourceDirectory;
    private TextView mTagsDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_variables);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        mCurrentDirectory = (TextView) findViewById(R.id.tv_current_directory);
        mDownloadsDirectory = (TextView) findViewById(R.id.tv_downloads_directory);
        mProfileDirectory = (TextView) findViewById(R.id.tv_profile_directory);
        mResourceDirectory = (TextView) findViewById(R.id.tv_resource_directory);
        mTagsDirectory = (TextView) findViewById(R.id.tv_tags_directory);

        mCurrentDirectory.setText(StorageConfiguration.currentDirectory.getAbsolutePath());
        mDownloadsDirectory.setText(StorageConfiguration.downloadsDirectory);
        mProfileDirectory.setText(StorageConfiguration.profileDirectory);
        mResourceDirectory.setText(StorageConfiguration.resourceDirectory);
        mTagsDirectory.setText(StorageConfiguration.tagsDirectory);
    }
}