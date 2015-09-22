package pipeline;

/**
 * El uso de esta interfaz permite abstraer el concepto de "Dato" utilizado como encapsulado
 * de los datos que se utilizan en la arquitectura "pipeline".
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public interface Data {

	/**
	 * Este método devuelve el objeto encapsulado en el tipo Data.
	 * 
	 * @return el objeto del dato instanciado.
	 */
	public Object getData();
	
	/**
	 * Este método encapsula el objeto pasado como parámetro con el tipo Data.
	 * 
	 * @param o el objeto a encapsular.
	 */
	public void setData(Object o);
	
	/**
	 * Este método permite definir las propiedades internas del tipo de dato de la clase Data.
	 */
	public void setProperties();
	
}
