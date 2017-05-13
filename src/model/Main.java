package model;

import javax.swing.SwingUtilities;
import controller.ButtonsController;
import utils.ConectorDB;
import view.MainWindow;

import java.sql.ResultSet;
import java.sql.SQLException;


public class Main {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//gestio BBDD
				ResultSet consulta;
				//Login BBDD
				ConectorDB conn = new ConectorDB("adminmaki", "3LwAuKuGFWb7IMwj", "makiTetrixdataBase", 8889);
				//Ens connectem a la BBDD
				conn.connect();

				// Creem la VISTA
				MainWindow view = new MainWindow();
				// Creem el CONTROLADOR
				// Establim la relacio CONTROLADOR->VISTA
				ButtonsController controller = new ButtonsController(view, conn);

				// Establim la "relacio" VISTA->CONTROLADOR
				view.registerController(controller);
				// Mostrem la VISTA
				view.setVisible(true);

				consulta = conn.selectQuery("SELECT * FROM usuarios");

				try {
					//Recorrem el ResultSet que ens retorna el selectQuery i agafem els par�metres desitjats
					while (consulta.next())
					{
						//Per recuperar el valor utilitzem la funci� .getObject() amb el nom del camp a recuperar
						System.out.println (consulta.getObject("Login") + " " + consulta.getObject("Password"));
					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					System.out.println("Problema al recuperar les dades...");
				}
				//Ens desconectem de la BBDD una vegada no la necessitem
				conn.disconnect();
			}
		});
	}
	
}