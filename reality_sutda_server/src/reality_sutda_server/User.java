package reality_sutda_server;

import java.nio.channels.SocketChannel;

public class User {
	public static final int EMPTY = 0;
	public static final int WAITING = 1;
	public static final int NORMAL = 2;
	public static final int DEALER = 3;
	public static final int DEAD = 4;
	
	public static final int NOTBETTING = 1000;
	
	private static int nextId = 0;
	private static int userCnt = 0;
	
	private SocketChannel sc;
	
	private int userId;
	private int status;		// empty , waiting , normal , dealer , dead
	private int bettingType;	// notbetting, ddouble, half, normal, check, call, die
	
	private Room room;
	
	private Card[] cards;
	private int cardCnt;
	
	public User(SocketChannel sc) {
		System.out.println("[Log] User.User() start");
		++User.userCnt;
		this.userId = User.nextId++;
		this.sc = sc;
		this.status = EMPTY;
		room = null;
		cards = null;
	}
	
	public void enterRoom(Room room) {
		System.out.println("[Log] User.enterRoom() start");
		this.status = WAITING;
		this.room = room;
	}
	
	public int getUserId() { return userId; }
	public SocketChannel getSocketChannel() { return sc; }
	public int getStatus() { return status; }
	public int getBettingType() { return bettingType; }
	public Room getRoom() { return room; }
	public Card[] getCards() { return cards; }
	public int getCardCnt() { return cardCnt; }

	public void gameStart(boolean isDealer) {
		System.out.println("[Log] User.gameStart() start");
		status = (isDealer ? DEALER : NORMAL);
		cards = new Card[2];
		cardCnt = 0;
	}
	
	public void setStatus(int status) {
		System.out.println("[Log] User.setStatus() start");
		this.status = status;
	}
	
	public void setBettingType(int bettingType) {
		System.out.println("[Log] User.setBettingType() start");
		this.bettingType = bettingType;
	}

	public void receiveCard(Card card) {
		System.out.println("[Log] User.receiveCard() start");
		cards[cardCnt++] = card;
	}
	
	public void betting(int type) {
		System.out.println("[Log] User.betting() start");
		bettingType = type;
	}

	public boolean isDead() {
		System.out.println("[Log] User.isDead() start");
		return status == DEAD;
	}

	public void die() {
		System.out.println("[Log] User.die() start");
		status = DEAD;
		bettingType = Protocol.BETTING_DIE;
	}
}
