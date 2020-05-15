package work.lclpnet.launcherlogic.util;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class ProgressCallbackClient {

	private static List<Socket> openSockets = new ArrayList<>();
	
	private String host;
	private int port;
	private transient Socket socket;
	private transient Gson gson = null;

	public ProgressCallbackClient(String host, int port) throws IOException {
		this.host = host;
		this.port = port;
		openSocket();
	}

	private void openSocket() throws IOException {
		socket = new Socket(host, port);
		openSockets.add(socket);
	}

	private void send(byte[] bytes) {
		try {
			socket.getOutputStream().write(bytes);
			socket.getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
			IllegalStateException ex = new IllegalStateException("Error during sending progress");
			ex.addSuppressed(e);
			throw ex;
		}
	}
	
	public void send(String s) {
		s += "\n";
		send(s.getBytes(StandardCharsets.UTF_8));
	}
	
	public void send(Object o) {
		send(gson.toJson(o));
	}
	
	public Gson getGson() {
		return gson;
	}
	
	public void setGson(Gson gson) {
		this.gson = gson;
	}

	public void stop() {
		try {
			socket.close();
			openSockets.remove(socket);
			socket = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeAllSockets() {
		openSockets.forEach(s -> {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public static boolean hasOpenSockets() {
		return !openSockets.isEmpty();
	}

}
