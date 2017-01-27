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
import java.util.Scanner;
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
    final static String URL_TOP100 = "http://78.140.251.40/tmp_audio/top100/";
    final static String UPLOAD_DIR_HISTORY = "e:/radio/history/";
    final static String UPLOAD_DIR_TOP100 = "e:/radio/top100/";
    static String UPLOAD_DIR = "";
    final static String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception
    {
        System.setProperty("java.net.useSystemProxies", "true");

        Scanner sc = new Scanner(System.in);
        App app = new App();

        System.out.println("1 history\n2 top 100\nother number - exit");
        int inputNumber = sc.nextInt();
        int i = 1;
        File file;

        switch (inputNumber)
        {
            case 1:
                List<StationHistory> stationsHistory = app.getStationsHistory();
                i = 1;
                for (StationHistory station : stationsHistory)
                {
                    System.out.println(i + " " + station.getName());
                    i++;
                }
                inputNumber = sc.nextInt();
                inputNumber--;
                System.out.println("\n-----------------------------\n");

                // Create folder if it is not exist
                UPLOAD_DIR += UPLOAD_DIR_HISTORY + stationsHistory.get(inputNumber).getName();
                file = new File(UPLOAD_DIR);
                if (!file.exists())
                {
                    file.mkdirs();
                }
                // Get days by station
                stationsHistory.get(inputNumber).setDays(app.getDays(stationsHistory.get(inputNumber)));
                for (Day day : stationsHistory.get(inputNumber).getDays())
                {

                    // Get tracks on day
                    day.setTracks(app.getTracks(day));

                    // Upload tracks on day
                    app.uploadTracks(day.getTracks());
                }
                break;

            case 2:
                List<StationTOP100> stationsTOP100 = app.getStationsTOP100();
                i = 1;
                for (StationTOP100 station : stationsTOP100)
                {
                    System.out.println(i + " " + station.getName());
                    i++;
                }
                inputNumber = sc.nextInt();
                inputNumber--;
                System.out.println("\n-----------------------------\n");

                // Create folder if it is not exist
                UPLOAD_DIR += UPLOAD_DIR_HISTORY + stationsTOP100.get(inputNumber).getName();
                file = new File(UPLOAD_DIR);
                if (!file.exists())
                {
                    file.mkdirs();
                }

                stationsTOP100.get(inputNumber).setTracks(app.getTracks(stationsTOP100.get(inputNumber)));
                app.uploadTracks(stationsTOP100.get(inputNumber).getTracks());
                break;
            default:
                break;
        }
        System.out.println("Good bye");
        sc.close();
    }

    private List<StationHistory> getStationsHistory() throws IOException
    {
        List<StationHistory> stations = new ArrayList<StationHistory>();

        Document doc = Jsoup.connect(URL_EVERYDAY).get();
        Elements links = doc.getElementsByTag("a");
        // remove back link
        links.remove(0);
        for (Element a : links)
        {
            StationHistory station = new StationHistory();
            station.setName(a.html().substring(0, a.html().length() - 1));
            station.setUrl(new URL(URL_EVERYDAY + a.attr("href")));
            stations.add(station);
        }
        return stations;
    }

    private List<StationTOP100> getStationsTOP100() throws IOException
    {
        List<StationTOP100> stations = new ArrayList<StationTOP100>();

        Document doc = Jsoup.connect(URL_TOP100).get();
        Elements links = doc.getElementsByTag("a");
        // remove back link
        links.remove(0);
        for (Element a : links)
        {
            StationTOP100 station = new StationTOP100();
            station.setName(a.html().substring(0, a.html().length() - 1));
            station.setUrl(new URL(URL_TOP100 + a.attr("href")));
            stations.add(station);
        }
        return stations;
    }

    private List<Day> getDays(StationHistory station) throws IOException
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

    private List<Track> getTracks(StationTOP100 station) throws IOException, URISyntaxException
    {
        List<Track> tracks = new ArrayList<Track>();
        Document doc = Jsoup.connect(station.getUrl().toString()).get();
        Elements links = doc.getElementsByTag("a");
        // remove back link
        links.remove(0);
        for (Element a : links)
        {
            String url = URLDecoder.decode(a.attr("href"), "UTF-8");
            Track track = new Track();
            track.setUrl(new URL((station.getUrl() + url).replaceAll(" ", "%20")));
            track.setName(url.substring(4));
            tracks.add(track);
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

            File f1 = new File(UPLOAD_DIR + "/" + track.getName());
            if (!f1.exists()
                    && (f1.length() > conn.getContentLength() - 100 || f1.length() < conn.getContentLength() + 100))
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
