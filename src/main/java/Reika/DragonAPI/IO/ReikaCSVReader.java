/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.IO;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;

public final class ReikaCSVReader {
	
	private ReikaCSVReader() {throw new RuntimeException("The class "+this.getClass()+" cannot be instantiated!");}
	
	private final BufferedReader bf;
	
	public ReikaCSVReader(Class root, String path) {
		InputStream input = new java.io.BufferedInputStream(root.getResourceAsStream(path), 16384);
		FileReader fr = null;
		if (input == null) {
			try{input.close();} catch (java.io.IOException e) {e.printStackTrace();}
			bf = null;
			return;
		}
		try {
			fr = new FileReader(path);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			try{input.close();} catch (java.io.IOException ex) {ex.printStackTrace();}
			bf = null;
			return;
		}
		bf = new BufferedReader(fr, 16384);
		try{input.close();} catch (java.io.IOException e) {e.printStackTrace();}
	}
	
}
