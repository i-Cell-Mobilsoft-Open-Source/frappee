/*-
 * #%L
 * Frappee
 * %%
 * Copyright (C) 2026 i-Cell Mobilsoft Zrt.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package hu.icellmobilsoft.frappee.hibernate.batch;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hu.icellmobilsoft.coffee.se.api.exception.BaseException;
import hu.icellmobilsoft.coffee.se.api.exception.TechnicalException;
import hu.icellmobilsoft.frappee.hibernate.batch.entity.CompositeKey;
import hu.icellmobilsoft.frappee.hibernate.batch.entity.EmbeddedIdEntity;
import hu.icellmobilsoft.frappee.hibernate.batch.entity.IdClassEntity;
import hu.icellmobilsoft.frappee.hibernate.batch.entity.LongIdEntity;
import hu.icellmobilsoft.frappee.hibernate.batch.entity.StringIdEntity;
import hu.icellmobilsoft.frappee.hibernate.util.HibernateEntityHelper;
import hu.icellmobilsoft.frappee.jpa.batch.enums.Status;

/**
 * Integration test of {@link HibernateBatchService} id handling with real Hibernate on an in-memory H2 database.
 *
 * @author attila.almasi
 * @since 2.2.0
 */
class HibernateBatchServiceH2Test {

    private static EntityManagerFactory emf;

    private EntityManager em;

    private HibernateBatchService batchService;

    @BeforeAll
    static void initEmf() {
        emf = Persistence.createEntityManagerFactory("frappee-batch-test");
    }

    @AfterAll
    static void closeEmf() {
        emf.close();
    }

    @BeforeEach
    void init() {
        em = emf.createEntityManager();
        batchService = new HibernateBatchService(em, new HibernateEntityHelper(em));
        inTransaction(() -> {
            for (Class<?> entityClass : List.of(StringIdEntity.class, LongIdEntity.class, EmbeddedIdEntity.class, IdClassEntity.class)) {
                em.createQuery("delete from " + entityClass.getSimpleName()).executeUpdate();
            }
        });
    }

    @AfterEach
    void close() {
        em.close();
    }

    @Test
    @DisplayName("String id: insert generates id, update and delete work")
    void stringIdTest() throws BaseException {
        // given
        StringIdEntity generated = stringIdEntity(null, "generated");
        StringIdEntity given = stringIdEntity("GIVEN_ID", "given");

        // when
        Map<String, Status> insertResult = inTransaction(() -> batchService.batchInsertNative(List.of(generated, given), StringIdEntity.class));

        // then
        Assertions.assertNotNull(generated.getId());
        assertAllSuccess(insertResult, Set.of(generated.getId(), "GIVEN_ID"));
        Assertions.assertEquals("generated", find(StringIdEntity.class, generated.getId()).getName());

        // when
        StringIdEntity toUpdate = find(StringIdEntity.class, "GIVEN_ID");
        toUpdate.setName("updated");
        Map<String, Status> updateResult = inTransaction(() -> batchService.batchUpdateNative(List.of(toUpdate), StringIdEntity.class));

        // then
        assertAllSuccess(updateResult, Set.of("GIVEN_ID"));
        StringIdEntity updated = find(StringIdEntity.class, "GIVEN_ID");
        Assertions.assertEquals("updated", updated.getName());
        Assertions.assertEquals(1L, updated.getVersion());

        // when
        Map<String, Status> deleteResult = inTransaction(() -> batchService.batchDeleteNative(List.of(updated), StringIdEntity.class));

        // then
        assertAllSuccess(deleteResult, Set.of("GIVEN_ID"));
        Assertions.assertNull(find(StringIdEntity.class, "GIVEN_ID"));
        Assertions.assertNotNull(find(StringIdEntity.class, generated.getId()));
    }

    @Test
    @DisplayName("String id: batchMergeNative inserts new and updates existing entities")
    void stringIdMergeNativeTest() throws BaseException {
        // given
        StringIdEntity existing = stringIdEntity("EXISTING", "existing");
        inTransaction(() -> batchService.batchInsertNative(List.of(existing), StringIdEntity.class));
        StringIdEntity toUpdate = find(StringIdEntity.class, "EXISTING");
        toUpdate.setName("merged");
        StringIdEntity toInsert = stringIdEntity(null, "new");

        // when
        Map<String, Status> result = inTransaction(() -> batchService.batchMergeNative(List.of(toUpdate, toInsert), StringIdEntity.class));

        // then
        assertAllSuccess(result, Set.of("EXISTING", toInsert.getId()));
        Assertions.assertEquals("merged", find(StringIdEntity.class, "EXISTING").getName());
        Assertions.assertEquals("new", find(StringIdEntity.class, toInsert.getId()).getName());
    }

