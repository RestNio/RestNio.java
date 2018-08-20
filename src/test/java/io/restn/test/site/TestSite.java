package io.restn.test.site;

import io.restn.RestInterface;
import io.restn.RestNioServer;
import io.restn.WebsocketInterface;

public class TestSite implements RestInterface, RestNioServer {

	/**
	 * @return main page using get.
	 */
	public String ROOT() {
		return "Welcome to the Test API";
	}

	/**
	 * Gets the name of the thing behind this thing.
	 * @return my name.
	 */
	public String getName() {
		return "Kasper";
	}

	/**
	 * Tests 
	 * @param toReturn
	 * @return idk
	 */
	public String testReturn(String toReturn) {
		return toReturn + " + some stuff.";
	}

	public static class age extends TestSite 
		implements RestInterface, WebsocketInterface {

		/**
		 * @return Gets the age.
		 */
		public int get() {
			return 17;
		}

		/**
		 * Sets the age.
		 * @param age
		 * @return the response code.
		 */
		public String set(int age) {
			return "Error: Can't change age!";
		}

	}
 
}
