module com.hotel {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j; 

    opens com.hotel to javafx.fxml;
    exports com.hotel;
}