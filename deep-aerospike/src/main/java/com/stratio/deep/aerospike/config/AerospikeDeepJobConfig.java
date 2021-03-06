/*
 * Copyright 2014, Stratio.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.deep.aerospike.config;

import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.FILTER_QUERY;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.NAMESPACE;
import static com.stratio.deep.commons.extractor.utils.ExtractorConstants.SET;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;

import com.aerospike.hadoop.mapreduce.AerospikeConfigUtil;
import com.stratio.deep.aerospike.extractor.AerospikeCellExtractor;
import com.stratio.deep.aerospike.extractor.AerospikeEntityExtractor;
import com.stratio.deep.commons.config.ExtractorConfig;
import com.stratio.deep.commons.config.HadoopConfig;
import com.stratio.deep.commons.entity.Cells;
import com.stratio.deep.commons.filter.Filter;
import com.stratio.deep.commons.filter.FilterType;

import scala.Tuple2;
import scala.Tuple3;

/**
 * Configuration class for Aerospike-Spark integration
 *
 * @param <T>
 */
public class AerospikeDeepJobConfig<T> extends HadoopConfig<T, AerospikeDeepJobConfig<T>> implements
        IAerospikeDeepJobConfig<T>, Serializable {

    private static final long serialVersionUID = 2778930913494063818L;

    /**
     * Configuration to be broadcasted to every spark node.
     */
    private transient Configuration configHadoop;

    private List<Integer> portList = new ArrayList<>();

    /**
     * Aerospike's bin name.
     */
    private String bin;

    /**
     * Aerospike's operation (defaults to scan).
     */
    private String operation = AerospikeConfigUtil.DEFAULT_INPUT_OPERATION;

    /**
     * Aerospike's equality filter value.
     */
    private Tuple2<String, Object> equalsFilter;

    /**
     * Aerospike's numrange filter value.
     */
    private Tuple3<String, Object, Object> numrangeFilter;

    /**
     * Constructor for Entity class-based configuration.
     *
     * @param entityClass
     */
    public AerospikeDeepJobConfig(Class<T> entityClass) {
        super(entityClass);
        if (Cells.class.isAssignableFrom(entityClass)) {
            extractorImplClass = AerospikeCellExtractor.class;
        } else {
            extractorImplClass = AerospikeEntityExtractor.class;
        }
    }

    /**
     * Gets first Aerospike node connection port.
     *
     * @return
     */
    public Integer getAerospikePort() {
        return !portList.isEmpty() ? portList.get(0) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> port(List<Integer> port) {
        this.portList.addAll(port);
        return this;
    }

    @Override
    public AerospikeDeepJobConfig<T> port(Integer port) {
        this.portList.add(port);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> namespace(String nameSpace) {
        this.catalog = nameSpace;
        return this;
    }

    /**
     * Set Aerospike nodes' ports.
     *
     * @param ports
     * @return
     */
    public AerospikeDeepJobConfig<T> port(Integer[] ports) {
        this.portList.addAll(Arrays.asList(ports));
        return this;
    }

    @Override
    public AerospikeDeepJobConfig<T> set(String set) {
        this.table = set;
        return this;
    }

    @Override
    public String getNamespace() {
        return this.catalog;
    }

    @Override
    public String getSet() {
        return this.table;
    }

    @Override
    public AerospikeDeepJobConfig<T> bin(String bin) {
        this.bin = bin;
        return this;
    }

    @Override
    public String getBin() {
        return this.bin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> operation(String operation) {
        this.operation = operation;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOperation() {
        return this.operation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> equalsFilter(Tuple2<String, Object> filter) {
        this.equalsFilter = filter;
        return this;
    }

    /**
     * Returns a tuple containing the configured equality filter field and its value.
     *
     * @return
     */
    public Tuple2<String, Object> getEqualsFilter() {
        return this.equalsFilter;
    }

    @Override
    public AerospikeDeepJobConfig<T> numrangeFilter(Tuple3<String, Object, Object> filter) {
        this.numrangeFilter = filter;
        return this;
    }

    /**
     * Returns a tuple containing the configured numeric range filter fields and its values.
     *
     * @return
     */
    public Tuple3<String, Object, Object> getNumrangeFilter() {
        return this.numrangeFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AerospikeDeepJobConfig<T> initialize() {
        validate();

        configHadoop = new JobConf();
        configHadoop = new Configuration();

        configHadoop.set(AerospikeConfigUtil.INPUT_HOST, getHost());

        configHadoop.set(AerospikeConfigUtil.INPUT_PORT, Integer.toString(getAerospikePort()));

        configHadoop.set(AerospikeConfigUtil.INPUT_NAMESPACE, catalog);

        configHadoop.set(AerospikeConfigUtil.INPUT_SETNAME, table);

        configHadoop.set(AerospikeConfigUtil.INPUT_OPERATION, operation);

        if (numrangeFilter != null) {
            configHadoop.set(AerospikeConfigUtil.INPUT_NUMRANGE_BIN, numrangeFilter._1());
            configHadoop.set(AerospikeConfigUtil.INPUT_NUMRANGE_BEGIN, numrangeFilter._2().toString());
            configHadoop.set(AerospikeConfigUtil.INPUT_NUMRANGE_END, numrangeFilter._3().toString());
        }

        configHadoop.set(AerospikeConfigUtil.OUTPUT_HOST, getHost());

        configHadoop.set(AerospikeConfigUtil.OUTPUT_PORT, Integer.toString(getAerospikePort()));

        configHadoop.set(AerospikeConfigUtil.OUTPUT_NAMESPACE, catalog);

        configHadoop.set(AerospikeConfigUtil.OUTPUT_SETNAME, table);

        if (operation != null) {
            configHadoop.set(AerospikeConfigUtil.INPUT_OPERATION, operation);
        }

        return this;
    }

    /**
     * Validates connection parameters.
     */
    private void validate() {
        if (host.isEmpty()) {
            throw new IllegalArgumentException("host cannot be null");
        }
        if (catalog == null) {
            throw new IllegalArgumentException("namespace cannot be null");
        }
        if (table == null) {
            throw new IllegalArgumentException("set cannot be null");
        }
        if (portList.isEmpty()) {
            if(port>0){
                port(port);
            }else{
                throw new IllegalArgumentException("port cannot be null");
            }

        }
        if (host.size() != portList.size()) {
            throw new IllegalArgumentException("Host and ports cardinality must be the same");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Configuration getHadoopConfiguration() {
        if (configHadoop == null) {
            initialize();
        }
        return configHadoop;
    }

    @Override
    public AerospikeDeepJobConfig<T> initialize(ExtractorConfig extractorConfig) {
        super.initialize(extractorConfig);

        Map<String, Serializable> values = extractorConfig.getValues();

        if (values.get(NAMESPACE) != null) {
            catalog(extractorConfig.getString(NAMESPACE));
        }

        if (values.get(SET) != null) {
            set(extractorConfig.getString(SET));
        }

        if (values.get(FILTER_QUERY) != null) {
            filterQuery(extractorConfig.getFilterArray(FILTER_QUERY));
        }

        this.initialize();

        return this;
    }

    /**
     * Configure Aerospike filters with the received Deep Filter objects.
     *
     * @param filters
     * @return
     */
    public AerospikeDeepJobConfig<T> filterQuery(Filter[] filters) {
        if (filters.length > 1) {
            throw new UnsupportedOperationException("Aerospike currently accepts only one filter operations");
        } else if (filters.length > 0) {
            Filter deepFilter = filters[0];
            if (!isValidAerospikeFilter(deepFilter)) {
                throw new UnsupportedOperationException(
                        "Aerospike currently supports only equality and range filter operations");
            } else if (!deepFilter.getFilterType().equals(FilterType.EQ)) {
                operation("numrange");
                setAerospikeNumrange(deepFilter);
            } else {
                operation("scan");
                setAerospikeEqualsFilter(deepFilter);
            }
        }
        return this;
    }

    private boolean isValidAerospikeFilter(Filter filter) {
        return filter.getFilterType().equals(FilterType.EQ) ||
                filter.getFilterType().equals(FilterType.LT) ||
                filter.getFilterType().equals(FilterType.GT) ||
                filter.getFilterType().equals(FilterType.GTE) ||
                filter.getFilterType().equals(FilterType.LTE);
    }

    private void setAerospikeNumrange(Filter filter) {
        String field = filter.getField();
        if (!filter.getValue().getClass().equals(Long.class)) {
            throw new UnsupportedOperationException("Range filters only accept Long type as parameters");
        }
        if (filter.getFilterType().equals(FilterType.LT)) {
            numrangeFilter(new Tuple3<String, Object, Object>(field, Long.MIN_VALUE, (Long) filter.getValue() - 1));
        }
        if (filter.getFilterType().equals(FilterType.LTE)) {
            numrangeFilter(new Tuple3<String, Object, Object>(field, Long.MIN_VALUE, (Long) filter.getValue()));
        }
        if (filter.getFilterType().equals(FilterType.GT)) {
            numrangeFilter(new Tuple3<String, Object, Object>(field, (Long) filter.getValue() + 1, Long.MAX_VALUE));
        }
        if (filter.getFilterType().equals(FilterType.GTE)) {
            numrangeFilter(new Tuple3<String, Object, Object>(field, (Long) filter.getValue(), Long.MAX_VALUE));
        }
    }

    private void setAerospikeEqualsFilter(Filter filter) {

        equalsFilter(new Tuple2<String, Object>(filter.getField(), filter.getValue()));
    }

}
