package network;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

import controller.ButtonsController;
import utils.ConectorDB;

import javax.swing.*;

import static sun.jvm.hotspot.debugger.win32.coff.DebugVC50X86RegisterEnums.IP;


public class MessageService {
	private ServerSocket sServer;
	private static final int PORT = 55555;
	private  ConectorDB conn;

	// Relacio amb el fil dexecucio que escolta les peticions de connexio
	private MessageServiceWorker msWorker;
	// Relacio amb el controlador per notificar les recepcions de missatges
	private ButtonsController controller;
	
	public MessageService(ButtonsController controller, ConectorDB conn) {
		this.controller = controller;
		this.conn = conn;
	}
	
	// Inicia el servei per la recepcio de missatges
	public void startService() {
		try {
			// Creem el ServerSocket
			sServer = new ServerSocket(PORT);
			// Creem i iniciem un nou fil d execucio per tal descoltar
			// els clients i rebre els missatges per part dels clients
			msWorker = new MessageServiceWorker(this, sServer, conn);
			new Thread(msWorker).start();
			// Informem al CONTROLADOR que informi que el servidor ha
			// estat iniciat, ell informara a la vista.
			controller.showInformation("SERVER started. \nAwaiting messages...");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stopService() {
		try {
			// Aturem el fil d execucio
			msWorker.stopListening();
			// Tanquem el ServerSpcket
			sServer.close();
			// Informem al CONTROLADOR que informi que el servidor ha
			// estat aturat
			controller.showInformation("SERVER stopped.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void messageReceived(String message) {
		// Informem al controlador de la recepcio dun missatge,
		// ell actualitzara la vista.
		controller.showMessage(message);
	}
}
