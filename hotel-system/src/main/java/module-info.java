open module com.hotel {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires java.sql;
    requires bcrypt;

    exports com.hotel;
    exports com.hotel.common;
    exports com.hotel.server;
}