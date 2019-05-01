package bot;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import bot.actions.ActionList;
import bot.messages.ResponseReceiver;

public class Connection {

	private static Connection connection;
	private String ip;
	private int port;
	private Socket socket;
	private ObjectInputStream inputStream;
	private ObjectOutputStream outputStream;
	private ResponseReceiver responseReceiver;

	public Connection(String ip, int port) {

		connection = this;
		this.ip = ip;
		this.port = port;

	}

	public void connect() {

		try {
			socket = new Socket(ip, port);
			System.out.println("Client connected");

			inputStream = new ObjectInputStream(socket.getInputStream());
			outputStream = new ObjectOutputStream(socket.getOutputStream());

			Runnable inputGrabber = new Runnable() {

				@Override
				public void run() {

					DataInputStream dataInputStream = new DataInputStream(inputStream);

					while (true) {

						try {
							String message = dataInputStream.readUTF();

							new Thread(() -> {
								responseReceiver.receive(message);
							}).start();

						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}
			};

			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.execute(inputGrabber);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void onResponse(ResponseReceiver receiver) {

		this.responseReceiver = receiver;

	}

	public static void SendActions(ActionList actions) {

		try {
			connection.outputStream.writeObject(actions);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
