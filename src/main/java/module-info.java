module com.example.project_lab {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.project_lab to javafx.fxml;
    exports com.example.project_lab;
}