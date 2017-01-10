package com.pauliuchuk.radio_uploader;

import java.net.URL;

public class Track
{
    private String name;
    private URL url;
    private String playTime;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public URL getUrl()
    {
        return url;
    }

    public void setUrl(URL url)
    {
        this.url = url;
    }

    public String getPlayTime()
    {
        return playTime;
    }

    public void setPlayTime(String playTime)
    {
        this.playTime = playTime;
    }

    @Override
    public String toString()
    {
        return "\nTrack [name=" + name + ", url=" + url + ", playTime=" + playTime + "]";
    }

}
