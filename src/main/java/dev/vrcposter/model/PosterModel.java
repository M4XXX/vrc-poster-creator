package dev.vrcposter.model;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;

public class PosterModel {

    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty subtitle = new SimpleStringProperty("");
    private final StringProperty date = new SimpleStringProperty("");
    private final ObjectProperty<Path> backgroundPath = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> clubLogoPath = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> qrCodePath = new SimpleObjectProperty<>();

    private final ObservableList<TimeslotEntry> timeslots = FXCollections.observableArrayList(
        te -> new Observable[]{
            te.startTimeProperty(), te.assignedDjIdProperty(),
            te.assignedDjArtistNameProperty(), te.assignedDjLogoPathProperty()
        }
    );

    // title triad
    public String getTitle() { return title.get(); }
    public void setTitle(String value) { title.set(value); }
    public StringProperty titleProperty() { return title; }

    // subtitle triad
    public String getSubtitle() { return subtitle.get(); }
    public void setSubtitle(String value) { subtitle.set(value); }
    public StringProperty subtitleProperty() { return subtitle; }

    // date triad
    public String getDate() { return date.get(); }
    public void setDate(String value) { date.set(value); }
    public StringProperty dateProperty() { return date; }

    // backgroundPath triad
    public Path getBackgroundPath() { return backgroundPath.get(); }
    public void setBackgroundPath(Path value) { backgroundPath.set(value); }
    public ObjectProperty<Path> backgroundPathProperty() { return backgroundPath; }

    // clubLogoPath triad
    public Path getClubLogoPath() { return clubLogoPath.get(); }
    public void setClubLogoPath(Path value) { clubLogoPath.set(value); }
    public ObjectProperty<Path> clubLogoPathProperty() { return clubLogoPath; }

    // qrCodePath triad
    public Path getQrCodePath() { return qrCodePath.get(); }
    public void setQrCodePath(Path value) { qrCodePath.set(value); }
    public ObjectProperty<Path> qrCodePathProperty() { return qrCodePath; }

    public ObservableList<TimeslotEntry> getTimeslots() { return timeslots; }
}
