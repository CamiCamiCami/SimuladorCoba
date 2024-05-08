

public class Instruccion {

	protected enum Tipo {
		ADD(1),
		SUB(1),
		AND(1),
		OR(1),
		ADDI(5),
		ANDI(4),
		ORI(9),
		BR(0),
		JR(10),
		JALR(10),
		TRAP(10),
		RETI(10),
		NOT(10),
		JAL(8),
		LD(2),
		ST(3),
		LDR(6),
		STR(6),
		LUI(7),
		LORI(7),
		LJMP(11);

		public final int opcode;

		private Tipo(int opcode){
			this.opcode = opcode;
		}

		public static Tipo bin2Tipo(short raw){
			int opcode = raw >> 12;
			int id, b;
			opcode = opcode & 0b00001111;

			switch (opcode){
				case 1:
					id = raw & 0b0000000000011000;
					id = id >> 3;
					switch (id){
						case 0:
							return ADD;
						case 1:
							return SUB;
						case 2:
							return AND;
						case 3:
							return OR;
						default:
							System.out.print("Problema en el primer Switch anidado, bin2Tipo");
							System.exit(1);
					}
				case 5:
					return ADDI;
				case 4:
					return ANDI;
				case 9:
					return ORI;
				case 0:
					return BR;
				case 10:
					b = raw & 0b0000000000100000;
					if (b != 0){
						return NOT;
					}
					id = raw & 0b0000110000000000;
					id = id >> 10;
					switch (id){
						case 0:
							return JR;
						case 1:
							return JALR;
						case 2:
							return TRAP;
						case 3:
							return RETI;
						default:
							System.out.print("Problema en el segundo Switch anidado, bin2Tipo");
							System.exit(1);
					}
				case 8:
					return JAL;
				case 2:
					return LD;
				case 3:
					return ST;
				case 6:
					b = raw & 0b0000000000100000;
					if (b == 0){
						return LDR;
					} else {
						return STR;
					}
				case 7:
					b = raw & 0b0000000100000000;
					if (b == 0){
						return LUI;
					} else {
						return LORI;
					}
				case 11:
					return LJMP;
				default:
					System.out.print("Se trato de usar un opcode reservado");
					System.exit(1);
			}
			System.exit(2);
			return null;
		}
	}

	public final Tipo OPCODE;
	public byte RD = -1;
	public byte RS1 = -1;
	public byte RS2 = -1;
	public byte RS3 = -1;
	public byte Inm = -1;
	public byte SH = -1;
	public byte CC = -1;
	public short PCOffset = -1;
	public byte RB = -1;
	public byte vect = -1;

	private static byte interpreta_inm(int temp, int largo){
		byte ins = (byte)temp;
		if((ins >> (largo-1)) == 0){
			return ins;
		} else {
			byte missing_1s = (byte) (0b11111111 << largo);
			return (byte)(missing_1s + ins);
		}
	}

	public Instruccion(short raw){
		this.OPCODE = Tipo.bin2Tipo(raw);
		int temp;
		switch (this.OPCODE){
			case ADD:
			case SUB:
			case AND:
			case OR:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000111000000;
				this.RS1 = (byte)(temp >> 6);
				temp = raw & 0b0000000000000111;
				this.RS2 = (byte)temp;
				break;
			case ADDI:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000111000000;
				this.RS1 = (byte)(temp >> 6);
				temp = raw & 0b0000000000111111;
				this.Inm = (byte)interpreta_inm(temp, 6);
				break;
			case ANDI:
			case ORI:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000111000000;
				this.RS1 = (byte)(temp >> 6);
				temp = raw & 0b0000000000110000;
				this.SH = (byte)(temp >> 4);
				temp = raw & 0b0000000000001111;
				this.Inm = (byte)temp;
				break;
			case BR:
				temp = raw & 0b0000110000000000;
				this.CC = (byte)(temp >> 10);
				temp = raw & 0b0000001111111111;
				this.PCOffset = (byte)temp;
				break;
			case JR:
			case JALR:
				temp = raw & 0b0000000111000000;
				this.RB = (byte)(temp >> 6);
				break;
			case TRAP:
				temp = raw & 0b0000000111000000;
				this.vect = (byte)(temp >> 6);
				break;
			case RETI:
				break;
			case NOT:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000111000000;
				this.RS1 = (byte)(temp >> 6);
				break;
			case JAL:
			case LJMP:
				temp = raw & 0b0000111111111111;
				this.PCOffset = (byte)temp;
				break;
			case LD:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000111111111;
				this.PCOffset = (byte)temp;
				break;
			case ST:
				temp = raw & 0b0000111000000000;
				this.RS1 = (byte)(temp >> 9);
				temp = raw & 0b0000000111111111;
				this.PCOffset = (byte)temp;
				break;
			case LDR:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000111000000;
				this.RS1 = (byte)(temp >> 6);
				temp = raw & 0b0000000000000111;
				this.RS2 = (byte)(temp);
				break;
			case STR:
				temp = raw & 0b0000111000000000;
				this.RS3 = (byte)(temp >> 9);
				temp = raw & 0b0000000111000000;
				this.RS1 = (byte)(temp >> 6);
				temp = raw & 0b0000000000000111;
				this.RS2 = (byte)(temp);
				break;
			case LUI:
			case LORI:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000011111111;
				this.Inm = (byte)temp;
				break;
			default:
				System.out.print("Problema con el switch, constructor de Instruccion");
				System.exit(1);		
		}
	}

	public void print(){
		System.out.println("");
		System.out.println(this.OPCODE.name());
		System.out.println("RD " + this.RD);
		System.out.println("RS1 " + this.RS1);
		System.out.println("RS2 " + this.RS2);
		System.out.println("RS3 " + this.RS3);
		System.out.println("Inm " + this.Inm);
		System.out.println("SH " + this.SH);
		System.out.println("CC " + this.CC);
		System.out.println("PCOffset " + this.PCOffset);
		System.out.println("RB " + this.RB);
		System.out.println("vect " + this.vect);
		System.out.println("");

	}
}
