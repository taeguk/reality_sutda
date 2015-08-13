package reality_sutda_server;

/*
 * bumsu's sutda game result judgement C++ code
 * 	porting to JAVA class by taeguk.
 */

public class GameResultJudge {
	public static final int WHOWINS = 1;
	public static final int DRAW = 2;
	public static final int REMATCH = 3;
	public static int card[] = { 1, 11, 2, 12, 3, 13, 4, 14, 5, 15, 6, 16, 7, 17, 8, 18, 9, 19, 10, 20 };
	
	private int index[];
	private int player_number;
	private int[][] player_card;
	private int flg;						// 1 -> �듅�옄, 2 -> 臾댁듅遺�, 3 -> �굹媛�由� (�쟾遺� 臾댁듅遺�)
	private int Draw_num;
	private int[] Draw_player;	// 臾댁듅遺� player �닔, 臾댁듅遺� players
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
		int i, j, a, temp; //�젙�닔�삎蹂��닔�꽑�뼵
		int jumsu[] = new int[player_number];
		char winner;
		
		int cnt = 0;
		for (i = 0; i < player_number; i++)
		{
			player_card[i][0] = card[index[cnt++]];
			player_card[i][1] = card[index[cnt++]];
			//printf("Player %d�쓽 �뙣 : %d, %d\n", i+1, player_card [i][0], player_card [i][1]);
		}

		for (i = 0; i < player_number; i++)
		{
			jumsu[i] = Logic(player_card[i][0], player_card[i][1]);		// logic�뿉 �쓽�빐 �젏�닔 援ы븯湲�
			//printf("Player %d�쓽 �젏�닔 : %d\n", i + 1, jumsu[i]);
		}

		int max = 0;
		int idx = 0;								// (�듅�닔 議깅낫)援ъ궗�� 硫랁뀉援щ━ 援ъ궗瑜� �젣�쇅�븳 媛��옣 �겙 �젏�닔
		for (i = 0; i < player_number; i++)
		{
			if (max < jumsu[i] && jumsu[i] != 55 && jumsu[i] != 54 && jumsu[i] != 115 && jumsu[i] != 9)
			{
				max = jumsu[i];
				idx = i;
			}
		}

