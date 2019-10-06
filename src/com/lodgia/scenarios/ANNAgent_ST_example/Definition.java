package com.lodgia.scenarios.ANNAgent_ST_example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

import com.lodgia.genesys.genetics.BiasedRouletteWheel;
import com.lodgia.genesys.genetics.GenericAlgoritm;
import com.lodgia.genesys.genetics.GenomeDoubleFloatFactory;
import com.lodgia.genesys.genetics.Stats;
import com.lodgia.genesys.genetics.interfaces.InterfaceEvaluationFunction;
import com.lodgia.genesys.gfx.interfaces.InterfaceWorldGenericRenderer;
import com.lodgia.genesys.lib.Logger;
import com.lodgia.genesys.main.MainProgram;
import com.lodgia.genesys.problemdef.InterfaceProblemDefinition;
import com.lodgia.genesys.utils.JarUtil;
import com.lodgia.genesys.utils.TypedProperties;
import com.lodgia.world2d.World2dAWTRenderer;
import com.lodgia.world2d.World2dFileRenderer;


public class Definition implements InterfaceProblemDefinition {
 
	
	//genetics
	GenericAlgoritm genAlg;
	//GenomeBitStringFactory GenGenomeFact;
	GenomeDoubleFloatFactory GenGenomeFact;
	BiasedRouletteWheel rouletteWheel;
	Simulator Evaluation1s[],Evaluation2;	
	
	MainProgram uiadapt;
	InterfaceWorldGenericRenderer renderer;
	InterfaceWorldGenericRenderer renderer2;
	
	int debugLevel;
	
	TypedProperties cfg;
	File dataRootDir;
	
	Logger l;

	public Definition()
	{
		
		
	}

	public String getDataRootDir()
	{
		return dataRootDir.getPath();
	}

	public void init(int pDebugLevel) throws Exception
	{
	
		System.out.println("Definition.Init");
		System.out.println("################");
		String baseDirName;
		
		File dataDir;
		File dataScreenshotsDir;

		File cfgDir;
		baseDirName = new String(packageName());
		
		cfgDir = new File( baseDirName + "_cfg" );

		cfg = new TypedProperties();
		
		try {
			cfg.load(new FileReader(cfgDir + "/configuration.properties"));
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw e;
		}
		catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
		
		String simName = cfg.gets("SIM_NAME");

		String dateString = "" + System.currentTimeMillis();
		dataRootDir = new File( "D:/" + baseDirName + "_runtimedata/" + simName + dateString);
		dataDir =  new File( dataRootDir.getPath() + "/states");
		dataScreenshotsDir = new File( dataRootDir.getPath() + "/states/screenshots");
		
		debugLevel=pDebugLevel;
		l = new Logger(this.getClass().getName(), pDebugLevel);
		
		dataRootDir.mkdir();
		dataDir.mkdir();
		dataScreenshotsDir.mkdir();
		
		cfg.set("rootdir", dataRootDir.getPath()  );
		cfg.set("states.dataDir", dataDir.getPath()  );
		cfg.set("screenshots.dataDir", dataScreenshotsDir.getPath()  );
	
		renderer=new World2dAWTRenderer(null);
		renderer2=new World2dFileRenderer(null, dataScreenshotsDir.getPath()); 
		
		Evaluation1s=new Simulator[1];
		Evaluation1s[ 0 ]=new Simulator( cfg, renderer2,debugLevel);
		Evaluation2=new Simulator( cfg, renderer,debugLevel);
		//Evaluation1.setEvaluationLength(SIMULATIONLENGTH);
		//Evaluation2.setEvaluationLength(SIMULATIONLENGTH);
		
		GenGenomeFact=new GenomeDoubleFloatFactory( Evaluation1s[0].getDnaLength(), debugLevel);
		
		rouletteWheel=new BiasedRouletteWheel(debugLevel);
		
		genAlg=new GenericAlgoritm(
					GenGenomeFact,
					Evaluation1s[0],
					rouletteWheel,
					cfg.geti("POPULATIONSIZE"),
					debugLevel
					);
		
		genAlg.setMatingpoolKeepingPercentage( cfg.getd("MATINGPOOLKEEPPERCENTAGE"));
		genAlg.setMutationChance( cfg.geti("SETMUTATIONCHANCE") );
		genAlg.setNumberOfRandomAgentsToInsert( cfg.geti("INJECTRANDOMAGENTS_NUMBEROF"));
		genAlg.setSimulationStartLength( cfg.geti("SIMULATIONSTARTLENGTH"));
		genAlg.setSimulationIncreaseStep( cfg.geti("SIMULATIONLENTHINCREASESTEP"));
		genAlg.setMaxNoGrowCycles( cfg.geti("SIMULATIONMAXNOGROWCYCLES"));
		genAlg.setSimulationPopulationIncreaseStep( cfg.geti("SIMULATIONPOPULATIONINCREASESTEP"));	
		genAlg.setSimulationPopulationIncreaseMax( cfg.geti("SIMULATIONPOPULATIONINCREASEMAX"));	
		if ( cfg.gets("INITIALPOPULATION") != null ) {
			if ( !cfg.gets("INITIALPOPULATION").equals("") ) {
					genAlg.setInitialPopulation( cfg.gets("INITIALPOPULATION") );
			}
		}
		
		File files[] = dataDir.listFiles();
		if( files == null ) {
			System.err.println("Cannot find files in data directory");
		}
		for (int i = 0; i < files.length; i++) {

			if (!files[i].isDirectory()) { 
				files[i].delete();
				}
		}
		files = dataScreenshotsDir.listFiles();
		for (int i = 0; i < files.length; i++) {

			if (!files[i].isDirectory()) { 
				files[i].delete();
				}
		}
		
		JarUtil.create(dataRootDir + "/src.zip", "src", l);
		cfg.store(new FileOutputStream( new File ( dataRootDir +"/configuration.properties") ), "");
		//FileUtils.cleanDirectory("Robot2/states");
		
	}



	private String packageName() {
		String parts[] = this.getClass().getName().split("[.]");
				
		
		return parts[parts.length-2];
	}

	public void connectToController(MainProgram pApp) {
		
		uiadapt=pApp;
		for( int i=0; i<1; i++ ) {
			Evaluation1s[ i ].connectToController(pApp);
		}
		Evaluation2.connectToController(pApp);
	}



	public int processSolveCycle() {
		
		return genAlg.processJobFraction();
		
	}

	
	public InterfaceWorldGenericRenderer getSolutionSpaceRenderer() {
		
		return renderer;
	}


	public InterfaceEvaluationFunction[] getEvaluationClass() {

		return Evaluation1s;
	}
	
	public InterfaceEvaluationFunction getEvaluationClassCopyForPlaying() {

		return Evaluation2;
	}	

	public GenericAlgoritm  getSearchAlgoritmClass() {
		
		return genAlg;
		
	}


	public Stats getStats() {
		
		return genAlg.getStats();
	}
	
}
