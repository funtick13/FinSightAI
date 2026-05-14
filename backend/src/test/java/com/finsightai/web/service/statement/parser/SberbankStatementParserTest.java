package com.finsightai.web.service.statement.parser;

import com.finsightai.web.dto.statement.ParsedTransaction;
import com.finsightai.web.exception.StatementParsingException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SberbankStatementParserTest {

    private final SberbankStatementParser parser = new SberbankStatementParser();

    @Test
    void parseExtractsTransactionLinesFromRawText() {
        String rawText = """
                СберБанк
                16.04.2026 19:28 Транспорт 37,00 167,89
                18.04.2026 410489 TPP_VPT SBERTROJKA Voronezh RUS. Операция по карте
                ****0000
                17.04.2026 00:59 Перевод СБП +200,00 204,89
                17.04.2026 515459 Перевод от клиента. Операция по карте
                ****0000
                """;

        List<ParsedTransaction> result = parser.parse(rawText);

        assertEquals(2, result.size());
        assertEquals("16.04.2026", result.get(0).getRawDate());
        assertEquals("19:28", result.get(0).getRawTime());
        assertEquals("Транспорт", result.get(0).getRawCategory());
        assertEquals("TPP_VPT SBERTROJKA Voronezh RUS", result.get(0).getRawDescription());
        assertEquals("37,00", result.get(0).getRawAmount());
        assertEquals("Перевод СБП", result.get(1).getRawCategory());
        assertEquals("Перевод от клиента", result.get(1).getRawDescription());
        assertEquals("+200,00", result.get(1).getRawAmount());
    }

    @Test
    void parseKeepsMultiWordCategoryAndAmountBeforeBalance() {
        String rawText = """
                05.04.2026 14:19 Оплата по QR–коду СБП 100,00 817,77
                05.04.2026 871523 ТОО "ОнлиПэй". Операция по карте ****0000
                04.04.2026 14:08 Здоровье и красота 2 500,00 1 389,74
                04.04.2026 455814 SOVA VORONEZH RUS. Операция по карте ****0000
                """;

        List<ParsedTransaction> result = parser.parse(rawText);

        assertEquals(2, result.size());
        assertEquals("Оплата по QR–коду СБП", result.get(0).getRawCategory());
        assertEquals("100,00", result.get(0).getRawAmount());
        assertEquals("817,77", result.get(0).getRawLine().substring(result.get(0).getRawLine().lastIndexOf(' ') + 1));
        assertEquals("ТОО \"ОнлиПэй\"", result.get(0).getRawDescription());
        assertEquals("Здоровье и красота", result.get(1).getRawCategory());
        assertEquals("2 500,00", result.get(1).getRawAmount());
    }

    @Test
    void parseUsesDefaultCategoryWhenLegacyCategoryIsNotSeparated() {
        String rawText = "03.04.2026 Такси до дома 450,00 ₽";

        List<ParsedTransaction> result = parser.parse(rawText);

        assertEquals(1, result.size());
        assertEquals("Прочее", result.get(0).getRawCategory());
        assertEquals("Такси до дома", result.get(0).getRawDescription());
    }

    @Test
    void parseThrowsWhenNoTransactionsFound() {
        assertThrows(StatementParsingException.class, () -> parser.parse("Выписка без строк операций"));
    }
}
