package reality_sutda_server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

public abstract class NioTcpServerModel {
	public final int BUFFER_ALLOCATE_SIZE = 256;
	
	private Selector selector = null;
	private ServerSocketChannel serverSocketChannel = null;
	private ServerSocket serverSocket = null;
	
	HashMap<SocketChannel, ByteBuffer> map = null;
	
	protected NioTcpServerModel(int port) {
		initServer(port);
	}
	
	private void initServer(int port) {
		try {
			selector = Selector.open();
			
			serverSocketChannel = ServerSocketChannel.open();
			
			serverSocketChannel.configureBlocking(false);
			
			serverSocket = serverSocketChannel.socket();
			
			InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), port);
			serverSocket.bind(isa);
			
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			
			map = new HashMap<SocketChannel, ByteBuffer>();
		} catch(IOException ex) {
			// error handling
		}
	}
	
	protected void runServer() {
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
			System.out.println("[Log] Server terminated! " + ex.getMessage());
			try {
				serverSocketChannel.close();
			} catch (IOException ex2) {
				// error handling
			}
			ex.printStackTrace();
		}
	}
	
	private void accept(SelectionKey skey) {
		try {
			SocketChannel sc = serverSocketChannel.accept();
			
			if(sc == null) 
				return;
			
			sc.configureBlocking(false);
			sc.register(selector, SelectionKey.OP_READ);
			
			map.put(sc, ByteBuffer.allocate(BUFFER_ALLOCATE_SIZE));
			
			_accept(sc);
			
		} catch(Exception ex) {
			// error handling
		}
	}
	
	private void read(SelectionKey skey) {
		SocketChannel sc = (SocketChannel) skey.channel();getClass();
		
		try {
			ByteBuffer buffer = map.get(sc);
			if(sc.read(buffer) <= 0) {
				throw new ClosedChannelException();
			}
			buffer.flip();
			while(true) {
				buffer.mark();
				int x;
				try {
					x = buffer.getInt();
					if(buffer.remaining() < x)
						throw new BufferUnderflowException();
					else if(x < 0)
						throw new IOException("Invalid packet");
				} catch(BufferUnderflowException ex) {
					buffer.reset();
					break;
				}

				byte[] b = new byte[x];
				buffer.get(b, 0, x);
				_read(sc, b);
			}
			buffer.compact();
		} catch(ClosedChannelException ex) {
			try {
				System.out.println("[Log] Disconnected!");
				_close(sc);
			} catch(IOException ex2) {
				// error handling
			}
		} catch(IOException ex) {
			try {
				System.out.println("[Log] I/O Exception! " + ex.getMessage());
				_close(sc);
			} catch(IOException ex2) {
				// error handling
			}
		}
	}
	
	protected abstract void _accept(SocketChannel sc);
	protected abstract void _read(SocketChannel sc, byte[] buffer);
	protected abstract void _close(SocketChannel sc) throws IOException;
}