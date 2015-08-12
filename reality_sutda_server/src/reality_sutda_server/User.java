package reality_sutda_server;

import java.nio.channels.SocketChannel;

public class User {
	public static final int EMPTY = 0;
	public static final int WAITING = 1;
	public static final int NORMAL = 2;
	public static final int DEALER = 3;
	public static final int DEAD = 4;
	
	public static final int DDOUBLE = 10;
	public static final int HALF = 11;
	public static final int BASIC = 12;
	public static final int CALL = 13;
	public static final int DIE = 14;
	
	private static int nextId = 0;
	private static int userCnt = 0;
	
	private SocketChannel sc;
	
	private int userId;
	private int status;		// empty , waiting , normal , dealer , dead
	private int batting;	// ddouble, half, basic, call, die
	
	private Room room;
	
	private Card[] cards;
	private int cardCnt;
	
	public User(SocketChannel sc) {
		++User.userCnt;
		this.userId = User.nextId++;
		this.sc = sc;
		this.status = EMPTY;
		room = null;
		cards = null;
	}
	
	public void enterRoom(Room room) {
		this.status = WAITING;
		this.room = room;
	}
	
	public int getUserId() { return userId; }
	public SocketChannel getSocketChannel() { return sc; }
	public int getStatus() { return status; }
	public int getBatting() { return batting; }
	public Room getRoom() { return room; }
	public Card[] getCards() { return cards; }
	public int getCardCnt() { return cardCnt; }

	public void gameStart(boolean isDealer) {
		status = (isDealer ? DEALER : NORMAL);
		cards = new Card[2];
		cardCnt = 0;
	}

	public void receiveCard(Card card) {
		cards[cardCnt++] = card;
	}
}
