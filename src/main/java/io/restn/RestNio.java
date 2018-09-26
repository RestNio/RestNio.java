package io.restn;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Main AccessPoint to RestNio
 * @author 7kasper
 *
 */
public class RestNio {

	public static final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

	public static void main(String[] args) {
		new RestServer(8080, true).run();
		exec.shutdownNow();
	}
	
	public static void startserver() {
		
	}

}
