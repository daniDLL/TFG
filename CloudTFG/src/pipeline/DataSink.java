package pipeline;

/**
 * El uso de esta interfaz permite abstraer el concepto de destino de los datos (elementos) finales
 * producidos por el ultimo proceso del "pipeline".
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public interface DataSink {

	/**
	 * Este método almacena (escribe) en el DataSink el dato (elemento) "dt".
	 * 
	 * @param dt dato a almacenar.
	 * @see Data.
	 */
	public void write(Data dt);
	
	/**
	 * Este método permite definir las propiedades internas del tipo DataSink.
	 */
	public void setProperties();
	
}
