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
	private JvnRemoteCoord jCoord = null;
	private HashMap<Integer, JvnObject> jvnObjects;

  /**
  * Default constructor
  * @throws JvnException
  **/
	private JvnServerImpl() throws Exception {
		super();
		String url = "rmi://localhost:1099/coord";
		this.jCoord = (JvnRemoteCoord) Naming.lookup(url);
		this.jvnObjects = new HashMap<Integer, JvnObject>();
		while (!connectionToCoord(url)) {
			System.err.println("Perte de la connexion. Tentative de reconnexion …");
		}
		System.out.println("Enregistrement de l'objet avec l'URL : " + url);
	}
	
	private Boolean connectionToCoord(String url) {
		try {
			this.jCoord = (JvnRemoteCoord) Naming.lookup(url);
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
			synchronized (JvnServerImpl.class) {
				try {
					js = new JvnServerImpl();
				} catch (Exception e) {
					System.err.println("JvnServerImpl : Impossible de GetServer : " + e.getMessage());
					return null;
				}
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
		if (jCoord != null) {
			try {
				jCoord.jvnTerminate(this);
			} catch(RemoteException e) {
				System.err.println("JvnServerImpl : Impossible de Terminate : " + e.getMessage());
			}
		}
	} 
	
	/**
	* creation of a JVN object
	* @param o : the JVN object state
	* @throws JvnException
	**/
	public  JvnObject jvnCreateObject(Serializable o)
	throws jvn.JvnException { 
		try {
			int id = jCoord.jvnGetObjectId();
			JvnObjectImpl obj = new JvnObjectImpl(o, id);
			return obj;
		} catch (Exception e) {
			System.err.println("JvnServerImpl : Impossible de CreateObject : " + e.getMessage());
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
		try {
			jCoord.jvnRegisterObject(jon, jo, js);
			int id = jo.jvnGetObjectId() ; 
			jvnObjects.put(id, jo);
		} catch (RemoteException e) {
			System.err.println("JvnServerImpl : Impossible de RegisterObject : " + e.getMessage());
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
		JvnObject obj = null ;
		if(jCoord != null) {
			try {
				obj = jCoord.jvnLookupObject(jon, this);
				if (obj != null) {
					int id = obj.jvnGetObjectId();
					jvnObjects.put(id, obj);
				}
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
	   if (jCoord != null) {
		   try {
			   return jCoord.jvnLockRead(joi, js);
		   } catch (RemoteException e) {
			   System.err.println("JvnServerImpl : Impossible de Lockread : " + e.getMessage());
		   }
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
	   if (jCoord != null) {
		   try {
			   return jCoord.jvnLockWrite(joi, js);
		   } catch (RemoteException e) {
			   System.err.println("JvnServerImpl : Impossible de LockWrite : " + e.getMessage());
		   }
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
	  JvnObject obj = jvnObjects.get(joi);
	  if (obj == null) {
		  throw new JvnException("JvnServerImpl : Object null");
	  }
	  try {
		  obj.jvnInvalidateReader();
	  } catch (JvnException e) {
		  throw new JvnException("JvnServerImpl : Impossible de InvalidateReader : " + e);
	  }
	};
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
	throws java.rmi.RemoteException,jvn.JvnException {
	  
	  JvnObject obj = jvnObjects.get(joi);
	  if (obj == null) {
		  throw new JvnException("JvnServerImpl : Impossible de InvalidateWriter");
	  }

	  return obj.jvnInvalidateWriter();
	};
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
	 throws java.rmi.RemoteException,jvn.JvnException { 
	   
	   JvnObject obj = jvnObjects.get(joi);
	   if (obj == null) {
		   throw new JvnException("JvnServerImpl : Impossible de InvalidateWriterForReader");
	   }

	   return obj.jvnInvalidateWriterForReader();
	 };

}

 
