package reality_sutda_server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class GameManager {
	private ArrayList<User> users = new ArrayList<User>();
	private HashMap<Integer, Room> rooms = new HashMap<Integer, Room>();	// roomId
	
	private HashMap<String, Integer> roomIdTable = new HashMap<String, Integer>();		// roomToken , roomId
	
	public void addUser(User user) {
		users.add(user);
	}
	
	public void makeRoom(User user, int playerNum) {
		int status;
		
		if(playerNum < 2 || playerNum > 10) {
			status = Protocol.MAKE_ROOM_RESULT_INVALID_PLAYERNUM;
		} else {
			Room room = new Room(user, playerNum); 
			user.enterRoom(room);
			status = Protocol.MAKE_ROOM_RESULT_SUCCESS;
		}
		ClientHandler.sendMakeRoomResponse(user, status);
	}

	public void enterRoom(User user, String roomToken) {
		int status;
		Integer roomId = roomIdTable.get(roomToken);
		if(roomId == null) {
			status = Protocol.ENTER_ROOM_RESULT_INVALID_ROOMTOKEN;
		} else {
			Room room = rooms.get(roomId);
			if(room.addUser(user) == false)
				status = Protocol.ENTER_ROOM_RESULT_ROOM_PLAYING;
			else
				status = Protocol.ENTER_ROOM_RESULT_SUCCESS;
		}
		
		ClientHandler.sendEnterRoomResponse(user, status);
		
		if(status == Protocol.ENTER_ROOM_RESULT_SUCCESS) {
			Room room = user.getRoom();
			ClientHandler.broadCastUpdateUserCnt(room);
			if(checkCanGameStart(room)) {
				room.gameStart();
			}
		}
	}

	public boolean checkCanGameStart(Room room) {
		return room.checkCanGameStart();
	}

	public void exitRoom(User user) {
		Room room = user.getRoom();
		if(room.exitRoom(user))
			rooms.remove(room);
		users.remove(user);
		ClientHandler.disconnect(user);
	}

	public void betting(User user, int type) {
		Room room = user.getRoom();
		room.betting(user, type);
	}
	
	public void dealing(User user) {
		Room room = user.getRoom();
		room.dealing();
	}

	public void checkOpinion(User user, int answer) {
		Room room = user.getRoom();
		room.checkOpinion(user, answer);
	}

	public void delUser(User user) {
		Room room = user.getRoom();
		if(room != null) {
			if(room.exitRoom(user))
				rooms.remove(room);
		}
		users.remove(user);
	}
}
