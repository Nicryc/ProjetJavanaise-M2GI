/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.io.Serializable;
//import java.net.InetAddress;

import jvn.JvnObjectImpl.LOCK_STATES;



public class JvnCoordImpl 	
              extends UnicastRemoteObject 
							implements JvnRemoteCoord{
	

  /**
	 * 
	 */
  private static final long serialVersionUID = 1L;
  
  private int id;
  private HashMap<String, JvnObject> jvnObjects; //Nom des objets JVN
  private HashMap<Integer, String> jvnIdsNames; //Lien id/nom des objets JVN
  private HashMap<Integer, JvnRemoteServer> jvnWriteServers; //Stockage des serveurs bloqués en écriture
  private HashMap<Integer, List<JvnRemoteServer>> jvnReadServers; //Stockage des serveurs bloqués en lecture
  private static JvnRemoteCoord jc = null;


/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
    // to be completed
		this.id = 0;
    	this.jvnObjects = new HashMap<String, JvnObject>();
		this.jvnIdsNames = new HashMap<Integer, String>();
		this.jvnWriteServers = new HashMap<Integer, JvnRemoteServer>();
		this.jvnReadServers = new HashMap<Integer, List<JvnRemoteServer>>();

    
    LocateRegistry.createRegistry(1099);
		Naming.rebind("rmi://localhost:1099/coord", this);
    System.out.println("Coordinateur enregistré. Prêt." + this.toString());
    
  }
  
  /**
  * Static method allowing an application to get a reference to 
  * a JVN coordinator instance
  * @throws JvnException
  **/
	public static JvnRemoteCoord jvnGetCoord()
	{
		if(jc == null)
		{
			try {
				jc = new JvnCoordImpl();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return jc;
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
    // to be completed 
    return this.id++;
  }
  
  /**
  * Associate a symbolic name with a JVN object
  * @param jon : the JVN object name
  * @param jo  : the JVN object 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    // to be completed 
    this.jvnObjects.put(jon, jo);
	this.jvnIdsNames.put(jo.jvnGetObjectId(), jon);
    this.jvnWriteServers.put(jo.jvnGetObjectId(), js);
    this.jvnReadServers.put(jo.jvnGetObjectId(), new ArrayList<JvnRemoteServer>());
    
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
    // to be completed 
    //TODO
    JvnObject jvnObject = jvnObjects.get(jon);
    int id = jvnObject.jvnGetObjectId();

    /*if (jvnObject != null) {
      jvnObject.free();
      return jvnObject;
    } else {
      System.out.println("jvnObject nul.");
      return null;
    }*/

    LOCK_STATES state = LOCK_STATES.NL;
    if(jvnWriteServers != null && jvnWriteServers.containsKey(id) && jvnWriteServers.get(id) != null) {
      state = LOCK_STATES.W;
    }
    else if(jvnReadServers != null && jvnReadServers.containsKey(id) && jvnReadServers.get(id).size() > 0) {
      state = LOCK_STATES.R;
    }
    
    JvnObject jo = new JvnObjectImpl(jvnObjects.get(jon).getObject(), id);
    jo.setState(state);
    return jo;

  }
  
  /**
  * Get a Read lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockRead(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
    // to be completed
    //TODO

    Serializable joState;
    List<JvnRemoteServer> jvnRemoteServers = jvnReadServers.get(joi);
	
    if(jvnRemoteServers == null) {
      jvnRemoteServers = new ArrayList<JvnRemoteServer>();
    }
    joState = jvnWriteServers.get(joi).jvnInvalidateWriterForReader(joi);
    jvnWriteServers.remove(joi);
    jvnObjects.get(jvnIdsNames.get(joi)).setObject(joState);
    if(!jvnRemoteServers.contains(js)) {
			jvnRemoteServers.add(js);
		}
		jvnReadServers.put(joi, jvnRemoteServers);

    return joState;
   }

  /**
  * Get a Write lock on a JVN object managed by a given JVN server 
  * @param joi : the JVN object identification
  * @param js  : the remote reference of the server
  * @return the current JVN object state
  * @throws java.rmi.RemoteException, JvnException
  **/
   public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
   throws java.rmi.RemoteException, JvnException{
    // to be completed
    //TODO
    Serializable joState;

    joState = this.jvnWriteServers.get(joi).jvnInvalidateWriter(joi);
		jvnWriteServers.remove(joi);
		jvnObjects.get(jvnIdsNames.get(joi)).setObject(joState);
    jvnWriteServers.put(joi, js);
    
    List<JvnRemoteServer> jvnRemoteServers = jvnReadServers.get(joi);
		if(jvnRemoteServers != null)
		{
			for(JvnRemoteServer jrs : jvnRemoteServers)
				jrs.jvnInvalidateReader(joi);
			
			this.jvnReadServers.remove(joi);
		}
		
		if(jvnIdsNames.get(joi) == null) {
			throw new JvnException("Demande de verrou impossible.");
    }
		joState = jvnObjects.get(jvnIdsNames.get(joi)).getObject();
		this.jvnWriteServers.put(joi, js);

    return joState;
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
    // to be completed
    for(List<JvnRemoteServer> jvnRemoteServers : jvnReadServers.values())
		{
			if(jvnRemoteServers.contains(js)) {
        jvnRemoteServers.remove(js);
      }
		}
		jvnWriteServers.values().remove(js);
   }

    public static void main(String[] args) {
      try {
        new JvnCoordImpl();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

}

 
