package ru.gosuslugi.pgu.kafkacli;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import ru.gosuslugi.pgu.kafkacli.props.IncludeExcludeProperties;
import ru.gosuslugi.pgu.kafkacli.props.TimeRangeProperties;

import java.time.Duration;

@Data
@ConfigurationProperties("app")
public class KafkaCliProperties {

    /**
     * Топик из которого читать сообщения
     */
    String topic;

    /**
     * Топик в который отправлять сообщения
     */
    String dest;

    /**
     * Фильтр по {@code orderId}
     */
    @NestedConfigurationProperty
    IncludeExcludeProperties<Long> orderId = new IncludeExcludeProperties<>();

    /**
     * Фильтр по {@code serviceId}
     */
    @NestedConfigurationProperty
    IncludeExcludeProperties<String> serviceId = new IncludeExcludeProperties<>();

    /**
     * <p>Сброс `offset` в Kafka</p>
     *
     * <p>Это приведет к повторной отправке
     * всех сообщений в топике - даже уже повторно отправленных</p>
     */
    boolean restart;

    /**
     * Таймаут для ожидания дополнительных сообщений в топике.
     * В случае если за это время в топике не будет добавлено ни одного
     * сообщений выполнение программы будет завершено.
     */
    Duration timeout = Duration.ofSeconds(10);

    /**
     * Фильтр по {@code timestamp} сообщений
     */
    TimeRangeProperties timestamp = new TimeRangeProperties();

    /**
     * <p>Флаг, означающий, что необходимо дожидаться всех сообщений в топике</p>
     *
     * <p>Иначе будут прочитаны только те сообщения которые были в топике
     * на момент запуска приложения</p>
     */
    boolean readUntilTimeout;

    /**
     * <p>Флаг означающий что необходимо логировать абсолютно все сообщения. Иначе в логе будут только те которые подходят по фильтру</p>
     */
    boolean logAll = false;

    /**
     * <p>Флаг означающий что переотправлять сообщения не нужно, работает вся логика кроме отправки в топик</p>
     */
    boolean dryRun = false;

}
