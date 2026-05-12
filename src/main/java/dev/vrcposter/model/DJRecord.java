package dev.vrcposter.model;

import java.util.UUID;

public final class DJRecord {

    private final String id;
    private final String artistName;
    private final String discordName;
    private final String vrcName;
    private final String genres;
    private final String streamLink1;
    private final String streamLink2;
    private final String logoFilename;
    private final long createdAt;
    private final long updatedAt;

    public DJRecord(String id, String artistName, String discordName, String vrcName,
                    String genres, String streamLink1, String streamLink2,
                    String logoFilename, long createdAt, long updatedAt) {
        this.id = id;
        this.artistName = artistName;
        this.discordName = discordName;
        this.vrcName = vrcName;
        this.genres = genres;
        this.streamLink1 = streamLink1;
        this.streamLink2 = streamLink2;
        this.logoFilename = logoFilename;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DJRecord create(String artistName, String discordName, String vrcName,
                                   String genres, String streamLink1, String streamLink2) {
        long now = System.currentTimeMillis();
        return new DJRecord(UUID.randomUUID().toString(), artistName,
                discordName, vrcName, genres, streamLink1, streamLink2,
                null, now, now);
    }

    public String getId()           { return id; }
    public String getArtistName()   { return artistName; }
    public String getDiscordName()  { return discordName; }
    public String getVrcName()      { return vrcName; }
    public String getGenres()       { return genres; }
    public String getStreamLink1()  { return streamLink1; }
    public String getStreamLink2()  { return streamLink2; }
    public String getLogoFilename() { return logoFilename; }
    public long getCreatedAt()      { return createdAt; }
    public long getUpdatedAt()      { return updatedAt; }

    public DJRecord withLogoFilename(String logoFilename) {
        return new DJRecord(id, artistName, discordName, vrcName, genres,
                streamLink1, streamLink2, logoFilename, createdAt, System.currentTimeMillis());
    }
}
