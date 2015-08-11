package reality_sutda_server;

import java.nio.channels.SocketChannel;

public class GameServer extends NioTcpServerModel {
	private static final int PORT = 7878;
	private static GameServer gameServer = new GameServer(PORT);

	private GameServer(int port) {
		super(port);
	}
	
	public static GameServer getInstance() {
		return gameServer;
	}
	
	public static void main(String[] args) {
		gameServer.runServer();
	}

	@Override
	protected void _accept(SocketChannel sc) {
		test();
	}

	@Override
	protected void _read(SocketChannel sc, byte[] buffer) {
		
		System.out.println("[Log] read data : " + new String (buffer));
	}
	
	private void test() {
		System.out.println("[Log] connected!");
		for(int i=0; i<1000; ++i)
			for(int j=0; j<10000; ++j) System.out.print("");
		System.out.println("[Log] busy waiting finished!");
	}

}
