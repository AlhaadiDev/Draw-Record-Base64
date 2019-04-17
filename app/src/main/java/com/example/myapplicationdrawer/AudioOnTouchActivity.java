package com.example.myapplicationdrawer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class AudioOnTouchActivity extends Activity {
    Button b1;
//        private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".wav";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private MediaRecorder recorder = null;
    private int currentFormat = 1;
    private int output_formats[] = {MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP};
    private String file_exts[] = {AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP};

    String audioFilePath;
    String audioBase64;

    /**
     * Called when the activity is first created.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        b1 = (Button) findViewById(R.id.button1);
        b1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        AppLog.logString("Start Recording");
                        startRecording();
                        return true;
                    case MotionEvent.ACTION_UP:
                        AppLog.logString("stop Recording");
                        stopRecording();
                        break;
                }
                return false;
            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer(audioFilePath);
//                convertBase64toAudio(audioBase64);
            }
        });
    }

    public void audioPlayer(String path) {
        Log.d("asd", "asdAudio1000: " + path);
        //set up MediaPlayer
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(path);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        ((TextView) findViewById(R.id.txt_file_store)).setText("File path:  " + file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
        audioFilePath = file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat];
        Log.d("urlStoreImages", "AudioFile: " + audioFilePath);
        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
    }

    private void startRecording() {
        Toast.makeText(AudioOnTouchActivity.this, "Start Recording", Toast.LENGTH_SHORT).show();
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        try {
            if (null != recorder) {
                audioBase64 = convertAudioToBase64(audioFilePath);
                CanvasImageUndo.canvasImageUndo.audioBase64 = audioBase64;
                Toast.makeText(AudioOnTouchActivity.this, "Stop Recording", Toast.LENGTH_SHORT).show();
                recorder.stop();
                recorder.reset();
                recorder.release();

                recorder = null;
//                finish();
            }
        } catch (Throwable e) {
            Toast.makeText(AudioOnTouchActivity.this, "Recording audio failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public static String convertAudioToBase64(String audioPath) {
        String _audioBase64 = null;
        byte[] audioBytes;
        try {
            // Just to check file size.. Its is correct i-e; Not Zero
            File audioFile = new File(audioPath);
            long fileSize = audioFile.length();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileInputStream fis = new FileInputStream(new File(audioPath));
            byte[] buf = new byte[1024];
            int n;
            while (-1 != (n = fis.read(buf)))
                baos.write(buf, 0, n);
            audioBytes = baos.toByteArray();
            // Here goes the Base64 string
            _audioBase64 = Base64.encodeToString(audioBytes, Base64.DEFAULT);

            final int chunkSize = 1000;
            for (int i = 0; i < _audioBase64.length(); i += chunkSize) {
                Log.d("Base64Audio: ", _audioBase64.substring(i, Math.min(_audioBase64.length(), i + chunkSize)));
            }

        } catch (Exception e) {
        }

        Log.d("asd", "asdAudio: " + _audioBase64 + " :path: " + audioPath);
        return _audioBase64;
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            AppLog.logString("Warning: " + what + ", " + extra);
        }
    };

    public void convertBase64toAudio(String ausioBase64) {
        File fileName = null;
        MediaPlayer mp;
        String path = getFilename();

        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        try {
            byte[] decoded = Base64.decode(ausioBase64, Base64.DEFAULT);
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
            fos.write(decoded);
            fos.close();
            try {
                mp = new MediaPlayer();
                mp.setDataSource(path);
                mp.prepare();
                mp.start();

            } catch (Exception e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}