/*-
 * #%L
 * Frappee
 * %%
 * Copyright (C) 2024 i-Cell Mobilsoft Zrt.
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

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.YearMonth;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import jakarta.persistence.EntityManager;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.type.internal.BasicTypeImpl;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.type.BasicType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.ConvertedBasicType;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.converter.spi.BasicValueConverter;
import org.hibernate.type.descriptor.java.LongJavaType;
import org.hibernate.type.descriptor.jdbc.BigIntJdbcType;
import org.hibernate.type.descriptor.jdbc.VarcharJdbcType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;

import hu.icellmobilsoft.coffee.se.util.string.RandomUtil;
import hu.icellmobilsoft.frappee.hibernate.batch.constants.TestBasicTypes;
import hu.icellmobilsoft.frappee.hibernate.batch.provider.BinaryArgumentsProvider;
import hu.icellmobilsoft.frappee.hibernate.batch.provider.DateArgumentsProvider;
import hu.icellmobilsoft.frappee.hibernate.batch.provider.TimeArgumentsProvider;
import hu.icellmobilsoft.frappee.hibernate.batch.provider.TimestampArgumentsProvider;
import hu.icellmobilsoft.frappee.hibernate.batch.types.StringBasicType;
import hu.icellmobilsoft.frappee.hibernate.util.HibernateEntityHelper;

/**
 * Class for testing {@link HibernateBatchService}.
 *
 * @author csaba.balogh
 * @since 1.0.0
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(MockitoExtension.class)
class HibernateBatchServiceTest {

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private EntityManager em;

    @Spy
    @InjectMocks
    private HibernateBatchService batchService = Mockito.spy(new HibernateBatchService(em, new HibernateEntityHelper(em)));

    private TimeZone dbTimeZone;

    @Test
    @Order(1)
    void setPsObjectNullTest() throws SQLException {
        // given

        // when
        batchService.setPsObject(preparedStatement, 0, null, null);

        // then
        Mockito.verify(preparedStatement).setNull(0, SqlTypes.NULL);
    }

    @Order(2)
    @ParameterizedTest(name = "[{index}] - type: [{0}] value: [{1}] expected: [{2}]")
    @ArgumentsSource(DateArgumentsProvider.class)
    void setPsObjectDateWithoutTimezoneTest(BasicType<?> type, Object value, java.sql.Date expectedDate) throws SQLException {
        // given

        // when
        batchService.setPsObject(preparedStatement, 0, type, value);

        // then
        Mockito.verify(preparedStatement).setDate(0, expectedDate);
    }

    @Order(3)
    @ParameterizedTest(name = "[{index}] - type: [{0}] value: [{1}] expected: [{2}]")
    @ArgumentsSource(DateArgumentsProvider.class)
    void setPsObjectDateWithTimezoneTest(BasicType<?> type, Object value, java.sql.Date expectedDate) throws SQLException {
        // given
        // BatchService-ben lévő dbTimeZone mock készítése miatt szükséges
        mockDbTimeZone();

        // when
        batchService.setPsObject(preparedStatement, 0, type, value);

        // then
        Mockito.verify(preparedStatement).setDate(0, expectedDate);
    }

    @Order(4)
    @ParameterizedTest(name = "[{index}] - type: [{0}] value: [{1}] expected: [{2}]")
    @ArgumentsSource(TimeArgumentsProvider.class)
    void setPsObjectTimeWithoutTimezoneTest(BasicType<?> type, Object value, Time expectedTimeWithoutTimeZone) throws SQLException {
        // given

        // when
        batchService.setPsObject(preparedStatement, 0, type, value);

        // then
        Mockito.verify(preparedStatement).setTime(0, expectedTimeWithoutTimeZone);
    }

    @Order(5)
    @ParameterizedTest(name = "[{index}] - type: [{0}] value: [{1}] expectedValue: [{3}]")
    @ArgumentsSource(TimeArgumentsProvider.class)
    void setPsObjectTimeWithTimezoneTest(BasicType<?> type, Object value, Time ignored, Time expectedTimeWithTimeZone) throws SQLException {
        // given
        // BatchService-ben lévő dbTimeZone mock készítése miatt szükségesek
        mockDbTimeZone();

        // when
        batchService.setPsObject(preparedStatement, 0, type, value);

        // then
        ArgumentCaptor<Calendar> calendarArgumentCaptor = ArgumentCaptor.forClass(Calendar.class);
        Mockito.verify(preparedStatement).setTime(ArgumentMatchers.eq(0), ArgumentMatchers.eq(expectedTimeWithTimeZone), calendarArgumentCaptor.capture());
    }

    @Order(6)
    @ParameterizedTest(name = "[{index}] - type: [{0}] value: [{1}] expected: [{2}]")
    @ArgumentsSource(TimestampArgumentsProvider.class)
    void setPsObjectTimestampWithoutTimezoneTest(BasicType<?> type, Object value, Timestamp expectedTimestampWithoutTimeZone) throws SQLException {
        // given

        // when
        batchService.setPsObject(preparedStatement, 0, type, value);

        // then
        Mockito.verify(preparedStatement).setTimestamp(0, expectedTimestampWithoutTimeZone);
    }

    @Order(7)
    @ParameterizedTest(name = "[{index}] - type: [{0}] value: [{1}] expectedValue: [{3}]")
    @ArgumentsSource(TimestampArgumentsProvider.class)
    void setPsObjectTimestampWithTimezoneTest(BasicType<?> type, Object value, Timestamp ignored, Timestamp expectedTimestampWithTimeZone)
            throws SQLException {
        // given
        // BatchService-ben lévő dbTimeZone mock készítése miatt szükségesek
        mockDbTimeZone();

        // when
        batchService.setPsObject(preparedStatement, 0, type, value);

        // then
        ArgumentCaptor<Calendar> calendarArgumentCaptor = ArgumentCaptor.forClass(Calendar.class);
        Mockito.verify(preparedStatement).setTimestamp(ArgumentMatchers.eq(0), ArgumentMatchers.eq(expectedTimestampWithTimeZone), calendarArgumentCaptor.capture());
    }

    @Order(8)
    @ParameterizedTest(name = "[{index}] - value: [{0}]")
    @ValueSource(booleans = { true, false })
    void setPsObjectBooleanTest(Boolean value) throws SQLException {
        // given

        // when
        batchService.setPsObject(preparedStatement, 0, TestBasicTypes.BOOLEAN_BASIC_TYPE, value);

        // then
        Mockito.verify(preparedStatement).setBoolean(0, value);
    }

    @Order(9)
    @ParameterizedTest(name = "[{index}] - value: [{0}]")
    @ValueSource(strings = { "true", "false", "yes", "no" })
    void setPsObjectBooleanStringTest(String value) throws SQLException {
        // given

        // when
        batchService.setPsObject(preparedStatement, 0, TestBasicTypes.BOOLEAN_STRING_BASIC_TYPE, value);

        // then
        Mockito.verify(preparedStatement).setObject(0, value);
    }

    @Test
    @Order(10)
    void setPsObjectCharTest() throws SQLException {
        // given
        char value = 'a';

        // when
        batchService.setPsObject(preparedStatement, 0, TestBasicTypes.CHARACTER_BASIC_TYPE, value);

        // then
        Mockito.verify(preparedStatement).setString(0, String.valueOf(value));
    }

    @Order(11)
    @ParameterizedTest(name = "[{index}] - type: [{0}] value: [{1}] expectedValue: [{2}]")
    @ArgumentsSource(BinaryArgumentsProvider.class)
    void setPsObjectBinaryTest(BasicType<?> basicType, Object value, Object expectedValue) throws SQLException {
        // given

        // when
        batchService.setPsObject(preparedStatement, 0, basicType, value);

        // then
        if (value instanceof Blob) {
            ArgumentCaptor<ByteArrayInputStream> byteArrayInputStreamArgumentCaptor = ArgumentCaptor.forClass(ByteArrayInputStream.class);
            Mockito.verify(preparedStatement).setBinaryStream(ArgumentMatchers.eq(0), byteArrayInputStreamArgumentCaptor.capture());

            byte[] expectedByteArray = ((ByteArrayInputStream) expectedValue).readAllBytes();
            byte[] actualByteArray = byteArrayInputStreamArgumentCaptor.getValue().readAllBytes();
            Assertions.assertArrayEquals(expectedByteArray, actualByteArray, "captured blob bytes");
        } else {
            Mockito.verify(preparedStatement).setBytes(0, (byte[]) expectedValue);
        }
    }

    @Test
    @Order(12)
    void setPsObjectManyToOneType() throws SQLException {
        // given
        String entityId = RandomUtil.generateId();

        ManyToOneType manyToOneTypeMock = Mockito.mock(ManyToOneType.class);
        HibernateProxy manyToOneEntityMock = Mockito.mock(HibernateProxy.class);
        LazyInitializer lazyInitializerMock = Mockito.mock(LazyInitializer.class);

        Mockito.doReturn(lazyInitializerMock).when(manyToOneEntityMock).getHibernateLazyInitializer();
        Mockito.doReturn(true).when(lazyInitializerMock).isUninitialized();
        Mockito.doReturn(entityId).when(lazyInitializerMock).getIdentifier();

        // when
        batchService.setPsObject(preparedStatement, 0, manyToOneTypeMock, manyToOneEntityMock);

        // then
        Mockito.verify(preparedStatement).setObject(0, entityId);
    }

    @Test
    @Order(13)
    @SuppressWarnings("unchecked")
    void setPsObjectConvertedBasicTypeTest() throws SQLException {
        // given
        YearMonth yearMonth = YearMonth.now();
        String yearMonthString = yearMonth.toString();

        ConvertedBasicType<YearMonth> convertedBasicTypeMock = Mockito.mock(ConvertedBasicType.class);
        BasicValueConverter<YearMonth, String> basicValueConverterMock = Mockito.mock(BasicValueConverter.class);

        Mockito.doReturn(VarcharJdbcType.INSTANCE).when(convertedBasicTypeMock).getJdbcType();
        Mockito.doReturn(basicValueConverterMock).when(convertedBasicTypeMock).getValueConverter();
        Mockito.doReturn(yearMonthString).when(basicValueConverterMock).toRelationalValue(yearMonth);

        // when
        batchService.setPsObject(preparedStatement, 0, convertedBasicTypeMock, yearMonth);

        // then
        Mockito.verify(preparedStatement).setString(0, yearMonthString);
    }

    @Test
    @Order(14)
    void getIdPartsBasicTypeTest() {
        // given
        BasicType<Long> longBasicType = new BasicTypeImpl<>(LongJavaType.INSTANCE, BigIntJdbcType.INSTANCE);
        SingleTableEntityPersister persister = Mockito.mock(SingleTableEntityPersister.class);
        Mockito.doReturn(longBasicType).when(persister).getIdentifierType();

        // when
        List<TypedValue> idParts = batchService.getIdParts(persister, 42L);

        // then
        Assertions.assertEquals(List.of(new TypedValue(longBasicType, 42L)), idParts);
    }

    @Test
    @Order(15)
    void getIdPartsComponentTypeTest() {
        // given
        Object compositeId = new Object();
        ComponentType componentType = Mockito.mock(ComponentType.class);
        Mockito.doReturn(new Type[] { StringBasicType.INSTANCE, TestBasicTypes.LOCAL_DATE_BASIC_TYPE }).when(componentType).getSubtypes();
        Mockito.doReturn("CODE").when(componentType).getPropertyValue(compositeId, 0);
        Mockito.doReturn(java.time.LocalDate.of(2026, 1, 2)).when(componentType).getPropertyValue(compositeId, 1);
        SingleTableEntityPersister persister = Mockito.mock(SingleTableEntityPersister.class);
        Mockito.doReturn(componentType).when(persister).getIdentifierType();

        // when
        List<TypedValue> idParts = batchService.getIdParts(persister, compositeId);

        // then
        Assertions.assertEquals(List.of(new TypedValue(StringBasicType.INSTANCE, "CODE"),
                new TypedValue(TestBasicTypes.LOCAL_DATE_BASIC_TYPE, java.time.LocalDate.of(2026, 1, 2))), idParts);
    }

    @Test
    @Order(16)
    void validateIdForInsertNullIdTest() {
        // given
        SingleTableEntityPersister persister = Mockito.mock(SingleTableEntityPersister.class);
        Mockito.doReturn("id").when(persister).getIdentifierPropertyName();

        // when
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> batchService.validateIdForInsert(persister, null));

        // then
        Assertions.assertTrue(exception.getMessage().contains("[id]"), exception.getMessage());
    }

    @Test
    @Order(17)
    void validateIdForInsertCompositeNullPartTest() {
        // given
        Object compositeId = new Object();
        ComponentType componentType = Mockito.mock(ComponentType.class);
        Mockito.doReturn(new String[] { "code", "seq" }).when(componentType).getPropertyNames();
        Mockito.doReturn(new Type[] { StringBasicType.INSTANCE, StringBasicType.INSTANCE }).when(componentType).getSubtypes();
        Mockito.doReturn("CODE").when(componentType).getPropertyValue(compositeId, 0);
        Mockito.doReturn(null).when(componentType).getPropertyValue(compositeId, 1);
        SingleTableEntityPersister persister = Mockito.mock(SingleTableEntityPersister.class);
        Mockito.doReturn(componentType).when(persister).getIdentifierType();

        // when
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> batchService.validateIdForInsert(persister, compositeId));

        // then
        Assertions.assertTrue(exception.getMessage().contains("[seq]"), exception.getMessage());
    }

    @Test
    @Order(18)
    void setParametersForDeleteTest() throws SQLException {
        // given
        Object entity = new Object();
        SingleTableEntityPersister persister = Mockito.mock(SingleTableEntityPersister.class);
        Mockito.doReturn(StringBasicType.INSTANCE).when(persister).getIdentifierType();
        Mockito.doReturn("ENTITY_ID").when(persister).getIdentifier(entity, (SharedSessionContractImplementor) null);

        // when
        batchService.setParametersForDelete(preparedStatement, persister, entity);

        // then
        Mockito.verify(preparedStatement).setString(1, "ENTITY_ID");
    }

    private void mockDbTimeZone() {
        dbTimeZone = Mockito.mock(TimeZone.class);
        dbTimeZone.setID("UTC");
        try {
            MockitoAnnotations.openMocks(this).close();
        } catch (Exception e) {
            throw new MockitoException("Failed to release mocks", e);
        }
    }
}
