package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import network.MessageService;
import utils.ConectorDB;
import view.MainWindow;


public class ButtonsController implements ActionListener {
	// VISTA
	private MainWindow view;
	// NETWORK
	private MessageService mService;
	private  ConectorDB conn;
	
	public ButtonsController(MainWindow view, ConectorDB conn) {
		this.view = view;
		this.conn = conn;
		// Instanciem la classe per rebre missatges.
		// Passem per parametre una referencia al propi objecte
		// per tal que notifiqui larribada de nous missatges.
		// Aquest tambe podria ser creat des del principal.
		this.mService = new MessageService(this, conn);
	}
	
	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("START")) {
			// Iniciem el servei
			mService.startService();
			view.changeButtonsStateStarted();
		} else if (event.getActionCommand().equals("STOP")) {
			// Aturem el servei
			mService.stopService();
			view.changeButtonsStateStopped();
		}
	}
	
	public void showMessage(String message) {
		view.addText(message);
	}
	
	// Aquest metode es invocat pel servei de missatges quan arriba
	// un nou missatge
	public void showInformation(String info) {
		// Mostrem el missatge rebut a la vista
		view.addText(info);
	}

}
