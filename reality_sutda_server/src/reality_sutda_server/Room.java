package reality_sutda_server;

import java.util.ArrayList;
import java.util.Collections;

public class Room {
	private static int WAITING = 0;
	private static int PLAYING = 1;
	
	private static int nextId = 0;
	private static int roomCnt = 0;
	
	private int roomId;
	private String roomToken;
	private int status;		// waiting , playing
	private int roomSize;
	
	private User[] users;
	private int userCnt;
	private int dealer;
	private int pivot;
	
	private Card[] cards;
	
	private int dealIdx;
	private int dealCnt;
	private boolean dealFinished;
	
	public Room(User maker, int roomSize) {
		++Room.roomCnt;
		roomId = Room.nextId++;
		roomToken = "test";		// will be changed.
		this.status = WAITING;
		this.roomSize = roomSize;
		users = new User[roomSize];
		users[0] = maker;
		userCnt = 1;
		dealer = 0;		// 딜러 랜덤으로? 아님 뭐 어떻게??
	}
	
	public int getRoomId() { return roomId; }
	public int getStatus() { return status; }
	public int getRoomSize() { return roomSize; }
	public User[] getUsers() { return users; }
	public int getUserCnt() { return userCnt; }
	public User getDealer() { return users[dealer]; }
	public User getPivot() { return users[pivot]; }
	public String getRoomToken() { return roomToken; }

	public boolean addUser(User user) {
		if(status != WAITING || userCnt >= roomSize)
			return false;
		
		users[userCnt++] = user;
		
		// 유저가 방 들어왔다고 broadcast하기. will be updated.
		
		return true;
	}

	public boolean checkCanGameStart() {
		if(status == WAITING && userCnt == roomSize)
			return true;
		else
			return false;
	}

	public void gameStart() {
		status = PLAYING;
		for(int i=0; i<userCnt; ++i) {
			users[i].gameStart(i == dealer);
		}
		
		pivot = dealer;
		dealIdx = (dealer+1) % userCnt;
		dealCnt = 0;
		dealFinished = false;
		
		createCards();
		
		broadCastGameStart();
		// 패 돌리기 요청 보내기.
		ClientHandler.sendDealingCmd(users[dealer]);
	}

	private void createCards() {
		cards = new Card[Card.CARDNUM];
		
		ArrayList<Integer> cardIds = new ArrayList<Integer>();
        for (int i=0; i<cards.length; i++) {
        	cardIds.add(new Integer(i));
        }
        Collections.shuffle(cardIds);
        for (int i=0; i<cards.length; i++) {
            cards[i] = new Card(cardIds.get(i));
        }
	}

	private void broadCastGameStart() {
		for(int i=0; i<userCnt; ++i) {
			ClientHandler.sendGameStart(users[i]);
		}
	}

	public void exitRoom(User user) {
		int userIdx = -1;
		while(users[++userIdx] != user);
		// dealer change code will be changed.
		
	}

	public void dealing() {
		User user = users[(dealIdx = (dealIdx + 1) % userCnt)];
		Card card = cards[dealCnt++];
		user.receiveCard(card);
		ClientHandler.sendReceiveCard(user, card);
		
		// check dealling finished
		if(dealCnt >= userCnt * 2) {
			dealFinished = true;
			
			
		}
	}
}
