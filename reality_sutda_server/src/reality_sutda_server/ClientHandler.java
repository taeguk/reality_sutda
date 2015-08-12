package reality_sutda_server;

import java.nio.channels.SocketChannel;

import org.json.simple.JSONObject;

public class ClientHandler {
	private static GameServer server = GameServer.getInstance();
	private GameManager gameManager;
	private SocketChannel sc;
	private User me;

	public ClientHandler(SocketChannel sc) {
		this.gameManager = server.getGameManager();
		this.sc = sc;
		this.me = new User(sc);
		gameManager.addUser(me);
	}
	
	public void processPacket(JSONObject data) {
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
		case Protocol.BATTING:
			processBatting(data);
			break;
		case Protocol.CHECK_OPINION:
			processCheckOpinion(data);
			break;
		case Protocol.REPLAY:
			processReplay(data);
			break;
		default:
			invalidPacket();
		}
	}
	
	private void processMakeRoomRequest(JSONObject data) {
		int playerNum = (int) data.get("playerNum");
		int status = gameManager.makeRoom(me, playerNum);
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.MAKE_ROOM_RES);
		jsonObject.put("status", status);
		if(status == Protocol.MAKE_ROOM_RESULT_SUCCESS)
			jsonObject.put("roomToken", me.getRoom().getRoomToken());		//jsonObject.put("roomToken", me.getRoom().getRoomToken().getBytes(server.getNetworkCharset()));
		server.sendJsonObject(sc, jsonObject);
	}

	private void processEnterRoomRequest(JSONObject data) {
		String roomToken = (String) data.get("roomToken");
		int status = gameManager.enterRoom(me, roomToken);
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.ENTER_ROOM_RES);
		jsonObject.put("status", status);
		server.sendJsonObject(sc, jsonObject);
		
		if(status == Protocol.ENTER_ROOM_RESULT_SUCCESS) {
			Room room = me.getRoom();
			if(gameManager.checkCanGameStart(room)) {
				gameManager.gameStart(room);
			}
		}
	}
	
	private void processExitRoom(JSONObject data) {
		gameManager.exitRoom(me);
		// 유저들에게 유저 나갔다는 패킷 보내기. will be updated.
	}
	
	private void processDealing(JSONObject data) {
		gameManager.dealing(me);
	}
	
	private void processBatting(JSONObject data) {
			
	}
	
	private void processCheckOpinion(JSONObject data) {
		
	}
	
	private void processReplay(JSONObject data) {
		
	}
	
	private void invalidPacket() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.INVALID_PACKET);
		server.sendJsonObject(sc, jsonObject);
	}
	
	public static void sendGameStart(User user) {
		SocketChannel sc = user.getSocketChannel();
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("packetFlag", Protocol.GAME_START);
		server.sendJsonObject(sc, jsonObject);
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
}
