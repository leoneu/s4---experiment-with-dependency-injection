package io.s4.example.twittertopiccount;

import io.s4.dispatcher.Dispatcher;
import io.s4.persist.Persister;
import io.s4.processor.PEContainer;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import com.google.inject.Inject;

public class PEGraph implements io.s4.processor.PEGraph {

    final private PropertiesConfiguration config;
    final private static Logger logger = Logger.getLogger(PEGraph.class);

    @Inject private Dispatcher dispatcher;
    @Inject private Persister persister;
    private PEContainer peContainer;
    
    @Inject
    PEGraph(PropertiesConfiguration config, PEContainer peContainer) {
        this.config = config;
        this.peContainer = peContainer;
        logger.debug("Constructor. config= " + config.getFileName());
        peContainer.setPeGraph(this);
    }

    public void create()  {

        TopicExtractorPE topicExtractorPE = new TopicExtractorPE();
        topicExtractorPE.setDispatcher(dispatcher);
        topicExtractorPE.setKeys(config
                .getStringArray("pe.topic_extractor.keys"));
        topicExtractorPE.setOutputStreamName(config
                .getString("pe.topic_extractor.output_stream_name"));
        peContainer.addProcessor(topicExtractorPE);

        TopicCountAndReportPE topicCountAndReportPE = new TopicCountAndReportPE();
        topicCountAndReportPE.setDispatcher(dispatcher);
        topicCountAndReportPE.setKeys(config
                .getStringArray("pe.topic_count_and_report.keys"));
        topicCountAndReportPE.setOutputStreamName(config
                .getString("pe.topic_count_and_report.output_stream_name"));
        topicCountAndReportPE.setThreshold(config
                .getInt("pe.topic_count_and_report.threshhold"));
        topicCountAndReportPE.setTtl(config
                .getInt("pe.topic_count_and_report.ttl"));
        topicCountAndReportPE
                .setOutputFrequencyByTimeBoundary(config
                        .getInt("pe.topic_count_and_report.output_frequency_by_time_boundary"));
        peContainer.addProcessor(topicCountAndReportPE);

        
        TopNTopicPE topNTopicPE = new TopNTopicPE();
        topNTopicPE.setEntryCount(config.getInt("pe.top_n_topic.entry_count"));
        topNTopicPE.setOutputFrequencyByTimeBoundary(config
                .getInt("pe.top_n_topic.output_frequency_by_time_boundary"));
        topNTopicPE.setPersister(persister);
        topNTopicPE.setPersistTime(config.getInt("pe.top_n_topic.persist_time"));
        topNTopicPE.setPersistKey(config.getString("pe.top_n_topic.persist_key"));
        topNTopicPE.setTtl(config.getInt("pe.top_n_topic.ttl"));
        topicCountAndReportPE.setKeys(config
                .getStringArray("pe.top_n_topic.keys"));
        peContainer.addProcessor(topNTopicPE);

    }

}
