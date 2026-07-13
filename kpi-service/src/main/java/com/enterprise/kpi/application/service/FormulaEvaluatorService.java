package com.enterprise.kpi.application.service;

import com.enterprise.kpi.domain.exception.FormulaEvaluationException;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FormulaEvaluatorService {

    public double evaluate(String formula, Map<String, Double> variables) {
        if (formula == null || formula.trim().isEmpty()) {
            return 0.0;
        }

        ExpressionBuilder builder = new ExpressionBuilder(formula);
        if (variables != null) {
            builder.variables(variables.keySet());
        }

        Expression expression = builder.build();
        if (variables != null) {
            expression.setVariables(variables);
        }

        double result = expression.evaluate();
        
        if (Double.isInfinite(result) || Double.isNaN(result)) {
            throw new FormulaEvaluationException("Division by zero or invalid formula");
        }
        
        return result;
    }
}
