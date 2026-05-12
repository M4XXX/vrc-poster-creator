package dev.vrcposter.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.nio.file.Path;

public class TimeslotEntry {

    private final StringProperty startTime = new SimpleStringProperty("");
    private final StringProperty assignedDjId = new SimpleStringProperty();
    private final StringProperty assignedDjArtistName = new SimpleStringProperty();
    private final ObjectProperty<Path> assignedDjLogoPath = new SimpleObjectProperty<>();

    private TimeslotEntry() {}

    public static TimeslotEntry create(String startTime) {
        if (startTime == null) throw new IllegalArgumentException("startTime must not be null");
        TimeslotEntry entry = new TimeslotEntry();
        entry.setStartTime(startTime);
        return entry;
    }

    public void clearAssignment() {
        assignedDjId.set(null);
        assignedDjArtistName.set(null);
        assignedDjLogoPath.set(null);
    }

    // startTime triad
    public String getStartTime() { return startTime.get(); }
    public void setStartTime(String value) { startTime.set(value); }
    public StringProperty startTimeProperty() { return startTime; }

    // assignedDjId triad
    public String getAssignedDjId() { return assignedDjId.get(); }
    public void setAssignedDjId(String value) { assignedDjId.set(value); }
    public StringProperty assignedDjIdProperty() { return assignedDjId; }

    // assignedDjArtistName triad
    public String getAssignedDjArtistName() { return assignedDjArtistName.get(); }
    public void setAssignedDjArtistName(String value) { assignedDjArtistName.set(value); }
    public StringProperty assignedDjArtistNameProperty() { return assignedDjArtistName; }

    // assignedDjLogoPath triad
    public Path getAssignedDjLogoPath() { return assignedDjLogoPath.get(); }
    public void setAssignedDjLogoPath(Path value) { assignedDjLogoPath.set(value); }
    public ObjectProperty<Path> assignedDjLogoPathProperty() { return assignedDjLogoPath; }
}
