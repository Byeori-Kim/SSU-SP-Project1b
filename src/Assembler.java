import java.io.*;
import java.util.ArrayList;

/**
 * Assembler: 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인루틴이다. 프로그램의 수행 작업은 다음과 같다.
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다.
 * 
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다
 * 
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1)
 * 
 * 4) 분석된 내용을바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2)
 * 
 * 
 * 작성중의 유의사항:
 * 
 * 1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은
 * 안된다.
 * 
 * 2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨
 * 
 * 3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.
 * 
 * 4) 파일, 또는 콘솔창에 한글을 출력시키지말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)
 * 
 * + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수
 * 있습니다.
 */

public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간 */
	ArrayList<LabelTable> symtabList;
	/** 프로그램의 section별로 literal table을 저장하는 공간 */
	ArrayList<LabelTable> literaltabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간 */
	ArrayList<TokenTable> TokenList;
	/**
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. 필요한 경우 String 대신 별도의 클래스를
	 * 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;

	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름.
	 */
	public Assembler(String instFile) {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<LabelTable>();
		literaltabList = new ArrayList<LabelTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/**
	 * 어셈블러의 메인 루틴
	 */
	public static void main(String[] args) {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");
		assembler.pass1();

		assembler.printSymbolTable("symtab_20171281.txt");
		assembler.printLiteralTable("literaltab_20171281.txt");
		assembler.pass2();
		assembler.printObjectCode("output_20171281.txt");
	}

	/**
	 * inputFile을 읽어들여서 lineList에 저장한다.
	 * 
	 * @param inputFile : input 파일 이름.
	 */
	int line_num = 0;
	private void loadInputFile(String inputFile) {
		try {
			File file = new File(inputFile);
			FileReader filereader = new FileReader(file);
			BufferedReader bufReader = new BufferedReader(filereader);
			String line = "";
			while((line = bufReader.readLine()) != null){
				lineList.add(line);
				line_num++;
			}
		}
		catch(IOException e){
			System.out.println("There is errer" + e);
		}

	}

	/**
	 * pass1 과정을 수행한다.
	 * 
	 * 1) 프로그램 소스를 스캔하여 토큰 단위로 분리한 뒤 토큰 테이블을 생성.
	 * 
	 * 2) symbol, literal 들을 SymbolTable, LiteralTable에 정리.
	 * 
	 * 주의사항: SymbolTable, LiteralTable, TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 *
	 */
	int pro_num;
	private void pass1() {
		//프로그램을 나누기 위해 필요
		LabelTable ST_for_pronum  = new LabelTable();
		LabelTable LT_for_pronum  = new LabelTable();
		TokenTable TT_for_pronum = new TokenTable(ST_for_pronum , LT_for_pronum , instTable);
		for (int i = 0; i < lineList.size(); i++) {
			TT_for_pronum.putToken(lineList.get(i));
		}
		//프로그램 나누기 위해 필요한 변수들
		int first[] = new int[10];
		int j = 0;
		pro_num = 0;

		//전체를 돌며 프로그램을 start, csect, end기준으로 구역을 나눔(input.txt의 경우 3개로 나뉨)
		for (int i = 0; i < line_num; i++) {
			if(!TT_for_pronum.getToken(i).operator.equals(null)) {
				if (TT_for_pronum.getToken(i).operator.equals("START")) {
					first[0] = i;
					j++;
				}
				if (TT_for_pronum.getToken(i).operator.equals("CSECT")) {
					pro_num++;
					first[j] = i;
					j++;
				}
				if (TT_for_pronum.getToken(i).operator.equals("END")) {
					first[j] = i+1;
					j++;
					first[j+1] =0;
				}
			}
		}

		for(int a = 0; a < pro_num +1; a++) { //프로그램 객수만큼 돌아 섹션 별로 저장해줌
			int loc = 0;
			LabelTable symtab = new LabelTable();
			LabelTable littab = new LabelTable();
			TokenTable TT = new TokenTable(symtab, littab, instTable);

			TT.symTab.label = new ArrayList();
			TT.symTab.locationList = new ArrayList();
			TT.literalTab.label = new ArrayList();
			TT.literalTab.locationList = new ArrayList();

			//TokenTable에 tokenList 더해주는 부분
			for(int i = first[a]; i < first[a+1]; i++) {
				TT.putToken(lineList.get(i));
			}

			//location 구해서 저장하고, literalTable 저장해주는 부분
			for(int i = 0; i < first[a+1]-first[a]; i++) {
				if(!TT.getToken(i).operator.isEmpty()) { //주석 부분은 걸러줌
					if(TT.getToken(i).operator.equals("RESW")) {
						loc += Integer.parseInt(TT.getToken(i).operand[0]) * 3;
						TT.getToken(i+1).location = loc;

					}
					if(TT.getToken(i).operator.equals("RESB")) {
						loc += Integer.parseInt(TT.getToken(i).operand[0]);
						TT.getToken(i+1).location = loc;
					}
					if(TT.getToken(i).operator.equals("BYTE")) {
						if (TT.getToken(i).operand[0].contains("C")) { //char형인 경우
							String tmp = TT.getToken(i).operand[0].split("'")[1];
							loc += tmp.length();
							TT.getToken(i+1).location = loc;
						}
						else if (TT.getToken(i).operand[0].contains("X")) { //16진수인 경우
							String tmp = TT.getToken(i).operand[0].split("'")[1];
							loc += tmp.length() / 2; //X는 두글자가 한바이트
							TT.getToken(i+1).location = loc;
						}
					}
					int lit_last = 0;
					if (TT.getToken(i).operator.equals("LTORG")) { //LTORG인 경우
						for (int p = 0; p < i ; p++) {
							if (TT.getToken(p).operand[0].contains("=C")) { //char형인 경우
								lit_last = i; //다음에 LTORG나 END나오면 이 찾은거 다음부터 해야해서
								String tmp = TT.getToken(p).operand[0].split("'")[1];
								loc += tmp.length();
								TT.getToken(i+1).location = loc;
								TT.literalTab.putName(TT.getToken(p).operand[0].split("'")[1], TT.getToken(i).location);
							}
							else if (TT.getToken(p).operand[0].contains("=X")) { //16진수인 경우
								lit_last = i; //다음에 LTORG나 END나오면 이 찾은거 다음부터 해야해서
								String tmp = TT.getToken(p).operand[0].split("'")[1];
								loc += tmp.length() / 2; //X는 두글자가 한바이트
								TT.getToken(i+1).location = loc;
								TT.literalTab.putName(TT.getToken(p).operand[0].split("'")[1], TT.getToken(i).location);
							}
						}
					}
					if (TT.getToken(i).operator.equals("END")) { //END인 경우
						for (int p = lit_last + 1; p < i; p++) { //LTORG가 찾은 이후부터
							if ((TT.getToken(p).operand != null && TT.getToken(p).operand[0] != null) &&TT.getToken(p).operand[0].contains("=C")) {
								String tmp = TT.getToken(p).operand[0].split("'")[1];
								loc += tmp.length();
								int count =0;
								if(TT.literalTab.label != null) { //label가 비어있지 않던 경우
									for(int k=0; k<TT.literalTab.label.size(); k++) {  //중복된 literal 검사
										if(TT.getToken(p).operand[0].equals(TT.literalTab.label.get(k))) {
											count = 1;
										}
									}
									if (count == 0) {
										TT.literalTab.putName(TT.getToken(p).operand[0].split("'")[1], TT.getToken(i).location);
									}
								}
								else if (TT.literalTab.label == null) { //label가 비어있던 초기의 경우
									TT.literalTab.putName(TT.getToken(p).operand[0].split("'")[1], TT.getToken(i).location);
								}
							}
							else if (TT.getToken(p).operand != null && (TT.getToken(p).operand[0] != null) && TT.getToken(p).operand[0].contains("=X")) { //16진수인 경우
								String tmp = TT.getToken(p).operand[0].split("'")[1];
								loc += tmp.length() / 2; //X는 두글자가 한바이트
								int count = 0;
								if(TT.literalTab.label != null) { //label가 비어있지 않던 경우
									for(int k=0; k<TT.literalTab.label.size(); k++) {  //중복된 literal 검사
										if(TT.getToken(p).operand[0].split("'")[1].equals(TT.literalTab.label.get(k))) {
											count = 1;
										}
									}
									if (count == 0) {
										TT.literalTab.putName(TT.getToken(p).operand[0].split("'")[1], TT.getToken(i).location);
									}
								}
								else if (TT.literalTab.label == null) { //label가 비어있던 초기의 경우
									TT.literalTab.putName(TT.getToken(p).operand[0].split("'")[1], TT.getToken(i).location);
								}
							}
						}
					}
					if(instTable.instMap.containsKey(TT.getToken(i).operator)) { //inst.data에 있는 경우
						Instruction temp = instTable.instMap.get(TT.getToken(i).operator);
						int type = temp.format;
						loc += type;
						TT.getToken(i+1).location = loc;
					}
					else if(instTable.instMap.containsKey(TT.getToken(i).operator.substring(1))) { //+, 4형식인 경우
						Instruction temp = instTable.instMap.get(TT.getToken(i).operator.substring(1));
						int type = temp.format + 1;
						loc += type;
						TT.getToken(i+1).location = loc;
					}
				}
			}

			//label유무 찾아서 symbolTable에 더해주는 부분
			for(int i = 0; i < first[a+1]-first[a]; i++) {
				if(TT.getToken(i).label.isEmpty()) {
				}
				else {
					int count = 0; //중복 검사 위한 변수
					if(TT.symTab.label != null) { //label가 비어있지 않던 경우
						for(int k=0; k<TT.symTab.label.size(); k++) {  //중복된 symbol 검사
							if(TT.getToken(i).operand[0].equals(TT.symTab.label.get(k))) {
								count = 1;
							}
						}
						if(count == 0) { //중복이 아닌경우에만 넣어줌
							TT.symTab.putName(TT.getToken(i).label, TT.getToken(i).location);
						}
					}
					else if (TT.symTab.label == null) { //label가 비어있던 초기의 경우
						TT.symTab.putName(TT.getToken(i).label, TT.getToken(i).location);
					}
				}
				if(TT.getToken(i).operator.equals("EQU")) {
					int m = 0, n = 0;
					if(TT.getToken(i-1).operator.equals("EQU")) {
						for(int k=0; k < TT.symTab.label.size(); k++) {
							if(TT.symTab.label.get(k).equals(TT.getToken(i).operand[0].split("-")[0])) {
								m = k;
							}
							else if(TT.symTab.label.get(k).equals(TT.getToken(i).operand[0].split("-")[1])) {
								n = k;
							}
						}
						loc = TT.symTab.locationList.get(m) - TT.symTab.locationList.get(n);
						TT.symTab.modifyName(TT.getToken(i).label, loc);
					}
				}
			}
			TokenList.add(TT);
			symtabList.add(symtab);
			literaltabList.add(littab);
		}

		//nixbpe저장하는 부분
		for (int i = 0; i < TokenList.size() ; i++) {
			for(int k = 0; k < first[i+1]-first[i]; k++) {
				if(!TokenList.get(i).tokenList.get(k).operator.isEmpty()) {
					if(TokenList.get(i).instTab.instMap.containsKey(TokenList.get(i).tokenList.get(k).operator)) {
						int count = 0;
						if(TokenList.get(i).tokenList.get(k).operand[0] != null) {
							if(instTable.instMap.get(TokenList.get(i).tokenList.get(k).operator).numberOfOperand == 0) {
								count++;
							}
							if (TokenList.get(i).tokenList.get(k).operand[0].contains("#")) { //immediate addressing인 경우
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.nFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.iFlag, 1);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.xFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.bFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.pFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.eFlag, 0);
								count++;
							}
							else if (TokenList.get(i).tokenList.get(k).operand[0].contains("@")) { //indirect addressing인 경우
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.nFlag, 1);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.iFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.xFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.bFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.pFlag, 1);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.eFlag, 0);
								count++;
							}
							else { //보통의 경우
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.nFlag, 1);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.iFlag, 1);
							}
							if(TokenList.get(i).tokenList.get(k).operator.contains("+")) { //4형식인 경우
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.bFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.pFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.eFlag, 1);
								count++;
							}
						}
						if (count == 0){ //보통의 경우
							TokenList.get(i).tokenList.get(k).setFlag(TokenTable.bFlag, 0);
							TokenList.get(i).tokenList.get(k).setFlag(TokenTable.pFlag, 1);
							TokenList.get(i).tokenList.get(k).setFlag(TokenTable.eFlag, 0);
						}
					}
					else if (TokenList.get(i).instTab.instMap.containsKey(TokenList.get(i).tokenList.get(k).operator.substring(1))) {
						int count = 0;
						if(TokenList.get(i).tokenList.get(k).operand[0] != null) {
							if (TokenList.get(i).tokenList.get(k).operand[0].contains("#")) { //immediate addressing인 경우
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.nFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.iFlag, 1);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.xFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.bFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.pFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.eFlag, 0);
								count++;
							}
							else if (TokenList.get(i).tokenList.get(k).operand[0].contains("@")) { //indirect addressing인 경우
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.nFlag, 1);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.iFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.xFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.bFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.pFlag, 1);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.eFlag, 0);
								count++;
							}
							else { //보통의 경우
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.nFlag, 1);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.iFlag, 1);
							}
							if(TokenList.get(i).tokenList.get(k).operator.contains("+")) { //4형식인 경우
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.bFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.pFlag, 0);
								TokenList.get(i).tokenList.get(k).setFlag(TokenTable.eFlag, 1);
								if(TokenList.get(i).tokenList.get(k).operand[0].equals("BUFFER")) {
									TokenList.get(i).tokenList.get(k).setFlag(TokenTable.xFlag, 1);
								}
								count++;
							}
						}
						if (count == 0){ //보통의 경우
							TokenList.get(i).tokenList.get(k).setFlag(TokenTable.bFlag, 0);
							TokenList.get(i).tokenList.get(k).setFlag(TokenTable.pFlag, 1);
							TokenList.get(i).tokenList.get(k).setFlag(TokenTable.eFlag, 0);
						}
					}
				}
			}
		}

		//EXTDEF, EXTREF 등 external 선언을 처리한다 (extdefList, extrefList에 저장)
		for(int i = 0; i < TokenList.size(); i++) {
			TokenList.get(i).symTab.extdefList = new ArrayList();
			TokenList.get(i).symTab.extrefList = new ArrayList();
			for(int k = 0; k < TokenList.get(i).tokenList.size(); k++){
				if(TokenList.get(i).tokenList.get(k).operator.equals("EXTREF")) {
					TokenList.get(i).symTab.extrefList.add(TokenList.get(i).tokenList.get(k).operand[0]);
					TokenList.get(i).symTab.extrefList.add(TokenList.get(i).tokenList.get(k).operand[1]);
					TokenList.get(i).symTab.extrefList.add(TokenList.get(i).tokenList.get(k).operand[2]);
				}
				else if(TokenList.get(i).tokenList.get(k).operator.equals("EXTDEF")) {
					TokenList.get(i).symTab.extdefList.add(TokenList.get(i).tokenList.get(k).operand[0]);
					TokenList.get(i).symTab.extdefList.add(TokenList.get(i).tokenList.get(k).operand[1]);
					TokenList.get(i).symTab.extdefList.add(TokenList.get(i).tokenList.get(k).operand[2]);
				}
			}
		}
	}

	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printSymbolTable(String fileName) {
		try{
			File file = new File(fileName);

            FileWriter fw = new FileWriter(file, false) ; 
             
            for (int i = 0; i < TokenList.size(); i++) {
            	for(int j = 0; j < symtabList.get(i).label.size(); j++) {
            		fw.write(TokenList.get(i).symTab.label.get(j) + "\t" + 
					Integer.toHexString(TokenList.get(i).symTab.locationList.get(j)).toUpperCase() + "\n");
                	fw.flush();
            	}
            	fw.write("\n");
            	fw.flush();
            }
            fw.close();
        }catch(IOException e){
			System.out.println("Error: " + e);
        }

	}

	private void printLiteralTable(String fileName) {
		try{
			File file = new File(fileName);

			FileWriter fw = new FileWriter(file, false) ;

			for (int i = 0; i < TokenList.size(); i++) {
				for(int j = 0; j < literaltabList.get(i).label.size(); j++) {
					fw.write(TokenList.get(i).literalTab.label.get(j) + "\t" +
							Integer.toHexString(TokenList.get(i).literalTab.locationList.get(j)).toUpperCase() + "\n");
					fw.flush();
				}
				fw.write("\n");
				fw.flush();
			}
			fw.close();
		}catch(IOException e){
			System.out.println("Error: " + e);
		}

	}

	/**
	 * pass2 과정을 수행한다.
	 * 
	 * 1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		String a = null;

		//objectCode와 byteSize를 저장하는 부분
		//분석한 objectCode를 codeList에 저장해줌
		for (int i = 0; i < TokenList.size() ; i++) { //프로그램 갯수만큼
			for(int k = 0; k < TokenList.get(i).tokenList.size(); k++) { //각 프로그램의 줄 수 만큼 돔
				if(!TokenList.get(i).tokenList.get(k).operator.isEmpty()) { //명령어가 있는 경우 (즉, 주석 제외)
					if(TokenList.get(i).instTab.instMap.containsKey(TokenList.get(i).tokenList.get(k).operator)) { //명령어가 inst.data파일에 있는 경우
						if(instTable.instMap.get(TokenList.get(i).tokenList.get(k).operator).format == 2) { //2형식인 경우
							TokenList.get(i).tokenList.get(k).byteSize = 2;
							int tmp = instTable.instMap.get(TokenList.get(i).tokenList.get(k).operator).opcode << 8;
							if(TokenList.get(i).tokenList.get(k).operand[0].equals("A")) {
								tmp += 0 << 4;
								if(lineList.get(k+1+TokenList.get(i).tokenList.size()).contains(",S")) {
									tmp += 4;
								}
							}
							else if(TokenList.get(i).tokenList.get(k).operand[0].equals("X")) {
								tmp += 1 << 4;
							}
							else if(TokenList.get(i).tokenList.get(k).operand[0].equals("S")) {
								tmp += 4 << 4;
							}
							else if(TokenList.get(i).tokenList.get(k).operand[0].equals("T")) {
								tmp += 5 << 4;
							}
							a = String.format("%04X", tmp);
							TokenList.get(i).tokenList.get(k).objectCode = a;
						}
						else{
							TokenList.get(i).tokenList.get(k).byteSize = 3;
							TokenList.get(i).makeObjectCode(k);
						}
					}
					else if(TokenList.get(i).tokenList.get(k).operator.contains("+")) { //4형식인 경우
						TokenList.get(i).tokenList.get(k).byteSize = 4;
						String b = TokenList.get(i).tokenList.get(k).operator.substring(1);
						int ob_code = instTable.instMap.get(b).opcode << 24;
						ob_code += TokenList.get(i).tokenList.get(k).nixbpe << 20;
						TokenList.get(i).tokenList.get(k).objectCode = String.format("%08X",ob_code);
					}
					if(TokenList.get(i).tokenList.get(k).operator.equals("LTORG")) {
						TokenList.get(i).tokenList.get(k).byteSize = 3;
						String tmp = TokenList.get(i).literalTab.label.get(0);
						byte[] byte_str = new byte[tmp.length()];
						for(int n = 0; n < tmp.length(); n++) {
							byte_str[n] = (byte)tmp.charAt(n);
						}
						int g = byte_str[0] << 16;
						g += byte_str[1] << 8;
						g += byte_str[2];
						TokenList.get(i).tokenList.get(k).objectCode = String.format("%06X", g);
					}
					if(TokenList.get(i).tokenList.get(k).operator.equals("BYTE")) {
						TokenList.get(i).tokenList.get(k).byteSize = 1;
						a = TokenList.get(i).tokenList.get(k).operand[0].split("'")[1];
						TokenList.get(i).tokenList.get(k).objectCode = a;
					}
					if(TokenList.get(i).tokenList.get(k).operator.equals("WORD")) {
						TokenList.get(i).tokenList.get(k).byteSize = 3;
						int count = 0;
						for(int j=0; j < TokenList.get(i).symTab.label.size(); j++) {
							if(TokenList.get(i).symTab.label.get(j).equals(TokenList.get(i).tokenList.get(k).operand[0].split("-")[0])) {
								count++;
							}
							else if(TokenList.get(i).symTab.label.get(j).equals(TokenList.get(i).tokenList.get(k).operand[0].split("-")[1])) {
								count++;
							}
						}
						if(count == 0) { //오퍼랜드가 프로그램에 없는 경우
							a = "000000";
							TokenList.get(i).tokenList.get(k).objectCode = a;
						}
					}
					if(TokenList.get(i).tokenList.get(k).operator.equals("END")) {
						TokenList.get(i).tokenList.get(k).byteSize = 1;
						a = TokenList.get(i).literalTab.label.get(0);
						TokenList.get(i).tokenList.get(k).objectCode = a;
					}
				}
				codeList.add(TokenList.get(i).tokenList.get(k).objectCode);
			}
		}
	}

	/**
	 * 작성된 codeList를 출력형태에 맞게 출력한다.
	 * 
	 * @param fileName : 저장되는 파일 이름
	 */
	private void printObjectCode(String fileName) {
		int [] length = new int[TokenList.size()]; //각 프로그램별 길이 저장
		try{
			File file = new File(fileName);

			FileWriter fw = new FileWriter(file, false) ; //파일 내용 지우고 새로 작성

			for (int i = 0; i < TokenList.size() ; i++) {
				//H, D, R출력 부분
				if(TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-1).operator.equals("EQU")) { //프로그램 별 길이 구하기
					length[i] = TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-2).location + TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-1).byteSize;
				}

				else {
					length[i] = TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-1).location + TokenList.get(i).tokenList.get(TokenList.get(i).tokenList.size()-1).byteSize;
				}
				fw.write("H" + String.format("%-6s", TokenList.get(i).tokenList.get(0).label) + String.format("%06X", TokenList.get(i).tokenList.get(0).location) + String.format("%06X", length[i]) + "\n");
				fw.flush();
				if(TokenList.get(i).symTab.extdefList.size() != 0) {
					fw.write("D");
					int idx = 0;
					for(int j = 0; j < TokenList.get(i).symTab.extdefList.size(); j++) {
						for(int k=0; k < TokenList.get(i).symTab.label.size(); k++) {
							if(TokenList.get(i).symTab.extdefList.get(j).equals(TokenList.get(i).symTab.label.get(k))) {
								idx = k;
							}
						}
						fw.write(TokenList.get(i).symTab.extdefList.get(j) + String.format("%06X", TokenList.get(i).symTab.locationList.get(idx)));
					}
					fw.write("\n");
					fw.flush();
				}

				if(TokenList.get(i).symTab.extrefList.size() != 0) {
					fw.write("R");
					for(int j = 0; j < TokenList.get(i).symTab.extrefList.size(); j++) {
						if(TokenList.get(i).symTab.extrefList.get(j) != null)
							fw.write( String.format("%-6s", TokenList.get(i).symTab.extrefList.get(j)));
					}
					fw.write("\n");
				}
				fw.flush();

				//T 출력 부분
				int line = 0;
				int count = 0;
				for(int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
					if(TokenList.get(i).tokenList.get(j).objectCode != null) {
						line += TokenList.get(i).tokenList.get(j).objectCode.length();
						if(line > 60) {
							count++;
							line = 0;
							fw.write("T");
							fw.write(String.format("%06X%02X", TokenList.get(i).tokenList.get(0).location, TokenList.get(i).tokenList.get(j).location));
							for(int k = 0; k < j; k++) {
								if(TokenList.get(i).tokenList.get(k).objectCode != null) {
									if(!TokenList.get(i).tokenList.get(k).operator.equals("LTORG")) {
										if( i == 0)
											fw.write(String.format("%s",codeList.get(k)));
										else
											fw.write(String.format("%s",codeList.get(TokenList.get(i-1).tokenList.size() + k)));
									}
								}
							}
						}
						if(count == 1) {
							count = 2;
							for(int k = j; k < TokenList.get(i).tokenList.size(); k++) {
								if(TokenList.get(i).tokenList.get(k).objectCode != null)
									if(!TokenList.get(i).tokenList.get(k).operator.equals("LTORG"))
										line += TokenList.get(i).tokenList.get(k).objectCode.length();
							}
							fw.write("\nT");
							fw.write(String.format("%06X%02X", TokenList.get(i).tokenList.get(j).location, line / 2));
							for(int k = j; k < TokenList.get(i).tokenList.size(); k++) {
								if(TokenList.get(i).tokenList.get(k).objectCode != null)
									if(!TokenList.get(i).tokenList.get(k).operator.equals("LTORG")) {
										if( i == 0)
											fw.write(String.format("%s",codeList.get(k)));
										else
											fw.write(String.format("%s",codeList.get(TokenList.get(i-1).tokenList.size() + k)));
									}
							}
						}
					}
					if(TokenList.get(i).tokenList.get(j).operator.equals("LTORG")) {
						fw.write(String.format("\nT%06X%02X%s", TokenList.get(i).tokenList.get(j).location, TokenList.get(i).tokenList.get(j).byteSize, codeList.get(j)));
					}
				}
				if(count == 0) {
					fw.write("T");
					fw.write(String.format("%06X%02X", TokenList.get(i).tokenList.get(0).location, line / 2));
					for(int k = 0; k < TokenList.get(i).tokenList.size(); k++) {
						if(TokenList.get(i).tokenList.get(k).objectCode != null) {
							int tmp = 0;
							for(int l = 0 ; l < i + 1; l++) {
								tmp +=TokenList.get(i).tokenList.size();
							}
							tmp -= 2;
							fw.write(String.format("%s",codeList.get(tmp + k)));
						}
					}
				}
				fw.write("\n");

				//M 출력 부분
				for(int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
					if(TokenList.get(i).tokenList.get(j).operand != null && TokenList.get(i).tokenList.get(j).operand[0] != null) {
						for(int k = 0; k < TokenList.get(i).symTab.extrefList.size(); k++) {
							if(TokenList.get(i).symTab.extrefList.get(k) != null) {
								if(TokenList.get(i).tokenList.get(j).operand[0].split("-")[0].equals(TokenList.get(i).symTab.extrefList.get(k))) {
									if(!TokenList.get(i).tokenList.get(j).operator.equals("EXTREF")) {
										if(TokenList.get(i).tokenList.get(j).operand[0].contains("-")) {
											if(TokenList.get(i).tokenList.get(j).operator.equals("WORD")) {
												fw.write("M");
												fw.write(String.format("%06X", TokenList.get(i).tokenList.get(j).location) + String.format("%02X", 06) + String.format("+%s", TokenList.get(i).tokenList.get(j).operand[0].split("-")[0]) + "\n");
												fw.write("M");
												fw.write(String.format("%06X", TokenList.get(i).tokenList.get(j).location) + String.format("%02X", 06) + String.format("-%s", TokenList.get(i).tokenList.get(j).operand[0].split("-")[1]) + "\n");
												fw.flush();
											}
										}
										else {
											fw.write("M");
											fw.write(String.format("%06X", TokenList.get(i).tokenList.get(j).location+1) + String.format("%02X", 05) + String.format("+%s", TokenList.get(i).tokenList.get(j).operand[0]) + "\n");
										}
									}
								}
							}
						}
					}
				}

				//E 출력 부분
				fw.write("E");
				for(int j = 0; j < TokenList.get(i).tokenList.size(); j++) {
					if(TokenList.get(i).tokenList.get(j).operator.equals("START")) {
						fw.write(String.format("%06X", TokenList.get(i).tokenList.get(j).location));
					}
				}
				fw.write("\n\n");
				fw.flush();
			}

			fw.close();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
}
