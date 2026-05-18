package com.finsightai.web.service.transaction;

import com.finsightai.web.dto.transaction.TransactionCandidate;
import com.finsightai.web.dto.transaction.TransactionResponse;
import com.finsightai.web.exception.InvalidPeriodException;
import com.finsightai.web.exception.StatementNotFoundException;
import com.finsightai.web.exception.TransactionNotFoundException;
import com.finsightai.web.exception.TransactionSaveException;
import com.finsightai.web.mapper.TransactionMapper;
import com.finsightai.web.model.Statement;
import com.finsightai.web.model.Transaction;
import com.finsightai.web.model.User;
import com.finsightai.web.repository.StatementRepository;
import com.finsightai.web.repository.TransactionRepository;
import com.finsightai.web.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final StatementRepository statementRepository;
    private final TransactionMapper transactionMapper;

    private static final DateTimeFormatter PERIOD_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    @Transactional
    public List<Transaction> saveCandidates(List<TransactionCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<Transaction> transactionsToSave = new ArrayList<>();

        for (TransactionCandidate candidate : candidates) {
            validateCandidate(candidate);

            User user = userRepository.findById(candidate.getUserId())
                    .orElseThrow(() -> new InvalidPeriodException("Пользователь не найден"));

            Statement statement = statementRepository.findById(candidate.getStatementId())
                    .orElseThrow(() -> new StatementNotFoundException("Выписка не найдена"));

            if (!statement.getUser().getId().equals(user.getId())) {
                throw new TransactionNotFoundException("Выписка не принадлежит пользователю");
            }

            boolean duplicateExists = transactionRepository
                    .existsByUserIdAndBankAndPeriodAndDateAndTimeAndAmountAndDescription(
                            candidate.getUserId(),
                            candidate.getBank(),
                            candidate.getPeriod(),
                            candidate.getDate(),
                            candidate.getTime(),
                            candidate.getAmount(),
                            candidate.getDescription()
                    );

            if (duplicateExists) {
                continue;
            }

            Transaction transaction = transactionMapper.toEntity(candidate, user, statement);
            transactionsToSave.add(transaction);
        }

        if (transactionsToSave.isEmpty()) {
            return List.of();
        }

        return transactionRepository.saveAll(transactionsToSave);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getUserTransactions(UUID userId, String period) {
        validatePeriod(period);

        return transactionRepository
                .findAllByUserIdAndPeriodOrderByDateDescTimeDesc(userId, period)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getUserTransaction(UUID userId, UUID transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(TransactionNotFoundException::new);

        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public void deleteByStatementId(UUID statementId) {
        transactionRepository.deleteAllByStatementId(statementId);
    }

    private void validateCandidate(TransactionCandidate candidate) {
        if (candidate == null) {
            throw new TransactionSaveException("Операция не может быть пустой");
        }

        if (candidate.getUserId() == null) {
            throw new TransactionSaveException("Не указан пользователь операции");
        }

        if (candidate.getStatementId() == null) {
            throw new TransactionSaveException("Не указана выписка операции");
        }

        if (candidate.getBank() == null) {
            throw new TransactionSaveException("Не указан банк операции");
        }

        validatePeriod(candidate.getPeriod());

        if (candidate.getDate() == null) {
            throw new TransactionSaveException("Не указана дата операции");
        }

        if (candidate.getTime() == null) {
            throw new TransactionSaveException("Не указано время операции");
        }

        if (candidate.getType() == null) {
            throw new TransactionSaveException("Не указан тип операции");
        }

        if (candidate.getAmount() == null) {
            throw new TransactionSaveException("Не указана сумма операции");
        }

        if (candidate.getCategory() == null || candidate.getCategory().isBlank()) {
            throw new TransactionSaveException("Не указана категория операции");
        }

        if (candidate.getDescription() == null || candidate.getDescription().isBlank()) {
            throw new TransactionSaveException("Не указано описание операции");
        }
    }

    private void validatePeriod(String period) {
        if (period == null || period.isBlank()) {
            throw new InvalidPeriodException("Период не указан");
        }

        try {
            YearMonth.parse(period, PERIOD_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidPeriodException("Период должен быть в формате YYYY-MM");
        }
    }
}