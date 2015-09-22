package vt;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import Test.Analysis;
import pipeline.BufferPipe;
import pipeline.Data;

/**
 * Esta clase implementa la interfaz BufferPipe definiendo una cola FIFO.
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class Pipe implements BufferPipe{
	
	private LinkedList<Data> fifo = new LinkedList<Data>();
	private Semaphore S;
	private int id;
	
	/**
	 * Constructor del Pipe.
	 * 
	 * @param S el semáforo que controla cuando hay datos para leer y cuando no.
	 */
	public Pipe(Semaphore S, int id){
		this.S = S;
		this.id = id;
	}
	
	/**
	 * Este método lee un dato de la estructura de datos del Pipe.
	 * 
	 * @return el dato leído.
	 */
	public Data read(){
		return fifo.removeFirst();
	}
	
	/**
	 * Este método escribe un dato en la estructura de datos del Pipe.
	 */
	public void write(Data dt){
		fifo.add(dt);
		this.S.release();
		if(dt != null){
			String e = ((DataBlock) dt.getData()).getResourceName();
			Analysis.addSize(e ,((Integer) id).toString(), ((DataBlock) dt.getData()).getFile().length());
		}
	}

	/**
	 * @return devuelve el número de datos dentro de la estructura de datos.
	 */
	public int size() {
		return fifo.size();
	}
	
}