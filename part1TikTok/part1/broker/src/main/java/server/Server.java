package server;

import communication.ConnectionMetadata;
import nodes.Broker;
import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Broker {

	ServerSocket providerSocket;

	class ActionsForPublishers extends Thread {
		private ConnectionMetadata connectionMetadata;

		public ActionsForPublishers(ConnectionMetadata connectionMetadata) {
			this.connectionMetadata = connectionMetadata;
		}

		public void run() {
			PublisherData publisherData = new PublisherData(connectionMetadata);

			System.out.println("New publisher data created with USERNAMEIP:PORT:" + publisherData.getConnectionMetadata().getUsername() +publisherData.getConnectionMetadata().getIp() + ": " +publisherData.getConnectionMetadata().getPort());

			try {
				acceptConnection(publisherData);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException | TikaException | SAXException e) {
				e.printStackTrace();
			}
		}
	}

	class ActionsForConsumer extends Thread {
		private ConnectionMetadata connectionMetadata;

		public ActionsForConsumer(ConnectionMetadata connectionMetadata) {
			this.connectionMetadata = connectionMetadata;
		}

		public void run() {
			ConsumerData consumerData = new ConsumerData(connectionMetadata);

			try {
				acceptConnection(consumerData);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void openServer(String myIp, int myPort) {
		try {
			providerSocket = new ServerSocket(myPort, 50);

			while (true) {
				//Αναμονή για σύνδεση με πελάτη
				System.out.println("Waiting for a connection on port:" + myPort + " ...");

				ConnectionMetadata connectionMetadata = super.accept(providerSocket);

				String role = handShake(connectionMetadata);

				System.out.println("Role detected: " + role);

				sendSocketMetadata(connectionMetadata);

				if (role.equals("PUBLISHER")) {
					ActionsForPublishers t = new ActionsForPublishers(connectionMetadata);
					t.start();
				} else if (role.equals("CONSUMER")) {
					ActionsForConsumer t = new ActionsForConsumer(connectionMetadata);
					t.start();
				} else {
					System.out.println("Unknown role, rejecting client");
				}
			}
		} catch (IOException | ClassNotFoundException ioException) {
			ioException.printStackTrace();
		} finally {
			try {
				providerSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}


}
