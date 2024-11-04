module com.um_project_game {

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

    opens com.um_project_game to javafx.graphics;
    exports com.um_project_game;
    exports com.um_project_game.board;
    exports com.um_project_game.AI;
    exports com.um_project_game.util;
    exports com.um_project_game.Server;
}
