package pipeline;

/**
 * El uso de esta interfaz permite abstraer el concepto global de "pipeline", donde se definen los 
 * elementos del sistema y se lanza en ejecución el flujo de eventos del mismo.
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public interface Pipeline {
	
	/**
	 * Este método permite instanciar y enlazar todos los componentes del sistema "pipeline".
	 */
	public void design();

	/**
	 * Este método permite poner en ejecución todos los filtros del sistema "pipeline" y que el flujo
	 * de eventos de comienzo.
	 */
	public void run();
	
	/**
	 * Este método devuelve información referente al estado del Pipeline.
	 * 
	 * @return un elemento de tipo ReportingResult con información referente al estado del Pipeline.
	 * @see ReportingResult
	 */
	public ReportingResult report();
	
}
