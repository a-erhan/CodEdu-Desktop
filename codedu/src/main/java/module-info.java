module com.codedu {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.codedu.views to javafx.fxml;
    opens com.codedu.controllers to javafx.fxml;

    exports com.codedu;
    exports com.codedu.models;
}