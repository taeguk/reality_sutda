package reality_sutda_server;

import java.util.ArrayList;
import java.util.HashMap;

public class GameManager {
	private ArrayList<User> users = new ArrayList<User>();
	private HashMap<Integer, Room> rooms = new HashMap<Integer, Room>();	// roomId
	
	private HashMap<String, Integer> roomIdTable = new HashMap<String, Integer>();		// roomToken , roomId
	
	public void addUser(User user) {
		users.add(user);
	}
	
	public int makeRoom(User user, int playerNum) {
		if(playerNum < 2 || playerNum > 10)
			return Protocol.MAKE_ROOM_RESULT_INVALID_PLAYERNUM;
		
		Room room = new Room(user, playerNum); 
		user.enterRoom(room);
		
		return Protocol.MAKE_ROOM_RESULT_SUCCESS;
	}

	public int enterRoom(User user, String roomToken) {
		Integer roomId = roomIdTable.get(roomToken);
		if(roomId == null)
			return Protocol.ENTER_ROOM_RESULT_INVALID_ROOMTOKEN;
		
		Room room = rooms.get(roomId);
		if(room.addUser(user) == false)
			return Protocol.ENTER_ROOM_RESULT_ROOM_PLAYING;
		
		return Protocol.ENTER_ROOM_RESULT_SUCCESS;
	}

	public boolean checkCanGameStart(Room room) {
		return room.checkCanGameStart();
	}

	public void gameStart(Room room) {
		room.gameStart();
	}

	// 미완성
	public void exitRoom(User user) {
		Room room = user.getRoom();
		room.exitRoom(user);	// 미완성
		users.remove(user);
	}

	public void dealing(User user) {
		Room room = user.getRoom();
		room.dealing();
	}
	
	
}
