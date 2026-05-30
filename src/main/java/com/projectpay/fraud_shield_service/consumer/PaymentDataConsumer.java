package com.projectpay.fraud_shield_service.consumer;

import com.projectpay.fraud_shield_service.entities.AccountStatus;
import com.projectpay.fraud_shield_service.entities.UserAccountsStatus;
import com.projectpay.fraud_shield_service.repository.BlackListRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.projectpay.dtos.TransactionPayloadDTO;
import org.projectpay.enums.Status;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.projectpay.dtos.TransactionFraudStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentDataConsumer {
    private final BlackListRepository blackListRepository;
    private final KafkaTemplate<@NonNull String,@NonNull TransactionFraudStatus> kafkaTemplate;

    @KafkaListener(topics = "payment-initiated-topic", groupId = "payment-group")
    public void consumedMessage(
            TransactionPayloadDTO transactionPayloadDTO,
            @Header(KafkaHeaders.RECEIVED_KEY) String transactionId){
        if(blackListRepository.existsById(transactionPayloadDTO.getUserId()) && blackListRepository.findById(transactionPayloadDTO.getUserId()).get().getAccountStatus() ==
                AccountStatus.INACTIVE) {
            kafkaTemplate.send("fraud-check-topic",transactionId,new TransactionFraudStatus(transactionId, transactionPayloadDTO.getUserId() ,Status.REJECTED))
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Message sent successfully to offset: {}", result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to send message: {}", ex.getMessage());
                        }
                    });
        }
        else if(blackListRepository.existsById(transactionPayloadDTO.getUserId()) && blackListRepository.findById(transactionPayloadDTO.getUserId()).get().getAccountStatus() ==
                AccountStatus.ACTIVE){
            kafkaTemplate.send("fraud-check-topic",transactionId,new TransactionFraudStatus
                    (transactionId, transactionPayloadDTO.getUserId(), Status.ACCEPTED))
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Message sent successfully to offset: {}", result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to send message: {}", ex.getMessage());
                        }
                    });
        }
        else{
            blackListRepository.save(new UserAccountsStatus(transactionPayloadDTO.getUserId(), AccountStatus.ACTIVE));
            kafkaTemplate.send("fraud-check-topic",transactionId,new TransactionFraudStatus
                    (transactionId, transactionPayloadDTO.getUserId(), Status.ACCEPTED))
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Message sent successfully to offset: {}", result.getRecordMetadata().offset());
                        } else {
                            log.error("Failed to send message: {}", ex.getMessage());
                        }
                    });
        }
    }
}
