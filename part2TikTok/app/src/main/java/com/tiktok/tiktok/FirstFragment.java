package com.tiktok.tiktok;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import Main.TikTokController;
import config.StorageConfiguration;
import structures.VideoFile;

public class FirstFragment extends Fragment {
    public static final String TAG = "FIRST_FRAGMENT";
    private static final int VIDEO_REQUEST = 101;
    private TextView message;
    private Spinner channelSpinner;
    private Spinner videoSpinner;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = this.getContext();
        return inflater.inflate(R.layout.fragment_publisher, container, false);
    }

    private boolean requestPermissionsForExternalStorage() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Permission: READ_EXTERNAL_STORAGE GRANTED without request", Toast.LENGTH_SHORT).show();

                initializeStorage();

                return true;
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 3);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted1");
            return true;
        }
    }

    private void initializeStorage() {
        File f = new File(Environment.getExternalStorageDirectory(), StorageConfiguration.PARENT_DIRECTORY_NAME);
        if (!f.exists()) {
            if (!f.mkdirs()) {
                Log.d(TAG, "############# directory could not be created #############" + f.getAbsolutePath());
            } else {
                Log.d(TAG, "############# directory created #############");
            }
        } else {
            Log.d(TAG, "############# directory already exists  #############" + f.getAbsolutePath());
        }
    }

    private class RegisterRunner extends AsyncTask<String, Void, String> {
        private final String bip;
        private final int bport;

        public RegisterRunner(String bip, int bport) {
            this.bip = bip;
            this.bport = bport;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                return TikTokController.registerAsPublisher(bip, bport);
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(getActivity(), "Register task completed with result:" + result, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateView() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

            if (!TikTokController.serverActive()) {
                String username = prefs.getString("PUBLISHER_USERNAME", "user1");
                String pip = prefs.getString("INITIAL_PUBLISHER_IP", "192.168.2.2");
                int pport = Integer.parseInt(prefs.getString("INITIAL_PUBLISHER_PORT", "20313"));
                TikTokController.openServer(username, pip, pport);
            }

            TikTokController.loadCatalog();

            String[] array = TikTokController.catalog.getChannelArray();

            ArrayAdapter<String> aa = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, array);
            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            channelSpinner.setAdapter(aa);


            if (!TikTokController.isRegisteredAsPublisher()) {
                String bip = prefs.getString("INITIAL_BROKER_IP", "192.168.2.17");
                int bport = Integer.parseInt(prefs.getString("INITIAL_BROKER_PORT", "14321"));

                RegisterRunner task = new RegisterRunner(bip, bport);
                task.execute();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 2:
                Log.d(TAG, "External storage2");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String result = "GRANTED";
                    Toast.makeText(getActivity(), "Permission: " + permissions[0] + "was " + result + " (request):" + grantResults[0], Toast.LENGTH_SHORT).show();
                } else {
                    String result = "REJECTED";
                    Toast.makeText(getActivity(), "Permission: " + permissions[0] + "was " + result + " (request): " + grantResults[0], Toast.LENGTH_LONG).show();
                    return;
                }
            case 3:
                Log.d(TAG, "External storage1");
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String result = "GRANTED";
                    Toast.makeText(getActivity(), "Permission: " + permissions[0] + "was " + result + " (request): " + grantResults[0], Toast.LENGTH_SHORT).show();
                } else {
                    String result = "REJECTED";
                    Toast.makeText(getActivity(), "Permission: " + permissions[0] + "was " + result + " (request): " + grantResults[0], Toast.LENGTH_LONG).show();
                    return;
                }
        }

        initializeStorage();

        updateView();
    }

    public void loadVideos(String channel) {
        VideoFile[] array = TikTokController.catalog.getVideoArray(channel);
        ArrayAdapter<VideoFile> aa = new ArrayAdapter<VideoFile>(context, android.R.layout.simple_spinner_item, array);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        aa.notifyDataSetChanged();
        videoSpinner.setAdapter(aa);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        message = view.findViewById(R.id.txtMessages);
        channelSpinner = view.findViewById(R.id.topicSpinner);
        videoSpinner = view.findViewById(R.id.videoSpinner);

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (NavHostFragment.findNavController(FirstFragment.this).popBackStack(R.id.action_FirstFragment_to_SecondFragment, false)) {
                } else {
                    NavHostFragment.findNavController(FirstFragment.this).navigate(R.id.action_FirstFragment_to_SecondFragment);
                }
            }
        });

        view.findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoFile selectedVideo = (VideoFile) videoSpinner.getSelectedItem();
                if (selectedVideo != null) {
                    String path = selectedVideo.getFilepath();

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                    intent.setDataAndType(Uri.parse(path), "video/mp4");
                    startActivity(intent);
                }
            }
        });

        channelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // select a channel
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String channelname = channelSpinner.getSelectedItem().toString();
                loadVideos(channelname);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        videoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // select a video
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v, int i, long l) {
                VideoFile selectedVideo = (VideoFile) videoSpinner.getSelectedItem();

                if (selectedVideo != null) {
                    TextView tv1 = view.findViewById(R.id.textViewVideoName);
                    tv1.setText(selectedVideo.getVideoName());

                    TextView tv2 = view.findViewById(R.id.textViewVideoSize);
                    tv2.setText(selectedVideo.getBytes() + " bytes");

                    TextView tv3 = view.findViewById(R.id.textViewVideoDuration);
                    tv3.setText(selectedVideo.getDuration() + " seconds");

                    TextView tv4 = view.findViewById(R.id.textViewVideoDateCreated);
                    tv4.setText(selectedVideo.getDateCreated());

                    TextView tv5 = view.findViewById(R.id.textViewVideoFrameRate);
                    tv5.setText(selectedVideo.getFramerate() + " fps");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        view.findViewById(R.id.button_get_channels).setOnClickListener(new View.OnClickListener() { // get channels
            @Override
            public void onClick(View view) {
                try {
                    String[] array = TikTokController.catalog.getChannelArray();

                    ArrayAdapter<String> aa = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, array);
                    aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    channelSpinner.setAdapter(aa);

                    // register
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        view.findViewById(R.id.buttonCapture).setOnClickListener(new View.OnClickListener() { // capture a video
            @Override
            public void onClick(View view) {
                CreateVideoDialog d = new CreateVideoDialog();
                AlertDialog alertDialog = d.create();
                alertDialog.show();
            }
        });

        if (!TikTokController.isLoaded()) {
            if (requestPermissionsForExternalStorage() == true) { // permissions already given
                updateView();
            }
        } else {
            updateView();
        }
    }

    private String rememberName="";

    public class CreateVideoDialog {
        public AlertDialog create() {
            String channelname = channelSpinner.getSelectedItem().toString();

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.create_video_layout, null);

            EditText et = view.findViewById(R.id.channel);
            et.setText(channelname);

            EditText et2 = view.findViewById(R.id.videoName);

            builder.setView(view).setPositiveButton(R.string.capture, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

                    String channelname = channelSpinner.getSelectedItem().toString();
                    String path = StorageConfiguration.getNewVideoPath(channelname, et2.getText().toString() + ".mp4");

//                    Uri uri = Uri.fromFile(new File(path));
                    Uri uri= FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", new File(path));
                    videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                    rememberName = et2.getText().toString() + ".mp4";

                    if (videoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(videoIntent, VIDEO_REQUEST);
                    }
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(getContext(), "Save cancelled by the user", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                }
            }).setTitle("Capture a new video");

            return builder.create();
        }
    }


    private class NewVideoRunner extends AsyncTask<String, Void, String> {

        private final String channelname;
        private final String videoName;

        public NewVideoRunner(String channelname, String videoName) {
            this.channelname = channelname;
            this.videoName = videoName;
        }
        @Override
        protected String doInBackground(String... strings) {
            try {
                TikTokController.getPublisher().addVideo(channelname, videoName);
                return "OK";
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            loadVideos(channelname);
            Toast.makeText(getContext(), "Save successful", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIDEO_REQUEST) {
            String channelname = channelSpinner.getSelectedItem().toString();

            NewVideoRunner videoRunner = new NewVideoRunner(channelname, rememberName);
            videoRunner.execute();
        }
    }
}
