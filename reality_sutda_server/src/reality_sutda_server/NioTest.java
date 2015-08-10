package reality_sutda_server;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class NioTest {
	public static void main(String[] args) {
		try {
			Socket sock = new Socket("localhost", 7878);
			
			BufferedOutputStream bos = new BufferedOutputStream(sock.getOutputStream());        	        	
        	byte[] b = new byte[1024];
        	DataOutputStream dos = new DataOutputStream(bos);
        	dos.writeInt(7);
        	dos.flush();
        	b = "ABC".getBytes();
        	bos.write(b);
        	bos.flush();
        	b = "1234".getBytes();
        	bos.write(b);
        	bos.flush();
        	try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
