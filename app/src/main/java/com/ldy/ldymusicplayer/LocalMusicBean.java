package com.ldy.ldymusicplayer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ${jiaojing} on 2017/10/7.
 * Desc：本地音乐
 */

public class LocalMusicBean implements Parcelable{
    private long id;
    private String title;
    private String album;
    private int duration;
    private long size;
    private String artist;
    private String path;

    public LocalMusicBean(Parcel in) {
        id = in.readLong();
        title = in.readString();
        album = in.readString();
        duration = in.readInt();
        size = in.readLong();
        artist = in.readString();
        path = in.readString();
    }

    public static final Creator<LocalMusicBean> CREATOR = new Creator<LocalMusicBean>() {
        @Override
        public LocalMusicBean createFromParcel(Parcel in) {
            return new LocalMusicBean(in);
        }

        @Override
        public LocalMusicBean[] newArray(int size) {
            return new LocalMusicBean[size];
        }
    };

    public LocalMusicBean(long id, String title) {
        this.id = id;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(album);
        parcel.writeString(artist);
        parcel.writeString(path);
        parcel.writeInt(duration);
        parcel.writeLong(size);
    }


}
