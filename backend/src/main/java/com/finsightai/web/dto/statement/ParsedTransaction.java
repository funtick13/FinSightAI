package com.finsightai.web.dto.statement;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParsedTransaction {
    private String rawDate;
    private String rawTime;
    private String rawCategory;
    private String rawDescription;
    private String rawAmount;
    private String rawLine;
}
