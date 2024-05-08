import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.DataInputStream;
import java.io.EOFException;

public class Simulador {

	private static int PC = 0;
	private static short[] r = new short[8];
	private static Memory memoria = new Memory();
	private static final int PALABRA = 16;
	private static short last_result;

	private static String filterNonBinary(String str){
		StringBuilder builder = new StringBuilder(str.length());
		for (char c : str.toCharArray()){
			if (c == '0' || c == '1'){
				builder.append(c);
			}
		}
		return builder.toString();
	}

	private static void loadCode(Scanner code){
		String line = code.nextLine();
		line = filterNonBinary(line);
		if (line.length() != PALABRA){
			System.out.println("Tamaño de palabra incorrecta (" + line.length() + ") en declaracion de posicion");
			System.exit(1);
		}

		int loadTo = 0;
		try {
			loadTo = Integer.parseInt(line, 2);
		} catch (NumberFormatException e){
			System.out.println("inparseable declaracion de posicion");
			System.exit(1);
		}

		for (int i = 0; code.hasNextLine(); i++){
			line = code.nextLine();
			line = filterNonBinary(line);
			
			if (line.length() != PALABRA){
				System.out.println("Tamaño de palabra incorrecta (" + line.length() + ") en linea " + i);
				System.exit(1);
			}
			
			try {
				short bin = (short)Integer.parseInt(line, 2);
				memoria.put(loadTo + i, bin);
			} catch (IOException e) {
				System.out.println("error de IO cargando linea " + i);
				System.exit(1);
			}
		}
		PC = loadTo;
	}

	private static void readHeader(DataInputStream bin){
		try {
			bin.readShort();
		} catch (EOFException e){
			System.out.println("el archivo binario estaba vacio");
			System.exit(1);
		} catch (IOException e){
			System.out.println("Error de IO leyendo el short de posicion");
			System.exit(1);
		}
	}

	private static void loadBin(DataInputStream bin){
		readHeader(bin);
		int loadTo = 0;
		try {
			short dir = bin.readShort();
			loadTo = Short.toUnsignedInt(dir);
		} catch (EOFException e){
			System.out.println("el archivo binario estaba vacio");
			System.exit(1);
		} catch (IOException e){
			System.out.println("Error de IO leyendo el short de posicion");
			System.exit(1);
		}

		System.out.println("Cargando en posicion " + loadTo);

		for (int i = 0; true; i++) {
			short b = 0;
			try{
				b = bin.readShort();
			} catch (EOFException e){
				break;
			} catch (IOException e){
				System.out.println("Error de IO leyendo el short numero " + i);
				System.exit(1);
			}
			
			try{
				memoria.put(loadTo + i, b);
			} catch (IOException e){
				System.out.println("Error de IO escribiendo a memoria " + i);
				System.exit(1);
			}


		}
		PC = loadTo;
	}