		for (i = 0; i < player_number; i++)		// �듅�닔 議깅낫�쓽 寃쎌슦 �듅�옄. �옱寃쎄린 �젙�븯湲�
		{
			if (jumsu[i] == 55) {
				if (38 >= max) {
					flg = 3;
					//printf("硫랁뀉援щ━ 援ъ궗 -> �옱寃쎄린\n");
					//printf("%d\n", flg);
					return 0;
				}
				else jumsu[i] = 13;
			}
			if (jumsu[i] == 54) {
				if (26 >= max) {
					flg = 3;
					//printf("援ъ궗 -> �옱寃쎄린\n");
					//printf("%d\n", flg);
					return -1;
				}
				else jumsu[i] = 13;
			}
			if (jumsu[i] == 115) {
				if (max == 40) {
					flg = 1;
					winnerIdx = i;
					//printf("�븫�뻾�뼱�궗濡� 愿묐븸�쓣 �옟�븘�꽌 �씠源�!\n");
					//printf("�듅�옄�뒗 player %d\n", i + 1);
					return -1;
				}
				else jumsu[i] = 11;
			}
			if (jumsu[i] == 9) {
				if (max <= 38 && max >= 30) {
					flg = 1;
					winnerIdx = i;
					//printf("�븸�옟�씠濡� �븸�쓣 �옟�븘�꽌 �씠源�!\n");
					//printf("�듅�옄�뒗 player %d\n", i + 1);
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
					win = i;			// �삁�쇅 泥섎━ ( �굹�삤硫� �븞�맖)
				else if (winner == 'D')
					Draw_player[Draw_num++] = i;	// 臾댁듅遺� �겮由� 紐⑥븘�꽌 �옱寃쎄린
			}
		}

		if (win != -1)
		{
			flg = 4;
			//printf("濡쒖쭅 �씠�긽�븿\.n");
			return 0;
		}

		else if (Draw_num != 0)
		{
			//for (i = 0; i < Draw_num; i++)
				//printf("player %d, ", Draw_player[i] + 1);
			//printf("player %d are Draw, Restart Please..\n", idx + 1);
			//printf("�젏�닔�뒗 %d �엯�땲�떎.\n", max);
			Draw_player[Draw_num++] = idx;
			flg = 2;
			return 0;			// Darr �븞�뿉 �엳�뒗 �궗�엺�뱾 紐⑤몢 �옱寃쎄린 (�궓�뒗 �궗�엺�씠 諛붾�뚭퀬 �옱寃쎄린)
		}
		else{
			flg = 1;
			winnerIdx = idx;
			//printf("player %d 媛� �듅�옄�엯�땲�떎!\n", idx + 1);
			//printf("�젏�닔�뒗 %d �엯�땲�떎.\n", max);
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
			if ((a == 3) && (b == 8)) //�궪�뙏愿묐븸
				result = 50;
			else if (((a == 1) && (b == 3)) || ((a == 1) && (b == 8))) //愿묐븸
				result = 40;
			else if (((a == 10) && (b == 20))) //�옣�븸
				result = 39;
			else if (((a == 9) && (b == 19))) //援щ븸
				result = 38;
			else if (((a == 8) && (b == 18))) //�뙏�븸
				result = 37;
			else if (((a == 7) && (b == 17))) //移좊븸
				result = 36;
			else if (((a == 6) && (b == 16))) //�쑁�븸
				result = 35;
			else if (((a == 5) && (b == 15))) //�삤�븸
				result = 34;
			else if (((a == 4) && (b == 14))) //�궗�븸
				result = 33;
			else if (((a == 3) && (b == 13))) //�궪�븸
				result = 32;
			else if (((a == 2) && (b == 12))) //�씠�븸
				result = 31;
			else if (((a == 1) && (b == 11))) //�궏�븸
				result = 30;
			else if (((a == 1) && (b == 2)) || ((a == 1) && (b == 12)) || ((a == 11) && (b == 2)) || ((a == 11) && (b == 12))) //�븣由�
				result = 26;
			else if (((a == 1) && (b == 4)) || ((a == 1) && (b == 14)) || ((a == 11) && (b == 4)) || ((a == 11) && (b == 14))) //�룆�궗
				result = 25;
			else if (((a == 1) && (b == 9)) || ((a == 1) && (b == 19)) || ((a == 11) && (b == 9)) || ((a == 11) && (b == 19))) //援ъ궏
				result = 24;
			else if (((a == 1) && (b == 10)) || ((a == 1) && (b == 20)) || ((a == 11) && (b == 10)) || ((a == 11) && (b == 20))) //�옣�궏
				result = 23;
			else if (((a == 4) && (b == 10)) || ((a == 4) && (b == 20)) || ((a == 14) && (b == 10)) || ((a == 14) && (b == 20))) //�옣�궗
				result = 22;
			else if (((a == 4) && (b == 6)) || ((a == 4) && (b == 16)) || ((a == 14) && (b == 6)) || ((a == 14) && (b == 16))) //�꽭瑜�
				result = 21;
			else if (((a == 3) && (b == 7)) || ((a == 3) && (b == 17)) || ((a == 13) && (b == 7)) || ((a == 13) && (b == 17))) //�븸�옟�씠
				result = 9;
			else if (((a == 4) && (b == 19)) || ((a == 14) && (b == 9)) || ((a == 14) && (b == 19))) //援ъ궗
				result = 55;
			else if (((a == 4) && (b == 9))) //硫랁뀉援щ━ 援ъ궗
				result = 54;
			else if (((a == 4) && (b == 7))) //�븫�뻾�뼱�궗
				result = 115;
			else if (((a + b) % 10) == 9) //媛묒삤
				result = 19;
			else if (((a + b) % 10) == 8) //8�걮
				result = 18;
			else if (((a + b) % 10) == 7) //7�걮
				result = 17;
			else if (((a + b) % 10) == 6) //6�걮
				result = 16;
			else if (((a + b) % 10) == 5) //5�걮
				result = 15;
			else if (((a + b) % 10) == 4) //4�걮
				result = 14;
			else if (((a + b) % 10) == 3) //3�걮
				result = 13;
			else if (((a + b) % 10) == 2) //2�걮
				result = 12;
			else if (((a + b) % 10) == 1) //1�걮
				result = 11;
			else if (((a + b) % 10) == 0) //留앺넻
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
