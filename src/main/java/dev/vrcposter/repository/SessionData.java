package dev.vrcposter.repository;

import java.util.List;

public class SessionData {
    public String title;
    public String subtitle;
    public String date;
    public String backgroundPath;
    public String clubLogoPath;
    public String qrCodePath;
    public List<TimeslotData> timeslots;

    public static class TimeslotData {
        public String startTime;
        public String assignedDjId;
        public String assignedDjArtistName;
        public String genre;
    }
}
