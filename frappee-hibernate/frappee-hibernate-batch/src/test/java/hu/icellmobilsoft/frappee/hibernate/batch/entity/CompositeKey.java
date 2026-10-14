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

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Composite key used both as {@code @EmbeddedId} and {@code @IdClass} in tests.
 *
 * @author attila.almasi
 * @since 2.2.0
 */
@Embeddable
public class CompositeKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "CODE", length = 30)
    private String code;

    @Column(name = "SEQ")
    private Long seq;

    /**
     * Default constructor.
     */
    public CompositeKey() {
        super();
    }

    /**
     * Constructor.
     *
     * @param code
     *            code part
     * @param seq
     *            sequence part
     */
    public CompositeKey(String code, Long seq) {
        this.code = code;
        this.seq = seq;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CompositeKey)) {
            return false;
        }
        CompositeKey that = (CompositeKey) o;
        return Objects.equals(code, that.code) && Objects.equals(seq, that.seq);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, seq);
    }

    @Override
    public String toString() {
        return code + ":" + seq;
    }
}
