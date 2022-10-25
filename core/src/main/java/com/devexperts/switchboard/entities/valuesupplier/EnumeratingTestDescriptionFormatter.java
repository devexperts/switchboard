package com.devexperts.switchboard.entities.valuesupplier;

import com.devexperts.switchboard.entities.Test;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This implementation of {@link ValuesExtractor} can be initiated by {@link Test}
 * Returns a test case description consisted of Actions (defined by {@link #actionRegex}) and Results (defined by {@link #resultRegex}).
 * Each action leading to result is followed by additional row like 'Check result (1)' (format specified by {@link #checkResultFormat})
 * referencing the result row number in 'Results' block
 * Each result starts with reference to action like 'After step (1) - ...'
 * Each action and result start with {@link #rowStartTemplate} providing row enumeration in target environment (e.g. '# " for Jira).
 */
public class EnumeratingTestDescriptionFormatter implements TestValuesExtractor {

    @JsonProperty(required = true)
    private TestValuesExtractor rawValueExtractor;
    @JsonProperty(defaultValue = "^\\s*Action:\\s*(.+)")
    private String actionRegex = "^\\s*Action:\\s*(.+)";
    @JsonProperty(defaultValue = "^\\s*Result:\\s*(.+)")
    private String resultRegex = "^\\s*Result:\\s*(.+)";

    @JsonProperty(defaultValue = "*Actions:*")
    private String actionsHeader = "*Actions:*";
    @JsonProperty(defaultValue = "*Results:*")
    private String resultsHeader = "*Results:*";

    @JsonProperty(defaultValue = "# ")
    private String rowStartTemplate = "# ";

    @JsonProperty(defaultValue = "Check result (%s)")
    private String checkResultFormat = "Check result (%s)";
    @JsonProperty(defaultValue = "After step (%s) - ")
    private String afterStepFormat = "After step (%s) - ";


    private EnumeratingTestDescriptionFormatter() {}

    public EnumeratingTestDescriptionFormatter(TestValuesExtractor rawValueExtractor, String actionRegex, String resultRegex, String actionsHeader,
                                               String resultsHeader, String rowStartTemplate, String checkResultFormat, String afterStepFormat)
    {
        this.rawValueExtractor = rawValueExtractor;
        this.actionRegex = actionRegex;
        this.resultRegex = resultRegex;
        this.actionsHeader = actionsHeader;
        this.resultsHeader = resultsHeader;
        this.rowStartTemplate = rowStartTemplate;
        this.checkResultFormat = checkResultFormat;
        this.afterStepFormat = afterStepFormat;
    }

    @Override
    public Set<String> getTestValues(Test t) {
        return new HashSet<>(Collections.singletonList(getTestValue(t)));
    }


    @Override
    public String getTestValue(Test t) {
        List<String> raw = rawValueExtractor.getTestValues(t)
                .stream()
                .flatMap(c -> Arrays.stream(c.split("\n")))
                .collect(Collectors.toList());
        Pattern actionPattern = Pattern.compile(actionRegex);
        Pattern resultPattern = Pattern.compile(resultRegex);

        List<String> undefined = new ArrayList<>();
        List<StringBuilder> actions = new ArrayList<>();
        List<StringBuilder> results = new ArrayList<>();
        RowType latestRow = RowType.UNDEFINED;

        for (String row : raw) {
            Matcher actionMatcher = actionPattern.matcher(row);
            Matcher resultMatcher = resultPattern.matcher(row);

            if (actionMatcher.matches()) {                            // row is explicitly an action?
                latestRow = RowType.ACTION;
                actions.add(new StringBuilder(rowStartTemplate)
                        .append(actionMatcher.group(1)));
            } else if (resultMatcher.matches()) {                      // row is explicitly a result?
                latestRow = RowType.RESULT;
                results.add(new StringBuilder(rowStartTemplate)
                        .append(String.format(afterStepFormat, actions.size()))
                        .append(resultMatcher.group(1)));
                actions.add(new StringBuilder(rowStartTemplate)
                        .append(String.format(checkResultFormat, results.size())));
            } else if (latestRow == RowType.ACTION) {                // we have to predict part of what this row is
                actions.get(actions.size() - 1).append("\n").append(row);
            } else if (latestRow == RowType.RESULT) {
                results.get(results.size() - 1).append("\n").append(row);
            } else {
                undefined.add(row);
            }
        }
        String actionsWithResults = String.format("%s%s\n%s%s",
                actionsHeader, actions.stream().map(StringBuilder::toString).collect(Collectors.joining("\n", "\n", "\n")),
                resultsHeader, results.stream().map(StringBuilder::toString).collect(Collectors.joining("\n", "\n", "\n")));
        return undefined.isEmpty() ? actionsWithResults : String.format("%s\n%s", String.join("\n", undefined), actionsWithResults);
    }

    private enum RowType {
        ACTION, RESULT, UNDEFINED
    }
}