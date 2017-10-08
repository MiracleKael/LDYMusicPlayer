package com.ldy.ldymusicplayer.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;

import com.ldy.ldymusicplayer.LocalMusicBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ${jiaojing} on 2017/10/7.
 * Desc：
 */

public class MusicLoader {
    private String TAG = getClass().getSimpleName();
    private List<LocalMusicBean> musicList = new ArrayList<>();
    private static ContentResolver mContentResolver;
    //Uri，指向external的database
    private Uri contentUri = Media.EXTERNAL_CONTENT_URI;
    //projection：选择的列; where：过滤条件; sortOrder：排序。
    private String[] projection = {
            Media._ID,
            Media.DISPLAY_NAME,
            Media.DATA,
            Media.ALBUM,
            Media.ARTIST,
            Media.DURATION,
            Media.SIZE,
    };
    private String where = "mime_type in ('audio/mpeg','audio/x-ms-wma') and bucket_display_name <> 'audio' and is_music > 0";
    private String sortOrder = Media.DATA;
    private static MusicLoader musicLoader;
    private MusicLoader(){
        //利用ContentResolver的query函数来查询数据，然后将得到的结果放到MusicInfo对象中，最后放到数组中
        Cursor cursor = mContentResolver.query(contentUri, null, null, null, null);
        if(cursor == null) {
            Log.e(TAG,"本地没有查找到音乐");
        }else if(cursor.moveToNext()) {
            int displayNameCol = cursor.getColumnIndex(Media.DISPLAY_NAME);
            int albumCol = cursor.getColumnIndex(Media.ALBUM);
            int idCol = cursor.getColumnIndex(Media._ID);
            int durationCol = cursor.getColumnIndex(Media.DURATION);
            int sizeCol = cursor.getColumnIndex(Media.SIZE);
            int artistCol = cursor.getColumnIndex(Media.ARTIST);
            int urlCol = cursor.getColumnIndex(Media.DATA);

            do {
                String title = cursor.getString(displayNameCol);
                String album = cursor.getString(albumCol);
                long id = cursor.getLong(idCol);
                int duration = cursor.getInt(durationCol);
                long size = cursor.getLong(sizeCol);
                String artist = cursor.getString(artistCol);
                String url = cursor.getString(urlCol);

                LocalMusicBean musicInfo = new LocalMusicBean(id, title);
                musicInfo.setAlbum(album);
                musicInfo.setDuration(duration);
                musicInfo.setSize(size);
                musicInfo.setArtist(artist);
                musicInfo.setPath(url);
                musicList.add(musicInfo);
            }while (cursor.moveToNext());
        }

        cursor.close();
    }

    public List<LocalMusicBean> getMusicList(){
        return musicList;
    }

    public static MusicLoader getInstance(ContentResolver contentResolver){
        if(musicLoader == null) {
            mContentResolver = contentResolver;
            musicLoader = new MusicLoader();
        }
        return musicLoader;
    }

    public Uri getMusicUriById(long id){
        Uri uri = ContentUris.withAppendedId(contentUri, id);
        return uri;
    }

}