	private static void runInstruction(Instruccion ins) throws IOException{
		switch (ins.OPCODE) {
			case ADD:
				r[ins.RD] = (short)(r[ins.RS1] + r[ins.RS2]);
				last_result = r[ins.RD];
				break;
			case SUB:
				r[ins.RD] = (short)(r[ins.RS1] - r[ins.RS2]);
				last_result = r[ins.RD];
				break;
			case AND:
				r[ins.RD] = (short)(r[ins.RS1] & r[ins.RS2]);
				last_result = r[ins.RD];
				break;
			case OR:
				r[ins.RD] = (short)(r[ins.RS1] | r[ins.RS2]);
				last_result = r[ins.RD];
				break;
			case ADDI:
				r[ins.RD] = (short)(r[ins.RS1] + ins.Inm);
				last_result = r[ins.RD];
				break;
			case ANDI:
				short and_pos_inm = (short)(ins.Inm << (ins.SH*4));
				short and_ext_inm = (short)(0b1111111111111111 & (and_pos_inm));
				r[ins.RD] = (short)(r[ins.RS1] & and_ext_inm);
				last_result = r[ins.RD];
				break;
			case ORI:
				short or_pos_inm = (short)(ins.Inm << (ins.SH*4));
				short or_ext_inm = (short)(0b0000000000000000 | (or_pos_inm));
				r[ins.RD] = (short)(r[ins.RS1] | or_ext_inm);
				last_result = r[ins.RD];
				break;
			case BR:
				switch (ins.CC) {
					case 0:
						if (last_result == 0){
							PC += ins.PCOffset;
						}
						break;
					case 1:
						if(((short)(last_result >> 16)) == 0){
							PC += ins.PCOffset;
						}
						break;
					case 2:
						if(((short)(last_result >> 16)) != 0){
							PC += ins.PCOffset;
						}
						break;
					default:
						System.out.println("Error De ejecucion, valor de SH ilegal: " + ins.SH);
						System.exit(1);
						break;
				}
				break;
			case JR:
				PC = Short.toUnsignedInt(r[ins.RB]);
				break;
			case JALR:
				r[7] = (short)PC;
				PC = Short.toUnsignedInt(r[ins.RB]);
				break;
			case TRAP:
				if(ins.vect == 0){
					memoria.close();
					System.exit(0);
				} else {
					System.out.println(r[0]);
				}
				break;
			case RETI:
				PC = Short.toUnsignedInt(r[7]);
				break;
			case NOT:
				r[ins.RD] = (short)(~(r[ins.RS1]));
				last_result = r[ins.RD];
				break;
			case JAL:
				r[7] = (short)PC;
				PC += Short.toUnsignedInt(ins.PCOffset);
				break;
			case LD:
				r[ins.RD] = memoria.get(PC + Short.toUnsignedInt(ins.PCOffset));
				last_result = r[ins.RD];
				break;
			case ST:
				memoria.put(PC + Short.toUnsignedInt(ins.PCOffset), r[ins.RS1]);
				last_result = r[ins.RS1];
				break;
			case LDR:
				r[ins.RD] = memoria.get(r[ins.RS1 + ins.RS2]);
				last_result = r[ins.RD];
				break;
			case STR:
				memoria.put(r[ins.RS2] + r[ins.RS3], r[ins.RS1]);
				last_result = r[ins.RS1];
				break;
			case LUI:
				r[ins.RD] = (short)(r[ins.RD] & 0b0000000011111111);
				r[ins.RD] += (byte)(ins.Inm << 8);
				last_result = r[ins.RD];
				break;
			case LORI:
				r[ins.RD] = (short)(r[ins.RD] & 0b1111111100000000);
				r[ins.RD] += (byte)(ins.Inm);
				last_result = r[ins.RD];
				break;
			case LJMP:
				PC += ins.PCOffset;
				break;
		}
	}

	private static void paso() throws IOException{
		short raw = memoria.get(PC);
		PC++;
		Instruccion ins = new Instruccion(raw);
		ins.print();
		runInstruction(ins);
	}

	public static void main(String[] args) throws IOException {
		memoria.nukeMemory();
		boolean txt = false;
		try {
			if(args[0].equals("bin")){
				txt = false;
			} else if (args[0].equals("txt")) {
				txt = true;
			} else {
				System.out.println("): (bin|txt)");
				System.exit(1);
			}
		} catch (ArrayIndexOutOfBoundsException e){
			System.out.println("Esperaba un argumento definiendo el tipo de archivo de entrada (bin|txt)");
			System.exit(1);
		}

		try {
			if (txt){
				File source = new File(args[1]);
				Scanner scan = new Scanner(source);
				loadCode(scan);
				scan.close();
			} else {
				FileInputStream source = new FileInputStream(args[1]);
				DataInputStream bin = new DataInputStream(source);
				loadBin(bin);
				bin.close();
			}	
		} catch (FileNotFoundException e){
			System.out.println("Archivo no encontrado");
			System.exit(1);
		} catch (ArrayIndexOutOfBoundsException e){
			System.out.println("Falta un argumento indicando el archivo que contiene el codigo a ejecutar");
			System.exit(1);
		}

		memoria.print(PC, PC+30);

		Scanner scan = new Scanner(System.in);
		while (true){
			for (int i = 0; i < 8; i++) {
				System.out.println("r[" + i + "] = " + r[i]);
			}

			String line = scan.next();
			
			try {
				int cant = Integer.parseInt(line, 10);
				while(cant > 0){
					paso();
					cant--;
				}
				continue;
			} catch (NumberFormatException e){}
	
			switch (line) {
				case ">":
					paso();
					break;
				
				case ">>":
					while (true) {
						paso();
					}
				default:
					break;
			}
		}
	}
}
