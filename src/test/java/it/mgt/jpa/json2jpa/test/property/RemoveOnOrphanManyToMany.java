package it.mgt.jpa.json2jpa.test.property;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.jpa.json2jpa.Json2Jpa;
import it.mgt.jpa.json2jpa.Json2JpaFactory;
import it.mgt.jpa.json2jpa.test.config.SpringContext;
import it.mgt.jpa.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.jpa.json2jpa.test.property.component.PropertyHelper;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyBook;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyBookseller;
import it.mgt.jpa.json2jpa.test.property.entity.PropertyBookstore;
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
public class RemoveOnOrphanManyToMany {

    private static final Logger logger = LoggerFactory.getLogger(RemoveOnOrphanManyToMany.class);

    @PersistenceContext
    private EntityManager em;

	@Autowired
	private ObjectMapper objectMapper;

    @Autowired
    private Json2JpaFactory json2JpaFactory;

	@Autowired
    private PropertyHelper helper;

    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing remove on orphan many-to-many");

        List<PropertyBookstore> stores = em.createNamedQuery("PropertyBookstore.findAll", PropertyBookstore.class)
                .getResultList();

        List<PropertyBookseller> sellers = em.createNamedQuery("PropertyBookseller.findAll", PropertyBookseller.class)
                .getResultList();

        List<PropertyBook> books = em.createNamedQuery("PropertyBook.findAll", PropertyBook.class)
                .getResultList();

        PropertyBookstore mousetonStore = em.createNamedQuery("PropertyBookstore.findByName", PropertyBookstore.class)
                .setParameter("name", "Mouseton Store")
                .getSingleResult();

        PropertyBookstore duckburgSrote = em.createNamedQuery("PropertyBookstore.findByName", PropertyBookstore.class)
                .setParameter("name", "Duckburg Store")
                .getSingleResult();

        PropertyBook mega2000 = em.createNamedQuery("PropertyBook.findByIsbn", PropertyBook.class)
                .setParameter("isbn", "002")
                .getSingleResult();

        int duckburgStoreInitialBooksCount = duckburgSrote.getBooks().size();
        int mousetonStoreInitialBooksCount = mousetonStore.getBooks().size();

        duckburgSrote.getBooks().remove(mega2000);

        int initialStoresCount = stores.size();
        int initialSellersCount = sellers.size();
        int initialBooksCount = books.size();

        ObjectNode json = objectMapper.createObjectNode();
        ArrayNode booksJson = json.putArray("books");
        for (PropertyBook b : mousetonStore.getBooks())
            if (b.equals(mega2000))
                continue;
            else
                booksJson.add(b.getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(mousetonStore, json);

        em.flush();
        em.clear();

        mousetonStore = em.createNamedQuery("PropertyBookstore.findByName", PropertyBookstore.class)
                .setParameter("name", "Mouseton Store")
                .getSingleResult();

        duckburgSrote = em.createNamedQuery("PropertyBookstore.findByName", PropertyBookstore.class)
                .setParameter("name", "Duckburg Store")
                .getSingleResult();

        mega2000 = em.createNamedQuery("PropertyBook.findByIsbn", PropertyBook.class)
                .setParameter("isbn", "002")
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);

        stores = em.createNamedQuery("PropertyBookstore.findAll", PropertyBookstore.class)
                .getResultList();

        sellers = em.createNamedQuery("PropertyBookseller.findAll", PropertyBookseller.class)
                .getResultList();

        books = em.createNamedQuery("PropertyBook.findAll", PropertyBook.class)
                .getResultList();

        Assert.assertNull(mega2000);
        Assert.assertEquals(mousetonStoreInitialBooksCount - 1, mousetonStore.getBooks().size());
        Assert.assertEquals(duckburgStoreInitialBooksCount - 1, duckburgSrote.getBooks().size());
        Assert.assertEquals(initialStoresCount, stores.size());
        Assert.assertEquals(initialSellersCount, sellers.size());
        Assert.assertEquals(initialBooksCount - 1, books.size());
    }

}
