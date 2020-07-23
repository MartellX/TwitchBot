package controllers;

import api.UnofficialTwitchApi;
import com.iheartradio.m3u8.*;
import com.iheartradio.m3u8.data.MasterPlaylist;

import java.io.IOException;
import java.io.InputStream;

public class M3U8PlaylistData{
    private String channelName;
    private MasterPlaylist masterPlaylist;
    private long getted_at;

    public M3U8PlaylistData(String channelName) {
        this.channelName = channelName;
        try {
            this.masterPlaylist = getNewPlaylist();
            if (masterPlaylist == null) {
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (PlaylistException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.getted_at = System.currentTimeMillis();
    }

    public M3U8PlaylistData(String channelName, MasterPlaylist masterPlaylist) {
        this.channelName = channelName;
        this.masterPlaylist = masterPlaylist;
        this.getted_at = System.currentTimeMillis();
    }

    public MasterPlaylist forceUpdate() {
        getted_at = System.currentTimeMillis();
        try {
            this.masterPlaylist = getNewPlaylist();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (PlaylistException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return masterPlaylist;
    }

    public MasterPlaylist getMasterPlaylist() {
        long currTime = System.currentTimeMillis();
        if ((currTime - getted_at) / (60 * 1000) > 15) {
            try {
                this.masterPlaylist = getNewPlaylist();
                if (this.masterPlaylist == null) {
                    return null;
                }
                getted_at = System.currentTimeMillis();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (PlaylistException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return masterPlaylist;
    }

    public void setMasterPlaylist(MasterPlaylist masterPlaylist) {
        getted_at = System.currentTimeMillis();
        this.masterPlaylist = masterPlaylist;
    }

    private MasterPlaylist getNewPlaylist() throws ParseException, PlaylistException, IOException {
        InputStream M3U8Data = UnofficialTwitchApi.getInstance().getM3U8ofChannel(channelName);
        if (M3U8Data == null) {
            return null;
        }
        MasterPlaylist masterPlaylist = new PlaylistParser(M3U8Data, Format.EXT_M3U, Encoding.UTF_8, ParsingMode.LENIENT)
                .parse()
                .getMasterPlaylist();
        return masterPlaylist;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }


        if (!(obj instanceof M3U8PlaylistData)) {
            return false;
        }

        return ((M3U8PlaylistData) obj).channelName.equals(this.channelName);
    }
}
