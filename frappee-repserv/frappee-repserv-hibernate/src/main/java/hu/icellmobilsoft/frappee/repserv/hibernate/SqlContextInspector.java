/*-
 * #%L
 * Frappee
 * %%
 * Copyright (C) 2024 - 2025 i-Cell Mobilsoft Zrt.
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
package hu.icellmobilsoft.frappee.repserv.hibernate;

import java.util.Optional;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import hu.icellmobilsoft.coffee.module.repserv.api.SqlContext;
import jakarta.enterprise.inject.spi.CDI;

/**
 * {@link StatementInspector} implementation to extend sql queries with {@link SqlContext#getId()} in a comment
 *
 * @author janos.boroczki
 * @since 2.1.0
 */
public class SqlContextInspector implements StatementInspector {

    /**
     * Context object to store id
     */
    private SqlContext sqlContext;

    /**
     * Default constructor.
     */
    public SqlContextInspector() {
        super();
    }

    /**
     * Constructs a new object with {@link SqlContext} object
     * 
     * @param sqlContext
     *            context to store id
     */
    public SqlContextInspector(SqlContext sqlContext) {
        super();
        this.sqlContext = sqlContext;
    }

    @Override
    public String inspect(String sql) {
        Optional<String> optId = getSqlContext().map(SqlContext::getId);

        if (optId.isPresent()) {
            String id = optId.get().replace("/*", "").replace("*/", "");

            return "/* SQLId: " + id + " */ " + sql;
        }

        return sql;
    }

    private Optional<SqlContext> getSqlContext() {
        if (sqlContext != null) {
            return Optional.of(sqlContext);
        }
        return CDI.current().select(SqlContext.class).stream().findFirst();
    }
}
