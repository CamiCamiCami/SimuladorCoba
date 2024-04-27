package proyecto;

import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Simulador {

	private static int PC = 0;
	private static short[] r = new short[8];
	private static Memory memoria = new Memory();
	private static final int PALABRA = 16;

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
		} finally {
			System.out.println("load position: " + loadTo);
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
				System.out.println("put " + bin + " in " + (loadTo + i));
			} catch (IOException e) {
				System.out.println("error de IO cargando linea " + i);
				System.exit(1);
			}
		}
		PC = loadTo;
	}

	private static void runInstruction(Instruccion ins){
		switch (ins.OPCODE) {
			case ADD:
				r[ins.RD] = (short)(r[ins.RS1] + r[ins.RS2]);
				break;
			case SUB:
				r[ins.RD] = (short)(r[ins.RS1] - r[ins.RS2]);
				break;
			case AND:
				r[ins.RD] = (short)(r[ins.RS1] & r[ins.RS2]);
				break;
			case OR:
				r[ins.RD] = (short)(r[ins.RS1] | r[ins.RS2]);
				break;
			case ADDI:
				r[ins.RD] = (short)(r[ins.RS1] + ins.Inm);
				break;
		}
	}

	public static void main(String[] args) throws IOException {
		try {
			File source = new File(args[0]);
			Scanner scan = new Scanner(source);
			loadCode(scan);
			scan.close();
		} catch (FileNotFoundException e){
			System.out.println("Archivo no encontrado");
			System.exit(1);
		} catch (ArrayIndexOutOfBoundsException e){
			System.out.println("Falta un argumento indicando el archivo que contiene el codigo a ejecutar");
			System.exit(1);
		}
		
		int cont = 22;
		while (cont > 0){
			short raw = memoria.get(PC);
			Instruccion ins = new Instruccion(raw);
			runInstruction(ins);
			PC++;
			cont--;
		}
		
		memoria.close();
	}
}
