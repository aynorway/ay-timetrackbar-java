import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

public class TimeTrackBar {
    private JFrame mainFrame;
    private JPanel taskPanel;

    public TimeTrackBar() {
        mainFrame = new JFrame("老6倒计时-时间进度条");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        mainFrame.add(taskPanel, BorderLayout.PAGE_START);

        addNewTimerTask(true);

        mainFrame.setSize(900, 70);
        mainFrame.setVisible(true);
    }

    private void addNewTimerTask(boolean isFirst) {
        TimerTaskPanel timerTask = new TimerTaskPanel(isFirst);
        taskPanel.add(timerTask);
        mainFrame.setSize(900, mainFrame.getHeight() + 45);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private void removeTimerTask(TimerTaskPanel timerTask) {
        taskPanel.remove(timerTask);
        mainFrame.setSize(900, mainFrame.getHeight() - 45);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    private class TimerTaskPanel extends JPanel {
        private JProgressBar progressBar;
        private JTextField nameField, daysField, hoursField, minutesField, secondsField;
        private JLabel countdownRemainingTime;
        private int countdownDuration;
        private Timer countdownTimer;
        private JPanel timeInputPanel; // 新增的面板，用于容纳时间设置部分
        private JButton soundToggleButton; // 警报声音按钮
        private JButton startButton, stopButton;
        private boolean isTimerRunning = false;
        private boolean isTimerPaused = false;
        private Clip clip;
        private boolean isSoundEnabled = true; // 初始状态不静音
        // private boolean isTimerMode = false; // 默认为倒计时模式
        private boolean isStopwatchMode = false; // 新增变量用于判断当前是否为秒表模式

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

            // ProgressBar
            progressBar = new JProgressBar();
            progressBar.setValue(0);
            progressBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 30));
            progressBar.setStringPainted(true);
            add(progressBar, BorderLayout.CENTER);

            JPanel eastPanel = new JPanel();

            // 定义新的timeInputPanel
            timeInputPanel = new JPanel();
            daysField = new JTextField(2);
            timeInputPanel.add(daysField);
            timeInputPanel.add(new JLabel("d"));
            hoursField = new JTextField(2);
            timeInputPanel.add(hoursField);
            timeInputPanel.add(new JLabel("h"));
            minutesField = new JTextField(2);
            timeInputPanel.add(minutesField);
            timeInputPanel.add(new JLabel("m"));
            secondsField = new JTextField(2);
            timeInputPanel.add(secondsField);
            timeInputPanel.add(new JLabel("s"));
            eastPanel.add(timeInputPanel);

            JButton toggleButton = new JButton("⏲"); // 使用时钟字符，用于显示/隐藏时间设置部分
            toggleButton.setPreferredSize(new Dimension(40, 30));
            toggleButton.setForeground(Color.MAGENTA);
            toggleButton.addActionListener(e -> {
                timeInputPanel.setVisible(!timeInputPanel.isVisible());
                TimeTrackBar.this.mainFrame.revalidate(); // 确保面板重新布局
            });
            eastPanel.add(toggleButton);

            soundToggleButton = new JButton("\u266B"); // 默认为有声音的状态
            Font emojiFont = new Font("Apple Color Emoji", Font.PLAIN, 12); // For macOS
            soundToggleButton.setFont(emojiFont);
            soundToggleButton.setForeground(Color.ORANGE);
            System.out.println(soundToggleButton.getFont());
            soundToggleButton.setPreferredSize(new Dimension(40, 30));
            soundToggleButton.addActionListener(e -> toggleSound());
            eastPanel.add(soundToggleButton);

            countdownRemainingTime = new JLabel("0d 0h 0m 0s");
            eastPanel.add(countdownRemainingTime);

            // 开始的入口
            startButton = new JButton("▶");
            startButton.setForeground(Color.GREEN);
            startButton.setPreferredSize(new Dimension(40, 30));
            startButton.addActionListener(e -> {
                if (isStopwatchMode) {
                    stopStopwatch();
                } else {
                    startCountdownOrStopwatch();
                }
            });

            stopButton = new JButton("⏹");
            stopButton.setForeground(Color.GRAY);
            stopButton.setEnabled(false); // 灰色时设为不可点击
            stopButton.setPreferredSize(new Dimension(40, 30));
            stopButton.addActionListener(e -> {
                if (isTimerRunning) {
                    if (isStopwatchMode) {
                        stopStopwatch();
                    } else {
                        stopCountdownTimer();
                    }
                }
            });

            eastPanel.add(startButton);
            eastPanel.add(stopButton);
            add(eastPanel, BorderLayout.EAST);

        }

        private void startCountdownOrStopwatch() {
            if (daysField.getText().isEmpty() && hoursField.getText().isEmpty() && minutesField.getText().isEmpty()
                    && secondsField.getText().isEmpty()) {
                startStopwatch();
            } else {
                startCountdownTimer();
            }
        }

        private void updateProgressDisplay() {
            int currentValue = progressBar.getValue();
            if (isStopwatchMode || currentValue < countdownDuration) {
                progressBar.setValue(currentValue + 1);
                int displayTime = isStopwatchMode ? currentValue : countdownDuration - currentValue;
                int daysLeft = displayTime / (24 * 60 * 60);
                displayTime %= 24 * 60 * 60;
                int hoursLeft = displayTime / (60 * 60);
                displayTime %= 60 * 60;
                int minutesLeft = displayTime / 60;
                int secondsLeft = displayTime % 60;
                countdownRemainingTime
                        .setText(daysLeft + "d " + hoursLeft + "h " + minutesLeft + "m " + secondsLeft + "s");
            }
        }

