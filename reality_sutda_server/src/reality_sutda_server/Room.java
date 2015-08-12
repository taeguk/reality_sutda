package reality_sutda_server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Room {
	private static int WAITING = 0;
	private static int PLAYING = 1;
	
	private static int nextId = 0;
	private static int roomCnt = 0;
	
	private int roomId;
	private String roomToken;
	private int status;		// waiting , playing, 
	private int roomSize;
	
	private User[] users;
	private int userCnt;
	private int dealer;
	
	private int pivot;
	private int turn;
	private int needBettingCnt;
	private int aliveCnt;
	private int bettingCnt;
	
	private boolean waitCheckOpinion;
	private int opinionCnt;
	private int opinionYesCnt;
	
	private int winner;
	
	private Card[] cards;
	
	private int dealIdx;
	private int dealCnt;
	private boolean dealFinished;
	
	private boolean waitReplaySelection;
	private boolean[] selectionTable;
	private HashMap<User, Integer> userMap;
	private int selectionCnt;
	private int selectionYesCnt;
	
	public Room(User maker, int roomSize) {
		System.out.println("[Log] Room.Room() start");
		++Room.roomCnt;
		roomId = Room.nextId++;
		roomToken = "test";		// will be changed.
		this.status = WAITING;
		this.roomSize = roomSize;
		users = new User[roomSize];
		users[0] = maker;
		userCnt = 1;
	}
	
	public int getRoomId() { return roomId; }
	public int getStatus() { return status; }
	public int getRoomSize() { return roomSize; }
	public User[] getUsers() { return users; }
	public int getUserCnt() { return userCnt; }
	public User getDealer() { return users[dealer]; }
	public User getPivot() { return users[pivot]; }
	public String getRoomToken() { return roomToken; }
	public User getWinner() { return users[winner]; }

	public boolean addUser(User user) {
		System.out.println("[Log] Room.addUser() start");
		if(status != WAITING || userCnt >= roomSize)
			return false;
		
		users[userCnt++] = user;
		
		return true;
	}

	public boolean checkCanGameStart() {
		System.out.println("[Log] Room.checkCanGameStart() start");
		if(status == WAITING && userCnt == roomSize)
			return true;
		else
			return false;
	}

	public void gameStart() {
		System.out.println("[Log] Room.gameStart() start");
		status = PLAYING;
		for(int i=0; i<userCnt; ++i) {
			users[i].gameStart(i == 0);
		}
		
		pivot = dealer = 0;
		dealIdx = (dealer+1) % userCnt;
		dealCnt = 0;
		dealFinished = false;
		aliveCnt = userCnt;
		
		waitCheckOpinion = false;
		waitReplaySelection = false;
		
		createCards();
		
		ClientHandler.broadCastGameStart(this);
		// �뙣 �룎由ш린 �슂泥� 蹂대궡湲�.
		ClientHandler.sendDealingCmd(users[dealer]);
	}

	public boolean exitRoom(User user) {
		System.out.println("[Log] Room.exitRoom() start");
		boolean isEmptyRoom = false;
		int userIdx = -1;
		while(users[++userIdx] != user);
		
		for(int i=userIdx; i<userCnt-1; ++i) {
			users[i] = users[i+1];
		}
		--userCnt;
		
		if(status == WAITING) {
			if(userCnt == 0) {
				isEmptyRoom = true;
			} else {
				ClientHandler.broadCastUpdateUserCnt(this);
			}
		} else {	// playing
			if(user.getStatus() == User.DEALER) {
				if(dealer >= userCnt) dealer = 0;
				users[dealer].setStatus(User.DEALER);
				ClientHandler.sendUpdateUserRole(users[dealer], Protocol.USER_ROLE_DEALER);
			}
			ClientHandler.broadCastUserAction(user, this, Protocol.USER_ACTION_EXIT_ROOM);
			
			if(userCnt <= 1) {
				finishGame();
				
				isEmptyRoom = true;
				
			}
		}
		
		ClientHandler.disconnect(user);
		
		return isEmptyRoom;
	}
	
	public void replaySelection(User user, int selection) {
		System.out.println("[Log] Room.replaySelection() start");
		// validation check will be updated.
		++selectionCnt;
		if(selection == Protocol.REPLAY_SELECTION_YES) {
			selectionTable[userMap.get(user).intValue()] = true;
			++selectionYesCnt;
		}
		if(selectionCnt >= userCnt) {
			User[] newUsers = new User[selectionYesCnt];
			int idx = 0;
			for(int i=0; i<userCnt; ++i) {
				if(selectionTable[i] == true) {
					newUsers[idx++] = users[i];
				} else {
					ClientHandler.disconnect(users[i]);
				}
			}
			users = newUsers;
			userCnt = selectionYesCnt;
			gameStart();
		}
	}

	private void finishGame() {
		System.out.println("[Log] Room.finishGame() start");
		int[] table = new int[aliveCnt];
		int[] index = new int[aliveCnt * 2];
		int cnt = 0;
		for(int i=0; i<userCnt; ++i) {
			if(users[i].isDead()) continue;
			table[cnt/2] = i;
			index[cnt++] = users[i].getCards()[0].getCardId();
			index[cnt++] = users[i].getCards()[1].getCardId();
		}
		GameResultJudge grj = new GameResultJudge(aliveCnt, index);
		grj.judge();
		int gameResult = grj.getGameResult();
		
		if(gameResult == GameResultJudge.WHOWINS) {
			winner = table[grj.getWinnerIdx()];
			ClientHandler.broadCastGameResult(this, Protocol.GAME_RESULT_WHO_WINS, null,0,null);
			// replaying processing
			waitReplaySelection = true;
			selectionTable = new boolean[userCnt];
			userMap = new HashMap<User, Integer>();
			for(int i=0; i<userCnt; ++i) {
				userMap.put(users[i], i);
			}
			selectionYesCnt = selectionCnt = 0;
		} else if(gameResult == GameResultJudge.DRAW) {
			ClientHandler.broadCastGameResult(this, Protocol.GAME_RESULT_DRAW, table, grj.getDrawPlayerNum(), grj.getDrawPlayerIdx());
			
			status = PLAYING;
			for(int i=0; i<userCnt; ++i) {
				users[i].gameStart(i == 0);
			}
			
			pivot = dealer = 0;
			dealIdx = (dealer+1) % userCnt;
			dealCnt = 0;
			dealFinished = false;
			aliveCnt = userCnt;
			
			waitCheckOpinion = false;
			
			createCards();
			
			int[] drawPlayerIdx = grj.getDrawPlayerIdx();
			boolean isDrawPlayer[] = new boolean[userCnt];
			
			for(int i=0; i<grj.getDrawPlayerNum(); ++i) {
				isDrawPlayer[table[drawPlayerIdx[i]]] = true;
			}
			
			for(int i=0; i<userCnt; ++i) {
				if(isDrawPlayer[i]) continue;
				users[i].die();
			}
			
			do {
				dealer = (dealer + 1) % userCnt;
			} while(users[dealer].isDead());
			
			ClientHandler.sendDealingCmd(users[dealer]);
		} else {	// rematch
			ClientHandler.broadCastGameResult(this, Protocol.GAME_RESULT_REMATCH, null,0,null);
			gameStart();
		}
	}

	public void dealing() {
		System.out.println("[Log] Room.dealing() start");
		User user = users[(dealIdx = (dealIdx + 1) % userCnt)];
		Card card = cards[dealCnt++];
		user.receiveCard(card);
		ClientHandler.sendReceiveCard(user, card);
		
		// check dealling finished
		if(dealCnt >= userCnt * 2) {
			dealFinished = true;
			
			startBetting();
		}
	}

	private void startBetting() {
		System.out.println("[Log] Room.startBetting() start");
		pivot = dealer;
		turn = 0;
		commandPivotBetting();
	}
	
	public void commandPivotBetting() {
		System.out.println("[Log] Room.commandPivotBetting() start");
		ClientHandler.sendBettingCmd(users[pivot], true);
		do {
			pivot = (pivot + 1) % userCnt;
		} while(users[pivot].isDead());
		++turn;
		bettingCnt = 0;
		needBettingCnt = aliveCnt;
		// all users bettingType set None will be updated.
	}

	public void betting(User user, int type) {
		System.out.println("[Log] Room.betting() start");
		// type validation check will be updated.
		// duplicate betting check will be updated.
		
		user.betting(type);
		if(checkPivotBettingType(type)) {
			ClientHandler.broadCastUserAction(user, this, Protocol.USER_ACTION_BETTING);
			needBettingCnt = aliveCnt;
			if(type != Protocol.BETTING_CHECK) {
				bettingCnt = 1;
				broadCastCommandNormalBetting();
			} else {	// when check
				startWaitCheckOpinion();
			}
		} else {
			if(type == Protocol.BETTING_DIE) {
				user.die();
				ClientHandler.broadCastUserAction(user, this, Protocol.USER_ACTION_DIE);
				if(--aliveCnt <= 1) {
					// game finished.
				}
			} else {
				ClientHandler.broadCastUserAction(user, this, Protocol.USER_ACTION_BETTING);
			}
			if(++bettingCnt >= needBettingCnt) {
				commandPivotBetting();
			}
		}
	}

	private void startWaitCheckOpinion() {
		System.out.println("[Log] Room.startWaitCheckOpinion() start");
		waitCheckOpinion = true;
		opinionCnt = 1;
		opinionYesCnt = 1;
		broadCastCheckOpinionCmd();
	}

	private void broadCastCheckOpinionCmd() {
		System.out.println("[Log] Room.broadCastCheckOpinionCmd() start");
		for(int i=0; i<userCnt; ++i) {
			if(i == pivot || users[i].isDead()) continue;
			ClientHandler.sendCheckOpinionCmd(users[i]);
		}
	}

	private void broadCastCommandNormalBetting() {
		System.out.println("[Log] Room.broadCastCommandNormalBetting() start");
		for(int i=0; i<userCnt; ++i) {
			if(i == pivot || users[i].isDead()) continue;
			ClientHandler.sendBettingCmd(users[i], false);
		}
	}
	
	private boolean checkPivotBettingType(int type) {
		return (type == Protocol.BETTING_DDOUBLE ||
				type == Protocol.BETTING_HALF || 
				type == Protocol.BETTING_NORMAL ||
				type == Protocol.BETTING_CHECK);
	}

	public void checkOpinion(User user, int answer) {
		System.out.println("[Log] Room.checkOpinion() start");
		// update user to opinion yes will be updated.
		// validation check will be updated.
		
		++opinionCnt;
		if(answer == Protocol.CHECK_OPINION_YES) {
			++opinionYesCnt;
		}
		
		if(opinionCnt >= aliveCnt) {
			// wait check finished.
			waitCheckOpinion = false;
			ClientHandler.broadCastCheckResult(this, opinionCnt == opinionYesCnt);
			if(opinionCnt == opinionYesCnt) {
				// game finished
			} else {
				commandPivotBetting();
			}
		}
	}
	
	private void createCards() {
		System.out.println("[Log] Room.createCards() start");
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
}
