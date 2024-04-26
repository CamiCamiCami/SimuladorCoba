package proyecto;

import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Memory {

	private static String FILE_NAME = "../memoriaRAM.data";
	private static RandomAccessFile FILE = null;

	public Memory(){
		if (FILE == null){
			try {
				FILE = new RandomAccessFile(FILE_NAME, "rw");
			} catch (FileNotFoundException e) {
				System.out.print("No existe el archivo de memoria");
				System.exit(1);				
			}
		}
	}

	private void setDir(short dir) throws IOException {
		long curr = FILE.getFilePointer();
		long new_p = dir * 2;
		if (curr != new_p){
			FILE.seek(new_p);
		}
	}
	
	public short get(short dir) throws IOException {
		setDir(dir);
		return FILE.readShort();
	}

	public void put(short dir, short data) throws IOException {
		setDir(dir);
		FILE.writeShort(data);
	}

	public void nukeMemory() throws IOException {
		setDir((short)0);
		for(int i = 0; i < 65536; i++){
			FILE.writeShort(-24187);
		}
	}

	public void close() throws IOException {
		FILE.close();
		FILE = null;
	}

	private String toWordString(short n){
		String bin = Integer.toBinaryString((int)n);
		while (bin.length() < 16){
			bin = "0" + bin;
		}
		if (bin.length() > 16){
			bin = bin.substring(bin.length() - 16, bin.length());
		}
		return bin;
	}

	public void print(short start, short end) throws IOException {
		setDir(start);
		for (int i=start; i < end; i++){
			short cont = FILE.readShort();
			String bin = toWordString(cont);
			System.out.print(bin + "\n");
		}
	}

}