package xyz.belochka.filemanager;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class PanelController implements Initializable {
    @FXML
    TableView<FileInfo> filesTable;
    @FXML
    ComboBox<String> disksBox;
    @FXML
    TextField pathField;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Имя");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameColumn.setPrefWidth(240);

        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Размер");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        fileSizeColumn.setCellFactory(column -> {
            return new TableCell<FileInfo, Long>(){
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty)
                    {
                        setText(null);
                        setStyle("");
                    }
                    else {
                        String text = String.format("%,d byte(s)", item);
                        if (item == -1L){
                            text = "[DIR]";
                        }
                        if (item == -2L)
                        {
                            text = "[UP]";
                        }
                        setText(text);
                    }
                }
            };
        });
        fileSizeColumn.setPrefWidth(120);

        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата Изменения");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dateTimeFormatter)));
        fileSizeColumn.setPrefWidth(120);

        filesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, fileDateColumn);
        filesTable.getSortOrder().add(fileTypeColumn);

        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()){
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0);

        filesTable.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2)
                {
                    if (filesTable.getSelectionModel().getSelectedItem().getSize() == -2L)
                    {
                        btnPathUpAction();
                        return;
                    }
                    Path path = Paths.get(pathField.getText()).resolve(filesTable.getSelectionModel().getSelectedItem().getFileName());
                    if (Files.isDirectory(path))
                    {
                        updateList(path);
                    }
                }
            }
        });

        updateList(Paths.get("."));
    }
    public void updateList(Path path){
        filesTable.getItems().clear();
        try {
            pathField.setText(path.normalize().toAbsolutePath().toString());
            filesTable.getItems().clear();
            filesTable.getItems().add(new FileInfo("[ .. ]"));
            filesTable.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTable.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов!", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnPathUpAction() {
        Path upperPath = Paths.get(pathField.getText()).getParent();
        if (upperPath != null)
        {
            updateList(upperPath);
        }
    }

    public void selectDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));
    }

    public String getSelectedFileName()
    {
        if (!filesTable.isFocused())
        {
            return null;
        }
        return filesTable.getSelectionModel().getSelectedItem().getFileName();
    }

    public String getCurrentPath()
    {
        return pathField.getText();
    }
}
