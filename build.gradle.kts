plugins {
  application
  id("org.openjfx.javafxplugin") version "0.1.0"
}
repositories {
	mavenCentral()
}
javafx {
    version = "21"
    modules("javafx.controls", "javafx.fxml")
}

application {
mainClass.set("application.Main")
}