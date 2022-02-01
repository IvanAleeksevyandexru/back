package ru.gosuslugi.pgu.kafkacli;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import ru.gosuslugi.pgu.dto.SpAdapterDto;
import ru.gosuslugi.pgu.dto.SpRequestErrorDto;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaCliRunner implements ApplicationRunner {

    private final KafkaCliProperties props;
    private final KafkaConsumer<Long, SpRequestErrorDto> consumer;
    private final KafkaProducer<Long, SpAdapterDto> producer;

    @Override
    public void run(ApplicationArguments args) {

        val partitions = getMutablePartitionSet();
        if (partitions.isEmpty()) {
            log.error("Topic {} either doesn't exists or doesn't have any partition", props.getTopic());
            return;
        }

        log.info("Partitions for topic {}: {}", props.getTopic(), partitions);

        consumer.assign(partitions);
        if (props.isRestart()) {
            consumer.seekToBeginning(partitions);
        }

        val processor = new MessageProcessor(
                partitions,
                consumer.beginningOffsets(partitions),
                consumer.endOffsets(partitions));

        processor.logOffsets();

        for (ConsumerRecords<Long, SpRequestErrorDto> records;
             !partitions.isEmpty() && !(records = consumer.poll(props.getTimeout())).isEmpty();) {
            for (val record: records) {
                processor.process(record);
            }
        }

    }

    @RequiredArgsConstructor
    private class MessageProcessor {

        private final Set<TopicPartition> remainingPartitions;
        private final Map<TopicPartition, Long> beginningOffsets;
        private final Map<TopicPartition, Long> endOffsets;

        public void process(ConsumerRecord<Long, SpRequestErrorDto> record) {
            val partition = new TopicPartition(record.topic(), record.partition());
            if (!remainingPartitions.contains(partition)) {
                return;
            }

            logMessage(true, record, partition);
            if (unassignPartitionAfterReadLimit(partition, record.offset(), false)) {
                return;
            }

            if (filter(record)) {
                logMessage(false, record, partition);
                if (!props.isDryRun()) {
                    send(mapRecord(record));
                }
            }

            unassignPartitionAfterReadLimit(partition, record.offset(), true);
        }

        private ProducerRecord<Long, SpAdapterDto> mapRecord(ConsumerRecord<Long, SpRequestErrorDto> record) {
            return new ProducerRecord<>(props.getDest(), record.key(), record.value().getAdapterRequestDto());
        }

        private boolean filter(ConsumerRecord<Long, SpRequestErrorDto> record) {
            val dto = record.value();
            return props.getServiceId().includes(dto.getAdapterRequestDto().getServiceId())
                    && props.getOrderId().includes(dto.getAdapterRequestDto().getOrderId())
                    && props.getTimestamp().includes(Instant.ofEpochMilli(record.timestamp()));
        }

        private void send(ProducerRecord<Long, SpAdapterDto> record) {
            log.info("Sending message to {} (key={}): {}", record.topic(), record.key(), record.value());
            producer.send(record);
        }

        public boolean unassignPartitionAfterReadLimit(TopicPartition partition, long offset, boolean afterProcess) {
            if (props.isReadUntilTimeout()) {
                return false;
            }

            val endOffset = endOffsets.getOrDefault(partition, 0L);

            if (afterProcess) {
                ++offset;
            }

            if (offset < endOffset) {
                return false;
            }

            consumer.commitSync(Map.of(partition, new OffsetAndMetadata(endOffset)));
            remainingPartitions.remove(partition);
            consumer.assign(remainingPartitions);
            return true;
        }

        public void logOffsets() {
            log.info("Offsets for partitions:\n{}", remainingPartitions.stream()
                    .map(p -> String.format("%s: beg=%d cur=%d end=%d", p,
                            beginningOffsets.getOrDefault(p, 0L), consumer.position(p), endOffsets.getOrDefault(p, 0L)))
                    .collect(Collectors.joining("\n")));
        }

    }

    private void logMessage(boolean beforeFilter, ConsumerRecord<Long, SpRequestErrorDto> record, TopicPartition partition) {
        if (props.isLogAll() == beforeFilter) {
            log.info("Received message {}#{} (key={}): {}", partition, record.offset(), record.key(), record.value());
        }
    }

    private Set<TopicPartition> getMutablePartitionSet() {
        val infos = consumer.partitionsFor(props.getTopic());
        return (infos != null ? infos.stream() : Stream.<PartitionInfo>empty())
                .map(i -> new TopicPartition(i.topic(), i.partition()))
                .collect(Collectors.toCollection(HashSet::new));
    }

}