    @Test
    @DisplayName("Long id: insert, update, merge and delete work")
    void longIdTest() throws BaseException {
        // given
        LongIdEntity first = longIdEntity(1L, "first");
        LongIdEntity second = longIdEntity(2L, "second");

        // when
        Map<String, Status> insertResult = inTransaction(() -> batchService.batchInsertNative(List.of(first, second), LongIdEntity.class));

        // then
        assertAllSuccess(insertResult, Set.of("1", "2"));

        // when
        LongIdEntity toUpdate = find(LongIdEntity.class, 1L);
        toUpdate.setName("updated");
        Map<String, Status> updateResult = inTransaction(() -> batchService.batchUpdateNative(List.of(toUpdate), LongIdEntity.class));

        // then
        assertAllSuccess(updateResult, Set.of("1"));
        Assertions.assertEquals("updated", find(LongIdEntity.class, 1L).getName());

        // when
        LongIdEntity toMerge = find(LongIdEntity.class, 2L);
        toMerge.setName("merged");
        Map<String, Status> mergeResult = inTransaction(() -> batchService.batchMergeNative(List.of(toMerge), LongIdEntity.class));

        // then
        assertAllSuccess(mergeResult, Set.of("2"));
        Assertions.assertEquals("merged", find(LongIdEntity.class, 2L).getName());

        // when
        Map<String, Status> deleteResult = inTransaction(
                () -> batchService.batchDeleteNative(List.of(find(LongIdEntity.class, 1L), find(LongIdEntity.class, 2L)), LongIdEntity.class));

        // then
        assertAllSuccess(deleteResult, Set.of("1", "2"));
        Assertions.assertNull(find(LongIdEntity.class, 1L));
        Assertions.assertNull(find(LongIdEntity.class, 2L));
    }

