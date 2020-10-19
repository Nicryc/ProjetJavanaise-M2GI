package jvn;

import java.io.Serializable;
import java.lang.reflect.Constructor;

public class JvnObjectImpl implements JvnObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public enum LOCK_STATES {
        NL, // No Lock
        R,  // Read
        W,  // Write
        RC, // Read Cache
        WC, // Write Cache
        RWC //Read Write Cache
    }

    private LOCK_STATES lockState = LOCK_STATES.NL;
    private int id = 0;
    private Serializable obj = null;


    public JvnObjectImpl(Serializable o, int joi) {
        super();
        obj = o;
		id = joi;
    }

    @Override
	public void setObject(Serializable obj) throws JvnException {
		this.obj = obj;
    }
    
    @Override
	public Serializable getObject() throws JvnException {
		return this.obj;
	}

    @Override
    public void jvnLockRead() throws JvnException {
        switch (lockState) {
		case NL:
			obj = JvnServerImpl.jvnGetServer().jvnLockRead(jvnGetObjectId());
            lockState = LOCK_STATES.R;
			break;
		case RC:
			lockState = LOCK_STATES.R;
			break;
		case WC:
			lockState = LOCK_STATES.RWC;
			break;
		case R:
			break;
		case W:
			break;
		case RWC:
			break;
			
		default:
			lockState = LOCK_STATES.NL;
			break;
		}

    }

    @Override
    public void jvnLockWrite() throws JvnException {
        switch (lockState) {
		case NL:
			obj = JvnServerImpl.jvnGetServer().jvnLockWrite(jvnGetObjectId());
            lockState = LOCK_STATES.W;
			break;
		case RC:
			obj = JvnServerImpl.jvnGetServer().jvnLockWrite(jvnGetObjectId());
            lockState = LOCK_STATES.W;
			break;
		case WC:
			lockState = LOCK_STATES.W;
			break;
		case R:
			obj = JvnServerImpl.jvnGetServer().jvnLockWrite(jvnGetObjectId());
            lockState = LOCK_STATES.W;
			break;
		case W:
			lockState = LOCK_STATES.W;
			break;
		case RWC:
			lockState = LOCK_STATES.W;
			break;

		default:
			lockState = LOCK_STATES.NL;
			break;
        }

    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {      
        switch (lockState) {
		case NL:
			break;
		case RC:
			lockState = LOCK_STATES.NL;
			break;
		case WC:
			lockState = LOCK_STATES.NL;
			break;
		case R:
			lockState = LOCK_STATES.RC;
			break;
		case W:
			lockState = LOCK_STATES.WC;
			break;
		case RWC:
			lockState = LOCK_STATES.WC;
			break;
			
		default:
			throw new JvnException("JvnObjectImpl : Impossible de Unlock");
		}
        
        notifyAll();
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return this.id;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return getObject();
    }
    
    @Override
	public JvnObject jvnSetSharedObject(Serializable obj) throws JvnException {
		setObject(obj);
		return this;
    }

    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
		while (lockState == LOCK_STATES.R) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println("JvnObjectImpl : Impossible de InvalidateReader : " + e);			
			}
		}
        jvnUnLock();
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
		while (lockState == LOCK_STATES.W) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println("JvnObjectImpl : Impossible de InvalidateWriter : " + e);			
			}
		}
        jvnUnLock();
        
        return obj;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		while (lockState == LOCK_STATES.W) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println("JvnObjectImpl : Impossible de InvalidateWriterForReader : " + e);			
			}
		}
        jvnUnLock();
        
        return obj;
    }

    @Override
    public void setState(LOCK_STATES lockState) {
        this.lockState = lockState;
    }
}
