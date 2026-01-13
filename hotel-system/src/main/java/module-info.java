module com.hotel {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires java.sql;
    requires bcrypt;

    opens com.hotel to javafx.fxml;

    exports com.hotel;
    exports com.hotel.common;
}