    @Test
    @DisplayName("Long id: insert without id fails, only String ids are generated")
    void longIdInsertWithoutIdTest() {
        // given
        LongIdEntity entity = longIdEntity(null, "no id");

        // when
        TechnicalException exception = Assertions.assertThrows(TechnicalException.class,
                () -> inTransaction(() -> batchService.batchInsertNative(List.of(entity), LongIdEntity.class)));

        // then
        Assertions.assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    @DisplayName("@EmbeddedId: insert, update and delete work")
    void embeddedIdTest() throws BaseException {
        // given
        CompositeKey key1 = new CompositeKey("A", 1L);
        CompositeKey key2 = new CompositeKey("A", 2L);
        EmbeddedIdEntity first = embeddedIdEntity(key1, "first");
        EmbeddedIdEntity second = embeddedIdEntity(key2, "second");

        // when
        Map<String, Status> insertResult = inTransaction(() -> batchService.batchInsertNative(List.of(first, second), EmbeddedIdEntity.class));

        // then
        assertAllSuccess(insertResult, Set.of("A:1", "A:2"));
        Assertions.assertEquals("first", find(EmbeddedIdEntity.class, key1).getName());

        // when
        EmbeddedIdEntity toUpdate = find(EmbeddedIdEntity.class, key1);
        toUpdate.setName("updated");
        Map<String, Status> updateResult = inTransaction(() -> batchService.batchUpdateNative(List.of(toUpdate), EmbeddedIdEntity.class));

        // then
        assertAllSuccess(updateResult, Set.of("A:1"));
        Assertions.assertEquals("updated", find(EmbeddedIdEntity.class, key1).getName());
        Assertions.assertEquals("second", find(EmbeddedIdEntity.class, key2).getName());

        // when
        Map<String, Status> deleteResult = inTransaction(() -> batchService.batchDeleteNative(List.of(find(EmbeddedIdEntity.class, key1)),
                EmbeddedIdEntity.class));

        // then
        assertAllSuccess(deleteResult, Set.of("A:1"));
        Assertions.assertNull(find(EmbeddedIdEntity.class, key1));
        Assertions.assertNotNull(find(EmbeddedIdEntity.class, key2));
    }

    @Test
    @DisplayName("@EmbeddedId: insert with a null id part fails")
    void embeddedIdInsertWithNullPartTest() {
        // given
        EmbeddedIdEntity entity = embeddedIdEntity(new CompositeKey("A", null), "null part");

        // when
        TechnicalException exception = Assertions.assertThrows(TechnicalException.class,
                () -> inTransaction(() -> batchService.batchInsertNative(List.of(entity), EmbeddedIdEntity.class)));

        // then
        Assertions.assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    @DisplayName("@IdClass: insert, update and delete work")
    void idClassTest() throws BaseException {
        // given
        IdClassEntity first = idClassEntity("B", 1L, "first");
        IdClassEntity second = idClassEntity("B", 2L, "second");

        // when
        Map<String, Status> insertResult = inTransaction(() -> batchService.batchInsertNative(List.of(first, second), IdClassEntity.class));

        // then
        assertAllSuccess(insertResult, Set.of("B:1", "B:2"));
        CompositeKey key1 = new CompositeKey("B", 1L);
        CompositeKey key2 = new CompositeKey("B", 2L);
        Assertions.assertEquals("first", find(IdClassEntity.class, key1).getName());

        // when
        IdClassEntity toUpdate = find(IdClassEntity.class, key1);
        toUpdate.setName("updated");
        Map<String, Status> updateResult = inTransaction(() -> batchService.batchUpdateNative(List.of(toUpdate), IdClassEntity.class));

        // then
        assertAllSuccess(updateResult, Set.of("B:1"));
        Assertions.assertEquals("updated", find(IdClassEntity.class, key1).getName());
        Assertions.assertEquals("second", find(IdClassEntity.class, key2).getName());

        // when
        Map<String, Status> deleteResult = inTransaction(
                () -> batchService.batchDeleteNative(List.of(find(IdClassEntity.class, key1)), IdClassEntity.class));

        // then
        assertAllSuccess(deleteResult, Set.of("B:1"));
        Assertions.assertNull(find(IdClassEntity.class, key1));
        Assertions.assertNotNull(find(IdClassEntity.class, key2));
    }

    @Test
    @DisplayName("Composite id: merge methods are rejected")
    void compositeIdMergeRejectedTest() {
        // given
        List<EmbeddedIdEntity> embeddedIdEntities = List.of(embeddedIdEntity(new CompositeKey("C", 1L), "embedded"));
        List<IdClassEntity> idClassEntities = List.of(idClassEntity("C", 1L, "idClass"));

        // when
        TechnicalException mergeNative = Assertions.assertThrows(TechnicalException.class,
                () -> batchService.batchMergeNative(embeddedIdEntities, EmbeddedIdEntity.class));
        TechnicalException merge = Assertions.assertThrows(TechnicalException.class, () -> batchService.batchMerge(idClassEntities));

        // then
        Assertions.assertTrue(mergeNative.getMessage().contains("batchMergeNative"), mergeNative.getMessage());
        Assertions.assertTrue(merge.getMessage().contains("[batchMerge]"), merge.getMessage());
    }

    @Test
    @DisplayName("Long id: batchMerge updates existing entity through stateless session")
    void longIdBatchMergeTest() throws BaseException {
        // given
        inTransaction(() -> batchService.batchInsertNative(List.of(longIdEntity(10L, "original")), LongIdEntity.class));
        LongIdEntity toMerge = find(LongIdEntity.class, 10L);
        toMerge.setName("merged");

        // when
        List<String> ids = inTransaction(() -> batchService.batchMerge(List.of(toMerge)));

        // then
        Assertions.assertEquals(List.of("10"), ids);
        Assertions.assertEquals("merged", find(LongIdEntity.class, 10L).getName());
    }

    private static void assertAllSuccess(Map<String, Status> result, Collection<String> expectedIds) {
        Assertions.assertEquals(Set.copyOf(expectedIds), result.keySet());
        Assertions.assertEquals(Set.of(Status.SUCCESS), result.values().stream().collect(Collectors.toSet()), result.toString());
    }

    private <E> E find(Class<E> entityClass, Object id) {
        em.clear();
        return em.find(entityClass, id);
    }

    private <T> T inTransaction(BatchCall<T> call) throws BaseException {
        em.getTransaction().begin();
        try {
            T result = call.call();
            em.getTransaction().commit();
            return result;
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        }
    }

    private void inTransaction(Runnable runnable) {
        em.getTransaction().begin();
        runnable.run();
        em.getTransaction().commit();
    }

    private static StringIdEntity stringIdEntity(String id, String name) {
        StringIdEntity entity = new StringIdEntity();
        entity.setId(id);
        entity.setName(name);
        return entity;
    }

    private static LongIdEntity longIdEntity(Long id, String name) {
        LongIdEntity entity = new LongIdEntity();
        entity.setId(id);
        entity.setName(name);
        return entity;
    }

    private static EmbeddedIdEntity embeddedIdEntity(CompositeKey id, String name) {
        EmbeddedIdEntity entity = new EmbeddedIdEntity();
        entity.setId(id);
        entity.setName(name);
        return entity;
    }

    private static IdClassEntity idClassEntity(String code, Long seq, String name) {
        IdClassEntity entity = new IdClassEntity();
        entity.setCode(code);
        entity.setSeq(seq);
        entity.setName(name);
        return entity;
    }

    @FunctionalInterface
    private interface BatchCall<T> {
        T call() throws BaseException;
    }
}
