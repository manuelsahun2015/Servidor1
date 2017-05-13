package network;

import utils.ConectorDB;

import javax.swing.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MessageServiceWorker implements Runnable{

	private MessageService mService;
	private ServerSocket sServer;
	private Socket sClient;
	private DataInputStream diStream;
	private DataOutputStream doStream;
	private boolean active;
	private String comando;
	private String login;
	private String register;
	private ConectorDB conn;

	public MessageServiceWorker(MessageService mService, ServerSocket sServer, ConectorDB conn) {
		this.mService = mService;
		this.sServer = sServer;
		this.conn = conn;
		active = true;
	}

	// Escolta peticions de connexio i llegeix els missatges dels clients
	public void run() {

		int x = 0;
		int n,m,a,b,d,f;
		ResultSet consulta;
		String loginLogin = new String();
		String loginPassword = new String();
		String registerLogin = new String();
		String registerPassword = new String();
		String registerMail = new String();
		Boolean succesfull = FALSE;


		conn.connect();

		while (active) {
			try {
				// Esperem peticions de connexio
				sClient = sServer.accept();
				// Atenem les connexions
				diStream = new DataInputStream(sClient.getInputStream());
				String newMessage = diStream.readUTF();
				// Informem a MessageService que sha rebut un nou missatge
				// ell informara al controlador i el controlador actualitzara la vista.
				mService.messageReceived("[" + getCurrentTime()+ "] " + newMessage);



				comando = newMessage;

				switch (comando){
					//controlar error limpiar las string
					case("LOGIN"):
						x = 1;
						loginLogin = "";
						loginPassword = "";
					break;

					case("REGISTER"):
						x = 2;
						registerLogin = "";
						registerMail = "";
						registerPassword = "";
					break;

					default:
						if(x == 1){
							login = newMessage;
							for (n = 0; n < login.length() && login.charAt(n) != '#'; n++) {
								char c = login.charAt(n);
								loginLogin += c;
							}
							for (m = n + 1; m < login.length() && login.charAt(m) != '#'; m++) {
								char c = login.charAt(m);
								loginPassword += c;
							}
							System.out.println(loginLogin);
							System.out.println(loginPassword);

							consulta = conn.selectQuery("SELECT * FROM usuarios");
							try {
								while (consulta.next()) {
									if((consulta.getObject("Login").equals(loginLogin)) && (consulta.getObject("Password").equals(loginPassword))){
										mService.messageReceived("login succesfull");
										succesfull = TRUE; //dejamos en TRUE para q siga dentro el jugador.
										sendLogged("logged");
										break;
									}
								}
								if(!succesfull) {
									mService.messageReceived("login failed");
								}
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								System.out.println("Problema al recuperar les dades...");
							}
						}
						if(x == 2){
							register = newMessage;
							for (a = 0; a < register.length() && register.charAt(a) != '#'; a++) {
								char c = register.charAt(a);
								registerLogin += c;
							}
							for (b = a + 1; b < register.length() && register.charAt(b) != '#'; b++) {
								char c = register.charAt(b);
								registerPassword += c;
							}
							for (d = b + 1; d < register.length() && register.charAt(d) != '#'; d++) {
								char c = register.charAt(d);
								registerMail += c;
							}
							System.out.println(registerLogin);
							System.out.println(registerMail);
							System.out.println(registerPassword);

							conn.insertQuery("INSERT INTO `usuarios` (`Login`, `Password`, `Mail`) VALUES ('"+registerLogin+"', '"+registerPassword+"', '"+registerMail+"')");

						}
					break;
				}
				/*conn.insertQuery("INSERT INTO usuarios (Login, Password) VALUES ('Rafa','4255','http://salle.url.edu')");*/

				// Tanquem el socket del client
				sClient.close();
			} catch (IOException e) { }
		}
		conn.disconnect();
	}

	// Operacio privada per generar la data de recepcio dels missatges
	private String getCurrentTime() {
		return new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
	}

	public void stopListening() {
		active = false;
	}


	public void sendLogged(String message) {
		try {
			doStream = new DataOutputStream(sClient.getOutputStream());
			doStream.writeUTF(message);
			// Tanquem el socket
			sServer.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "SERVER CONNECTION ERROR (message not sent)");
		}
	}
}
