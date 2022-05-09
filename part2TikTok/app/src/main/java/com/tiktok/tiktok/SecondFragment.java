package com.tiktok.tiktok;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import org.xml.sax.SAXException;

import java.io.IOException;

import Main.TikTokController;
import structures.VideoFile;

public class SecondFragment extends Fragment {
    private Spinner topicSpinner;
    private Spinner videoSpinner;
    private Context context;
    private Button buttonPlay;
    private Button buttonSave;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        context = this.getContext();
        return inflater.inflate(R.layout.fragment_subscriber, container, false);
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
                return TikTokController.registerAsConsumer(bip, bport);
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

    private class GetChannelsRunner extends AsyncTask<String, Void, String[]> {
        @Override
        protected String [] doInBackground(String... strings) {
            try {
                String [] data = TikTokController.getConsumer().askForChannels();
                return data;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            if (result != null) {
                String[] array = result;

                ArrayAdapter<String> aa = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, array);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                topicSpinner.setAdapter(aa);
            }
        }
    }

    private class GetTagsRunner extends AsyncTask<String, Void, String[]> {
        @Override
        protected String [] doInBackground(String... strings) {
            try {
                String [] data = TikTokController.getConsumer().askForTags();
                return data;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            if (result != null) {
                String[] array = result;

                ArrayAdapter<String> aa = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, array);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                topicSpinner.setAdapter(aa);
            }
        }
    }

    private class GetVideoRunner extends AsyncTask<String, Void, String[]> {
        @Override
        protected String [] doInBackground(String... strings) {
            try {
                String key = strings[0];

                try {
                    if (key.startsWith("#")) {
                        String tag = key.substring(1);
                        String[] data = TikTokController.getConsumer().askForVideosOfTag(tag);
                        return data;
                    } else {
                        String[] data = TikTokController.getConsumer().askForVideosOfChannel(key);
                        return data;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }


            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            if (result != null) {
                String[] array = result;

                ArrayAdapter<String> aa = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, array);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                videoSpinner.setAdapter(aa);
            }
        }
    }


    private class DownloadVideoRunner extends AsyncTask<String, Void, String> {

        private String topic;
        private String key;

        public DownloadVideoRunner(String topic, String key) {
            this.topic = topic;
            this.key = key;
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                boolean b =  TikTokController.downloadVideo(topic,key);
                return "OK";
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(getActivity(), "Video download complete. " + result, Toast.LENGTH_SHORT).show();

            buttonPlay.setVisibility(View.VISIBLE);
            buttonSave.setVisibility(View.INVISIBLE);

            VideoFile selectedVideo = null;
            try {
                String videoPath = TikTokController.getVideoPath(topic, key);
                selectedVideo = new VideoFile(key, topic, videoPath);

                if (selectedVideo != null) {
                    TextView tv1 = view.findViewById(R.id.textViewVideoName);
                    tv1.setText(selectedVideo.getVideoName());

                    TextView tv2 = view.findViewById(R.id.textViewVideoSize);
                    tv2.setText(selectedVideo.getBytes() + " bytes");

                    TextView tv3 = view.findViewById(R.id.textViewVideoDuration);
                    tv3.setText(selectedVideo.getDuration() + " seconds");

                    TextView tv4 = view.findViewById(R.id.textViewVideoDateCreated);
                    tv4.setText(selectedVideo.getDateCreated() );

                    TextView tv5 = view.findViewById(R.id.textViewVideoFrameRate);
                    tv5.setText(selectedVideo.getFramerate() + " fps");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }


        }
    }


    private void updateView() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

//            String[] array = TikTokController.catalog.getChannelArray();
//
//            ArrayAdapter<String> aa = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, array);
//            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//            channelSpinner.setAdapter(aa);


        if (!TikTokController.isRegisteredAsConsumer()) {
            String bip = prefs.getString("INITIAL_BROKER_IP", "192.168.2.17");
            int bport = Integer.parseInt(prefs.getString("INITIAL_BROKER_PORT", "14321"));

            RegisterRunner task = new RegisterRunner(bip, bport);
            task.execute();
        }

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        this.view = view;
        super.onViewCreated(view, savedInstanceState);

