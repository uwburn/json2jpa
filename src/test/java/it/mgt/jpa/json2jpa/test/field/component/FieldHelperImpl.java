package it.mgt.jpa.json2jpa.test.field.component;

import it.mgt.jpa.json2jpa.test.field.entity.FieldBook;
import it.mgt.jpa.json2jpa.test.field.entity.FieldBookseller;
import it.mgt.jpa.json2jpa.test.field.entity.FieldBookstore;
import it.mgt.jpa.json2jpa.test.field.entity.FieldCompany;
import it.mgt.jpa.json2jpa.test.field.entity.FieldEmployee;
import it.mgt.jpa.json2jpa.test.field.entity.FieldEmploymentContract;
import it.mgt.jpa.json2jpa.test.field.entity.FieldEmploymentContractType;
import it.mgt.jpa.json2jpa.test.field.entity.FieldOperation;
import it.mgt.jpa.json2jpa.test.field.entity.FieldRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;

@Component
public class FieldHelperImpl implements FieldHelper {

    private static final Logger logger = LoggerFactory.getLogger(FieldHelperImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initialize() {
        logger.info("Initializing sample data");

        initializeCompany();
        initializeBookstore();
    }

    private void initializeCompany() {
        if (em.createQuery("SELECT COUNT(c) FROM FieldCompany c", Long.class).getSingleResult() > 0)
            return;

        Date now = new Date();

        FieldCompany acme = new FieldCompany("ACME", "Somewhere", "Hooray", "Na-na-na");
        em.persist(acme);

        FieldRole employee = new FieldRole("employee", acme, FieldOperation.EDIT_PROFILE, FieldOperation.CREATE_DOCUMENTS);
        em.persist(employee);

        FieldRole manager = new FieldRole("manager", acme, FieldOperation.READ_OTHERS_DOCUMENTS, FieldOperation.EDIT_OTHER_DOCUMENTS);
        em.persist(manager);

        FieldRole admin = new FieldRole("admin", acme, FieldOperation.READ_OTHER_PROFILE, FieldOperation.EDIT_OTHER_PROFILE);
        em.persist(admin);

        FieldEmploymentContract contractWile = new FieldEmploymentContract("Wile", "Coyote", "123", FieldEmploymentContractType.CONSULTANT, now, now, 0, acme);
        em.persist(contractWile);

        FieldEmployee wile = contractWile.buildEmployee("wile.coyote@acme.it");
        em.persist(wile);

        wile.addRole(employee)
                .addRole(manager)
                .addRole(admin);

        FieldEmploymentContract contractBugs = new FieldEmploymentContract("Bugs", "Bunny", "456", FieldEmploymentContractType.FULL_TIME, now, now, 0, acme);
        em.persist(contractBugs);

        FieldEmployee bugs = contractBugs.buildEmployee("bugs.bunny@acme.it");
        em.persist(bugs);

        bugs.addRole(employee)
                .addRole(manager);

        FieldEmploymentContract contractBeep = new FieldEmploymentContract("Beep", "Beep", "789", FieldEmploymentContractType.FIXED_TERM, now, now, 0, acme);
        em.persist(contractBeep);

        FieldEmployee beep = contractBeep.buildEmployee("beep.beep@acme.it");
        em.persist(beep);

        beep.addRole(employee);

        em.flush();
        em.clear();
    }

    private void initializeBookstore() {
        FieldBookstore mousetonStore = em.createNamedQuery("FieldBookstore.findByName", FieldBookstore.class)
                .setParameter("name", "Mouseton Store")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    FieldBookstore store = new FieldBookstore("Mouseton Store");
                    em.persist(store);
                    return store;
                });

        FieldBookseller mickeyMouse = em.createNamedQuery("FieldBookseller.findByName", FieldBookseller.class)
                .setParameter("firstName", "Mickey")
                .setParameter("lastName", "Mouse")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    FieldBookseller seller = new FieldBookseller("Mickey", "Mouse");
                    em.persist(seller);
                    return seller;
                });

        FieldBookseller goofyGoof = em.createNamedQuery("FieldBookseller.findByName", FieldBookseller.class)
                .setParameter("firstName", "Goofy")
                .setParameter("lastName", "Goof")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    FieldBookseller seller = new FieldBookseller("Goofy", "Goof");
                    em.persist(seller);
                    return seller;
                });

        mousetonStore.setDirector(mickeyMouse);
        mickeyMouse.setEmployingBookstore(mousetonStore);
        mousetonStore.getEmployees().add(mickeyMouse);
        mickeyMouse.setDirectedBookstore(mousetonStore);
        goofyGoof.setEmployingBookstore(mousetonStore);
        mousetonStore.getEmployees().add(goofyGoof);

        FieldBookstore duckburgStore = em.createNamedQuery("FieldBookstore.findByName", FieldBookstore.class)
                .setParameter("name", "Duckburg Store")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    FieldBookstore store = new FieldBookstore("Duckburg Store");
                    em.persist(store);
                    return store;
                });

        FieldBookseller scrooge = em.createNamedQuery("FieldBookseller.findByName", FieldBookseller.class)
                .setParameter("firstName", "Scrooge")
                .setParameter("lastName", "McDuck")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    FieldBookseller seller = new FieldBookseller("Scrooge", "McDuck");
                    em.persist(seller);
                    return seller;
                });

        FieldBookseller donald = em.createNamedQuery("FieldBookseller.findByName", FieldBookseller.class)
                .setParameter("firstName", "Donald")
                .setParameter("lastName", "McDuck")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    FieldBookseller seller = new FieldBookseller("Donald", "McDuck");
                    em.persist(seller);
                    return seller;
                });

        duckburgStore.setDirector(scrooge);
        scrooge.setEmployingBookstore(duckburgStore);
        duckburgStore.getEmployees().add(scrooge);
        scrooge.setDirectedBookstore(duckburgStore);
        donald.setEmployingBookstore(duckburgStore);
        duckburgStore.getEmployees().add(donald);

        FieldBook topolino = em.createNamedQuery("FieldBook.findByIsbn", FieldBook.class)
                .setParameter("isbn", "001")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    FieldBook book = new FieldBook("Topolino", "001", 4D);
                    em.persist(book);
                    return book;
                });

        FieldBook mega2000 = em.createNamedQuery("FieldBook.findByIsbn", FieldBook.class)
                .setParameter("isbn", "002")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    FieldBook book = new FieldBook("Mega 2000", "002", 5D);
                    em.persist(book);
                    return book;
                });

        FieldBook grandiClassici = em.createNamedQuery("FieldBook.findByIsbn", FieldBook.class)
                .setParameter("isbn", "003")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    FieldBook book = new FieldBook("I Grandi Classici", "003", 6.5D);
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