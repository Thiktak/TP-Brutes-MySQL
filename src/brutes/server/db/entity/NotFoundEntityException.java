package brutes.server.db.entity;

/**
 *
 * @author Olivares Georges <dev@olivares-georges.fr>
 */
public class NotFoundEntityException extends Exception {

    Class cType;
    
    public NotFoundEntityException(Class c) {
        super();
        this.cType = c;
    }
    
    public Class getClassType() {
        return this.cType;
    }

    public boolean getClassTypeIs(Class aClass) {
        return this.getClassType().equals(aClass);
    }
}
