package benchmarks;

import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import jmh.mbr.core.OptionsCustomizer;

public class ProfilerOptionsCustomizer implements OptionsCustomizer {

	@Override
	public ChainedOptionsBuilder customize(ChainedOptionsBuilder options) {
		return options.addProfiler("gc");
	}

}
