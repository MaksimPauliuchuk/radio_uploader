package com.pauliuchuk.radio_uploader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Hello world!
 */
public class App
{
    final static String URL_EVERYDAY = "http://history.radiorecord.ru/air/";
    final static String UPLOAD_DIR = "d:/radio";
    final static String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception
    {
        App app = new App();
        List<Station> stations = app.getStations();

        // Future house radio
        Station future = stations.get(6);
        future.setDays(app.getDays(future));
        // Future house radio today
        Day day = future.getDays().get(1);
        day.setTracks(app.getTracks(day));
        app.uploadTracks(day.getTracks().subList(0, 1));
    }

    private List<Station> getStations() throws IOException
    {
        List<Station> stations = new ArrayList<Station>();

        Document doc = Jsoup.connect(URL_EVERYDAY).get();
        Elements links = doc.getElementsByTag("a");
        // remove back link
        links.remove(0);
        for (Element a : links)
        {
            Station station = new Station();
            station.setName(a.html().substring(0, a.html().length() - 1));
            station.setUrl(new URL(URL_EVERYDAY + a.attr("href")));
            stations.add(station);
        }
        return stations;
    }

    private List<Day> getDays(Station station) throws IOException
    {
        List<Day> days = new ArrayList<Day>();
        Document doc = Jsoup.connect(station.getUrl().toString()).get();
        Elements links = doc.getElementsByTag("a");
        // remove back link
        links.remove(0);
        for (Element a : links)
        {
            Day day = new Day();
            day.setName(a.html().substring(0, a.html().length() - 1));
            day.setUrl(new URL(station.getUrl().toString() + a.attr("href")));
            days.add(day);
        }
        return days;
    }

    private List<Track> getTracks(Day day) throws IOException, URISyntaxException
    {
        Pattern patternEmptyTrack = Pattern
                .compile("\\d\\d:\\d\\d:\\d\\d -  - .mp3|\\d\\d:\\d\\d:\\d\\d - Record Future House - VO Short.mp3");
        List<Track> tracks = new ArrayList<Track>();
        Document doc = Jsoup.connect(day.getUrl().toString()).get();
        Elements links = doc.getElementsByTag("a");
        // remove back link
        links.remove(0);
        for (Element a : links)
        {
            String url = URLDecoder.decode(a.attr("href"), "UTF-8");
            Matcher matcherEmptyTrack = patternEmptyTrack.matcher(url);
            if (!matcherEmptyTrack.find())
            {
                Track track = new Track();
                track.setPlayTime(url.substring(0, 8));
                track.setUrl(new URL(day.getUrl() + url));
                track.setName(url.substring(11));
                tracks.add(track);
            }
        }
        return tracks;
    }

    private void uploadTracks(List<Track> tracks) throws Exception
    {
        for (Track track : tracks)
        {
            URLConnection conn = track.getUrl().openConnection();
            InputStream is = conn.getInputStream();
            OutputStream outstream = new FileOutputStream(new File(UPLOAD_DIR + "/" + track.getName()));
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0)
            {
                outstream.write(buffer, 0, len);
            }
            outstream.close();
        }
    }

}
