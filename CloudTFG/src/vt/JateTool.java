package vt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import Test.Analysis;
import pipeline.BufferPipe;
import pipeline.Data;
import pipeline.DataSink;
import pipeline.DataSource;
import pipeline.ReportingResult;
import uk.ac.shef.dcs.oak.jate.JATEException;
import uk.ac.shef.dcs.oak.jate.core.algorithm.AbstractFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.Algorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.AverageCorpusTFAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.AverageCorpusTFFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.CValueAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.CValueFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.FrequencyAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.FrequencyFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.GlossExAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.GlossExFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.RIDFAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.RIDFFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TFIDFAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TFIDFFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TermExAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.TermExFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.algorithm.WeirdnessAlgorithm;
import uk.ac.shef.dcs.oak.jate.core.algorithm.WeirdnessFeatureWrapper;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderDocumentTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderRefCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureBuilderTermNest;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureDocumentTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureRefCorpusTermFrequency;
import uk.ac.shef.dcs.oak.jate.core.feature.FeatureTermNest;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexBuilderMem;
import uk.ac.shef.dcs.oak.jate.core.feature.indexer.GlobalIndexMem;
import uk.ac.shef.dcs.oak.jate.core.npextractor.CandidateTermExtractor;
import uk.ac.shef.dcs.oak.jate.core.npextractor.NounPhraseExtractorOpenNLP;
import uk.ac.shef.dcs.oak.jate.core.npextractor.WordExtractor;
import uk.ac.shef.dcs.oak.jate.core.voting.Voting;
import uk.ac.shef.dcs.oak.jate.core.voting.WeightedOutput;
import uk.ac.shef.dcs.oak.jate.model.Corpus;
import uk.ac.shef.dcs.oak.jate.model.CorpusImpl;
import uk.ac.shef.dcs.oak.jate.model.DocumentImpl;
import uk.ac.shef.dcs.oak.jate.model.Term;
import uk.ac.shef.dcs.oak.jate.util.control.Lemmatizer;
import uk.ac.shef.dcs.oak.jate.util.control.StopList;
import uk.ac.shef.dcs.oak.jate.util.counter.TermFreqCounter;
import uk.ac.shef.dcs.oak.jate.util.counter.WordCounter;

/**
 * Esta clase extiende la clase abstracta Filtro. La finalidad de la herramienta
 * JATE es la extracción automática de términos sobre un corpus de documentos.
 * 
 * @author Daniel Delgado Llamas
 * @version %I%, %G%
 * @since 1.0
 */
public class JateTool extends Filtro{

	private final static String voting_properties_filename = "voting.properties";
	private final static String limit_v = "limit";
	private final static String tf_v = "tf";
	private final static String avg_v = "avg";
	private final static String ridf_v = "ridf";
	private final static String gloss_v = "gloss";
	private final static String weird_v = "weird";
	private final static String termex_v = "termex";
	private final static String cvalue_v = "cvalue";
	private static int limit = 20;
	private static double tf_weight = 0.1;
	private static double avg_weight = 0.1;
	private static double ridf_weight = 0.1;
	private static double gloss_weight = 0.1;
	private static double weird_weight = 0.1;
	private static double termex_weight = 0.1;
	private static double cvalue_weight = 0.1;
	
	private String out = "";

	/**
	 * Constructor de JateTool con un BufferPipe como entrada de datos y un
	 * BufferPipe como salida de datos.
	 * 
	 * @param bpi
	 *            el objeto BufferPipe de entrada de datos.
	 * @param bpo
	 *            el objeto BufferPipe de salida de datos.
	 * @param S
	 *            el semáforo que controla la lectura del BufferPipe de entrada
	 *            de datos.
	 */
	public JateTool(BufferPipe bpi, BufferPipe bpo, Semaphore S, String output) {
		super(bpi, bpo, S, "Jate");
		out = output;
	}

