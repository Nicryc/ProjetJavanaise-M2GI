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
	public void setObject(Serializable obj) {
		this.obj = obj;
    }
    
    @Override
	public Serializable getObject() throws JvnException {
		return this.obj;
	}

    @Override
    public void jvnLockRead() throws JvnException {
        // TODO Auto-generated method stub

        if (this.lockState == LOCK_STATES.NL) {
            JvnServerImpl js = JvnServerImpl.jvnGetServer();
            setObject(js.jvnLockWrite(this.jvnGetObjectId()));
            this.lockState = LOCK_STATES.R;
        } else {
            throw new JvnException("Read lock impossible");
        }

    }

    @Override
    public void jvnLockWrite() throws JvnException {
        // TODO Auto-generated method stub

        if (this.lockState == LOCK_STATES.NL) {
            JvnServerImpl js = JvnServerImpl.jvnGetServer();
            setObject(js.jvnLockWrite(this.jvnGetObjectId()));
            this.lockState = LOCK_STATES.W;
        } else {
            throw new JvnException("Write lock impossible");
        }

    }

    @Override
    public void jvnUnLock() throws JvnException {
        // TODO Auto-generated method stub
        lockState = LOCK_STATES.NL;
    }

    @Override
    public int jvnGetObjectId() throws JvnException {
        // TODO Auto-generated method stub
        return id;
    }

    @Override
    public Serializable jvnGetSharedObject() throws JvnException {
        // TODO Auto-generated method stub
        return obj;
    }

    @Override
    public void jvnInvalidateReader() throws JvnException {
        // TODO Auto-generated method stub
        lockState = LOCK_STATES.NL ;
    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        // TODO Auto-generated method stub
        lockState = LOCK_STATES.NL ;
        return obj;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        // TODO Auto-generated method stub
        lockState = LOCK_STATES.R ;
        return obj;
    }

    @Override
    public void setState(LOCK_STATES lockState) {
        this.lockState = lockState;
    }

    /*@Override
    public void free() {
        lockState = LOCK_STATES.NL;
        return null;
    }
    */
}
