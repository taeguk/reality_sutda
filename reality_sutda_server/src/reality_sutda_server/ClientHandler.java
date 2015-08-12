package reality_sutda_server;

import java.io.IOException;
import java.net.Socket;
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
		System.out.println("[Log] ClientHandler.processPacket() start (JSON : " + data.toJSONString() + ")");
		int packetFlag = (int)(long) data.get("packetFlag");
		
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
		System.out.println("[Log] ClientHandler.processMakeRoomRequest() start");
		int playerNum = (int)(long) data.get("playerNum");
		gameManager.makeRoom(me, playerNum);
	}

	private void processEnterRoomRequest(JSONObject data) {
		System.out.println("[Log] ClientHandler.processEnterRoomRequest() start");
		String roomToken = (String) data.get("roomToken");
		gameManager.enterRoom(me, roomToken);
	}
	
	private void processExitRoom(JSONObject data) {
		System.out.println("[Log] ClientHandler.processExitRoom() start");
		gameManager.exitRoom(me);
	}
	
	private void processDealing(JSONObject data) {
		System.out.println("[Log] ClientHandler.processDealing() start");
		gameManager.dealing(me);
	}
	
	private void processBetting(JSONObject data) {
		System.out.println("[Log] ClientHandler.processBetting() start");
		int type = (int)(long) data.get("type");
		gameManager.betting(me, type);
	}
	
	private void processCheckOpinion(JSONObject data) {
		System.out.println("[Log] ClientHandler.processCheckOpinion() start");
		int answer = (int)(long) data.get("answer");
		gameManager.checkOpinion(me, answer);
	}
	
	private void processReplaySelection(JSONObject data) {
		System.out.println("[Log] ClientHandler.processReplaySelection() start");
		int selection = (int)(long) data.get("selection");
		gameManager.replaySelection(me, selection);
	}
	
	private void invalidPacket() {
		System.out.println("[Log] ClientHandler.invalidPacket() start");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.INVALID_PACKET);
		server.sendJsonObject(me.getSocketChannel(), jsonObject);
	}
	
	public static void sendMakeRoomResponse(User user, int status) {
		System.out.println("[Log] ClientHandler.sendMakeRoomResponse() start");
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.MAKE_ROOM_RES);
		jsonObject.put("status", status);
		if(status == Protocol.MAKE_ROOM_RESULT_SUCCESS) {
			jsonObject.put("playerNum", user.getRoom().getRoomSize());
			jsonObject.put("roomToken", user.getRoom().getRoomToken());
		}
		server.sendJsonObject(sc, jsonObject);
	}
	
	public static void sendEnterRoomResponse(User user, int status) {
		System.out.println("[Log] ClientHandler.sendEnterRoomResponse() start");
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.ENTER_ROOM_RES);
		jsonObject.put("status", status);
		if(status == Protocol.ENTER_ROOM_RESULT_SUCCESS) {
			jsonObject.put("playerNum", user.getRoom().getRoomSize());
			jsonObject.put("roomToken", user.getRoom().getRoomToken());
		}
		server.sendJsonObject(sc, jsonObject);
	}
	
	public static void broadCastGameStart(Room room) {
		System.out.println("[Log] ClientHandler.broadCastGameStart() start");
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
		System.out.println("[Log] ClientHandler.sendDealingCmd() start");
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.DEALING_CMD);
		server.sendJsonObject(sc, jsonObject);
	}

	public static void sendReceiveCard(User user, Card card) {
		System.out.println("[Log] ClientHandler.sendReceiveCard() start");
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.RECEIVE_CARD);
		jsonObject.put("cardId", card.getCardId());
		server.sendJsonObject(sc, jsonObject);
	}

	public static void sendBettingCmd(User user, boolean isPivot) {
		System.out.println("[Log] ClientHandler.sendBettingCmd() start");
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.BETTING_CMD);
		jsonObject.put("mode", (isPivot ? Protocol.BETTING_MODE_PIVOT : Protocol.BETTING_MODE_NORMAL));
		server.sendJsonObject(sc, jsonObject);
	}

	public static void sendCheckOpinionCmd(User user) {
		System.out.println("[Log] ClientHandler.sendCheckOpinionCmd() start");
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.CHECK_OPINION_CMD);
		server.sendJsonObject(sc, jsonObject);
	}

	public static void broadCastCheckResult(Room room, boolean isCheckResultOk) {
		System.out.println("[Log] ClientHandler.broadCastCheckResult() start");
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
		System.out.println("[Log] ClientHandler.broadCastUpdateUserCnt() start");
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
		System.out.println("[Log] ClientHandler.sendUpdateUserRole() start");
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.UPDATE_USER_ROLE);
		jsonObject.put("result", userRole);
		server.sendJsonObject(sc, jsonObject);
	}

	public static void broadCastUserAction(User user, Room room, int action) {
		System.out.println("[Log] ClientHandler.broadCastUserAction() start");
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
		System.out.println("[Log] ClientHandler.broadCastGameResult() start");
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
		System.out.println("[Log] ClientHandler.disconnect() start");
		server.disconnect(user);
	}
	
	public static void disconnectByClient(User user) {
		System.out.println("[Log] ClientHandler.disconnectByClient() start");
		gameManager.delUser(user);
	}

	
}
