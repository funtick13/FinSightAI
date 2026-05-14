package com.finsightai.web.service.statement.duplicate;

import com.finsightai.web.dto.statement.TransactionCandidate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class DuplicateTransactionDetector {

    public List<TransactionCandidate> removeDuplicates(List<TransactionCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<TransactionCandidate> uniqueCandidates = new ArrayList<>();
        Set<TransactionKey> seenKeys = new LinkedHashSet<>();

        for (TransactionCandidate candidate : candidates) {
            TransactionKey key = TransactionKey.from(candidate);
            if (seenKeys.add(key)) {
                uniqueCandidates.add(candidate);
            }
        }

        // TODO: when Operation persistence is introduced, check existing rows by user, bank and period here.
        return uniqueCandidates;
    }

    private record TransactionKey(
            Object date,
            Object time,
            String amount,
            String description,
            Object bank,
            String period
    ) {
        private static TransactionKey from(TransactionCandidate candidate) {
            return new TransactionKey(
                    candidate.date(),
                    candidate.time(),
                    candidate.amount().stripTrailingZeros().toPlainString(),
                    candidate.description(),
                    candidate.bank(),
                    candidate.period()
            );
        }
    }
}
