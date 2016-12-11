package com.sheremet.checkers.client;
import checkers.client.Client;

/**
 * Created by mykhaylo sheremet on 11.12.2016.
 */
public class Bot2 {
	/**
	 * Runs Bot2
	 * @param args
	 */
	public static void main(String[] args){
		int port = 3000;
		String ip = "localhost";
		Client client = new Client(ip, port, new FirstStepBot("bot2", new BoardRenderer()));
		client.run();
	}

}
