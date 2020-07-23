package controllers;

import api.UnofficialTwitchApi;
import com.iheartradio.m3u8.*;
import com.iheartradio.m3u8.data.MasterPlaylist;
import com.iheartradio.m3u8.data.MediaPlaylist;
import com.iheartradio.m3u8.data.Playlist;
import com.iheartradio.m3u8.data.TrackData;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class M3U8Controller {
    Map<String, M3U8PlaylistData> channelPlaylists = new HashMap<>();


    public void addChannel(String channel) {
        channelPlaylists.put(channel, new M3U8PlaylistData(channel));
    }

    private M3U8PlaylistData getPlaylist(String channel) {
        if (channelPlaylists.containsKey(channel)) {
            return channelPlaylists.get(channel);
        } else {
            return new M3U8PlaylistData(channel);
        }

    }

    private MediaPlaylist getAudioPlaylistForChannel(String channel) throws ParseException, PlaylistException, IOException {
        M3U8PlaylistData chnl = getPlaylist(channel);
        if (chnl == null) {
            return null;
        }
        MasterPlaylist masterPlaylist = chnl.getMasterPlaylist();
        /*MasterPlaylist masterPlaylist = null;
        try {
            masterPlaylist = new PlaylistParser(new FileInputStream("response.m3u8"), Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT)
                    .parse()
                    .getMasterPlaylist();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (PlaylistException e) {
            e.printStackTrace();
        }
        if (masterPlaylist == null) {
            return null;
        }

         */
        int size = masterPlaylist.getPlaylists().size();
        InputStream mediaPlaylistStream = null;
        try{
            mediaPlaylistStream = new URL(masterPlaylist.getPlaylists().get(size - 1).getUri()).openConnection().getInputStream();
        } catch (IOException e) {
            chnl.forceUpdate();
            masterPlaylist = chnl.getMasterPlaylist();
            if (masterPlaylist == null) {
                return null;
            }
            size = masterPlaylist.getPlaylists().size();
            mediaPlaylistStream = new URL(masterPlaylist.getPlaylists().get(size - 1).getUri()).openConnection().getInputStream();
        }

        MediaPlaylist mediaPlaylist = new PlaylistParser(mediaPlaylistStream, Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT)
                .parse()
                .getMediaPlaylist();
        return mediaPlaylist;
    }

    public List<String> getLastTsUrls(String channel, int count) {
        List<String> tracksUrl = new ArrayList<>();
        try {
            MediaPlaylist mediaPlaylist = getAudioPlaylistForChannel(channel);
            if (mediaPlaylist == null) {
                return null;
            }
            List<TrackData> tracks = mediaPlaylist.getTracks();
            int tracksSize = tracks.size();
            if (count > tracksSize) {
                count = tracksSize;
            }
            for (int i = count; i > 0; i--) {
                String ts = tracks.get(tracksSize - i).getUri();
                tracksUrl.add(ts);
            }

        } catch (IOException | ParseException | PlaylistException e) {
            e.printStackTrace();
        }
        return tracksUrl;
    }
}

