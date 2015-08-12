package reality_sutda_server;

import java.io.IOException;
import java.nio.channels.SocketChannel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ClientHandler {
	private static GameServer server = GameServer.getInstance();
	private static GameManager gameManager = server.getGameManager();;
	private User me;

	public ClientHandler(SocketChannel sc) {
		this.me = new User(sc);
		gameManager.addUser(me);
	}
	
	public User getUser() { return me; }
	
	public void processPacket(JSONObject data) {
		System.out.println("[Log] ClientHandler.processPacket() start");
		int packetFlag = (int) data.get("packetFlag");
		
		switch(packetFlag) {
		case Protocol.MAKE_ROOM_REQ:
			processMakeRoomRequest(data);
			break;
		case Protocol.ENTER_ROOM_REQ:
			processEnterRoomRequest(data);
			break;
		case Protocol.EXIT_ROOM:
			processExitRoom(data);
			break;
		case Protocol.DEALING:
			processDealing(data);
			break;
		case Protocol.BETTING:
			processBetting(data);
			break;
		case Protocol.CHECK_OPINION:
			processCheckOpinion(data);
			break;
		case Protocol.REPLAY_SELECTION:
			processReplaySelection(data);
			break;
		default:
			invalidPacket();
		}
		System.out.println("[Log] ClientHandler.processPacket() end");
	}
	
	private void processMakeRoomRequest(JSONObject data) {
		int playerNum = (int) data.get("playerNum");
		gameManager.makeRoom(me, playerNum);
	}

	private void processEnterRoomRequest(JSONObject data) {
		String roomToken = new String((byte[])data.get("roomToken"));
		gameManager.enterRoom(me, roomToken);
	}
	
	private void processExitRoom(JSONObject data) {
		gameManager.exitRoom(me);
	}
	
	private void processDealing(JSONObject data) {
		gameManager.dealing(me);
	}
	
	private void processBetting(JSONObject data) {
		int type = (int) data.get("type");
		gameManager.betting(me, type);
	}
	
	private void processCheckOpinion(JSONObject data) {
		int answer = (int) data.get("answer");
		gameManager.checkOpinion(me, answer);
	}
	
	private void processReplaySelection(JSONObject data) {
		int selection = (int) data.get("selection");
		gameManager.replaySelection(me, selection);
	}
	
	private void invalidPacket() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.INVALID_PACKET);
		server.sendJsonObject(me.getSocketChannel(), jsonObject);
	}
	
	public static void sendMakeRoomResponse(User user, int status) {
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.MAKE_ROOM_RES);
		jsonObject.put("status", status);
		if(status == Protocol.MAKE_ROOM_RESULT_SUCCESS)
			jsonObject.put("roomToken", user.getRoom().getRoomToken().getBytes(server.getNetworkCharset()));
		server.sendJsonObject(sc, jsonObject);
	}
	
	public static void sendEnterRoomResponse(User user, int status) {
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.ENTER_ROOM_RES);
		jsonObject.put("status", status);
		server.sendJsonObject(sc, jsonObject);
	}
	
	public static void broadCastGameStart(Room room) {
		JSONArray userIds = new JSONArray();
		User[] users = room.getUsers();
		for(int i=0; i<room.getUserCnt(); ++i) {
			userIds.add(users[i].getUserId());
		}
		
		for(int i=0; i<room.getUserCnt(); ++i) {
			SocketChannel sc = users[i].getSocketChannel();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("packetFlag", Protocol.GAME_START);
			jsonObject.put("userCnt", room.getUserCnt());
			jsonObject.put("userIds", userIds);
			jsonObject.put("myId", users[i].getUserId());
			jsonObject.put("dealerId", room.getDealer().getUserId());
			server.sendJsonObject(sc, jsonObject);
		}
	}

	public static void sendDealingCmd(User user) {
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.DEALING_CMD);
		server.sendJsonObject(sc, jsonObject);
	}

	public static void sendReceiveCard(User user, Card card) {
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.RECEIVE_CARD);
		jsonObject.put("cardId", card.getCardId());
		server.sendJsonObject(sc, jsonObject);
	}

	public static void sendBettingCmd(User user, boolean isPivot) {
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.BETTING_CMD);
		jsonObject.put("mode", (isPivot ? Protocol.BETTING_MODE_PIVOT : Protocol.BETTING_MODE_NORMAL));
		server.sendJsonObject(sc, jsonObject);
	}

	public static void sendCheckOpinionCmd(User user) {
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.CHECK_OPINION_CMD);
		server.sendJsonObject(sc, jsonObject);
	}

	public static void broadCastCheckResult(Room room, boolean isCheckResultOk) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.CHECK_RESULT);
		jsonObject.put("result", (isCheckResultOk ? Protocol.CHECK_RESULT_OK : Protocol.CHECK_RESULT_NO));
		User[] users = room.getUsers();
		for(int i=0; i<room.getUserCnt(); ++i) {
			SocketChannel sc = users[i].getSocketChannel();
			server.sendJsonObject(sc, jsonObject);	
		}
	}

	public static void broadCastUpdateUserCnt(Room room) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.UPDATE_USER_CNT);
		jsonObject.put("userCnt", room.getUserCnt());
		User[] users = room.getUsers();
		for(int i=0; i<room.getUserCnt(); ++i) {
			SocketChannel sc = users[i].getSocketChannel();
			server.sendJsonObject(sc, jsonObject);	
		}
	}

	public static void sendUpdateUserRole(User user, int userRole) {
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.UPDATE_USER_ROLE);
		jsonObject.put("result", userRole);
		server.sendJsonObject(sc, jsonObject);
	}

	public static void broadCastUserAction(User user, Room room, int action) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.USER_ACTION);
		jsonObject.put("userId", user.getUserId());
		jsonObject.put("action", action);
		User[] users = room.getUsers();
		for(int i=0; i<room.getUserCnt(); ++i) {
			if(users[i] == user) continue;
			SocketChannel sc = users[i].getSocketChannel();
			server.sendJsonObject(sc, jsonObject);	
		}
	}
	
	public static void broadCastGameResult(Room room, int gameResult, int[] table,  int drawPlayerCnt, int[] drawPlayerIdx) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.GAME_RESULT);
		jsonObject.put("result", gameResult);
		if(gameResult == Protocol.GAME_RESULT_WHO_WINS) {
			jsonObject.put("winner", room.getWinner().getUserId());
		} else if(gameResult == Protocol.GAME_RESULT_DRAW) {
			JSONArray drawPlayerIds = new JSONArray();
			for(int i=0; i<drawPlayerCnt; ++i)
				drawPlayerIds.add(table[drawPlayerIdx[i]]);
			jsonObject.put("drawPlayerCnt", drawPlayerCnt);
			jsonObject.put("drawPlayerIds", drawPlayerIds);
		}
	}
	
	public static void disconnect(User user) {
		server.disconnect(user);
	}
	
	public static void disconnectByClient(User user) {
		gameManager.delUser(user);
	}

	
}
