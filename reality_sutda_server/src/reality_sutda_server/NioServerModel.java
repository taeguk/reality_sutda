package reality_sutda_server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

public abstract class NioServerModel {
	public final int BUFFER_ALLOCATE_SIZE = 1024;
	
	private Selector selector = null;
	private ServerSocketChannel serverSocketChannel = null;
	private ServerSocket serverSocket = null;
	
	HashMap<SocketChannel, BufferedInputStream> map = null;
	
	protected NioServerModel(int port) {
		initServer(port);
	}
	
	private void initServer(int port) {
		try {
			selector = Selector.open();
			
			serverSocketChannel = ServerSocketChannel.open();
			
			serverSocketChannel.configureBlocking(false);
			
			serverSocket = serverSocketChannel.socket();
			
			InetSocketAddress isa = new InetSocketAddress("localhost", port);
			serverSocket.bind(isa);
			
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			map = new HashMap<SocketChannel, BufferedInputStream>();
		} catch(IOException ex) {
			// error handling
		}
	}
	
	protected void startServer() {
		try {
			while(true) {
				if(selector.select() == 0)
					continue;
				
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
			
			if(sc == null) 
				return;
			
			sc.configureBlocking(false);
			sc.register(selector, SelectionKey.OP_READ);
			
			map.put(sc, new BufferedInputStream(sc.socket().getInputStream()));
			
			_accept(sc);
			
		} catch(Exception ex) {
			// error handling
		}
	}
	
	private void read(SelectionKey skey) {
		SocketChannel sc = (SocketChannel) skey.channel();
		//ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_ALLOCATE_SIZE);
		byte[] buffer = new byte[BUFFER_ALLOCATE_SIZE];
		try {
			BufferedInputStream bis = map.get(sc);
			bis.mark(BUFFER_ALLOCATE_SIZE);
			
			int readSz = bis.read(buffer);
			
			/*
			int readSz = bis.read(buffer);
			System.out.println(buffer);
			buffer.flip();
			System.out.println(buffer);
			if(readSz < 0) {
				System.out.println("[Log] disconnected!");
				buffer.clear(); return;
			} else if(readSz < 4) {
				System.out.println("[Log] hehehe");
				bis.reset();
				buffer.clear(); return;
			} else if(readSz < buffer.getInt(0)) {
				System.out.println("[Log] yayaya");
				sc.socket().getInputStream().reset();
				buffer.clear(); return;
			} else if(readSz > buffer.getInt(0)) {
				System.out.println("[Log] haters...");
				buffer.limit(buffer.getInt(0));
				System.out.println(buffer);
				//sc.socket().getInputStream().reset();
				System.out.println(buffer);
				//sc.socket().getInputStream().skip(buffer.getInt(0));
			}
			*/
			/*
			//buffer.mark();
			int x = buffer.getInt(0);
			if(readSz < x) {
				System.out.println("[Log] yayaya");
				//buffer.reset();
				sc.socket().getInputStream().reset();
				buffer.clear(); return;
			}
			*/
			
			_read(sc, buffer);
			
		} catch(IOException ex) {
			try {
				sc.close();
			} catch(IOException ex2) {
				// error handling
			}
		}
		
		buffer.clear();
	}
	
	protected abstract void _accept(SocketChannel sc);
	protected abstract void _read(SocketChannel sc, ByteBuffer buffer);
}
