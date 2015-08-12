package reality_sutda_server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GameServer extends NioTcpServerModel {
	private static final int PORT = 7878;
	private static GameServer gameServer = new GameServer(PORT);
	
	private GameManager gameManager = new GameManager();
	private JSONParser jsonParser = new JSONParser();
	private HashMap<SocketChannel, ClientHandler> map = new HashMap<SocketChannel, ClientHandler>();
	private Charset networkCharset = StandardCharsets.UTF_8;

	private GameServer(int port) {
		super(port);
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
		map.put(sc, new ClientHandler(sc));
	}

	@Override
	protected void _read(SocketChannel sc, byte[] buffer) {
		try {
			ClientHandler handler = map.get(sc);
			JSONObject data = (JSONObject) jsonParser.parse(new String(buffer));
			
			handler.processPacket(data);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendJsonObject(SocketChannel sc, JSONObject jsonObject) {
		byte[] data = jsonObject.toJSONString().getBytes(networkCharset);
		ByteBuffer buffer = ByteBuffer.allocate(4 + data.length);
		
		buffer.putInt(data.length);
		buffer.put(data, 0, data.length);
		
		try {
			sc.write(buffer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void test() {
		System.out.println("[Log] connected!");
		for(int i=0; i<1000; ++i)
			for(int j=0; j<10000; ++j) System.out.print("");
		System.out.println("[Log] busy waiting finished!");
	}
}
