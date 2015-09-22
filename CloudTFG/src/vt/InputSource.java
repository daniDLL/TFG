package vt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import pipeline.Data;
import pipeline.DataSource;

/**
 * Esta clase implementa la interfaz DataSource. De esta forma instanciamos la entrada de datos del sistema "pipeline".
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class InputSource implements DataSource{

	private BufferedReader br = null;
	
	/**
	 * Constructor del InputSource.
	 * 
	 * @param inputfile el fichero de texto con URIs de videos (uno en cada línea del fichero).
	 */
	public InputSource(String inputfile){
		try {
			br = new BufferedReader(new FileReader(new File(inputfile)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Este método lee una URI (una por línea) del fichero y la devuelve como tipo Data.
	 */
	public Data read() {
		String uri;
		try{
			if((uri = br.readLine())!=null){
				Data dc = new DataContainer();
				dc.setData(uri);
				return dc;
			}
			else{
				return null;
			}
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * UNIMPLEMENTED
	 */
	public void setProperties() {
		
	}

}
