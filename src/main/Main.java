package main;

import jvn.JvnCoordImpl;
import jvn.JvnRemoteCoord;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;


public class Main {

	public static void main(String[] args) throws MalformedURLException {
		try {
			LocateRegistry.createRegistry(1099);
			JvnRemoteCoord coordinator = JvnCoordImpl.jvnGetCoord();
			Naming.rebind("rmi://localhost:1099/coord", coordinator);
		    System.out.println("Coordinateur enregistré. Prêt." + coordinator.toString());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}
