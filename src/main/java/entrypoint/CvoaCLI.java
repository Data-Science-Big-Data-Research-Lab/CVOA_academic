package entrypoint;

import java.io.File;
import java.util.concurrent.Callable;

import fitness.FitnessFunction;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.net.URL;
import java.net.URLClassLoader;

public class CvoaCLI implements Callable<Integer> {

	// Common parameters
	@Parameters(index = "0", description = "Number of bits",defaultValue = "10") 
	private int bits;
	
	
	@Option(names = {"-it", "--iterations"}, description = "Number of iterations",defaultValue = "20") 
	private int iterations;
	
	
	@Option(names = {"-f", "--fitnessFunction"}, description = "Fitness function") 
	private File fitnessFunction;


	private ClassLoader cl;
	
	public static void main(String[] args) {
		int exitCode = new CommandLine(new CvoaCLI()).execute(args);
        System.exit(exitCode);
	}


	@Override
	public Integer call() throws Exception {
		
		System.out.println("BITS = "+bits+", ITERATIONS = "+iterations);
		
		if (fitnessFunction==null)
			fitnessFunction = new File("/Users/davgutavi/Desktop/test/");

				
		
		System.out.println(fitnessFunction.getAbsolutePath());
		
		URL url = fitnessFunction.toURI().toURL();
		URL[] urls = new URL[]{url};
		
		cl = new URLClassLoader(urls);
		
		Class<?> cls = cl.loadClass("fitness.Xminus15alt");
		
		FitnessFunction f = (FitnessFunction) cls.newInstance();
		
		System.out.println("best value="+f.getBestValue());
		
		return new Integer(0);
	}
	
	
}
