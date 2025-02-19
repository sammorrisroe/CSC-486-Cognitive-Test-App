import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class DigitSpanTest extends JFrame {
    private ArrayList<Character> sequence;
    private int currentStage = 3;
    private int currentIndex = 0;
    private JLabel displayLabel;
    private JTextField inputField;
    private JButton startButton, submitButton, modeButton;
    private Timer displayTimer, dataTimer;
    private Random random;
    private boolean isLetterNumberMode = false;
    private boolean isShowingDigits = false;
    private int numberOfDigits = 0;
    private FileWriter csvWriter;

    public DigitSpanTest() {
        setTitle("Digit Span Test");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        displayLabel = new JLabel("Press Start to begin", SwingConstants.CENTER);
        displayLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(displayLabel, BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.setHorizontalAlignment(JTextField.CENTER);
        inputField.setFont(new Font("Arial", Font.PLAIN, 18));
        inputField.setVisible(false);
        add(inputField, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel();
        startButton = new JButton("Start");
        submitButton = new JButton("Submit");
        modeButton = new JButton("Switch to Letter-Number");
        submitButton.setEnabled(false);
        buttonPanel.add(startButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(modeButton);
        add(buttonPanel, BorderLayout.NORTH);

        random = new Random();

        startButton.addActionListener(e -> startTest());
        submitButton.addActionListener(e -> checkResponse());
        modeButton.addActionListener(e -> toggleMode());

        try {
            // Set up CSV writer to log data
            csvWriter = new FileWriter("digit_span_data.csv");
            csvWriter.append("Time,Mode,Number of Digits,Is Typing,Correctness\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void toggleMode() {
        isLetterNumberMode = !isLetterNumberMode;
        modeButton.setText(isLetterNumberMode ? "Switch to Digit Span" : "Switch to Letter-Number");
    }

    private void startTest() {
        sequence = new ArrayList<>();
        for (int i = 0; i < currentStage; i++) {
            if (isLetterNumberMode) {
                if (random.nextBoolean()) {
                    sequence.add((char) ('A' + random.nextInt(26)));
                } else {
                    sequence.add((char) ('0' + random.nextInt(10)));
                }
            } else {
                sequence.add((char) ('0' + random.nextInt(10)));
            }
        }
        currentIndex = 0;
        inputField.setText("");
        inputField.setVisible(false);
        submitButton.setEnabled(false);
        startButton.setEnabled(false);
        numberOfDigits = sequence.size();
        isShowingDigits = true;
        displayDigits();
    }

    private void displayDigits() {
        if (currentIndex < sequence.size()) {
            displayLabel.setText(String.valueOf(sequence.get(currentIndex)));
            currentIndex++;
            displayTimer = new Timer(1000, e -> displayDigits());
            displayTimer.setRepeats(false);
            displayTimer.start();
        } else {
            displayLabel.setText("Enter the sequence:");
            inputField.setVisible(true);
            submitButton.setEnabled(true);
            isShowingDigits = false;
        }
    }

    private void checkResponse() {
        String response = inputField.getText().trim().toUpperCase();
        StringBuilder correctSequence = new StringBuilder();

        if (isLetterNumberMode) {
            ArrayList<Character> sortedSequence = new ArrayList<>(sequence);
            Collections.sort(sortedSequence, (a, b) -> {
                if (Character.isDigit(a) && Character.isLetter(b)) return -1;
                if (Character.isLetter(a) && Character.isDigit(b)) return 1;
                return a - b;
            });
            for (char ch : sortedSequence) {
                correctSequence.append(ch);
            }
        } else {
            for (char ch : sequence) {
                correctSequence.append(ch);
            }
        }

        boolean isCorrect = response.equals(correctSequence.toString());

        // Log when the user is incorrect
        if (!isCorrect) {
            logData("Incorrect");
            displayLabel.setText("Incorrect. Restarting test.");
            currentStage = 3;
        } else {
            logData("Correct");
            displayLabel.setText("Correct! Increasing difficulty.");
            currentStage++;
        }

        startButton.setEnabled(true);
        inputField.setVisible(false);
        submitButton.setEnabled(false);
    }

    private void logData(String correctness) {
        long time = System.currentTimeMillis();
        String mode = isLetterNumberMode ? "Letter-Number" : "Digit Span";
        int isTyping = isShowingDigits ? 0 : 1;  // 0 for showing digits, 1 for typing response

        try {
            // Log data every 250ms (4 times per second)
            csvWriter.append(time + "," + mode + "," + numberOfDigits + "," + isTyping + "," + correctness + "\n");
            csvWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DigitSpanTest frame = new DigitSpanTest();
            frame.setVisible(true);

            // Start the data logging timer
            Timer dataLoggingTimer = new Timer(250, e -> frame.logData(""));  // This empty log entry is for timing only
            dataLoggingTimer.start();
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        try {
            if (csvWriter != null) {
                csvWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
