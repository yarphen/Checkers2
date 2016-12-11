package checkers.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import checkers.pojo.ChangeObject;
import checkers.pojo.step.Step;
import checkers.utils.NetworkClient;

/**
 * Created by oleh_kurpiak on 14.09.2016.
 *
 * Edited by mykhaylo sheremet on 11.12.2016.
 */

public class Client {
	private static final int DEFAULT_PORT = 3000;
	private NetworkClient client;
	private CheckersBot bot;
	public Client(CheckersBot bot){
		this(DEFAULT_PORT, bot);
	}

	public Client(int port, CheckersBot bot){
		this("localhost", port, bot);
	}

	public Client(String ip, int port, CheckersBot bot){
		try {
			client = new NetworkClient( new Socket(InetAddress.getByName(ip), port));
			this.bot = bot;
		} catch (IOException e) {
			e.printStackTrace();
			client.endConnection(e);
		}
	}

	public void run(){
		//write client name
		client.write(new ChangeObject(){{setMessage(bot.clientBotName());}});

		// get player color
		ChangeObject object = client.read();
		if(object == null){
			client.endConnection(null);
			return;
		}

		bot.onGameStart(object.getPlayerColor());


		while (true){
			object = client.read();
			if(object == null || object.isEnd()) {
				String message = NetworkClient.CONNECTION_CLOSED;
				if(object != null && object.getMessage() != null)
					message = object.getMessage();

				bot.onGameEnd(message);
				break;
			}

			Step step = bot.next(object.getBoard());
			client.write(new ChangeObject().step(step));
		}
		client.endConnection(null);
	}

}
