module com.schedulix {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Optional export libraries (add when dependencies present)
    // requires org.apache.poi.ooxml;
    // requires com.itextpdf;

    opens com.schedulix to javafx.fxml;
    opens com.schedulix.controllers to javafx.fxml;
    opens com.schedulix.models to javafx.fxml;
    opens com.schedulix.utils to javafx.fxml;
    opens com.schedulix.algorithms to javafx.fxml;

    exports com.schedulix;
    exports com.schedulix.controllers;
    exports com.schedulix.models;
    exports com.schedulix.algorithms;
    exports com.schedulix.utils;
}
