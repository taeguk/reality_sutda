package reality_sutda_server;

/*
 * bumsu's sutda game result judgement C++ code
 * 	porting to JAVA class by taeguk.
 */

public class GameResultJudge {
	public static final int WHOWINS = 1;
	public static final int DRAW = 2;
	public static final int REMATCH = 3;
	
	private int index[];
	private int player_number;
	private int[][] player_card;
	private int flg;						// 1 -> 승자, 2 -> 무승부, 3 -> 나가리 (전부 무승부)
	private int Draw_num;
	private int[] Draw_player;	// 무승부 player 수, 무승부 players
	private int winnerIdx;

	public GameResultJudge(int playerNum, int[] index) {
		this.index = index;
		player_number = playerNum;
		player_card = new int[playerNum][2];
		Draw_player = new int[playerNum];
	}
	
	public void judge() {
		bumsuMain();
	}
	
	public int getGameResult() {
		return flg;
	}
	
	public int getWinnerIdx() {
		return winnerIdx;
	}
	
	public int getDrawPlayerNum() {
		return Draw_num;
	}
	
	public int[] getDrawPlayerIdx() {
		return Draw_player;
	}
	
	private int bumsuMain() {
		int i, j, a, temp; //정수형변수선언
		int jumsu[] = new int[player_number];
		int card[] = { 1, 11, 2, 12, 3, 13, 4, 14, 5, 15, 6, 16, 7, 17, 8, 18, 9, 19, 10, 20 };
		char winner;
		
		int cnt = 0;
		for (i = 0; i < player_number; i++)
		{
			player_card[i][0] = card[index[cnt++]];
			player_card[i][1] = card[index[cnt++]];
			//printf("Player %d의 패 : %d, %d\n", i+1, player_card [i][0], player_card [i][1]);
		}

		for (i = 0; i < player_number; i++)
		{
			jumsu[i] = Logic(player_card[i][0], player_card[i][1]);		// logic에 의해 점수 구하기
			//printf("Player %d의 점수 : %d\n", i + 1, jumsu[i]);
		}

		int max = 0;
		int idx = 0;								// (특수 족보)구사와 멍텅구리 구사를 제외한 가장 큰 점수
		for (i = 0; i < player_number; i++)
		{
			if (max < jumsu[i] && jumsu[i] != 55 && jumsu[i] != 54 && jumsu[i] != 115 && jumsu[i] != 9)
			{
				max = jumsu[i];
				idx = i;
			}
		}

		for (i = 0; i < player_number; i++)		// 특수 족보의 경우 승자. 재경기 정하기
		{
			if (jumsu[i] == 55) {
				if (38 >= max) {
					flg = 3;
					//printf("멍텅구리 구사 -> 재경기\n");
					//printf("%d\n", flg);
					return 0;
				}
				else jumsu[i] = 13;
			}
			if (jumsu[i] == 54) {
				if (26 >= max) {
					flg = 3;
					//printf("구사 -> 재경기\n");
					//printf("%d\n", flg);
					return -1;
				}
				else jumsu[i] = 13;
			}
			if (jumsu[i] == 115) {
				if (max == 40) {
					flg = 1;
					winnerIdx = i;
					//printf("암행어사로 광땡을 잡아서 이김!\n");
					//printf("승자는 player %d\n", i + 1);
					return -1;
				}
				else jumsu[i] = 11;
			}
			if (jumsu[i] == 9) {
				if (max <= 38 && max >= 30) {
					flg = 1;
					winnerIdx = i;
					//printf("땡잡이로 땡을 잡아서 이김!\n");
					//printf("승자는 player %d\n", i + 1);
					return -1;
				}
				else jumsu[i] = 10;
			}
		}


		int win = -1;
		Draw_num = 0;
		for (i = 0; i < player_number; i++)
		{
			if (i != idx)
			{
				int aa = max;
				int b = jumsu[i];

				winner = WinOrLose(aa, b);
				if (winner == 'P')
					continue;
				else if (winner == 'C')
					win = i;			// 예외 처리 ( 나오면 안됨)
				else if (winner == 'D')
					Draw_player[Draw_num++] = i;	// 무승부 끼리 모아서 재경기
			}
		}

		if (win != -1)
		{
			flg = 4;
			//printf("로직 이상함\.n");
			return 0;
		}

		else if (Draw_num != 0)
		{
			//for (i = 0; i < Draw_num; i++)
				//printf("player %d, ", Draw_player[i] + 1);
			//printf("player %d are Draw, Restart Please..\n", idx + 1);
			//printf("점수는 %d 입니다.\n", max);
			Draw_player[Draw_num++] = idx;
			flg = 2;
			return 0;			// Darr 안에 있는 사람들 모두 재경기 (남는 사람이 바뀌고 재경기)
		}
		else{
			flg = 1;
			winnerIdx = i;
			//printf("player %d 가 승자입니다!\n", idx + 1);
			//printf("점수는 %d 입니다.\n", max);
		}
		return 0;
	}
	
