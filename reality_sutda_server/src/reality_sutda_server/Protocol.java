package reality_sutda_server;

public class Protocol {
	// packetFlag
	public static final int MAKE_ROOM_REQ = 1;
	public static final int MAKE_ROOM_RES = 2;
	public static final int UPDATE_USER_CNT = 3;
	public static final int EXIT_ROOM = 4;
	public static final int UPDATE_USER_ROLE = 5;
	public static final int GAME_START = 6;
	public static final int DEALING_CMD = 7;
	public static final int DEALING = 8;
	public static final int RECEIVE_CARD = 9;
	public static final int BATTING_CMD = 10;
	public static final int BATTING = 11;
	public static final int UPDATE_USER_STATUS = 12;
	public static final int CHECK_OPINION_CMD = 13;
	public static final int CHECK_OPINION = 14;
	public static final int CHECK_RESULT = 15;
	public static final int GAME_RESULT = 16;
	public static final int ACTION_TIME_OUT = 17;
	public static final int REPLAY = 18;
	public static final int INVALID_PACKET = 400;
	public static final int ENTER_ROOM_REQ = 100;
	public static final int ENTER_ROOM_RES = 101;
	
	public static final int MAKE_ROOM_RESULT_INVALID_PLAYERNUM = 0;
	public static final int MAKE_ROOM_RESULT_SERVER_BUSY = 1;
	public static final int MAKE_ROOM_RESULT_SUCCESS = 2;
	
	public static final int ENTER_ROOM_RESULT_INVALID_ROOMTOKEN = 0;
	public static final int ENTER_ROOM_RESULT_ROOM_PLAYING = 1;
	public static final int ENTER_ROOM_RESULT_SUCCESS = 2;
}
