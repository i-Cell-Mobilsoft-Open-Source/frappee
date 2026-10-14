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
package hu.icellmobilsoft.frappee.hibernate.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Test entity with an {@code @IdClass} composite id.
 *
 * @author attila.almasi
 * @since 2.2.0
 */
@Entity
@IdClass(CompositeKey.class)
@Table(name = "ID_CLASS_ENTITY")
public class IdClassEntity {

    @Id
    @Column(name = "CODE", length = 30)
    private String code;

    @Id
    @Column(name = "SEQ")
    private Long seq;

    @Column(name = "NAME")
    private String name;

    @Version
    @Column(name = "VERSION")
    private Long version;

    /**
     * Getter for code.
     *
     * @return code
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter for code.
     *
     * @param code
     *            code
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Getter for seq.
     *
     * @return seq
     */
    public Long getSeq() {
        return seq;
    }

    /**
     * Setter for seq.
     *
     * @param seq
     *            seq
     */
    public void setSeq(Long seq) {
        this.seq = seq;
    }

    /**
     * Getter for name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name.
     *
     * @param name
     *            name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for version.
     *
     * @return version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Setter for version.
     *
     * @param version
     *            version
     */
    public void setVersion(Long version) {
        this.version = version;
    }
}
