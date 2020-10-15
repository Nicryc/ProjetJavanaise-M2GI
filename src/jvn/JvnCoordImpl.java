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
  
  private int id = 0;
  /*private HashMap<String, JvnObject> jvnObjects; //Nom des objets JVN
  private HashMap<Integer, String> jvnIdsNames; //Lien id/nom des objets JVN*/
  
  private HashMap<Integer, String> jvnNamesFromIds; //Lien id/nom des objets JVN;
  private HashMap<String, JvnObject> jvnObjectsFromNames ; // objets JVN en fonction de leur nom
  
  private HashMap<Integer, JvnRemoteServer> jvnWriteServers; //Stockage des serveurs bloqués en écriture
  private HashMap<Integer, List<JvnRemoteServer>> jvnReadServers; //Stockage des serveurs bloqués en lecture
  private static JvnCoordImpl jc = null;


/**
  * Default constructor
  * @throws JvnException
  **/
	private JvnCoordImpl() throws Exception {
		this.jvnWriteServers = new HashMap<Integer, JvnRemoteServer>();
		this.jvnReadServers = new HashMap<Integer, List<JvnRemoteServer>>();
		this.jvnNamesFromIds = new HashMap<Integer, String>();
		this.jvnObjectsFromNames = new HashMap<String, JvnObject>();
	    //OK
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
		//OK
	}

  /**
  *  Allocate a NEW JVN object id (usually allocated to a 
  *  newly created JVN object)
  * @throws java.rmi.RemoteException,JvnException
  **/
  public int jvnGetObjectId()
  throws java.rmi.RemoteException,jvn.JvnException {
    return this.id++;
    //OK
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
	
	jvnObjectsFromNames.put(jon, jo);
	jvnNamesFromIds.put(jo.jvnGetObjectId(), jon);
	
    jvnWriteServers.put(jo.jvnGetObjectId(), js);
    jvnReadServers.put(jo.jvnGetObjectId(), new ArrayList<JvnRemoteServer>());
    //OK
  }
  
  /**
  * Get the reference of a JVN object managed by a given JVN server 
  * @param jon : the JVN object name
  * @param js : the remote reference of the JVNServer
  * @throws java.rmi.RemoteException,JvnException
  **/
  public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
  throws java.rmi.RemoteException,jvn.JvnException{
	System.out.println("JvnCoordImpl:jvnLookupObject return object : " + jon);
	return jvnObjectsFromNames.get(jon);
	//OK
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
	   //TODO
	   
	   Serializable object = jvnObjectsFromNames.get(jvnNamesFromIds.get(joi)).jvnGetSharedObject();

	   if (jvnWriteServers.containsKey(joi) && !jvnWriteServers.get(joi).equals(js)) {
		   synchronized (this) {
			   try {
				   System.out.println("Write server has write lock on object " + joi);
				   object = jvnWriteServers.get(joi).jvnInvalidateWriterForReader(joi);
				   jvnWriteServers.remove(joi);
				   jvnObjectsFromNames.get(jvnNamesFromIds.get(joi)).jvnSetSharedObject(object);
			   } catch (ConnectException e) {
				   System.err.println(e.getMessage());
				   jvnWriteServers.remove(joi);

				   jvnObjectsFromNames.get(jvnNamesFromIds.get(joi)).jvnSetSharedObject(object);
			   }
				
		   }
	   }

	   jvnReadServers.get(joi).add(js);

	   System.out.println("==========================");
	   System.out.println("");

	   return object;
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
    //TODO
    
	   Serializable object = jvnObjectsFromNames.get(jvnNamesFromIds.get(joi)).jvnGetSharedObject();

	   if (jvnWriteServers.containsKey(joi) && jvnWriteServers.get(joi) != js) {
		   synchronized (this) {
			   try {
				   System.out.println("JvnCoordImpl:jvnLockWrite jvnWriteServers containsKey joi : " + joi);
				   object = jvnWriteServers.get(joi).jvnInvalidateWriter(joi);
				   jvnObjectsFromNames.get(jvnNamesFromIds.get(joi)).jvnSetSharedObject(object);
			   } catch (ConnectException e) {
				   System.err.println(e.getMessage());
				   jvnWriteServers.remove(joi);
			   }
		   }
	   }

	   for (int i = 0; i < jvnReadServers.get(joi).size(); i++) {
		   if (!jvnReadServers.get(joi).get(i).equals(js)) {
			   try {
				   System.out.println("JvnCoordImpl:jvnLockWrite try to invalidateReader for joi : " + joi);
				   jvnReadServers.get(joi).get(i).jvnInvalidateReader(joi);
				   jvnReadServers.get(joi).remove(i);
				   System.out.println("JvnCoordImpl:jvnLockWrite invalidateReader for joi : " + joi + " Ok");
					
			   } catch (ConnectException e) {
				   jvnReadServers.get(joi).remove(i);
			   }	
		   }
	   }

	   jvnWriteServers.put(joi, js);
	   System.out.println("==========================");
	   System.out.println("");

	   return object;
   }

	/**
	* A JVN server terminates
	* @param js  : the remote reference of the server
	* @throws java.rmi.RemoteException, JvnException
	**/
    public void jvnTerminate(JvnRemoteServer js)
	 throws java.rmi.RemoteException, JvnException {
    	//TODO
    	
    	for (int i = 0; i < jvnWriteServers.size(); i++) {
    		if (jvnWriteServers.get(i).equals(js)) {
				jvnWriteServers.remove(i);
			}
		}

		for (int i = 0; i < jvnReadServers.size(); i++) {
			for (JvnRemoteServer jvns : jvnReadServers.get(i)) {
				if (jvns.equals(js)) {
					jvnReadServers.get(i).remove(js);
				}
			}
		}
   }
}

 
