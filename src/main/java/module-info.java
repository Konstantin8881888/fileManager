module xyz.belochka.filemanager {
    requires javafx.controls;
    requires javafx.fxml;


    opens xyz.belochka.filemanager to javafx.fxml;
    exports xyz.belochka.filemanager;
}