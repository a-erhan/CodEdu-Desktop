<<<<<<< HEAD
module com.codedu {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.codedu to javafx.fxml;
    opens com.codedu.controllers to javafx.fxml;
    opens com.codedu.models to javafx.fxml;

    exports com.codedu;
    exports com.codedu.controllers;
    exports com.codedu.models;
}
=======
module com.codedu {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.codedu to javafx.fxml;
    opens com.codedu.controllers to javafx.fxml;
    opens com.codedu.models to javafx.fxml;

    exports com.codedu;
    exports com.codedu.controllers;
    exports com.codedu.models;
}
>>>>>>> 68084a056cecabbca83ad1f2bea89e41b0d5769a