	private char WinOrLose(int a, int b)
	{
		char result;
		if (a > b)
			result = 'P';
		else if (a < b)
			result = 'C';
		else
			result = 'D';
		return result;
	}
	
	private int Logic(int a, int b)
	{
		int i, temp1;
		int result, temp[] = new int[2];
		for (i = 0; i<2; i++){
			if ((a == 3) && (b == 8)) //삼팔광땡
				result = 50;
			else if (((a == 1) && (b == 3)) || ((a == 1) && (b == 8))) //광땡
				result = 40;
			else if (((a == 10) && (b == 20))) //장땡
				result = 39;
			else if (((a == 9) && (b == 19))) //구땡
				result = 38;
			else if (((a == 8) && (b == 18))) //팔땡
				result = 37;
			else if (((a == 7) && (b == 17))) //칠땡
				result = 36;
			else if (((a == 6) && (b == 16))) //육땡
				result = 35;
			else if (((a == 5) && (b == 15))) //오땡
				result = 34;
			else if (((a == 4) && (b == 14))) //사땡
				result = 33;
			else if (((a == 3) && (b == 13))) //삼땡
				result = 32;
			else if (((a == 2) && (b == 12))) //이땡
				result = 31;
			else if (((a == 1) && (b == 11))) //삥땡
				result = 30;
			else if (((a == 1) && (b == 2)) || ((a == 1) && (b == 12)) || ((a == 11) && (b == 2)) || ((a == 11) && (b == 12))) //알리
				result = 26;
			else if (((a == 1) && (b == 4)) || ((a == 1) && (b == 14)) || ((a == 11) && (b == 4)) || ((a == 11) && (b == 14))) //독사
				result = 25;
			else if (((a == 1) && (b == 9)) || ((a == 1) && (b == 19)) || ((a == 11) && (b == 9)) || ((a == 11) && (b == 19))) //구삥
				result = 24;
			else if (((a == 1) && (b == 10)) || ((a == 1) && (b == 20)) || ((a == 11) && (b == 10)) || ((a == 11) && (b == 20))) //장삥
				result = 23;
			else if (((a == 4) && (b == 10)) || ((a == 4) && (b == 20)) || ((a == 14) && (b == 10)) || ((a == 14) && (b == 20))) //장사
				result = 22;
			else if (((a == 4) && (b == 6)) || ((a == 4) && (b == 16)) || ((a == 14) && (b == 6)) || ((a == 14) && (b == 16))) //세륙
				result = 21;
			else if (((a == 3) && (b == 7)) || ((a == 3) && (b == 17)) || ((a == 13) && (b == 7)) || ((a == 13) && (b == 17))) //땡잡이
				result = 9;
			else if (((a == 4) && (b == 19)) || ((a == 14) && (b == 9)) || ((a == 14) && (b == 19))) //구사
				result = 55;
			else if (((a == 4) && (b == 9))) //멍텅구리 구사
				result = 54;
			else if (((a == 4) && (b == 7))) //암행어사
				result = 115;
			else if (((a + b) % 10) == 9) //갑오
				result = 19;
			else if (((a + b) % 10) == 8) //8끗
				result = 18;
			else if (((a + b) % 10) == 7) //7끗
				result = 17;
			else if (((a + b) % 10) == 6) //6끗
				result = 16;
			else if (((a + b) % 10) == 5) //5끗
				result = 15;
			else if (((a + b) % 10) == 4) //4끗
				result = 14;
			else if (((a + b) % 10) == 3) //3끗
				result = 13;
			else if (((a + b) % 10) == 2) //2끗
				result = 12;
			else if (((a + b) % 10) == 1) //1끗
				result = 11;
			else if (((a + b) % 10) == 0) //망통
				result = 10;
			else
				result = 0;
			temp[i] = result;
			temp1 = a;
			a = b;
			b = temp1;
		}
		return (temp[0] > temp[1] ? temp[0] : temp[1]);
	}
}
