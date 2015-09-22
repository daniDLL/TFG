package Test;

import java.io.File;
import java.util.Date;

import pipeline.Data;
import pipeline.DataSource;
import vt.DataBlock;
import vt.DataContainer;

/**
 * Esta clase implementa la interfaz DataSource. De esta forma instanciamos la entrada de datos del sistema "pipeline".
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class InputSourceFromDirectory implements DataSource{
	
	private File[] fd;
	private int ifile = 0;
	
	
	/**
	 * Constructor del InputSource.
	 * 
	 * @param inputfile el fichero de texto con URIs de videos (uno en cada línea del fichero).
	 */
	public InputSourceFromDirectory(File inputfile){
		fd = inputfile.listFiles();
	}

	/**
	 * Este método lee una URI (una por línea) del fichero y la devuelve como tipo Data.
	 */
	public Data read() {
		Date d = new Date();
		if(ifile < fd.length){
			File f = fd[ifile];
			Analysis.startLocalClock(f.getName().split("\\.")[0], d);
			Data dc = new DataContainer();
			dc.setData(new DataBlock(f,"",f.getName().split("\\.")[0]));
			ifile++;
			return dc;
		}
		else{
			return null;
		}
		
	}

	/**
	 * UNIMPLEMENTED
	 */
	public void setProperties() {
		
	}

}
