package vt;

import pipeline.Data;

/**
 * Esta clase implementa la interfaz Data y para poder instanciarla dentro del sistema "pipeline".
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class DataContainer implements Data{
	
	private Object o;

	/**
	 * Este método devuelve el objeto contenido en la clase.
	 */
	public Object getData() {
		return o;
	}

	/**
	 * Este método modifica el objeto contenido en la clase.
	 */
	public void setData(Object o) {
		this.o = o;
	}

	/**
	 * Este método modifica las propiedades generales de la clase.
	 */
	public void setProperties() {
		
	}

}
