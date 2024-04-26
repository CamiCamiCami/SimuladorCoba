package proyecto;

import java.io.IOException;

public class Simulador {

	int PC = 0;
	private static short[] registros = new short[8];
	private static Memory memoria = new Memory();

	public static void main(String[] args) {
		try {
			memoria.nukeMemory();
			memoria.print((short)0, (short)500);
			memoria.close();
		} catch (IOException e) {
			System.out.print("Salio mal");
		}
		System.out.println("Hello, World!");
	
	}
}
