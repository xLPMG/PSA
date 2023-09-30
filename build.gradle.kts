plugins {
  application
  id("org.openjfx.javafxplugin") version "0.1.0"
}
repositories {
	mavenCentral()
}
javafx {
    version = "19"
    modules("javafx.controls", "javafx.fxml")
}

application {
mainClass.set("application.Main")
}