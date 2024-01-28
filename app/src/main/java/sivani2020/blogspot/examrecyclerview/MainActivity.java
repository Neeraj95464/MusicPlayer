package sivani2020.blogspot.examrecyclerview;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private TextView noMusicTextView;
    private static final String CHANNEL_ID = "MusicPlayer";
    private static final int NOTIFICATION_ID = 1;
    private ArrayList<SongModel> songsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        noMusicTextView = findViewById(R.id.noSongText);
        viewPager2 = findViewById(R.id.viewPager);

        if (checkPermissions()) {
            loadAudioFiles();
            createNotificationChannel();
        } else {
            requestPermissions();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Music Player", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Music Player Notification Channel");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private boolean checkPermissions() {
        int readStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return readStoragePermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (Environment.isExternalStorageManager()) {
                loadAudioFiles();
            } else {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (Environment.isExternalStorageManager()) {
                    loadAudioFiles();
                } else {
                    Log.e("PermissionError", "Permission denied!");
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || Environment.isExternalStorageManager()) {
                    loadAudioFiles();
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadAudioFiles() {
        songsList = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION};
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'audio/%'";
        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SongModel songData = new SongModel(cursor.getString(1), cursor.getString(0), cursor.getString(2));
                songsList.add(songData);
            }
            cursor.close();
        }

        if (songsList.size() > 0) {
            MyAdapter adapter = new MyAdapter(songsList, this, viewPager2);
//            adapter.setNotificationListener(this);
            viewPager2.setAdapter(adapter);
        } else {
            noMusicTextView.setVisibility(View.VISIBLE);
        }
    }

//    private void showNotification(String songTitle, boolean isPlaying) {
//        // Create a PendingIntent for the Play action
//        Intent playIntent = new Intent(this, NotificationActionReceiver.class);
//        playIntent.setAction("PLAY");
//        PendingIntent playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Create a PendingIntent for the Pause action
//        Intent pauseIntent = new Intent(this, NotificationActionReceiver.class);
//        pauseIntent.setAction("PAUSE");
//        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Create the notification
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setSmallIcon(R.drawable.app_logo)
//                .setContentTitle("Music Player")
//                .setContentText(songTitle)
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setOnlyAlertOnce(true)
//                .setOngoing(true)
//                .setAutoCancel(false);
//
//        // Add the play and pause actions
//        if (isPlaying) {
//            builder.addAction(R.drawable.ic_baseline_pause_circle_outline_24, "Pause", pausePendingIntent);
//        } else {
//            builder.addAction(R.drawable.ic_baseline_play_circle_outline_24, "Play", playPendingIntent);
//        }
//
//        // Set the notification's click action to launch the MainActivity
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
//        builder.setContentIntent(pendingIntent);
//
//        // Show the notification
//        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
//        notificationManager.notify(NOTIFICATION_ID, builder.build());
//    }
}
