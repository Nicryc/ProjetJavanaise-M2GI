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
    
    public JvnObjectImpl(Serializable o, int joi, LOCK_STATES state) {
        super();
        obj = o;
		id = joi;
		lockState = state ;
    }

    @Override
	public JvnObject setObject(Serializable obj) throws JvnException {
		this.obj = obj;
		return this;
    }
    
    @Override
	public Serializable getObject() throws JvnException {
		return this.obj;
	}

    @Override
    public void jvnLockRead() throws JvnException {
        switch (this.lockState) {
		case NL:
			this.obj = JvnServerImpl.jvnGetServer().jvnLockRead(this.jvnGetObjectId());
            this.lockState = LOCK_STATES.R;
			break;
		case RC:
			this.lockState = LOCK_STATES.R;
			break;
		case WC:
			this.lockState = LOCK_STATES.RWC;
			break;
		case R:
			break;
		case W:
			break;
		case RWC:
			break;
		default:
			this.lockState = LOCK_STATES.NL;
			break;
		}

    }

    @Override
    public void jvnLockWrite() throws JvnException {
        switch (this.lockState) {
		case NL:
			this.obj = JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnGetObjectId());
            this.lockState = LOCK_STATES.W;
			break;
		case RC:
			this.obj = JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnGetObjectId());
            this.lockState = LOCK_STATES.W;
			break;
		case WC:
			this.lockState = LOCK_STATES.W;
			break;
		case R:
			this.obj = JvnServerImpl.jvnGetServer().jvnLockWrite(this.jvnGetObjectId());
            this.lockState = LOCK_STATES.W;
			break;
		case W:
			this.lockState = LOCK_STATES.W;
			break;
		case RWC:
			this.lockState = LOCK_STATES.W;
			break;

		default:
			this.lockState = LOCK_STATES.NL;
			break;
        }

    }

    @Override
    public synchronized void jvnUnLock() throws JvnException {
        lockState = LOCK_STATES.NL;
        
        switch (this.lockState) {
		case NL:
			break;
		case RC:
			this.lockState = LOCK_STATES.NL;
			break;
		case WC:
			this.lockState = LOCK_STATES.NL;
			break;
		case R:
			this.lockState = LOCK_STATES.RC;
			break;
		case W:
			this.lockState = LOCK_STATES.WC;
			break;
		case RWC:
			this.lockState = LOCK_STATES.WC;
			break;
			
		default:
			throw new JvnException("Erreur de verrou");
		}
        
        notifyAll();
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        return id;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        return obj;
    }

    @Override
    public synchronized void jvnInvalidateReader() throws JvnException {
		while (this.lockState == LOCK_STATES.R) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();			
			}
		}
        
        this.jvnUnLock();
    }

    @Override
    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
		while (this.lockState == LOCK_STATES.W) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();			
			}
		}
        
        this.jvnUnLock();
        return obj;
    }

    @Override
    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
		while (this.lockState == LOCK_STATES.W) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();			
			}
		}
        
        this.jvnUnLock();
        return obj;
    }

    @Override
    public void setState(LOCK_STATES lockState) {
        this.lockState = lockState;
    }
}
