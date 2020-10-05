/***
 * JAVANAISE Implementation
 * JvnServerImpl class
 * Implementation of a Jvn server
 * Contact: 
 *
 * Authors: 
 */

package jvn;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.io.*;
//import java.net.InetAddress;



public class JvnServerImpl 	
              extends UnicastRemoteObject 
							implements JvnLocalServer, JvnRemoteServer{ 
	
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	// A JVN server is managed as a singleton 
	private static JvnServerImpl js = null;
	
	private static JvnRemoteCoord jCoord = null;
	
	private HashMap<Integer, JvnObject> jvnObjects;

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		// to be completed
		String url = "rmi://localhost:1099/coord";
		jCoord = (JvnRemoteCoord) Naming.lookup(url);
		this.jvnObjects = new HashMap<Integer, JvnObject>();
		while (!connectionToCoord(url)) {
			System.err.println("Perte de la connexion. Tentative de reconnexion …");
		}
		System.out.println("Enregistrement de l'objet avec l'url : " + url);
	}
	
	private Boolean connectionToCoord(String url) {
		try {
			jCoord = (JvnRemoteCoord) Naming.lookup(url);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
  /**
    * Static method allowing an application to get a reference to 
    * a JVN server instance
    * @throws JvnException
    **/
	public static JvnServerImpl jvnGetServer() {
		if (js == null){
			try {
				js = new JvnServerImpl();
			} catch (Exception e) {
				System.err.println("Erreur Serveur :" + e.getMessage());
				return null;
			}
		}
		return js;
	}
	
	/**
	* The JVN service is not used anymore
	* @throws JvnException
	**/
	public  void jvnTerminate()
	throws jvn.JvnException {
	// to be completed 
		try {
			jCoord.jvnTerminate(this);
		} catch(RemoteException e) {
			System.err.println("Erreur : " + e.getMessage());
		}
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public  JvnObject jvnCreateObject(Serializable o)
	throws jvn.JvnException { 
		// to be completed 

		int id ;
		JvnObject obj;

		try {
			id = jCoord.jvnGetObjectId();
			obj = new JvnObjectImpl(o, id);
			return obj; 
		} catch (RemoteException e) {
			System.err.println("Erreur : " + e.getMessage());
		}
		return null;
	}
	
	/**
	*  Associate a symbolic name with a JVN object
	* @param jon : the JVN object name
	* @param jo : the JVN object 
	* @throws JvnException
	**/
	public  void jvnRegisterObject(String jon, JvnObject jo)
	throws jvn.JvnException {
		// to be completed 
		try {
			jCoord.jvnRegisterObject(jon, jo, this);
			jvnObjects.put(jo.jvnGetObjectId(), jo);
		} catch (RemoteException e) {
			System.err.println("Erreur : " + e.getMessage());
		}
	}
	
	/**
	* Provide the reference of a JVN object beeing given its symbolic name
	* @param jon : the JVN object name
	* @return the JVN object 
	* @throws JvnException
	**/
	public  JvnObject jvnLookupObject(String jon)
	throws jvn.JvnException {
	// to be completed 
		JvnObject obj = null ;
		if(jCoord != null) {
			try {
				obj = jCoord.jvnLookupObject(jon, this);
				jvnObjects.put(obj.jvnGetObjectId(), obj);
			} catch (RemoteException e) {
				System.err.println("Erreur : " + e);
			}
		} else {
			System.err.println("Erreur : le coordinateur est nul.");
		}

		return obj;
	}	
	
	/**
	* Get a Read lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockRead(int joi)
	 throws JvnException {
		try {
			return jCoord.jvnLockRead(joi, this);
		} catch (RemoteException e) {
			System.err.println("Erreur : " + e);
		}
		return null;
	}	
	/**
	* Get a Write lock on a JVN object 
	* @param joi : the JVN object identification
	* @return the current JVN object state
	* @throws  JvnException
	**/
   public Serializable jvnLockWrite(int joi)
	 throws JvnException {
		// to be completed 
		try {
			return jCoord.jvnLockWrite(joi, this);
		} catch (RemoteException e) {
			System.err.println("Erreur : " + e);
		}
		return null;
	}	

	
  /**
	* Invalidate the Read lock of the JVN object identified by id 
	* called by the JvnCoord
	* @param joi : the JVN object id
	* @return void
	* @throws java.rmi.RemoteException,JvnException
	**/
  public void jvnInvalidateReader(int joi)
	throws java.rmi.RemoteException,jvn.JvnException {
		// to be completed 
		jvnObjects.get(joi).jvnInvalidateReader();
	};
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.JvnException { 
		// to be completed 
		Serializable obj = jvnObjects.get(joi).jvnInvalidateWriter();
		jvnObjects.get(joi).setObject(obj);
		return obj;
	};
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.JvnException { 
		// to be completed 
		Serializable obj = jvnObjects.get(joi).jvnInvalidateWriterForReader();
		jvnObjects.get(joi).setObject(obj);
		return obj;
	 };

}

 
