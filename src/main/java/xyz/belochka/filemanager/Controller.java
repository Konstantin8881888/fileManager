package xyz.belochka.filemanager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


public class Controller {
    @FXML
    VBox leftPanel, rightPanel;

    @FXML
    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    private PanelController getPanelController(VBox panel) {
        return (PanelController) panel.getProperties().get("ctrl");
    }

    private PanelController getSourcePanel() {
        PanelController leftPanelController = getPanelController(leftPanel);
        PanelController rightPanelController = getPanelController(rightPanel);
        if (leftPanelController.getSelectedFileName() == null && rightPanelController.getSelectedFileName() == null) {
            showAlert("Файл или панель не выбраны!", Alert.AlertType.WARNING);
            return null;
        }
        return leftPanelController.getSelectedFileName() != null ? leftPanelController : rightPanelController.getSelectedFileName() != null ? rightPanelController : null;
    }

    private PanelController getDestinationPanel(PanelController sourcePanelController) {
        PanelController leftPanelController = getPanelController(leftPanel);
        PanelController rightPanelController = getPanelController(rightPanel);
        return sourcePanelController == leftPanelController ? rightPanelController : rightPanelController == sourcePanelController ? leftPanelController : null;
    }

    private void performFileOperation(Path sourcePath, Path destinationPath, PanelController sourcePanelController, PanelController destinationPanelController, String operationName, boolean isCopy) {
        try {
            if (isCopy) {
                Files.copy(sourcePath, destinationPath);
            } else {
                Files.move(sourcePath, destinationPath);
            }
            destinationPanelController.updateList(destinationPath.getParent());
            if (!isCopy) {
                sourcePanelController.updateList(sourcePath.getParent());
            }
        } catch (IOException e) {
            showAlert(String.format("Файл не удалось %s!", operationName), Alert.AlertType.WARNING);
        }
    }

    private void performFileAction(boolean isCopy) {
        PanelController sourcePanelController = getSourcePanel();
        if (sourcePanelController == null) {
            return;
        }
        PanelController destinationPanelController = getDestinationPanel(sourcePanelController);
        Path sourcePath = Paths.get(sourcePanelController.getCurrentPath(), sourcePanelController.getSelectedFileName());
        Path destinationPath = Paths.get(destinationPanelController.getCurrentPath(), sourcePanelController.getSelectedFileName());

        performFileOperation(sourcePath, destinationPath, sourcePanelController, destinationPanelController, isCopy ? "скопировать" : "переместить", isCopy);
    }

    public void btnCopyAction(ActionEvent actionEvent) {
        performFileAction(true);
    }

    public void btnMoveAction(ActionEvent actionEvent) {
        performFileAction(false);
    }



        public void btnDeleteAction(ActionEvent actionEvent) {
        PanelController sourcePanelController = getSourcePanel();
        if (sourcePanelController == null) {
            return;
        }
        PanelController destinationPanelController = getDestinationPanel(sourcePanelController);
        Path sourcePath = Paths.get(sourcePanelController.getCurrentPath(), sourcePanelController.getSelectedFileName());

        if (Files.isDirectory(sourcePath) && !isDirectoryEmpty(sourcePath)) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Удаление папки");
            alert.setHeaderText("Папка не пуста");
            alert.setContentText("Удалить содержимое папки вместе с самой папкой?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    deleteDirectory(sourcePath);
                } catch (IOException e) {
                    showAlert("Папку не удалось удалить!", Alert.AlertType.WARNING);
                }
                sourcePanelController.updateList(Paths.get(sourcePanelController.getCurrentPath()));
                destinationPanelController.updateList(Paths.get(destinationPanelController.getCurrentPath()));
            }
        } else {
            try {
                Files.delete(sourcePath);
                sourcePanelController.updateList(Paths.get(sourcePanelController.getCurrentPath()));
                destinationPanelController.updateList(Paths.get(destinationPanelController.getCurrentPath()));
            } catch (IOException e) {
                showAlert("Файл не удалось удалить!", Alert.AlertType.WARNING);
            }
        }
    }

    private boolean isDirectoryEmpty(Path directory) {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void deleteDirectory(Path directoryPath) throws IOException {
        if (!Files.exists(directoryPath)) {
            return;
        }
        if (!Files.isDirectory(directoryPath)) {
            Files.delete(directoryPath);
            return;
        }
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directoryPath)) {
            for (Path path : directoryStream) {
                deleteDirectory(path);
            }
        }

        Files.delete(directoryPath);
    }

    private void createNewFileOrFolder(String title, String inputText) {
        PanelController sourcePanelController = getSourcePanel();
        if (sourcePanelController == null) {
            return;
        }

        // создаем диалоговое окно для ввода имени нового файла или папки
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(inputText);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String name = result.get();

            PanelController destinationPanelController = getDestinationPanel(sourcePanelController);
            Path newPath = Paths.get(sourcePanelController.getCurrentPath(), name);
            try {
                if (title.equals("Создание новой папки")) {
                    Files.createDirectory(newPath);
                } else if (title.equals("Создание нового файла")) {
                    Files.createFile(newPath);
                }
                sourcePanelController.updateList(Paths.get(sourcePanelController.getCurrentPath()));
                destinationPanelController.updateList(Paths.get(destinationPanelController.getCurrentPath()));
            } catch (IOException e) {
                String message = title.equals("Создание новой папки") ? "Папку не удалось создать!" : "Файл не удалось создать!";
                showAlert(message, Alert.AlertType.WARNING);
            }
        }
    }

    public void btnNewFolderAction(ActionEvent actionEvent) {
        createNewFileOrFolder("Создание новой папки", "Введите имя новой папки:");
    }

    public void btnNewFileAction(ActionEvent actionEvent) {
        createNewFileOrFolder("Создание нового файла", "Введите имя нового файла:");
    }


    public void btnRenameAction(ActionEvent actionEvent) {
        PanelController sourcePanelController = getSourcePanel();
        if (sourcePanelController == null) {
            return;
        }

        String selectedFileName = sourcePanelController.getSelectedFileName();
        if (selectedFileName == null) {
            showAlert("Файл не выбран!", Alert.AlertType.WARNING);
            return;
        }
        if (selectedFileName == "[ .. ]") {
            showAlert("Папку верхнего уровня нельзя переименовать!", Alert.AlertType.WARNING);
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selectedFileName);
        dialog.setTitle("Переименовать");
        dialog.setHeaderText("Введите новое имя для файла или папки:");
        dialog.setContentText("Новое имя:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().isEmpty()) {
            PanelController destinationPanelController = getDestinationPanel(sourcePanelController);
            Path sourcePath = Paths.get(sourcePanelController.getCurrentPath(), selectedFileName);
            Path destinationPath = Paths.get(sourcePanelController.getCurrentPath(), result.get());

            try {
                Files.move(sourcePath, destinationPath);
                sourcePanelController.updateList(Paths.get(sourcePanelController.getCurrentPath()));
                destinationPanelController.updateList(Paths.get(sourcePanelController.getCurrentPath()));
            } catch (IOException e) {
                showAlert("Файл не удалось переименовать!", Alert.AlertType.WARNING);
            }
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}