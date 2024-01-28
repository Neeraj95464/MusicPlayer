package sivani2020.blogspot.examrecyclerview;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.io.IOException;
import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    private ArrayList<SongModel> songsList;
    private Context context;
    private MediaPlayer mediaPlayer;
    private ViewPager2 viewPager;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private int pagePosition;
    private int currentPlayingPosition = -1;
    private boolean isLooping = false;

    public MyAdapter(ArrayList<SongModel> songsList, Context context, ViewPager2 viewPager) {
        this.songsList = songsList;
        this.context = context;
        this.viewPager = viewPager;
        mediaPlayer = new MediaPlayer();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_page, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final SongModel songData = songsList.get(position);
        holder.titleTextView.setText(songData.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    holder.playPause.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                } else {
                    if (currentPlayingPosition != position) {
                        currentPlayingPosition = position;
                        mediaPlayer.reset();
                        try {
                            mediaPlayer.setDataSource(context, Uri.parse(songData.getPath()));
                            mediaPlayer.prepareAsync();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mediaPlayer.start();
                                    holder.playPause.setImageDrawable(null);
                                }
                            });
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    playNextSong(position);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        mediaPlayer.start();
                        holder.playPause.setImageDrawable(null);
                    }
                }
            }
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                pagePosition = position;
                if (position != holder.getAdapterPosition()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    currentPlayingPosition = -1;
                    SongModel selectedSong = songsList.get(position);
                    try {
                        mediaPlayer.setDataSource(context, Uri.parse(selectedSong.getPath()));
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        holder.playPause.setImageDrawable(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mRunnable);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mHandler.removeCallbacks(mRunnable);
                mediaPlayer.seekTo(seekBar.getProgress());
                updateSeekBar(holder);
            }
        });

        holder.loopSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isLooping = !isLooping;
                mediaPlayer.setLooping(isLooping);
                Toast.makeText(context, "Now this song will play in loop " + isLooping, Toast.LENGTH_SHORT).show();
            }
        });

        updateSeekBar(holder);
    }

    private void playNextSong(int currentPosition) {
        int nextPosition = currentPosition + 1;
        if (currentPosition == pagePosition) {
            Toast.makeText(context, "yes matched", Toast.LENGTH_SHORT).show();
        }
        if (nextPosition < songsList.size()) {
            SongModel nextSong = songsList.get(nextPosition);
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(context, Uri.parse(nextSong.getPath()));
                mediaPlayer.prepareAsync();
                viewPager.setCurrentItem(nextPosition);
                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mediaPlayer.start();
                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (isLooping) {
                            mediaPlayer.start(); // Start playing the same song again
                        } else {
                            playNextSong(nextPosition); // Play the next song
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // If the next position exceeds the song list size, stop the playback
            mediaPlayer.stop();
            mediaPlayer.reset();
            currentPlayingPosition = -1;
        }
    }

    @Override
    public int getItemCount() {
        return songsList.size();
    }

    private void updateSeekBar(final ViewHolder holder) {
        holder.seekBar.setMax(mediaPlayer.getDuration());
        holder.totalTime.setText(formatTime(mediaPlayer.getDuration()));
        holder.currentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));

        mRunnable = new Runnable() {
            @Override
            public void run() {
                holder.seekBar.setProgress(mediaPlayer.getCurrentPosition());
                holder.currentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mRunnable, 1000);
    }

    private String formatTime(int duration) {
        int seconds = (duration / 1000) % 60;
        int minutes = (duration / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, currentTime, totalTime;
        SeekBar seekBar;
        ImageView playPause, MusicIcon, loveSong, listSong, loopSong;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_textView);
            playPause = itemView.findViewById(R.id.playPauseButton);
            MusicIcon = itemView.findViewById(R.id.music_icon_big);
            seekBar = itemView.findViewById(R.id.seek_bar);
            currentTime = itemView.findViewById(R.id.current_time);
            totalTime = itemView.findViewById(R.id.total_time);
            loveSong = itemView.findViewById(R.id.loveSongs);
            loopSong = itemView.findViewById(R.id.loopSong);
            listSong = itemView.findViewById(R.id.listSongs);
        }
    }
}
