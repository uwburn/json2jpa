package it.mgt.jpa.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.jpa.json2jpa.Json2Jpa;
import it.mgt.jpa.json2jpa.Json2JpaFactory;
import it.mgt.jpa.json2jpa.test.config.SpringContext;
import it.mgt.jpa.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.jpa.json2jpa.test.field.component.FieldHelper;
import it.mgt.jpa.json2jpa.test.field.entity.FieldBook;
import it.mgt.jpa.json2jpa.test.field.entity.FieldBookseller;
import it.mgt.jpa.json2jpa.test.field.entity.FieldBookstore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;


@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( classes = {StandaloneDataConfig.class, SpringContext.class })
public class RemoveOnOrphanOneToOne {

    private static final Logger logger = LoggerFactory.getLogger(RemoveOnOrphanOneToOne.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private FieldHelper helper;

    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing remove on orphan one-to-one");

        List<FieldBookstore> stores = em.createNamedQuery("FieldBookstore.findAll", FieldBookstore.class)
                .getResultList();

        List<FieldBookseller> sellers = em.createNamedQuery("FieldBookseller.findAll", FieldBookseller.class)
                .getResultList();

        List<FieldBook> books = em.createNamedQuery("FieldBook.findAll", FieldBook.class)
                .getResultList();

        FieldBookstore mousetonStore = em.createNamedQuery("FieldBookstore.findByName", FieldBookstore.class)
                .setParameter("name", "Mouseton Store")
                .getSingleResult();

        int initialEmployeesCount = mousetonStore.getEmployees().size();
        int initialStoresCount = stores.size();
        int initialSellersCount = sellers.size();
        int initialBooksCount = books.size();

        ObjectNode json = objectMapper.createObjectNode();
        json.putNull("director");
        ArrayNode employeesJson = json.putArray("employees");
        for (FieldBookseller s : mousetonStore.getEmployees())
            if (mousetonStore.getDirector().equals(s))
                continue;
            else
                employeesJson.add(s.getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(mousetonStore, json);

        em.flush();
        em.clear();

        mousetonStore = em.createNamedQuery("FieldBookstore.findByName", FieldBookstore.class)
                .setParameter("name", "Mouseton Store")
                .getSingleResult();

        FieldBookseller mickeyMouse = em.createNamedQuery("FieldBookseller.findByName", FieldBookseller.class)
                .setParameter("firstName", "Mickey")
                .setParameter("lastName", "Mouse")
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);

        stores = em.createNamedQuery("FieldBookstore.findAll", FieldBookstore.class)
                .getResultList();

        sellers = em.createNamedQuery("FieldBookseller.findAll", FieldBookseller.class)
                .getResultList();

        books = em.createNamedQuery("FieldBook.findAll", FieldBook.class)
                .getResultList();

        Assert.assertNull(mousetonStore.getDirector());
        Assert.assertNull(mickeyMouse);
        Assert.assertEquals(initialEmployeesCount - 1, mousetonStore.getEmployees().size());
        Assert.assertEquals(initialStoresCount, stores.size());
        Assert.assertEquals(initialSellersCount - 1, sellers.size());
        Assert.assertEquals(initialBooksCount, books.size());
    }

}