	/**
	 * Constructor de JateTool con un BufferPipe como entrada de datos y un
	 * DataSink como salida de datos.
	 * 
	 * @param bp
	 *            el objeto BufferPipe de entrada de datos.
	 * @param dsk
	 *            el objeto DataSink de salida de datos.
	 * @param S
	 *            el semáforo que controla la lectura del BufferPipe.
	 */
	public JateTool(BufferPipe bp, DataSink dsk, Semaphore S, String output) {
		super(bp, dsk, S, "Jate");
		out = output;
	}

	/**
	 * Constructor de JateTool con un DataSource como entrada de datos y un
	 * BufferPipe como salida de datos.
	 * 
	 * @param ds
	 *            el objeto DataSource de entrada de datos.
	 * @param bp
	 *            el objeto BufferPipe de salida de datos.
	 */
	public JateTool(DataSource ds, BufferPipe bp, String output) {
		super(ds, bp, "Jate");
		out = output;
	}

	/**
	 * 
	 */
	private void loadFileProperties(){
		try{
			BufferedReader br = new BufferedReader(new FileReader(new File(voting_properties_filename)));

			String linea;
			while((linea = br.readLine()) != null){
				String[] split = linea.split("=");
				if(split.length != 2) continue;
				if(split[0].startsWith("#")) continue;
				if(split[0].equals(limit_v)){
					limit = Integer.parseInt(split[1]);
				}
				else if(split[0].equals(tf_v)){
					tf_weight = Double.parseDouble(split[1]);
				}
				else if(split[0].equals(avg_v)){
					avg_weight = Double.parseDouble(split[1]);
				}
				else if(split[0].equals(ridf_v)){
					ridf_weight = Double.parseDouble(split[1]);
				}
				else if(split[0].equals(gloss_v)){
					gloss_weight = Double.parseDouble(split[1]);
				}
				else if(split[0].equals(weird_v)){
					weird_weight = Double.parseDouble(split[1]);
				}
				else if(split[0].equals(weird_v)){
					termex_weight = Double.parseDouble(split[1]);
				}
				else if(split[0].equals(cvalue_v)){
					cvalue_weight = Double.parseDouble(split[1]);
				}
			}

			br.close();
		}
		catch(IOException e){
			System.err.println("[JATE] Error al abrir el fichero: voting.properties. Fichero no encontrado en el directorio principal del proyecto.");
		}
		catch(NumberFormatException e){
			System.err.println("[!] Error al leer valor de variable en: voting.properties.");
		}
	}

	/**
	 * Este método realiza una votación por mayoria para las distintas listas pasadas como parámetro.
	 * @param interms Listas de terminos.
	 * @return Lista de terminos que estan en la mayoria.
	 */
	private String[] majority_voting(Term[]... interms){
		Map<String,Integer> votes = new HashMap<String,Integer>();

		for(Term[] lt : interms){
			if(lt.length > limit){
				for(int i = 0; i < limit; i++){
					if(votes.get(lt[i].getConcept()) != null ){
						votes.put(lt[i].getConcept(), votes.get(lt[i].getConcept()) + (limit - i));
					}
					else{
						votes.put(lt[i].getConcept(), (limit - i));
					}
				}
			}
			else{
				for(int i = 0; i < lt.length; i++){
					if(votes.get(lt[i].getConcept()) != null ){
						votes.put(lt[i].getConcept(), votes.get(lt[i].getConcept()) + (limit - i));
					}
					else{
						votes.put(lt[i].getConcept(), (limit - i));
					}
				}
			}
		}

		Set<String> s = votes.keySet();
		String[] as = new String[s.size()];
		as = s.toArray(as);
		List<VotesTerm> lve = new ArrayList<VotesTerm>();

		for(String str : as){
			lve.add(new VotesTerm(str,votes.get(str)));
		}
		Collections.sort(lve);

		String [] majority = new String[20];

		int limit = (lve.size() < 20) ? lve.size() : 20;
		for(int i = 0; i < limit; i++){
			majority[i] = lve.get(i).getElement();
		}

		return majority;
	}