        topicSpinner = view.findViewById(R.id.topicSpinner);
        videoSpinner = view.findViewById(R.id.videoSpinner);
        buttonPlay  = view.findViewById(R.id.buttonPlay);
        buttonSave  = view.findViewById(R.id.buttonSave);

        view.findViewById(R.id.button_previous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( NavHostFragment.findNavController(SecondFragment.this).popBackStack(R.id.action_SecondFragment_to_FirstFragment, false)) {
                } else {
                    NavHostFragment.findNavController(SecondFragment.this).navigate(R.id.action_SecondFragment_to_FirstFragment);
                }
            }
        });

        view.findViewById(R.id.button_get_channels).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TikTokController.isRegisteredAsConsumer()) {
                    GetChannelsRunner runner = new GetChannelsRunner();
                    runner.execute();
                }
            }
        });

        view.findViewById(R.id.button_get_tags).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TikTokController.isRegisteredAsConsumer()) {
                    GetTagsRunner runner = new GetTagsRunner();
                    runner.execute();
                }
            }
        });

        topicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String key = topicSpinner.getSelectedItem().toString();

                GetVideoRunner runner= new GetVideoRunner();
                runner.execute(key);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        videoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View v, int i, long l) {
                String topic = topicSpinner.getSelectedItem().toString();
                String key = videoSpinner.getSelectedItem().toString();

                boolean ok = TikTokController.videoIsDownloaded(topic, key);

                if (ok) {
                    buttonPlay.setVisibility(View.VISIBLE);
                    buttonSave.setVisibility(View.INVISIBLE);

                    String videoPath = TikTokController.getVideoPath(topic, key);

                    VideoFile selectedVideo = null;
                    try {
                        selectedVideo = new VideoFile(key, topic, videoPath);

                        if (selectedVideo != null) {
                            TextView tv1 = view.findViewById(R.id.textViewVideoName);
                            tv1.setText(selectedVideo.getVideoName());

                            TextView tv2 = view.findViewById(R.id.textViewVideoSize);
                            tv2.setText(selectedVideo.getBytes() + " bytes");

                            TextView tv3 = view.findViewById(R.id.textViewVideoDuration);
                            tv3.setText(selectedVideo.getDuration() + " seconds");

                            TextView tv4 = view.findViewById(R.id.textViewVideoDateCreated);
                            tv4.setText(selectedVideo.getDateCreated() );

                            TextView tv5 = view.findViewById(R.id.textViewVideoFrameRate);
                            tv5.setText(selectedVideo.getFramerate() + " fps");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    }
                } else {
                    buttonPlay.setVisibility(View.INVISIBLE);
                    buttonSave.setVisibility(View.VISIBLE);

                    TextView tv1 = view.findViewById(R.id.textViewVideoName);
                    tv1.setText("");

                    TextView tv2 = view.findViewById(R.id.textViewVideoSize);
                    tv2.setText("");

                    TextView tv3 = view.findViewById(R.id.textViewVideoDuration);
                    tv3.setText("");

                    TextView tv4 = view.findViewById(R.id.textViewVideoDateCreated);
                    tv4.setText("" );

                    TextView tv5 = view.findViewById(R.id.textViewVideoFrameRate);
                    tv5.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String topic = topicSpinner.getSelectedItem().toString();
                String key = videoSpinner.getSelectedItem().toString();

                String path = TikTokController.getVideoPath(topic, key);

                if (path != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                    intent.setDataAndType(Uri.parse(path), "video/mp4");
                    startActivity(intent);
                }
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String topic = topicSpinner.getSelectedItem().toString();
                String key = videoSpinner.getSelectedItem().toString();

                DownloadVideoRunner task = new DownloadVideoRunner(topic, key);
                task.execute();
            }
        });

        updateView();
    }

}