module com.frisian_draught {
    requires org.jetbrains.annotations;
    requires transitive org.joml;
    requires org.apache.logging.log4j;
    requires javafx.controls;
    requires javafx.web;
    requires transitive javafx.graphics;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires transitive javafx.media;
    requires javafx.base;
    requires toml4j;

    opens com.frisian_draught to
            javafx.graphics;

    exports com.frisian_draught;
    exports com.frisian_draught.board;
    exports com.frisian_draught.board.Bot;
    exports com.frisian_draught.AI;
    exports com.frisian_draught.util;
    exports com.frisian_draught.Server;
}
