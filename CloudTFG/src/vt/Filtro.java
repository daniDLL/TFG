package vt;

import java.util.Date;
import java.util.concurrent.Semaphore;

import Test.Analysis;
import pipeline.BufferPipe;
import pipeline.Data;
import pipeline.DataSink;
import pipeline.DataSource;
import pipeline.Filter;
import pipeline.ReportingResult;

/**
 * Esta clase abstracta implementa la interfaz Filter pero deja los método de dicha interfaz abstractos.
 * La finalidad de esta clase es definir un filtro genérico, sin importar que su entrada sea o de un DataSource
 * o de un BufferPipe, y por otro lado que su salida pueda ser o bien un DataSink o un BufferPipe.
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public abstract class Filtro implements Filter{
	
	protected String msgReport = "";
	private Object in;
	private Object out;
	private boolean ind;
	private boolean outd;
	protected Data dt;
	private Semaphore S;
	protected String id;
	private double readtime;
	
	/**
	 * Constructor de un filtro con un DataSource como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param ds el objeto DataSource de entrada de datos.
	 * @param bp el objeto BufferPipe de salida de datos.
	 */
	public Filtro(DataSource ds, BufferPipe bp, String idp){
		in = ds; out = bp; ind = true; outd = false; id = idp;
	}
	
	/**
	 * Constructor de un filtro con un BufferPipe como entrada de datos y un BufferPipe como salida de datos.
	 * 
	 * @param bpi el objeto BufferPipe de entrada de datos.
	 * @param bpo el objeto BufferPipe de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe de entrada de datos.
	 */
	public Filtro(BufferPipe bpi, BufferPipe bpo, Semaphore S, String idp){
		in = bpi; out = bpo; ind = false; outd = false; this.S = S; id = idp;
	}
	
	/**
	 * Constructor de un filtro con un BufferPipe como entrada de datos y un DataSink como salida de datos.
	 * 
	 * @param bp el objeto BufferPipe de entrada de datos.
	 * @param dsk el objeto DataSink de salida de datos.
	 * @param S el semáforo que controla la lectura del BufferPipe.
	 */
	public Filtro(BufferPipe bp, DataSink dsk, Semaphore S, String idp){
		in = bp; out = dsk; ind = false; outd = true; this.S = S; id = idp;
	}
	
	/**
	 * Este método realiza la tarea destinada al Filter dentro del "pipeline".
	 */
	public abstract void doWork();
	
	/**
	 * Este método devuelve información referente al estado del filter.
	 * 
	 * @return un elemento de tipo ReportingResult con información referente al estado del Filter.
	 * @see ReportingResult
	 */
	public abstract ReportingResult report();
	
	/**
	 * Este método lee un dato de la entrada de datos, sin importar si es un DataSource o un BufferPipe.
	 * En el caso del BufferPipe a su vez se adquiere el token del semáforo asociado a ese buffer.
	 * 
	 * @return devuelve un dato de la entrada de datos.
	 * @throws InterruptedException si el hilo actual es interrumpido.
	 */
	public Data readFromInput() throws InterruptedException{
		if(ind){
			Date d1 = new Date();
			Data d = ((DataSource) in).read();
			Date d2 = new Date();
			readtime = (d2.getTime()-d1.getTime())/1000.0;
	
			return d;
		}
		else{
			this.S.acquire();
			
			Date d1 = new Date();
			Data d = ((BufferPipe) in).read();
			Date d2 = new Date();
			readtime = (d2.getTime()-d1.getTime())/1000.0;
			
			return d;
		}
	}
	
	/**
	 * Este método escribe un dato en la salida de datos, sin importar si es un DataSink o un BufferPipe.
	 * 
	 * @param dt el dato a escribir en la salida de datos.
	 */
	public void writeToOutput(Data dt){
		Date d = new Date();
		if(outd){
			((DataSink) out).write(dt);
		}
		else{
			((BufferPipe) out).write(dt);
		}
		Date ndate = new Date();
		double time = (ndate.getTime()-d.getTime())/1000.0;
		
		if(dt != null){
			Analysis.addTime(((DataBlock) dt.getData()).getResourceName(), id, time, 0 );
		}
	}

	/**
	 * Este método implementa el método definido en la interfaz Runnable de la que extiende la interfaz Filter.
	 * Este método se encarga de leer un dato de la entrada de datos y si el dato es distinto de null entonces realiza
	 * el trabajo designado al filtro.
	 */
	public void run() {
		boolean _continue = true;
		try{
			while(_continue){
				//Date d = new Date();
				dt = readFromInput();
				//Date ndate = new Date();
				//double time = (ndate.getTime()-d.getTime())/1000.0;
				
				if(dt == null){
					_continue = false;
					writeToOutput(null);
				}
				else{
					Analysis.addTime(((DataBlock) dt.getData()).getResourceName(), id, readtime, 1 );
					doWork();
				}
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
