package Main;

public class Instruccion {

	protected enum Tipo {
		AND(1),
		OR(1),
		ADD(1),
		NOR(1),
		XOR(1),
		ANN(1),
		SUB(1),
		SLT(1),
		ADDI(5),
		LUI(4),
		LORI(4),
		LD(2),
		ST(3),
		LDR(6),
		STR(7),
		BR(0),
		JUMP(8),
		JAL(9),
		JR(8),
		JALR(9),
		TRAP(10),
		RTI(11);
		
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
					id = raw & 0b0000000000000111;
					switch (id){
						case 0:
							return AND;
						case 1:
							return OR;
						case 2:
							return ADD;
						case 3:
							return NOR;
						case 4:
							return ANN;
						case 5:
							return XOR;
						case 6:
							return SUB;
						case 7: 
							return SLT;
						default:
							System.out.print("Problema en el primer Switch anidado, bin2Tipo");
							System.exit(1);
					}
				case 5:
					return ADDI;
				case 4:
					b = raw & 0b0000000100000000;
					if (b == 0){
						return LUI;
					} else {
						return LORI;
					}
				case 2:
					return LD;
				case 3:
					return ST;
				case 6:
					return LDR;
				case 7:
					return STR;
				case 0:
					return BR;
				case 8:
					b = raw & 0b0000100000000000;
					if(b == 0) {
						return JR;
					} else {
						return JUMP;
					}
				case 9:
					b = raw & 0b0000100000000000;
					if(b == 0) {
						return JALR;
					} else {
						return JAL;
					}
				case 10:
					return TRAP;
				case 11:
					return RTI;
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
	public byte NZP = -1;
	public short PCOffset = -1;
	public byte RB = -1;
	public byte vect = -1;

	private static byte extend_sign(int temp, int largo){
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
			case NOR:
			case ANN:
			case XOR:
			case SLT:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000111000000;
				this.RS1 = (byte)(temp >> 6);
				temp = raw & 0b0000000000111000;
				this.RS2 = (byte)(temp >> 3);
				break;
			case ADDI:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000111000000;
				this.RS1 = (byte)(temp >> 6);
				temp = raw & 0b0000000000111111;
				this.Inm = (byte)extend_sign(temp, 6);
				break;
			case LUI:
			case LORI:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000011111111;
				this.Inm = (byte)temp;
				break;
			case LD:
			case ST:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000111111111;
				this.PCOffset = (byte)extend_sign(temp, 9);
				break;
			case LDR:
			case STR:
				temp = raw & 0b0000111000000000;
				this.RD = (byte)(temp >> 9);
				temp = raw & 0b0000000111000000;
				this.RS1 = (byte)(temp >> 6);
				temp = raw & 0b0000000000111111;
				this.PCOffset = extend_sign(temp, 6);
				break;
			case BR:
				temp = raw & 0b0000111000000000;
				this.NZP = (byte)(temp >> 9);
				temp = raw & 0b0000000111111111;
				this.PCOffset = (byte)extend_sign(temp, 9);
				break;
			case JAL:
			case JUMP:
				temp = raw & 0b0000011111111111;
				this.PCOffset = (byte)extend_sign(temp, 12);
				break;
			case JR:
			case JALR:
				temp = raw & 0b0000000111000000;
				this.RB = (byte)(temp >> 6);
				break;
			case TRAP:
				temp = raw & 0b0000000000111111;
				this.vect = (byte)(temp >> 6);
				break;
			case RTI:
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
		System.out.println("NZP " + this.NZP);
		System.out.println("PCOffset " + this.PCOffset);
		System.out.println("RB " + this.RB);
		System.out.println("vect " + this.vect);
		System.out.println("");

	}
}
