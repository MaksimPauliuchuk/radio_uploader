package com.pauliuchuk.radio_uploader;

import java.net.URL;
import java.util.List;

public class StationTOP100
{
    private String name;
    private URL url;
    private List<Track> tracks;

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

    public List<Track> getTracks()
    {
        return tracks;
    }

    public void setTracks(List<Track> tracks)
    {
        this.tracks = tracks;
    }

    @Override
    public String toString()
    {
        return "StationTOP100 [name=" + name + ", url=" + url + ", days=" + tracks + "]";
    }

}
