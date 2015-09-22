package pipeline;

/**
 * El uso de esta interfaz permite abstraer el concepto de origen de los datos (elementos) a utilizar
 * el "pipeline".
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public interface DataSource {
	
	/**
	 * Este método devuelve un dato (elemento) del DataSource.
	 * 
	 * @return un dato (elemento) del DataSource.
	 * @see Data.
	 */
	public Data read();
	
	/**
	 * Este método permite definir las propiedades internas del tipo DataSource.
	 */
	public void setProperties();
	
}
