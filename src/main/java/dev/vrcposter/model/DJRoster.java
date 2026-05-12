package dev.vrcposter.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class DJRoster {

    private final ObservableList<DJRecord> items = FXCollections.observableArrayList();

    public void setAll(List<DJRecord> records) {
        items.setAll(records);
    }

    public ObservableList<DJRecord> getObservableList() {
        return items;
    }
}
