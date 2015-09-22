package vt;

import java.io.File;

/**
 * Esta clase permite definir el bloque de datos que "fluirá" por el "pipeline".
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class DataBlock {

	private File file;
	private String URI;
	private String resource_name;
	
	/**
	 * Constructor del DataBlock.
	 * @param f fichero del DataBlock.
	 * @param uri URI del DataBlock.
	 */
	public DataBlock(File f, String uri, String resource){
		this.file = f;
		this.URI = uri;
		this.resource_name = resource;
	}
	
	/**
	 * Constructor del DataBlock.
	 * @param uri URI del DataBlock.
	 */
	public DataBlock(String uri){
		this.file = null;
		this.URI = uri;
		this.resource_name = "";
	}
	
	/**
	 * Delvuelve la URI del DataBlock.
	 * @return la URI.
	 */
	public String getUri(){
		return this.URI;
	}
	
	/**
	 * Devuelve el fichero del DataBlock.
	 * @return el fichero.
	 */
	public File getFile(){
		return this.file;
	}
	
	/**
	 * Devuelve el nombre del recurso.
	 * @return el nombre del recurso.
	 */
	public String getResourceName(){
		return this.resource_name;
	}
}
