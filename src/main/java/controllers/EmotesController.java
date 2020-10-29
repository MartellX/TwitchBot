package controllers;

import api.EmotesGetter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EmotesController {
    EmotesGetter emotesGetter = new EmotesGetter();
    Map<String, Map<String, String>> channelEmotesMap = new HashMap<>();
    Map<String, String> globalTwitchMap = new HashMap<>();
    Map<String, String> globalThirdPartyMap = new HashMap<>();

    EmotesController() {
        try {
            globalTwitchMap = emotesGetter.getGlobalTwitchEmotes();
            var BTTVMap = emotesGetter.getGlobalBTTVEmotes();
            var FFZMap = emotesGetter.getGlobalFFZEmotes();
            globalThirdPartyMap.putAll(BTTVMap);
            globalThirdPartyMap.putAll(FFZMap);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void updateChannelEmotes(String channel) {
            //var twitchMap = emotesGetter.getTwitchEmotes(channel);
            var BTTVMap = emotesGetter.getBTTVEmotes(channel);
            var FFZMap = emotesGetter.getFFZEmotes(channel);

            Map<String, String> resultMap = new HashMap<>();

            if (BTTVMap != null) {
                resultMap.putAll(BTTVMap);
            }
            if (FFZMap != null) {
                resultMap.putAll(FFZMap);
            }


            if (!resultMap.isEmpty()) {
                channelEmotesMap.put(channel, resultMap);
            }


    }

    void updateAllChannelsEmotes() {
        for (var channel:channelEmotesMap.keySet()
             ) {
            updateChannelEmotes(channel);
        }
    }

    String getEmoteUrl(String emote, String channel) {
//        for (String twEmote : globalTwitchMap.keySet()) {
//            if (emote.matches(twEmote)) {
//                return globalTwitchMap.get(twEmote);
//            }
//        }

        if (globalThirdPartyMap.containsKey(emote)) {
            return globalThirdPartyMap.get(emote);
        }

        if (!channelEmotesMap.containsKey(channel)) {
            updateChannelEmotes(channel);
        }

        Map<String, String> emotesMap = channelEmotesMap.get(channel);
        if (emotesMap.containsKey(emote)) {
            return emotesMap.get(emote);
        }
        return null;
    }
}
