import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TimeTrackBar {
    private JFrame frame;
    private JPanel taskPanel;

    public TimeTrackBar() {
        frame = new JFrame("老6时间进度条");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        frame.add(taskPanel, BorderLayout.PAGE_START);

        addNewTimerTask(true);

        frame.setSize(700, 70);
        frame.setVisible(true);
    }

    private void addNewTimerTask(boolean isFirst) {
        TimerTaskPanel timerTask = new TimerTaskPanel(isFirst);
        taskPanel.add(timerTask);
        frame.setSize(700, frame.getHeight() + 40);
    }

    private void removeTimerTask(TimerTaskPanel timerTask) {
        taskPanel.remove(timerTask);
        frame.setSize(700, frame.getHeight() - 40);
        frame.revalidate();
        frame.repaint();
    }

    private class TimerTaskPanel extends JPanel {
        private JProgressBar progressBar;
        private JTextField nameField, daysField, hoursField, minutesField, secondsField;
        private JLabel remainingTimeLabel;
        private int duration;
        private Timer timer;
        private JPanel timeInputPanel;  // 新增的面板，用于容纳时间设置部分

        public TimerTaskPanel(boolean isFirst) {
            super(new BorderLayout());

            JPanel westPanel = new JPanel();
            JButton controlButton;
            if (isFirst) {
                controlButton = new JButton("+");
                controlButton.setPreferredSize(new Dimension(40, 30));
                controlButton.addActionListener(e -> addNewTimerTask(false));
            } else {
                controlButton = new JButton("-");
                controlButton.setPreferredSize(new Dimension(40, 30));
                controlButton.addActionListener(e -> removeTimerTask(this));
            }
            westPanel.add(controlButton);

            nameField = new JTextField(7);
            nameField.addKeyListener(new KeyAdapter() {
                public void keyTyped(KeyEvent e) {
                    if (nameField.getText().length() >= 7) {
                        e.consume();
                    }
                }
            });
            westPanel.add(nameField);
            add(westPanel, BorderLayout.WEST);

            progressBar = new JProgressBar();
            progressBar.setValue(0);
            progressBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
            progressBar.setStringPainted(true);
            add(progressBar, BorderLayout.CENTER);

            JPanel eastPanel = new JPanel();

            // 定义新的timeInputPanel
            timeInputPanel = new JPanel();
            daysField = new JTextField(2);
            daysField.setText("0");
            timeInputPanel.add(daysField);
            timeInputPanel.add(new JLabel("d"));
            hoursField = new JTextField(2);
            hoursField.setText("0");
            timeInputPanel.add(hoursField);
            timeInputPanel.add(new JLabel("h"));
            minutesField = new JTextField(2);
            minutesField.setText("0");
            timeInputPanel.add(minutesField);
            timeInputPanel.add(new JLabel("m"));
            secondsField = new JTextField(2);
            secondsField.setText("0");
            timeInputPanel.add(secondsField);
            timeInputPanel.add(new JLabel("s"));
            eastPanel.add(timeInputPanel);

            remainingTimeLabel = new JLabel("0d 0h 0m 0s");
            JButton startButton = new JButton("▶");
            startButton.setForeground(Color.GREEN);
            startButton.setPreferredSize(new Dimension(40, 30));
            startButton.addActionListener(e -> startCountdown());

            JButton toggleButton = new JButton("⏲");  // 使用时钟字符，用于显示/隐藏时间设置部分
            toggleButton.setPreferredSize(new Dimension(40, 30));
            toggleButton.addActionListener(e -> {
                timeInputPanel.setVisible(!timeInputPanel.isVisible());
                TimeTrackBar.this.frame.revalidate();  // 确保面板重新布局
            });
            eastPanel.add(toggleButton);

            eastPanel.add(remainingTimeLabel);
            eastPanel.add(startButton);
            add(eastPanel, BorderLayout.EAST);
        }

        private void startCountdown() {
            if (timer != null) {
                timer.stop();
            }

            try {
                int days = Integer.parseInt(daysField.getText());
                int hours = Integer.parseInt(hoursField.getText());
                int minutes = Integer.parseInt(minutesField.getText());
                int seconds = Integer.parseInt(secondsField.getText());
                duration = seconds + minutes * 60 + hours * 60 * 60 + days * 24 * 60 * 60;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "请输入有效的时间数值.");
                return;
            }

            progressBar.setMaximum(duration);
            progressBar.setValue(0);

            // 当计时开始时，隐藏timeInputPanel
            timeInputPanel.setVisible(false);
            TimeTrackBar.this.frame.revalidate();

            timer = new Timer(1000, e -> {
                int currentValue = progressBar.getValue();
                if (currentValue < duration) {
                    progressBar.setValue(currentValue + 1);
                    int remainingTime = duration - currentValue - 1;
                    int daysLeft = remainingTime / (24 * 60 * 60);
                    remainingTime %= 24 * 60 * 60;
                    int hoursLeft = remainingTime / (60 * 60);
                    remainingTime %= 60 * 60;
                    int minutesLeft = remainingTime / 60;
                    int secondsLeft = remainingTime % 60;
                    remainingTimeLabel.setText(daysLeft + "d " + hoursLeft + "h " + minutesLeft + "m " + secondsLeft + "s");
                } else {
                    ((Timer) e.getSource()).stop();
                }
            });
            timer.start();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimeTrackBar::new);
    }
}
