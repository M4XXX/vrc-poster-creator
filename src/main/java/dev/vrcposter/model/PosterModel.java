package dev.vrcposter.model;

import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;

import java.nio.file.Path;

public class PosterModel {

    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty subtitle = new SimpleStringProperty("");
    private final StringProperty date = new SimpleStringProperty("");
    private final BooleanProperty showAvatarHint = new SimpleBooleanProperty(true);
    private final ObjectProperty<Color> fontColor = new SimpleObjectProperty<>(Color.WHITE);
    private final ObjectProperty<Path> backgroundPath = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> clubLogoPath = new SimpleObjectProperty<>();
    private final ObjectProperty<Path> qrCodePath = new SimpleObjectProperty<>();

    private final ObservableList<TimeslotEntry> timeslots = FXCollections.observableArrayList(
        te -> new Observable[]{
            te.startTimeProperty(), te.assignedDjIdProperty(),
            te.assignedDjArtistNameProperty(), te.assignedDjLogoPathProperty(),
            te.genreProperty()
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

    // showAvatarHint triad
    public boolean isShowAvatarHint() { return showAvatarHint.get(); }
    public void setShowAvatarHint(boolean value) { showAvatarHint.set(value); }
    public BooleanProperty showAvatarHintProperty() { return showAvatarHint; }

    // fontColor triad
    public Color getFontColor() { return fontColor.get(); }
    public void setFontColor(Color value) { fontColor.set(value != null ? value : Color.WHITE); }
    public ObjectProperty<Color> fontColorProperty() { return fontColor; }

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
