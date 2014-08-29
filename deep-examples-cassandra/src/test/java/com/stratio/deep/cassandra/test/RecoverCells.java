package com.stratio.deep.cassandra.test;

import com.stratio.deep.config.ExtractorConfig;
import com.stratio.deep.core.context.DeepSparkContext;
import com.stratio.deep.entity.Cells;
import com.stratio.deep.extractor.server.ExtractorServer;
import com.stratio.deep.extractor.utils.ExtractorConstants;
import com.stratio.deep.rdd.CassandraCellExtractor;
import com.stratio.deep.utils.ContextProperties;
import org.apache.log4j.Logger;
import org.apache.spark.rdd.RDD;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by dgomez on 28/08/14.
 */
@org.testng.annotations.Test
public class RecoverCells {

    private static final Logger LOG = Logger.getLogger(RecoverCells.class);

    /* used to perform external tests */

    private String job = "java:aggregatingData";

    private String KEYSPACENAME = "test";
    private String TABLENAME    = "tweets";
    private String CQLPORT      = "9042";
    private String RPCPORT      = "9160";
    private String HOST         = "127.0.0.1";

    /**
     *
     *
     *
     */
    @BeforeMethod
    public void createExtractorServer() {


        //Call async the Extractor netty Server
        ExecutorService es = Executors.newFixedThreadPool(3);
        final Future future = es.submit(new Callable() {
            public Object call() throws Exception {
                ExtractorServer.main(null);
                return null;
            }
        });
    }

    /**
     * This is the method called by both main and tests.
     *
     */
    @Test
    public void recoverCells() {


        // Creating the Deep Context where args are Spark Master and Job Name
        ContextProperties p = new ContextProperties(null);
        DeepSparkContext deepContext = new DeepSparkContext(p.getCluster(), job, p.getSparkHome(), p.getJars());

        // Creating a configuration for the Extractor and initialize it
        ExtractorConfig<Cells> config = new ExtractorConfig(Cells.class);

        config.setExtractorImplClass(CassandraCellExtractor.class);
        config.setEntityClass(Cells.class);

        Map<String, String> values = new HashMap<>();
        values.put(ExtractorConstants.KEYSPACE, KEYSPACENAME);
        values.put(ExtractorConstants.TABLE,    TABLENAME);
        values.put(ExtractorConstants.CQLPORT,  CQLPORT);
        values.put(ExtractorConstants.RPCPORT,  RPCPORT);
        values.put(ExtractorConstants.HOST,     HOST );

        config.setValues(values);


        // Creating the RDD
        RDD<Cells> rdd = deepContext.createRDD(config);
        LOG.info("count: " + rdd.count());
        LOG.info("first: " + rdd.first());

        System.out.println("count: " + rdd.count());
        System.out.println("first: " + rdd.first());

        //close
        ExtractorServer.close();
        deepContext.stop();
    }


}