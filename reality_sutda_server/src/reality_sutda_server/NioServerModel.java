package reality_sutda_server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioServerModel {
	public final int BUFFER_ALLOCATE_SIZE = 1024;
	
	private int PORT = 7788;
	private Selector selector = null;
	private ServerSocketChannel serverSocketChannel = null;
	private ServerSocket serverSocket = null;
	
	public void initServer() {
		try {
			selector = Selector.open();
			
			serverSocketChannel = ServerSocketChannel.open();
			
			serverSocketChannel.configureBlocking(false);
			
			serverSocket = serverSocketChannel.socket();
			
			InetSocketAddress isa = new InetSocketAddress("localhost", PORT);
			serverSocket.bind(isa);
			
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch(IOException ex) {
			// error handling
		}
	}
	
	public void startServer() {
		try {
			while(true) {
				selector.select();
				
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while(it.hasNext()) {
					SelectionKey skey = it.next();
					if(skey.isAcceptable()) {
						accept(skey);
					} else if(skey.isReadable()) {
						read(skey);
					}
					it.remove();
				}
			}
		} catch(Exception ex) {
			// error handling
		}
	}
	
	private void accept(SelectionKey skey) {
		try {
			SocketChannel sc = serverSocketChannel.accept();
			
			if(sc == null) return;
			
			sc.configureBlocking(false);
			sc.register(selector, SelectionKey.OP_READ);
			
		} catch(Exception ex) {
			// error handling
		}
	}
	
	private void read(SelectionKey skey) {
		SocketChannel sc = (SocketChannel) skey.channel();
		ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_ALLOCATE_SIZE);
		
		try {
			sc.read(buffer);
			buffer.flip();
		} catch(IOException ex) {
			try {
				sc.close();
			} catch(IOException ex2) {
				// error handling
			}
		}
		
		buffer.clear();
	}
	
	public abstract void
}
