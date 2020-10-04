package jvn;

import java.io.Serializable;

public class JvnObjectImpl implements JvnObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private enum LOCK_STATES {
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
            this.lockState = (LOCK_STATES) js.jvnLockRead(id);
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
            this.lockState = (LOCK_STATES) js.jvnLockRead(id);
            this.lockState = LOCK_STATES.W;
        } else {
            throw new JvnException("Write lock impossible");
        }

    }

    @Override
    public void jvnUnLock() throws JvnException {
        // TODO Auto-generated method stub
        this.lockState = LOCK_STATES.NL;
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

    }

    @Override
    public Serializable jvnInvalidateWriter() throws JvnException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        // TODO Auto-generated method stub
        return null;
    }
    
}
