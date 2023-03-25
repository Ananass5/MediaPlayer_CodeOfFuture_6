package com.ananas.mediaplayer_codeoffuture_6;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements Runnable {

    // создание полей
    private MediaPlayer mediaPlayer; // создание поля медиа-плеера
    private SeekBar seekBar; // создание поля SeekBar
    private boolean wasPlaying = false; // поле проигрывания аудио-файла
    private FloatingActionButton fabPlayPause; // поле кнопки проигрывания и постановки на паузу аудиофайла
    private FloatingActionButton fabBack;
    private FloatingActionButton fabForward;
    private FloatingActionButton fabNextSong;
    private Switch loopSong;
    private TextView seekBarHint; // поле информации у SeekBar
    private String[] listOfSongs;

    private static int numOfSong = 0;

    private TextView metaDataAudio;
    private String metaData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // присваивание полям id ресурсов
        fabPlayPause = findViewById(R.id.fabPlayPause);
        fabBack = findViewById(R.id.fabBack);
        fabForward = findViewById(R.id.fabForward);
        fabNextSong = findViewById(R.id.fabNextSong);
        loopSong = findViewById(R.id.loopSong);
        seekBarHint = findViewById(R.id.seekBarHint);
        seekBar = findViewById(R.id.seekBar);
        metaDataAudio = findViewById(R.id.metaDataAudio);

        fabPlayPause.setOnClickListener(listener);
        fabNextSong.setOnClickListener(listener);
        fabBack.setOnClickListener(listener);
        fabForward.setOnClickListener(listener);
        loopSong.setOnClickListener(listener);


        // создание слушателя изменения SeekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            // метод при перетаскивании ползунка по шкале,
            // где progress позволяет получить нове значение ползунка (позже progress назрачается длина трека в миллисекундах)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE); // установление видимости seekBarHint

                SimpleDateFormat durationFormat = new SimpleDateFormat("mm:ss");
                seekBarHint.setText(durationFormat.format(new Date(progress)));

                // передвижение времени отсчёта трека
                double percentTrack = progress / (double) seekBar.getMax(); // получение процента проигранного трека (проигранное время делится на длину трека)
                // seekBar.getX() - начало seekBar по оси Х
                // seekBar.getWidth() - ширина контейнера seekBar
                // 0.9 - поправочный коэффициент (так как seekBar занимает не всю ширину своего контейнера)
                seekBarHint.setX(seekBar.getX() + Math.round(seekBar.getWidth() * percentTrack * 0.9));

                if (progress > 0 && mediaPlayer != null && !mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer не воспроизводится
                    // назначение кнопке картинки play
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
                }
            }

            // метод при начале перетаскивания ползунка по шкале
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // метод при завершении перетаскивания ползунка по шкале
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) { // если mediaPlayer не пустой и mediaPlayer воспроизводится
                    mediaPlayer.seekTo(seekBar.getProgress()); // обновление позиции трека при изменении seekBar
                }
            }
        });

    }

    // метод запуска аудио-файла
    public void playSong() {

        try { // обработка исключения на случай отстутствия файла
            listOfSongs = getApplicationContext().getAssets().list(""); // массив имён файлов


            if (mediaPlayer != null && mediaPlayer.isPlaying()) { // если mediaPlayer не пустой и mediaPlayer воспроизводится
                wasPlaying = true; // инициализация значения запуска аудио-файла
                mediaPlayer.pause();
                // назначение кнопке картинки play
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_play));
            }

            if (mediaPlayer != null && !mediaPlayer.isPlaying() && !wasPlaying) { // если медиаПлеер не пустой и не воспроизводится
                wasPlaying = true;

                mediaPlayer.start();
                new Thread(this).start(); // запуск дополнительного потока
                // назначение кнопке картинки пауза
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
            }

            if (!wasPlaying) {
                if (mediaPlayer == null) { // если mediaPlayer пустой
                    mediaPlayer = new MediaPlayer(); // то выделяется для него память
                }
                // назначение кнопке картинки pause
                fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));

                // альтернативный способ считывания файла с помощью файлового дескриптора
                AssetFileDescriptor descriptor = getAssets().openFd(listOfSongs[numOfSong]);
                // запись файла в mediaPlayer, задаются параметры (путь файла, смещение относительно начала файла, длина аудио в файле)
                mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());

                // запись метаданных в окно вывода информации
                MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                metadataRetriever.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());

                String author = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                if (author == null) metaData = "Автор неизвестен";
                else metaData = author;

                metaData += " — " + title;
                metaDataAudio.setText(metaData);

                metadataRetriever.release();
                descriptor.close(); // закрытие дескриптора

                mediaPlayer.prepare(); // ассинхронная подготовка плейера к проигрыванию
                //mediaPlayer.setVolume(0.7f, 0.7f); // задание уровня громкости левого и правого динамиков
                mediaPlayer.setLooping(false); // назначение отстутствия повторов
                seekBar.setMax(mediaPlayer.getDuration()); // ограниечение seekBar длинной трека

                mediaPlayer.start(); // старт mediaPlayer
                new Thread(this).start(); // запуск дополнительного потока
            }

            wasPlaying = false; // возврат отсутствия проигрывания mediaPlayer

        } catch (Exception e) { // обработка исключения на случай отстутствия файла
            e.printStackTrace(); // вывод в консоль сообщения отсутствия файла
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                numOfSong++;
                clearMediaPlayer();
            }
        });

    }

    private final View.OnClickListener listener = view -> {
        switch (view.getId()) {
            case R.id.fabPlayPause:
                playSong();
                break;
            case R.id.fabBack:
                if (mediaPlayer != null)
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
                break;
            case R.id.fabForward:
                if (mediaPlayer != null)
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
                break;
            case R.id.fabNextSong:
                try {
                    if (numOfSong < listOfSongs.length - 2) {
                        numOfSong++;
                        clearMediaPlayer();
                    } else {
                        Toast.makeText(this, "Больше композиций нет", Toast.LENGTH_SHORT).show();
                        numOfSong = listOfSongs.length - 1;
                    }
                    playSong();
                    fabPlayPause.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_media_pause));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.loopSong:
                mediaPlayer.setLooping(loopSong.isChecked());
                break;
        }

    };

    // при уничтожении активити вызов метода остановки и очиски MediaPlayer
    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    // метод остановки и очиски MediaPlayer
    private void clearMediaPlayer() {
        mediaPlayer.stop(); // остановка медиа
        mediaPlayer.release(); // освобождение ресурсов
        mediaPlayer = null; // обнуление mediaPlayer
    }

    // метод дополнительного потока для обновления SeekBar
    @Override
    public void run() {
        int currentPosition = mediaPlayer.getCurrentPosition(); // считывание текущей позиции трека
        int total = mediaPlayer.getDuration(); // считывание длины трека

        // бесконечный цикл при условии не нулевого mediaPlayer, проигрывания трека и текущей позиции трека меньше длины трека
        while (mediaPlayer != null && mediaPlayer.isPlaying() && currentPosition < total) {
            try {

                Thread.sleep(1000); // засыпание вспомогательного потока на 1 секунду
                currentPosition = mediaPlayer.getCurrentPosition(); // обновление текущей позиции трека

            } catch (InterruptedException e) { // вызывается в случае блокировки данного потока
                e.printStackTrace();
                return; // выброс из цикла
            } catch (Exception e) {
                return;
            }

            seekBar.setProgress(currentPosition); // обновление seekBar текущей позицией трека

        }
    }

}