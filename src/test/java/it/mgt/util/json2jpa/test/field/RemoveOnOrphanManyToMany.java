package it.mgt.util.json2jpa.test.field;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import it.mgt.util.json2jpa.Json2Jpa;
import it.mgt.util.json2jpa.Json2JpaFactory;
import it.mgt.util.json2jpa.test.config.SpringContext;
import it.mgt.util.json2jpa.test.config.StandaloneDataConfig;
import it.mgt.util.json2jpa.test.field.component.FieldHelper;
import it.mgt.util.json2jpa.test.field.entity.FieldBook;
import it.mgt.util.json2jpa.test.field.entity.FieldBookseller;
import it.mgt.util.json2jpa.test.field.entity.FieldBookstore;
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
    private FieldHelper helper;

    @Before
    @Transactional
    public void before() {
        helper.initialize();
    }

    @Test
    @Transactional
    public void test()  {
        logger.info("Testing remove on orphan many-to-many");

        List<FieldBookstore> stores = em.createNamedQuery("FieldBookstore.findAll", FieldBookstore.class)
                .getResultList();

        List<FieldBookseller> sellers = em.createNamedQuery("FieldBookseller.findAll", FieldBookseller.class)
                .getResultList();

        List<FieldBook> books = em.createNamedQuery("FieldBook.findAll", FieldBook.class)
                .getResultList();

        FieldBookstore mousetonStore = em.createNamedQuery("FieldBookstore.findByName", FieldBookstore.class)
                .setParameter("name", "Mouseton Store")
                .getSingleResult();

        FieldBookstore duckburgSrote = em.createNamedQuery("FieldBookstore.findByName", FieldBookstore.class)
                .setParameter("name", "Duckburg Store")
                .getSingleResult();

        FieldBook mega2000 = em.createNamedQuery("FieldBook.findByIsbn", FieldBook.class)
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
        for (FieldBook b : mousetonStore.getBooks())
            if (b.equals(mega2000))
                continue;
            else
                booksJson.add(b.getId());

        Json2Jpa json2Jpa = json2JpaFactory.build();
        json2Jpa.merge(mousetonStore, json);

        em.flush();
        em.clear();

        mousetonStore = em.createNamedQuery("FieldBookstore.findByName", FieldBookstore.class)
                .setParameter("name", "Mouseton Store")
                .getSingleResult();

        duckburgSrote = em.createNamedQuery("FieldBookstore.findByName", FieldBookstore.class)
                .setParameter("name", "Duckburg Store")
                .getSingleResult();

        mega2000 = em.createNamedQuery("FieldBook.findByIsbn", FieldBook.class)
                .setParameter("isbn", "002")
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

        Assert.assertNull(mega2000);
        Assert.assertEquals(mousetonStoreInitialBooksCount - 1, mousetonStore.getBooks().size());
        Assert.assertEquals(duckburgStoreInitialBooksCount - 1, duckburgSrote.getBooks().size());
        Assert.assertEquals(initialStoresCount, stores.size());
        Assert.assertEquals(initialSellersCount, sellers.size());
        Assert.assertEquals(initialBooksCount - 1, books.size());
    }

}
