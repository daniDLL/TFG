package vt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Date;

import Test.Analysis;
import pipeline.Data;
import pipeline.DataSink;

/**
 * Esta clase implementa la interfaz DataSink. De esta forma instanciamos la salida de datos del sistema "pipeline".
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class OutputSource implements DataSink{

	private String destinationFolder;
	private int id;
	
	/**
	 * Constructor del OutputSource.
	 * 
	 * @param destinationFolder indica el directorio donde se escribiran los resultados del sistema.
	 */
	public OutputSource(String destinationFolder, int id){
		this.destinationFolder = destinationFolder;
		this.id = id;
	}
	
	private void copyFileText(File src, File dst){
		try {
			BufferedReader br = new BufferedReader(new FileReader(src));
			PrintWriter pw =  new PrintWriter(dst);

			String linea;
			while((linea = br.readLine()) != null){
				pw.println(linea);
			}
			
			br.close();
			pw.close();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Este método escribe en el directorio de destino del OutputSource los datos que van saliendo del "pipeline".
	 */
	public void write(Data dt){
		if(dt != null){
			File kw = new File(((DataBlock) dt.getData()).getFile().getAbsolutePath());
			File dst = new File(destinationFolder + "/" + ((DataBlock) dt.getData()).getFile().getName());
			copyFileText(kw, dst);
			System.out.println("dst: " + dst.getAbsolutePath());
			System.out.println("kw: " + kw.getAbsolutePath());
			//((File) dt.getData()).delete();
			Analysis.addSize(((DataBlock) dt.getData()).getResourceName(), ((Integer)id).toString(), dst.length());
			Date d = new Date();
			System.out.println("TEST: " + ((DataBlock) dt.getData()).getResourceName());
			Analysis.addTime(((DataBlock) dt.getData()).getResourceName(), null, (d.getTime() - Analysis.getLocalElapsedTime(((DataBlock) dt.getData()).getResourceName()).getTime())/1000.0, -1);
		}
		else{
			System.out.println("[Output] NULL Data.");
			Analysis.printInformation();
		}
	}

	/**
	 * UNIMPLEMENTED
	 */
	public void setProperties() {
		
	}

}
