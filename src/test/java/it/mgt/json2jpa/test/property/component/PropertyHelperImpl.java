package it.mgt.json2jpa.test.property.component;

import it.mgt.json2jpa.test.property.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;

@Component
public class PropertyHelperImpl implements PropertyHelper {

    private static final Logger logger = LoggerFactory.getLogger(PropertyHelperImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initialize() {
        logger.info("Initializing sample data");

        initializeCompany();
        initializeBookstore();
    }

    private void initializeCompany() {
        if (em.createQuery("SELECT COUNT(c) FROM PropertyCompany c", Long.class).getSingleResult() > 0)
            return;

        Date now = new Date();

        PropertyCompany mgt = new PropertyCompany("ACME", "Somewhere", "Hooray", "Na-na-na");
        em.persist(mgt);

        PropertyRole employee = new PropertyRole("employee", mgt, PropertyOperation.EDIT_PROFILE, PropertyOperation.CREATE_DOCUMENTS);
        em.persist(employee);

        PropertyRole manager = new PropertyRole("manager", mgt, PropertyOperation.READ_OTHERS_DOCUMENTS, PropertyOperation.EDIT_OTHER_DOCUMENTS);
        em.persist(manager);

        PropertyRole admin = new PropertyRole("admin", mgt, PropertyOperation.READ_OTHER_PROFILE, PropertyOperation.EDIT_OTHER_PROFILE);
        em.persist(admin);

        PropertyEmploymentContract contractWile = new PropertyEmploymentContract("Wile", "Coyote", "123", PropertyEmploymentContractType.CONSULTANT, now, now, 0, mgt);
        em.persist(contractWile);

        PropertyEmployee wile = contractWile.buildEmployee("wile.coyote@acme.it");
        em.persist(wile);

        wile.addRole(employee)
                .addRole(manager)
                .addRole(admin);

        PropertyEmploymentContract contractBugs = new PropertyEmploymentContract("Bugs", "Bunny", "456", PropertyEmploymentContractType.FULL_TIME, now, now, 0, mgt);
        em.persist(contractBugs);

        PropertyEmployee bugs = contractBugs.buildEmployee("bugs.bunny@acme.it");
        em.persist(bugs);

        bugs.addRole(employee)
                .addRole(manager);

        PropertyEmploymentContract contractBeep = new PropertyEmploymentContract("Beep", "Beep", "789", PropertyEmploymentContractType.FIXED_TERM, now, now, 0, mgt);
        em.persist(contractBeep);

        PropertyEmployee beep = contractBeep.buildEmployee("beep.beep@acme.it");
        em.persist(beep);

        beep.addRole(employee);

        em.flush();
        em.clear();
    }

    private void initializeBookstore() {
        PropertyBookstore mousetonStore = em.createNamedQuery("PropertyBookstore.findByName", PropertyBookstore.class)
                .setParameter("name", "Mouseton Store")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PropertyBookstore store = new PropertyBookstore("Mouseton Store");
                    em.persist(store);
                    return store;
                });

        PropertyBookseller mickeyMouse = em.createNamedQuery("PropertyBookseller.findByName", PropertyBookseller.class)
                .setParameter("firstName", "Mickey")
                .setParameter("lastName", "Mouse")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PropertyBookseller seller = new PropertyBookseller("Mickey", "Mouse");
                    em.persist(seller);
                    return seller;
                });

        PropertyBookseller goofyGoof = em.createNamedQuery("PropertyBookseller.findByName", PropertyBookseller.class)
                .setParameter("firstName", "Goofy")
                .setParameter("lastName", "Goof")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PropertyBookseller seller = new PropertyBookseller("Goofy", "Goof");
                    em.persist(seller);
                    return seller;
                });

        mousetonStore.setDirector(mickeyMouse);
        mickeyMouse.setEmployingBookstore(mousetonStore);
        mousetonStore.getEmployees().add(mickeyMouse);
        mickeyMouse.setDirectedBookstore(mousetonStore);
        goofyGoof.setEmployingBookstore(mousetonStore);
        mousetonStore.getEmployees().add(goofyGoof);

        PropertyBookstore duckburgStore = em.createNamedQuery("PropertyBookstore.findByName", PropertyBookstore.class)
                .setParameter("name", "Duckburg Store")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PropertyBookstore store = new PropertyBookstore("Duckburg Store");
                    em.persist(store);
                    return store;
                });

        PropertyBookseller scrooge = em.createNamedQuery("PropertyBookseller.findByName", PropertyBookseller.class)
                .setParameter("firstName", "Scrooge")
                .setParameter("lastName", "McDuck")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PropertyBookseller seller = new PropertyBookseller("Scrooge", "McDuck");
                    em.persist(seller);
                    return seller;
                });

        PropertyBookseller donald = em.createNamedQuery("PropertyBookseller.findByName", PropertyBookseller.class)
                .setParameter("firstName", "Donald")
                .setParameter("lastName", "McDuck")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PropertyBookseller seller = new PropertyBookseller("Donald", "McDuck");
                    em.persist(seller);
                    return seller;
                });

        duckburgStore.setDirector(scrooge);
        scrooge.setEmployingBookstore(duckburgStore);
        duckburgStore.getEmployees().add(scrooge);
        scrooge.setDirectedBookstore(duckburgStore);
        donald.setEmployingBookstore(duckburgStore);
        duckburgStore.getEmployees().add(donald);

        PropertyBook topolino = em.createNamedQuery("PropertyBook.findByIsbn", PropertyBook.class)
                .setParameter("isbn", "001")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PropertyBook book = new PropertyBook("Topolino", "001", 4D);
                    em.persist(book);
                    return book;
                });

        PropertyBook mega2000 = em.createNamedQuery("PropertyBook.findByIsbn", PropertyBook.class)
                .setParameter("isbn", "002")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PropertyBook book = new PropertyBook("Mega 2000", "002", 5D);
                    em.persist(book);
                    return book;
                });

        PropertyBook grandiClassici = em.createNamedQuery("PropertyBook.findByIsbn", PropertyBook.class)
                .setParameter("isbn", "003")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    PropertyBook book = new PropertyBook("I Grandi Classici", "003", 6.5D);
                    em.persist(book);
                    return book;
                });

        mousetonStore.getBooks().add(topolino);
        mousetonStore.getBooks().add(mega2000);
        mousetonStore.getBooks().add(grandiClassici);
        duckburgStore.getBooks().add(topolino);
        duckburgStore.getBooks().add(mega2000);
        duckburgStore.getBooks().add(grandiClassici);

        topolino.getBookstores().add(mousetonStore);
        topolino.getBookstores().add(duckburgStore);
        mega2000.getBookstores().add(mousetonStore);
        mega2000.getBookstores().add(duckburgStore);
        grandiClassici.getBookstores().add(mousetonStore);
        grandiClassici.getBookstores().add(duckburgStore);

        em.flush();
        em.clear();
    }

}