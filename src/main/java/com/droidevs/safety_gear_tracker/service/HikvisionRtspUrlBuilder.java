package com.droidevs.safety_gear_tracker.service;

public class HikvisionRtspUrlBuilder {

    private String ipAddress;
    private int port;
    private String username;
    private String password;

    public HikvisionRtspUrlBuilder(String ipAddress, int port, String username, String password) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String buildRtspUrl(int channel, boolean mainStream) {
        String streamType = mainStream ? "01" : "02"; // 01 = main, 02 = sub
        String channelId = channel + streamType;      // e.g., 101 = channel 1 main

        return String.format(
                "rtsp://%s:%s@%s:%d/Streaming/Channels/%s",
                username, password, ipAddress, port, channelId
        );
    }
}
