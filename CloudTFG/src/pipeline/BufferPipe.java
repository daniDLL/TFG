package pipeline;

/**
 * El uso de esta interfaz permite abstraer el concepto de "pipe" como elemento (almacén) intermedio
 * entre dos procesos, uno que genera datos, y otro que los lee (utiliza).
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public interface BufferPipe {

	/**
	 * Este método devuelve un dato (elemento) del BufferPipe.
	 * 
	 * @return un dato (elemento) del BufferPipe.
	 * @see Data
	 */ 
	public Data read();
	
	/**
	 * Este método almacena (escribe) un nuevo dato (elemento) en el BufferPipe.
	 * 
	 * @param el dato que queremos almacenar (escribir) en el BufferPipe.
	 * @see Data
	 */
	public void write(Data dt);
	
	/**
	 * Este método devuelve el tamaño total del BufferPipe, indicando el numero de datos (elementos)
	 * almacenado en él actualmente.
	 * 
	 * @return el número de datos (elementos) dentro del BufferPipe.
	 */
	public int size();
	
}