        private void startCountdownTimer() {
            if (isTimerRunning) {
                if (isStopwatchMode) {
                    stopStopwatch();
                } else {
                    stopCountdownTimer();
                }
                return;
            }

            // 检查是否有输入时间
            if (daysField.getText().isEmpty() && hoursField.getText().isEmpty() && minutesField.getText().isEmpty()
                    && secondsField.getText().isEmpty()) {
                startStopwatch();
                return;
            }

            if (countdownTimer != null) {
                countdownTimer.stop();
            }

            try {
                int days = daysField.getText().isEmpty() ? 0 : Integer.parseInt(daysField.getText());
                int hours = hoursField.getText().isEmpty() ? 0 : Integer.parseInt(hoursField.getText());
                int minutes = minutesField.getText().isEmpty() ? 0 : Integer.parseInt(minutesField.getText());
                int seconds = secondsField.getText().isEmpty() ? 0 : Integer.parseInt(secondsField.getText());
                countdownDuration = seconds + minutes * 60 + hours * 60 * 60 + days * 24 * 60 * 60;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "请输入有效的时间数值.");
                return;
            }

            progressBar.setMaximum(countdownDuration);
            progressBar.setValue(0);

            // 当计时开始时，隐藏timeInputPanel
            timeInputPanel.setVisible(false);
            TimeTrackBar.this.mainFrame.revalidate();

            // 每1000ms刷新一次Timer
            countdownTimer = new Timer(1000, e -> {
                int currentValue = progressBar.getValue();
                // 更新倒计时显示的逻辑
                if (currentValue < countdownDuration) {
                    progressBar.setValue(currentValue + 1);
                    int remainingTime = countdownDuration - currentValue - 1;
                    int daysLeft = remainingTime / (24 * 60 * 60);
                    remainingTime %= 24 * 60 * 60;
                    int hoursLeft = remainingTime / (60 * 60);
                    remainingTime %= 60 * 60;
                    int minutesLeft = remainingTime / 60;
                    int secondsLeft = remainingTime % 60;
                    countdownRemainingTime
                            .setText(daysLeft + "d " + hoursLeft + "h " + minutesLeft + "m " + secondsLeft + "s");
                } else {
                    // 倒计时结束，所以停止计时器
                    ((Timer) e.getSource()).stop();
                    // 当计时结束时，再打开timeInputPanel
                    timeInputPanel.setVisible(true);
                    TimeTrackBar.this.mainFrame.revalidate();
                    if (isSoundEnabled) {
                        // 播放警报声音
                        playAlarmSound();
                    }
                }
            });

            countdownTimer.start();
            isTimerRunning = true;
            startButton.setText("\u23F8");
            startButton.setForeground(Color.GREEN);
            startButton.removeActionListener(startButton.getActionListeners()[0]); // 移除旧的监听器
            startButton.addActionListener(e -> stopCountdownTimer()); // 添加一个新的监听器来停止计时
            stopButton.setForeground(Color.RED);
            stopButton.setEnabled(true);

        }

        private void startStopwatch() {
            progressBar.setMaximum(Integer.MAX_VALUE); // 设置一个大的最大值
            progressBar.setValue(0);
            isStopwatchMode = true;

            countdownTimer = new Timer(1000, e -> {
                int currentValue = progressBar.getValue();
                progressBar.setValue(currentValue + 1);

                int daysLeft = currentValue / (24 * 60 * 60);
                currentValue %= 24 * 60 * 60;
                int hoursLeft = currentValue / (60 * 60);
                currentValue %= 60 * 60;
                int minutesLeft = currentValue / 60;
                int secondsLeft = currentValue % 60;

                countdownRemainingTime
                        .setText(daysLeft + "d " + hoursLeft + "h " + minutesLeft + "m " + secondsLeft + "s");
            });

            updateProgressDisplay();

            countdownTimer.start();
            isTimerRunning = true;
            startButton.setText("\u23F8");
            startButton.setForeground(Color.GREEN);
            // 当计时开始时，隐藏timeInputPanel
            timeInputPanel.setVisible(false);
            TimeTrackBar.this.mainFrame.revalidate();
            stopButton.setForeground(Color.RED);
            stopButton.setEnabled(true);
        }

        private void resetTimerState() {
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            if (clip != null && clip.isActive()) {
                clip.stop();
            }
            isTimerRunning = false;
            isStopwatchMode = false;
            startButton.setText("▶");
            startButton.setForeground(Color.GREEN);
            stopButton.setForeground(Color.GRAY);
            stopButton.setEnabled(false);
            timeInputPanel.setVisible(true);
            TimeTrackBar.this.mainFrame.revalidate();
        }

        private void stopStopwatch() {
            resetTimerState();
        }

        private void stopCountdownTimer() {
            resetTimerState();
            startButton.removeActionListener(startButton.getActionListeners()[0]); // 移除旧的监听器
            startButton.addActionListener(e -> startCountdownTimer()); // 添加回原始的监听器
        }

        private void toggleSound() {
            if (clip != null && clip.isActive()) { // 如果音乐正在播放
                clip.stop();
            }

            isSoundEnabled = !isSoundEnabled;
            updateSoundButtonAppearance();
        }

        private void updateSoundButtonAppearance() {
            if (isSoundEnabled) {
                soundToggleButton.setText("\u266B");
                soundToggleButton.setForeground(Color.ORANGE);
            } else {
                soundToggleButton.setText("\u263D");
                soundToggleButton.setForeground(Color.BLUE);
            }
        }

        private void playAlarmSound() {
            if (!isSoundEnabled) {
                return;
            }

            try {
                URL audioURL = TimeTrackBar.class.getResource("/timbre_whaaat.wav");
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioURL);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "无法播放声音。");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "发生了其他错误。");
            }
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimeTrackBar::new);
    }
}
