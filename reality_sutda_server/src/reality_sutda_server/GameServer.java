package reality_sutda_server;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GameServer extends NioTcpServerModel {
	private static final int PORT = 4389;
	private static GameServer gameServer = new GameServer(PORT);
	
	private GameManager gameManager = new GameManager();
	private JSONParser jsonParser = new JSONParser();
	private HashMap<SocketChannel, ClientHandler> map = new HashMap<SocketChannel, ClientHandler>();
	private Charset networkCharset = StandardCharsets.UTF_8;

	private GameServer(int port) {
		super(port);
		System.out.println("[Log] GameServer runs!");
	}
	
	public static GameServer getInstance() {
		return gameServer;
	}
	
	public GameManager getGameManager() { return gameManager; }
	public Charset getNetworkCharset() { return networkCharset; }
	
	public static void main(String[] args) {
		gameServer.runServer();
	}

	@Override
	protected void _accept(SocketChannel sc) {
		System.out.println("[Log] GameServer._accept() start");
		map.put(sc, new ClientHandler(sc));
		System.out.println("[Log] GameServer._accept() end");
	}

	@Override
	protected void _read(SocketChannel sc, byte[] buffer) {
		System.out.println("[Log] GameServer._read() start");
		try {
			ClientHandler handler = map.get(sc);
			JSONObject data = (JSONObject) jsonParser.parse(new String(buffer));
			
			handler.processPacket(data);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("[Log] GameServer._read() end");
	}

	@Override
	protected void _close(SocketChannel sc) throws IOException {
		System.out.println("[Log] GameServer._close() start");
		User user = map.get(sc).getUser();
		if(map.remove(sc) != null) {
			ClientHandler.disconnectByClient(user);
			sc.close();
		}
		System.out.println("[Log] GameServer._close() end");
	}
	
	public void sendJsonObject(SocketChannel sc, JSONObject jsonObject) {
		System.out.println("[Log] GameServer.sendJsonObject() start");
		byte[] data = jsonObject.toJSONString().getBytes(networkCharset);
		ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
		
		buffer.putInt(data.length);
		buffer.put(data, 0, data.length);
		
		try {
			buffer.flip();
			sc.write(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("[Log] GameServer.sendJsonObject() end (" + data.length + ", "+ new String(data) + ")");
	}

	public void disconnect(User user) {
		System.out.println("[Log] GameServer.disconnect() start");
		SocketChannel sc = user.getSocketChannel();
		if(map.remove(sc) != null) {
			try {
				sc.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("[Log] GameServer.disconnect() end");
	}
	
	
	private void test() {
		System.out.println("[Log] connected!");
		for(int i=0; i<1000; ++i)
			for(int j=0; j<10000; ++j) System.out.print("");
		System.out.println("[Log] busy waiting finished!");
	}
}
