package xyz.belochka.filemanager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Controller {
    @FXML
    VBox leftPanel, rightPanel;

    @FXML
    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void btnCopyAction(ActionEvent actionEvent) {
        PanelController leftPanelController = (PanelController) leftPanel.getProperties().get("ctrl");
        PanelController rightPanelController = (PanelController) rightPanel.getProperties().get("ctrl");

//        TODO: Сделать прогресс-бар копирования файлов.
//        ProgressBar progressBar = new ProgressBar();
//        for (int i = 0; i < 100(количество отправленных на копирование файлов); i++){
//            progressBar.setProgress(1.0/100 * i);
//        }

        if (leftPanelController.getSelectedFileName() == null && rightPanelController.getSelectedFileName() == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "Файл не выбран!", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        PanelController sourcePanelController = null;
        PanelController destinationPanelController = null;
        if (leftPanelController.getSelectedFileName() != null)
        {
            sourcePanelController = leftPanelController;
            destinationPanelController = rightPanelController;
        }
        if (rightPanelController.getSelectedFileName() != null)
        {
            destinationPanelController = leftPanelController;
            sourcePanelController = rightPanelController;
        }
        Path sourcePath = Paths.get(sourcePanelController.getCurrentPath(), sourcePanelController.getSelectedFileName());
        Path destinationPath = Paths.get(destinationPanelController.getCurrentPath()).resolve(sourcePath.getFileName().toString());

        try
        {
            Files.copy(sourcePath, destinationPath);
            destinationPanelController.updateList(Paths.get(destinationPanelController.getCurrentPath()));
        }
        catch (IOException e)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Файл не удалось скопировать!", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnDeleteAction(ActionEvent actionEvent) {
        PanelController leftPanelController = (PanelController) leftPanel.getProperties().get("ctrl");
        PanelController rightPanelController = (PanelController) rightPanel.getProperties().get("ctrl");

        if (leftPanelController.getSelectedFileName() == null && rightPanelController.getSelectedFileName() == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "Файл не выбран!", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        PanelController sourcePanelController = null;
        if (leftPanelController.getSelectedFileName() != null)
        {
            sourcePanelController = leftPanelController;
        }
        if (rightPanelController.getSelectedFileName() != null)
        {
            sourcePanelController = rightPanelController;
        }
        Path sourcePath = Paths.get(sourcePanelController.getCurrentPath(), sourcePanelController.getSelectedFileName());

        try
        {
            Files.delete(sourcePath);
            sourcePanelController.updateList(Paths.get(sourcePanelController.getCurrentPath()));
        }
        catch (IOException e)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Файл не удалось удалить!", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnMoveAction(ActionEvent actionEvent) {
        PanelController leftPanelController = (PanelController) leftPanel.getProperties().get("ctrl");
        PanelController rightPanelController = (PanelController) rightPanel.getProperties().get("ctrl");

        if (leftPanelController.getSelectedFileName() == null && rightPanelController.getSelectedFileName() == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "Файл не выбран!", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        PanelController sourcePanelController = null;
        PanelController destinationPanelController = null;
        if (leftPanelController.getSelectedFileName() != null)
        {
            sourcePanelController = leftPanelController;
            destinationPanelController = rightPanelController;
        }
        if (rightPanelController.getSelectedFileName() != null)
        {
            destinationPanelController = leftPanelController;
            sourcePanelController = rightPanelController;
        }
        Path sourcePath = Paths.get(sourcePanelController.getCurrentPath(), sourcePanelController.getSelectedFileName());
        Path destinationPath = Paths.get(destinationPanelController.getCurrentPath()).resolve(sourcePath.getFileName().toString());

        try
        {
            Files.move(sourcePath, destinationPath);
            destinationPanelController.updateList(Paths.get(destinationPanelController.getCurrentPath()));
            sourcePanelController.updateList(Paths.get(sourcePanelController.getCurrentPath()));
        }
        catch (IOException e)
        {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Файл не удалось переместить!", ButtonType.OK);
            alert.showAndWait();
        }
    }
}