import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다.
 * 
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로
 * 이를 링크시킨다. section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND = 3;

	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag = 32;
	public static final int iFlag = 16;
	public static final int xFlag = 8;
	public static final int bFlag = 4;
	public static final int pFlag = 2;
	public static final int eFlag = 1;

	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	LabelTable symTab;
	LabelTable literalTab;
	InstTable instTab;

	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;

	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * 
	 * @param symTab    : 해당 section과 연결되어있는 symbol table
	 * @param literaTab : 해당 section과 연결되어있는 literal table
	 * @param instTab   : instruction 명세가 정의된 instTable
	 */
	public TokenTable(LabelTable symTab, LabelTable literalTab, InstTable instTab) {
<<<<<<< HEAD
		this.symTab = symTab;
		this.literalTab = literalTab;
		this.instTab = instTab;
		tokenList = new ArrayList<Token>();
=======
		// ...
>>>>>>> ff8d1842b32e883bd334a3890a27d06792fe59a9
	}

	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * 
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}

	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * 
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}

	/**
	 * Pass2 과정에서 사용한다. instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를
	 * 저장한다.
	 * 
	 * @param index
	 */
	public void makeObjectCode(int index) {
<<<<<<< HEAD
		if(instTab.instMap.containsKey(tokenList.get(index).operator)) { 				
			int code = instTab.instMap.get(tokenList.get(index).operator).opcode << 16; 
			code += getToken(index).nixbpe << 12;
			if(tokenList.get(index).operand[0].contains("#")) {
				code += Integer.parseInt(tokenList.get(index).operand[0].substring(1));
			}
			for(int i = 0; i < symTab.symbolList.size(); i++) {
				if(tokenList.get(index).operand[0].contains(symTab.symbolList.get(i))) { 
					int addr = symTab.locationList.get(i) - tokenList.get(index+1).location;
					if(addr > 0) {
						code += addr;
					} 
					else {
						short ad = (short)addr;
						short mask = 4095;
						code += (ad & mask);
					}
				}
			}
			for(int i = 0; i < literalTab.literalList.size(); i++) {
				if(tokenList.get(index).operand[0].contains(literalTab.literalList.get(i))) {
					int addr = literalTab.locationList.get(i) - tokenList.get(index+1).location;
					ob_code += addr;
				}
			}				
			getToken(index).objectCode = String.format("%06X",ob_code);
	}
=======
		// ...
>>>>>>> ff8d1842b32e883bd334a3890a27d06792fe59a9
	}

	/**
	 * index번호에 해당하는 object code를 리턴한다.
	 * 
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}

}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후 의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 의미 해석이 끝나면 pass2에서
 * object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token {
	// 의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들
	String objectCode;
	int byteSize;

	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다.
	 * 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
<<<<<<< HEAD
=======
		// initialize ???
>>>>>>> ff8d1842b32e883bd334a3890a27d06792fe59a9
		parsing(line);
	}

	/**
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * 
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
<<<<<<< HEAD
		operand = new String[3];
		
		if(line.contains(".")) { //�ּ��� �κ�
			label = ""; 
			operator = ""; 
		}
		else if(line.contains("LTORG")) {
			label = line.split("\t")[0];
			operator = line.split("\t")[1];
		}
		else if(line.contains("CSECT")) {
			label = line.split("\t")[0];
			operator = line.split("\t")[1];
		}
		else { //�ּ��� �ƴ� ���������� operator�� �ִ� ���
			label = line.split("\t")[0];
			operator = line.split("\t")[1];
			if(line.split("\t")[2].contains(",")) { //operand�� ������ �ִ� ���(2, 3��)
				int idx = line.split("\t")[2].indexOf(","); //ó�� ��ǥ �������� �ڸ���
				String ifthree = line.split("\t")[2].substring(idx+1); //ó�� ��ǥ ���� �޺κ� ���
				if(ifthree.contains(",")) { //operand�� 3���� ���
					operand = new String[3];
					String[] array2 = line.split("\t")[2].split(",",3);
					operand[0] = array2[0];
					operand[1] = array2[1];
					operand[2] = array2[2];
					
					int idx2 = line.split(",")[1].indexOf(",");
					String ifcomment = line.split(",")[1].substring(idx2+1);
					if(ifcomment.contains("\t")) { //comment�� �ִ� ���
						comment = line.split("\t")[3];
					}
				}
				else { //operand�� 2���� ���
					operand = new String[3];
					String[] array2 = line.split("\t")[2].split(",",3);
					operand[0] = array2[0];
					operand[1] = array2[1];
					int idx2 = line.split(",")[1].indexOf(",");
					String ifcomment = line.split(",")[1].substring(idx2+1);
					if(ifcomment.contains("\t")) { //comment�� �ִ� ���
						comment = line.split("\t")[3];
					}
				}
			}
			else {//operand������ 0, 1���� ���
				operand = new String[1];
				operand[0] = line.split("\t")[2];
				
				int idx = line.indexOf("\t");
				String ifcomment = line.substring(idx+1);
				int idx2 = ifcomment.indexOf("\t");
				int idx3 = ifcomment.substring(idx2+1).indexOf("\t");
				if(idx3 != -1) { //comment�� �ִ� ���
					comment = ifcomment.substring(idx2+1).substring(idx3+1);
				}
			}
		}
=======

>>>>>>> ff8d1842b32e883bd334a3890a27d06792fe59a9
	}

	/**
	 * n,i,x,b,p,e flag를 설정한다.
	 * 
	 * 
	 * 사용 예 : setFlag(nFlag, 1) 또는 setFlag(TokenTable.nFlag, 1)
	 * 
	 * @param flag  : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
	 */
	public void setFlag(int flag, int value) {
<<<<<<< HEAD
		if (value == 1 ) { 
			nixbpe = (char) (nixbpe | flag);
		}
=======
		// ...
>>>>>>> ff8d1842b32e883bd334a3890a27d06792fe59a9
	}

	/**
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다.
	 * 
	 * 사용 예 : getFlag(nFlag) 또는 getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */

	public int getFlag(int flags) {
		return nixbpe & flags;
	}
}
