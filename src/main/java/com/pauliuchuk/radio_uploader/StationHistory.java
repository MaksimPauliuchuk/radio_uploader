package com.pauliuchuk.radio_uploader;

import java.net.URL;
import java.util.List;

public class StationHistory
{
    private String name;
    private URL url;
    private List<Day> days;

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

    public List<Day> getDays()
    {
        return days;
    }

    public void setDays(List<Day> days)
    {
        this.days = days;
    }

    @Override
    public String toString()
    {
        return "StationHistory [name=" + name + ", url=" + url + ", days=" + days + "]";
    }

}
