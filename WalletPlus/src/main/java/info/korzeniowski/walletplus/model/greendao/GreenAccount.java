package info.korzeniowski.walletplus.model.greendao;

import java.util.List;

import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table Account.
 */
public class GreenAccount {

    private Long id;
    /** Not-null value. */
    private String name;
    private String passwordHash;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient GreenAccountDao myDao;

    private List<GreenCategory> greenCategoryList;
    private List<GreenWallet> greenWalletList;

    public GreenAccount() {
    }

    public GreenAccount(Long id) {
        this.id = id;
    }

    public GreenAccount(Long id, String name, String passwordHash) {
        this.id = id;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getGreenAccountDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getName() {
        return name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name) {
        this.name = name;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<GreenCategory> getGreenCategoryList() {
        if (greenCategoryList == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            GreenCategoryDao targetDao = daoSession.getGreenCategoryDao();
            List<GreenCategory> greenCategoryListNew = targetDao._queryGreenAccount_GreenCategoryList(id);
            synchronized (this) {
                if(greenCategoryList == null) {
                    greenCategoryList = greenCategoryListNew;
                }
            }
        }
        return greenCategoryList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetGreenCategoryList() {
        greenCategoryList = null;
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<GreenWallet> getGreenWalletList() {
        if (greenWalletList == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            GreenWalletDao targetDao = daoSession.getGreenWalletDao();
            List<GreenWallet> greenWalletListNew = targetDao._queryGreenAccount_GreenWalletList(id);
            synchronized (this) {
                if(greenWalletList == null) {
                    greenWalletList = greenWalletListNew;
                }
            }
        }
        return greenWalletList;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetGreenWalletList() {
        greenWalletList = null;
    }

    /** Convenient call for {@link AbstractDao#delete(Object)}. Entity must attached to an entity context. */
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.delete(this);
    }

    /** Convenient call for {@link AbstractDao#update(Object)}. Entity must attached to an entity context. */
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.update(this);
    }

    /** Convenient call for {@link AbstractDao#refresh(Object)}. Entity must attached to an entity context. */
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }    
        myDao.refresh(this);
    }

}