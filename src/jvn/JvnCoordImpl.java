/***
 * JAVANAISE Implementation
 * JvnCoordImpl class
 * This class implements the Javanaise central coordinator
 * Contact:  
 *
 * Authors: 
 */

package jvn;

import java.rmi.ConnectException;
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
  /*private HashMap<String, JvnObject> jvnObjects; //Nom des objets JVN
  private HashMap<Integer, String> jvnIdsNames; //Lien id/nom des objets JVN*/
  
  private HashMap<String, Integer> jvnIdsFromNames; //Lien Nom/id des objets JVN;
  private HashMap<Integer, JvnObject> jvnObjectsFromIds ; // objets JVN en fonction de leur id
  
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
    	/*this.jvnObjects = new HashMap<String, JvnObject>();
		this.jvnIdsNames = new HashMap<Integer, String>();*/
		this.jvnWriteServers = new HashMap<Integer, JvnRemoteServer>();
		this.jvnReadServers = new HashMap<Integer, List<JvnRemoteServer>>();
		this.jvnIdsFromNames = new HashMap<String, Integer>();
		this.jvnObjectsFromIds = new HashMap<Integer, JvnObject>();

    
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
    /*this.jvnObjects.put(jon, jo);
	this.jvnIdsNames.put(jo.jvnGetObjectId(), jon);*/
	
	jvnIdsFromNames.put(jon, jo.jvnGetObjectId());
	jvnObjectsFromIds.put(jo.jvnGetObjectId(), jo);
	
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
	  System.out.println("JvnCoordLookupObject");
	//int id = jvnIdsFromNames.get(jon);
	System.out.println("JvnCoordLookupObject2");
	JvnObject jvnObject = jvnObjectsFromIds.get(id);
	if (true)
		return null;
    //JvnObject jvnObject = jvnObjects.get(jon);
    //int id = jvnObject.jvnGetObjectId();

    //new way
    /*Integer id  = jvnIdsFromNames.get(jon);
    
    if (id != null) {
    	return jvnObjectsFromIds.get(id);
    }
    
    return null;*/
 

    if (jvnObject != null) {
    	LOCK_STATES state = LOCK_STATES.NL;
        if(jvnWriteServers != null && jvnWriteServers.containsKey(id) && jvnWriteServers.get(id) != null) {
          state = LOCK_STATES.W;
        }
        else if(jvnReadServers != null && jvnReadServers.containsKey(id) && jvnReadServers.get(id).size() > 0) {
          state = LOCK_STATES.R;
        }
        
        jvnObject = new JvnObjectImpl(jvnObjectsFromIds.get(id), id, state);
        return jvnObject;
    } else {
      System.out.println("jvnObject nul.");
      return null;
    }  

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

	   /*Serializable object = jvnObjectsFromIds.get(joi).jvnGetSharedObject(); //jvnReferences.get(joi)).jvnGetObjectState();

	   if (jvnWriteServers.containsKey(joi) && !jvnWriteServers.get(joi).equals(js)) {
		   try {
			   System.out.println("Le serveur Write a un verrou write sur l'objet " + joi);
			   object = jvnWriteServers.get(joi).jvnInvalidateWriterForReader(joi);
			   jvnWriteServers.remove(joi);
			   jvnObjectsFromIds.get(joi).setObject(object);
		   } catch (ConnectException e) {
			   System.err.println(e.getMessage());
			   jvnWriteServers.remove(joi);

			   jvnObjectsFromIds.get(joi).setObject(object);
		   }
	   }
	   jvnReadServers.get(joi).add(js);
	   
	   return object;*/
	   
	   
    List<JvnRemoteServer> jvnRemoteServers = jvnReadServers.get(joi);
	
    if(jvnRemoteServers == null) {
      jvnRemoteServers = new ArrayList<JvnRemoteServer>();
    }
    
    if (jvnWriteServers.get(joi) != null && jvnWriteServers.size() > 0) {
    	JvnObject jo = (JvnObject) jvnWriteServers.get(joi).jvnInvalidateWriter(joi);
    	jvnObjectsFromIds.put(joi, jo);
    	
    	if(!jvnRemoteServers.contains(js)) {
    		jvnRemoteServers.add(js);
    	}
    	
    	jvnReadServers.put(joi, jvnRemoteServers);
    } else {
    	if (!jvnRemoteServers.contains(js)) {
    		jvnRemoteServers.add(js);
    	}
    	jvnReadServers.put(joi, jvnRemoteServers);
    }

    return jvnObjectsFromIds.get(joi);
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
		jvnObjectsFromIds.get(joi).setObject(joState);
		//jvnObjects.get(jvnIdsNames.get(joi)).setObject(joState);
    jvnWriteServers.put(joi, js);
    
    List<JvnRemoteServer> jvnRemoteServers = jvnReadServers.get(joi);
		if(jvnRemoteServers != null)
		{
			for(JvnRemoteServer jrs : jvnRemoteServers)
				jrs.jvnInvalidateReader(joi);
			
			this.jvnReadServers.remove(joi);
		}
		
		if(jvnObjectsFromIds.get(joi) == null) {
		//if(jvnIdsNames.get(joi) == null) {
			throw new JvnException("Demande de verrou impossible.");
    }
		
		joState = jvnObjectsFromIds.get(joi).getObject();
		//joState = jvnObjects.get(jvnIdsNames.get(joi)).getObject();
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

 