	/**
	 * Este método escribe en un fichero de texto los terminos encontrados junto
	 * con su valor de confianza.
	 * 
	 * @param tm
	 *            vector de terminos.
	 * @param file
	 *            fichero destino donde escribir los terminos encontrados.
	 */
	private void saveTerms(Term[] tm, File file) {
		try {
			PrintWriter pw = null;

			pw = new PrintWriter(file);

			for (Term t : tm) {
				pw.println(t.getConcept() + " " + t.getConfidence());
			}
			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Este método escribe en un fichero de texto los terminos obtenidos de la votación por mayoría.
	 * 
	 * @param tm vector de terminos.
	 * @param file fichero destino donde escribir los terminos encontrados.
	 */
	private void saveTerms(String[] tm, File file) {
		try {
			PrintWriter pw = null;

			pw = new PrintWriter(file);

			for (String t : tm) {
				if(t == null) continue;
				pw.println(t);
			}
			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ejecuta el algortimo alg.
	 * @param alg
	 * @param afw
	 * @return devuelve el resultado de la ejecución del algoritmo.
	 * @throws JATEException
	 */
	private Term[] executeAlgorithm(Algorithm alg, AbstractFeatureWrapper afw) throws JATEException{
		return alg.execute(afw);
	}

	/**
	 * Este método realiza la extracción automática de los términos de fichero
	 * leído de la entrada de datos.
	 */
	public void doWork(){
		loadFileProperties();
		File transcribedAudio = ((DataBlock) dt.getData()).getFile();
		String uri = ((DataBlock) dt.getData()).getUri();

		String e = ((DataBlock) dt.getData()).getResourceName();

		Date date = new Date();

		System.out.println("[Jate] Loading Transcribed Audio: "
				+ e + " ...");
		System.out.println("[Jate] Starting Jate ...");
		
		File majority = new File(out + e + "-voting_majority_keywords.txt");

		try {
			StopList stop = new StopList(true);
			Lemmatizer lemmatizer = new Lemmatizer();
			CandidateTermExtractor npextractor = new NounPhraseExtractorOpenNLP(stop, lemmatizer);

			CandidateTermExtractor wordextractor = new WordExtractor(stop,
					lemmatizer);

			GlobalIndexBuilderMem builder = new GlobalIndexBuilderMem();
			Corpus c = new CorpusImpl();
			c.add(new DocumentImpl(transcribedAudio.toURL()));

			GlobalIndexMem wordDocIndex = builder.build(c, wordextractor);
			GlobalIndexMem termDocIndex = builder.build(c, npextractor);

			WordCounter wordcounter = new WordCounter();

			TermFreqCounter npcounter = new TermFreqCounter();

			FeatureCorpusTermFrequency termCorpusFreq = new FeatureBuilderCorpusTermFrequency(
					npcounter, wordcounter, lemmatizer).build(wordDocIndex);
			FeatureCorpusTermFrequency wordFreq =
					new FeatureBuilderCorpusTermFrequency(npcounter, wordcounter, lemmatizer).build(wordDocIndex);
			String refStatsPath = "./bnc_unifrqs.normal.txt";
			FeatureRefCorpusTermFrequency bncRef =
					new FeatureBuilderRefCorpusTermFrequency(refStatsPath).build(null);
			FeatureDocumentTermFrequency termDocFreq =
					new FeatureBuilderDocumentTermFrequency(npcounter, wordcounter, lemmatizer).build(termDocIndex);
			FeatureTermNest termNest =
					new FeatureBuilderTermNest().build(termDocIndex);

			Term[] tf = executeAlgorithm(new FrequencyAlgorithm(), new FrequencyFeatureWrapper(termCorpusFreq));
			Term[] avg = executeAlgorithm(new AverageCorpusTFAlgorithm(), new AverageCorpusTFFeatureWrapper(termCorpusFreq));
			Term[] ridf = executeAlgorithm(new RIDFAlgorithm(), new RIDFFeatureWrapper(termCorpusFreq));
			Term[] gloss = executeAlgorithm(new GlossExAlgorithm(), new GlossExFeatureWrapper(termCorpusFreq, wordFreq, bncRef));
			Term[] weird = executeAlgorithm(new WeirdnessAlgorithm(), new WeirdnessFeatureWrapper(wordFreq, termCorpusFreq, bncRef));
			Term[] termex = executeAlgorithm(new TermExAlgorithm(), new TermExFeatureWrapper(termDocFreq, wordFreq, bncRef));
			Term[] cvalue = executeAlgorithm(new CValueAlgorithm(), new CValueFeatureWrapper(termCorpusFreq, termNest));

			saveTerms(tf, new File("./tmpFiles/" + e + "-frequency_keywords.txt"));
			saveTerms(avg, new File("./tmpFiles/" + e + "-avg_keywords.txt"));
			saveTerms(ridf, new File("./tmpFiles/" + e + "-ridf_keywords.txt"));
			saveTerms(gloss, new File("./tmpFiles/" + e + "-gloss_keywords.txt"));
			saveTerms(weird, new File("./tmpFiles/" + e + "-weird_keywords.txt"));
			saveTerms(termex, new File("./tmpFiles/" + e + "-termex_keywords.txt"));
			saveTerms(cvalue, new File("./tmpFiles/" + e + "-cvalue_keywords.txt"));
			
			Voting v = new Voting();
			List<Term> r = v.calculate(new WeightedOutput(Arrays.asList(tf),tf_weight)
			,new WeightedOutput(Arrays.asList(avg),avg_weight),new WeightedOutput(Arrays.asList(ridf),ridf_weight)
			,new WeightedOutput(Arrays.asList(gloss),gloss_weight),new WeightedOutput(Arrays.asList(weird),weird_weight)
			,new WeightedOutput(Arrays.asList(termex),termex_weight),new WeightedOutput(Arrays.asList(cvalue),cvalue_weight));

			Term[] voting = new Term[r.size()];
			saveTerms(r.toArray(voting), new File("./tmpFiles/" + e + "-voting_weight_keywords.txt"));

			String[] mresult = majority_voting(tf,avg,ridf,gloss,weird,cvalue);

			saveTerms(mresult, majority);

		} 
		catch (JATEException e1){
			e1.printStackTrace();
		}
		catch (IOException e1){
			e1.printStackTrace();
		}

		Date ndate = new Date();

		double time = (ndate.getTime() - date.getTime()) / 1000.0;
		System.out.println("[Jate] Time elapsed: " + time + " sec.");

		Analysis.addTime(e, id, time, -1);

		// Eliminamos el fichero transcribedAudio, dado que ya no lo necesitamos
		// en las tareas siguientes
		// y asi eliminamos los ficheros residuales del sistema.
		// transcribedAudio.delete();
		// Escribimos la salida.

	
		Data d = new DataContainer(); d.setData(new DataBlock(majority, uri, e));
		writeToOutput(d);
	}

	/**
	 * Este método devuelve información referente al estado del Filtro.
	 * 
	 * @return un elemento de tipo ReportingResult con información referente al
	 *         estado del Filter.
	 * @see ReportingResult
	 */
	public ReportingResult report() {
		return null;
	}

	/**
	 * Esta clase se ha definido para tratar los votos realizados a cada uno de los términos de los distintos
	 * algoritmos devuelvos por JATE. Implementa la interfaz Comparable para asi poderse ordenar por valor del voto del término.
	 * 
	 * @author Daniel Delgado Llamas
	 * @version %I%, %G%
	 * @since 1.0
	 */
	private class VotesTerm implements java.lang.Comparable<VotesTerm>{
		private String element;
		private int value;

		public VotesTerm(String s, int votes){
			this.element = s;
			this.value = votes;
		}

		public String getElement(){
			return this.element;
		}

		public int getValue(){
			return this.value;
		}

		public int compareTo(VotesTerm v){
			if(value > v.getValue()){
				return -1;
			}
			else if(value < v.getValue()){
				return 1;
			}
			else{
				return 0;
			}
		}
	}

}
