open module work.lclpnet.launcherlogic {
	requires info.picocli;
	exports work.lclpnet.launcherlogic.cmd to info.picocli;
	
	requires com.google.gson;
	requires org.jsoup;
	
	requires java.sql;
	requires java.net.http;
	
	/* this is necessary for the forge installer */
	requires jdk.aot; 
	requires java.desktop;
	/* - */
	
}