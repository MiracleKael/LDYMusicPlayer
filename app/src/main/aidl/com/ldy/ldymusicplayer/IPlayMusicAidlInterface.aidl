// IPlayMusicAidlInterface.aidl
package com.ldy.ldymusicplayer;

// Declare any non-default types here with import statements

interface IPlayMusicAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
//    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
//            double aDouble, String aString);


/**
     * 根据播放列表位置打开音频文件
     * @param position
     */
    void openAudio(int position);
 /**
     * 开始播放
     */
     void start();
    /**
     * 暂停
     */
    void pause();
    /**
     * 下一首
     */
    void next();
    /**
     * 上一首
     */
    void pre();
    /**
     * 停止
     */
    void stop();
    /**
     * 改变播放速率
     */
    void changeSpeed(float speed);
    /**
     * 得到乐曲时长
     */
    int getDuration();
    /**
     * 得到当前播放进度，用于更新进度条
     */
    int getProgress();
     /**
         * 设置播放模式
         */
    void setPlayMode();
    /**
         * 是否正在播放
         * @return
         */
    boolean isPlaying();

     /**
         * 音频的拖动播放
         */
         void seekTo(int position);

}