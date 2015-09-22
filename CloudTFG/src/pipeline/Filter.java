package pipeline;

/**
 * El uso de esta interfaz permite abstraer el concepto de "proceso" (filtro) que interviene en el
 * "pipeline" realizando una tarea con los datos leídos por la entrada y generando una salida utilizada por
 * otros elementos posteriores del "pipeline".
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public interface Filter extends Runnable{
	
	/**
	 * Este método realiza la tarea destinada al Filter dentro del "pipeline".
	 */
	public void doWork();
	
	/**
	 * Este método devuelve información referente al estado del filter.
	 * 
	 * @return un elemento de tipo ReportingResult con información referente al estado del Filter.
	 * @see ReportingResult
	 */
	public ReportingResult report();
	
}
