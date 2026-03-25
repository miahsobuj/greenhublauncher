package com.greenhub.launcher;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CalculatorActivity extends AppCompatActivity {

    private TextView displayPrimary;
    private TextView displaySecondary;
    private String currentInput = "";
    private String currentOperator = "";
    private double firstOperand = 0;
    private boolean isNewNumber = true;
    private boolean isScientificMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        
        initViews();
        setupButtons();
    }
    
    private void initViews() {
        displayPrimary = findViewById(R.id.display_primary);
        displaySecondary = findViewById(R.id.display_secondary);
        
        findViewById(R.id.btn_mode_toggle).setOnClickListener(v -> toggleMode());
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }
    
    private void toggleMode() {
        isScientificMode = !isScientificMode;
        HorizontalScrollView scientificPanel = findViewById(R.id.scientific_panel);
        scientificPanel.setVisibility(isScientificMode ? View.VISIBLE : View.GONE);
    }
    
    private void setupButtons() {
        // Number buttons
        int[] numberIds = {R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                          R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.btn_dot};
        
        for (int id : numberIds) {
            findViewById(id).setOnClickListener(v -> onNumberClick(((Button) v).getText().toString()));
        }
        
        // Operator buttons
        int[] operatorIds = {R.id.btn_plus, R.id.btn_minus, R.id.btn_multiply, R.id.btn_divide,
                            R.id.btn_power};
        
        for (int id : operatorIds) {
            findViewById(id).setOnClickListener(v -> onOperatorClick(((Button) v).getText().toString()));
        }
        
        // Scientific buttons
        int[] scientificIds = {R.id.btn_sin, R.id.btn_cos, R.id.btn_tan, R.id.btn_log,
                               R.id.btn_ln, R.id.btn_sqrt, R.id.btn_pi, R.id.btn_e,
                               R.id.btn_open_bracket, R.id.btn_close_bracket};
        
        for (int id : scientificIds) {
            Button btn = findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(v -> onScientificClick(((Button) v).getText().toString()));
            }
        }
        
        // Function buttons
        findViewById(R.id.btn_equals).setOnClickListener(v -> calculateResult());
        findViewById(R.id.btn_clear).setOnClickListener(v -> clearAll());
        findViewById(R.id.btn_delete).setOnClickListener(v -> deleteLast());
        findViewById(R.id.btn_percent).setOnClickListener(v -> onPercent());
        findViewById(R.id.btn_plus_minus).setOnClickListener(v -> toggleSign());
    }
    
    private void onNumberClick(String number) {
        if (isNewNumber) {
            currentInput = number;
            isNewNumber = false;
        } else {
            if (number.equals(".") && currentInput.contains(".")) return;
            currentInput += number;
        }
        updateDisplay();
    }
    
    private void onOperatorClick(String operator) {
        if (!currentInput.isEmpty()) {
            firstOperand = Double.parseDouble(currentInput);
            currentOperator = operator;
            displaySecondary.setText(currentInput + " " + operator);
            isNewNumber = true;
        }
    }
    
    private void onScientificClick(String func) {
        switch (func) {
            case "sin":
                currentInput = String.valueOf(Math.sin(Math.toRadians(getCurrentValue())));
                break;
            case "cos":
                currentInput = String.valueOf(Math.cos(Math.toRadians(getCurrentValue())));
                break;
            case "tan":
                currentInput = String.valueOf(Math.tan(Math.toRadians(getCurrentValue())));
                break;
            case "log":
                currentInput = String.valueOf(Math.log10(getCurrentValue()));
                break;
            case "ln":
                currentInput = String.valueOf(Math.log(getCurrentValue()));
                break;
            case "√":
                currentInput = String.valueOf(Math.sqrt(getCurrentValue()));
                break;
            case "π":
                currentInput = String.valueOf(Math.PI);
                break;
            case "e":
                currentInput = String.valueOf(Math.E);
                break;
        }
        isNewNumber = true;
        updateDisplay();
    }
    
    private double getCurrentValue() {
        try {
            return Double.parseDouble(currentInput.isEmpty() ? "0" : currentInput);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private void calculateResult() {
        if (currentInput.isEmpty() || currentOperator.isEmpty()) return;
        
        double secondOperand = Double.parseDouble(currentInput);
        double result = 0;
        
        switch (currentOperator) {
            case "+":
                result = firstOperand + secondOperand;
                break;
            case "−":
                result = firstOperand - secondOperand;
                break;
            case "×":
                result = firstOperand * secondOperand;
                break;
            case "÷":
                if (secondOperand != 0) {
                    result = firstOperand / secondOperand;
                } else {
                    displayPrimary.setText("Error");
                    return;
                }
                break;
            case "^":
                result = Math.pow(firstOperand, secondOperand);
                break;
        }
        
        String resultText = formatResult(result);
        displaySecondary.setText(firstOperand + " " + currentOperator + " " + secondOperand + " =");
        currentInput = resultText;
        currentOperator = "";
        isNewNumber = true;
        updateDisplay();
    }
    
    private String formatResult(double result) {
        if (result == (long) result) {
            return String.valueOf((long) result);
        } else {
            return String.format("%.8f", result).replaceAll("0+$", "").replaceAll("\\.$", "");
        }
    }
    
    private void clearAll() {
        currentInput = "";
        currentOperator = "";
        firstOperand = 0;
        isNewNumber = true;
        updateDisplay();
        displaySecondary.setText("");
    }
    
    private void deleteLast() {
        if (!currentInput.isEmpty() && !isNewNumber) {
            currentInput = currentInput.substring(0, currentInput.length() - 1);
            updateDisplay();
        }
    }
    
    private void onPercent() {
        if (!currentInput.isEmpty()) {
            double value = Double.parseDouble(currentInput) / 100;
            currentInput = String.valueOf(value);
            updateDisplay();
        }
    }
    
    private void toggleSign() {
        if (!currentInput.isEmpty() && !currentInput.equals("0")) {
            if (currentInput.startsWith("-")) {
                currentInput = currentInput.substring(1);
            } else {
                currentInput = "-" + currentInput;
            }
            updateDisplay();
        }
    }
    
    private void updateDisplay() {
        displayPrimary.setText(currentInput.isEmpty() ? "0" : currentInput);
    }
    
    @Override
    public void onBackPressed() {
        if (!currentInput.isEmpty()) {
            clearAll();
        } else {
            super.onBackPressed();
        }
    }
}