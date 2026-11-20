module Candy1 {
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires transitive java.desktop;
	requires transitive java.sql;
	requires transitive java.logging;
	requires javafx.swing;
	
	exports application;
	opens application to javafx.graphics, javafx.fxml;
}
