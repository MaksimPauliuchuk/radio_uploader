package com.pauliuchuk.radio_uploader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
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
        // Checking that a directory is exist
        File file = new File(UPLOAD_DIR);
        if (!file.exists())
        {
            file.mkdirs();
        }

        App app = new App();
        List<Station> stations = app.getStations();

        // Future house radio
        Station future = stations.get(6);
        future.setDays(app.getDays(future));

        for (Day day : future.getDays())
        {
            day.setTracks(app.getTracks(day));
            app.uploadTracks(day.getTracks());
        }
        System.out.println("END");
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
                track.setUrl(new URL((day.getUrl() + url).replaceAll(" ", "%20")));
                track.setName(url.substring(11));
                tracks.add(track);
            }
        }
        return tracks;
    }

    private void uploadTracks(List<Track> tracks) throws Exception
    {
        int isEx = 0;
        for (Track track : tracks)
        {
            System.out.println(track.getUrl());

            HttpURLConnection conn = (HttpURLConnection) track.getUrl().openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);
            conn.setDoOutput(true);

            // FileWriter fw = new Fil
            File f1 = new File(UPLOAD_DIR + "/" + track.getName());
            if (!f1.exists() && f1.length() != conn.getContentLength())
            {
                BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
                FileOutputStream fw = new FileOutputStream(f1);

                byte[] b = new byte[1024];
                int count;

                while ((count = bis.read(b)) != -1)
                    fw.write(b, 0, count);

                fw.close();
            }
            else
            {
                isEx++;
                System.out.println("File exist");
            }

            conn.disconnect();
            System.out.println("OK - " + track.getName());
        }
        System.out.println(isEx + " files already exist");
    }

}
