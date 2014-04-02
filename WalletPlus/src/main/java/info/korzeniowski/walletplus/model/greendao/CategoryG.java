package info.korzeniowski.walletplus.model.greendao;

import java.util.List;
import info.korzeniowski.walletplus.model.greendao.DaoSession;
import de.greenrobot.dao.DaoException;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
import java.util.ArrayList;
import info.korzeniowski.walletplus.model.Category;
// KEEP INCLUDES END
/**
 * Entity mapped to table Category.
 */
public class CategoryG {

    private Long id;
    private Long parentId;
    /** Not-null value. */
    private String name;
    private Integer type;

    /** Used to resolve relations */
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    private transient CategoryGDao myDao;

    private CategoryG parent;
    private Long parent__resolvedKey;

    private List<CategoryG> children;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public CategoryG() {
    }

    public CategoryG(Long id) {
        this.id = id;
    }

    public CategoryG(Long id, Long parentId, String name, Integer type) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
    }

    /** called by internal mechanisms, do not call yourself. */
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getCategoryGDao() : null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    /** Not-null value. */
    public String getName() {
        return name;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    /** To-one relationship, resolved on first access. */
    public CategoryG getParent() {
        Long __key = this.parentId;
        if (parent__resolvedKey == null || !parent__resolvedKey.equals(__key)) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CategoryGDao targetDao = daoSession.getCategoryGDao();
            CategoryG parentNew = targetDao.load(__key);
            synchronized (this) {
                parent = parentNew;
            	parent__resolvedKey = __key;
            }
        }
        return parent;
    }

    public void setParent(CategoryG parent) {
        synchronized (this) {
            this.parent = parent;
            parentId = parent == null ? null : parent.getId();
            parent__resolvedKey = parentId;
        }
    }

    /** To-many relationship, resolved on first access (and after reset). Changes to to-many relations are not persisted, make changes to the target entity. */
    public List<CategoryG> getChildren() {
        if (children == null) {
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            CategoryGDao targetDao = daoSession.getCategoryGDao();
            List<CategoryG> childrenNew = targetDao._queryCategoryG_Children(id);
            synchronized (this) {
                if(children == null) {
                    children = childrenNew;
                }
            }
        }
        return children;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    public synchronized void resetChildren() {
        children = null;
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

    // KEEP METHODS - put your custom methods here

    //TODO: ugly pice of code, fix?
    public CategoryG(Category category) {
        if (category == null) return;

        setId(category.getId());
        setName(category.getName());
        setParentId(category.getParentId());
        setType(Category.Type.convertEnumToBitwise(category.getTypes()));
    }

    public static Category toCategory(CategoryG categoryG) {
        if (categoryG == null) return null;

        if (categoryG.getParent() == null) {
            Category category = toCategoryWithoutNavigations(categoryG);
            copyChildren(category, categoryG);
            return category;
        } else {
            Category parent = toCategory(categoryG.getParent());
            return copyChildren(parent, categoryG.getParent(), categoryG.getId());

        }
    }

    private static Category copyChildren(Category parent, CategoryG parentG, Long id) {
        List<Category> children = new ArrayList<Category>();
        List<CategoryG> childrenG = parentG.getChildren();
        Category result = null;
        for(CategoryG childG : childrenG) {
            Category child = toCategoryWithoutNavigations(childG);
            child.setParent(parent);
            child.setParentId(parent.getId());
            children.add(child);
            if (child.getId().equals(id)) {
                result = child;
            }
        }
        parent.setChildren(children);
        return result;
    }

    private static void copyChildren(Category parent, CategoryG parentG) {
        List<Category> children = new ArrayList<Category>();
        List<CategoryG> childrenG = parentG.getChildren();
        for(CategoryG childG : childrenG) {
            Category child = toCategoryWithoutNavigations(childG);
            child.setParent(parent);
            child.setParentId(parent.getId());
            children.add(child);
        }
        parent.setChildren(children);
    }

    private static Category toCategoryWithoutNavigations(CategoryG categoryG) {
        if (categoryG != null) {
            Category result = new Category();
            result.setId(categoryG.getId());
            result.setName(categoryG.getName());
            result.setTypes(Category.Type.convertBitwiseToEnumSet(categoryG.getType()));
            return result;
        }
        return null;
    }
    // KEEP METHODS END

}
