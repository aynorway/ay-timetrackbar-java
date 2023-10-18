import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import java.awt.event.MouseEvent;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.io.IOException;
import java.net.URL;

public class TimeTrackBar {

    private JFrame mainFrame;
    private JPanel taskPanel;

    public interface MenuSetup {
        void setupMenu(JFrame mainFrame);
    }

    public class MacOSMenuSetup implements MenuSetup {
        @Override
        public void setupMenu(JFrame mainFrame) {
            // 检查是否运行在 macOS 上
            String os = System.getProperty("os.name").toLowerCase();
            if (!os.contains("mac")) {
                System.out.println(System.getProperty("os.name"));
                return;
            }
            // 使用MenuHelper类来设置菜单
            MenuHelper.setupCommonMenu(mainFrame);
        }
    }

    public class WindowsMenuSetup implements MenuSetup {
        @Override
        public void setupMenu(JFrame mainFrame) {
            // 使用MenuHelper类来设置菜单
            MenuHelper.setupCommonMenu(mainFrame);
        }
    }

    public TimeTrackBar() {
        UIManagerHelper.setDefaultUIFont(new FontUIResource("Serif", Font.PLAIN, 12));

        mainFrame = new JFrame("");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        mainFrame.add(taskPanel, BorderLayout.PAGE_START);

        addNewTimerTask(true);

        MenuSetup menuSetup;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) {
            menuSetup = new MacOSMenuSetup();
        } else if (os.contains("win")) {
            menuSetup = new WindowsMenuSetup();
        } else {
            // 对于其他未知操作系统，可能不提供特定的菜单设置
            menuSetup = frame -> {
            }; // No-op
        }
        menuSetup.setupMenu(mainFrame);

        mainFrame.setSize(900, 70);
        mainFrame.setVisible(true);
    }

    private void addNewTimerTask(boolean isFirst) {
        TimerTaskPanel timerTask = new TimerTaskPanel(isFirst);
        taskPanel.add(timerTask);
        mainFrame.setSize(900, mainFrame.getHeight() + 45);
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
        private Clip clip;
        private boolean isSoundEnabled = true; // 初始状态不静音
        private boolean isStopwatchMode = false; // 是否是秒表模式
        private boolean isTimerRunning = false; // 是否计时器正在运行
        private boolean isTimerPaused = false; // 是否计时器暂停了
        private boolean isTimerFinished = true; // 是否计时器停止了

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
            addHoverEffectWithOriginalColor(controlButton, Color.MAGENTA);

            nameField = new ClearableTextField();
            nameField.setColumns(7);
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
            progressBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 15));
            progressBar.setStringPainted(true);
            add(progressBar, BorderLayout.CENTER);

            JPanel eastPanel = new JPanel();

            // 定义新的timeInputPanel
            timeInputPanel = new JPanel();
            daysField = new JTextField(2);
            daysField.addKeyListener(enterKeyAdapter);
            timeInputPanel.add(daysField);
            timeInputPanel.add(new JLabel("d"));

            hoursField = new JTextField(2);
            hoursField.addKeyListener(enterKeyAdapter);
            timeInputPanel.add(hoursField);
            timeInputPanel.add(new JLabel("h"));

            minutesField = new JTextField(2);
            minutesField.addKeyListener(enterKeyAdapter);
            timeInputPanel.add(minutesField);
            timeInputPanel.add(new JLabel("m"));

            secondsField = new JTextField(2);
            secondsField.addKeyListener(enterKeyAdapter);
            timeInputPanel.add(secondsField);
            timeInputPanel.add(new JLabel("s"));
            eastPanel.add(timeInputPanel);

            JButton toggleButton = new JButton("⏲"); // ⏲ \u23F2 用于显示/隐藏时间设置部分
            toggleButton.setPreferredSize(new Dimension(40, 30));
            toggleButton.setForeground(Color.MAGENTA);
            toggleButton.addActionListener(e -> {
                timeInputPanel.setVisible(!timeInputPanel.isVisible());
                TimeTrackBar.this.mainFrame.revalidate(); // 确保面板重新布局
            });
            eastPanel.add(toggleButton);
            addHoverEffectWithOriginalColor(toggleButton, Color.CYAN);

            soundToggleButton = new JButton("\u266B"); // ♫ \u266B
            soundToggleButton.setPreferredSize(new Dimension(40, 30));
            soundToggleButton.setForeground(Color.ORANGE);
            soundToggleButton.addActionListener(e -> toggleSound());
            eastPanel.add(soundToggleButton);
            addHoverEffectWithOriginalColor(soundToggleButton, Color.CYAN);

            countdownRemainingTime = new JLabel("0d 0h 0m 0s");
            eastPanel.add(countdownRemainingTime);

            // 开始的入口
            startButton = new JButton("▶"); // ▶ \u25B6
            startButton.setForeground(Color.GREEN);
            startButton.setPreferredSize(new Dimension(40, 30));

            startButton.addActionListener(e -> triggerStartButtonAction());
            eastPanel.add(startButton);
            addHoverEffectWithOriginalColor(startButton, Color.CYAN);

            stopButton = new JButton("⏹"); // ⏹ \u23F9
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
            eastPanel.add(stopButton);
            addHoverEffectWithOriginalColor(stopButton, Color.CYAN);

            add(eastPanel, BorderLayout.EAST);

        }

        private void addHoverEffectToButton(JButton button, Color hoverColor, Color normalColor) {
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setForeground(hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setForeground(normalColor);
                }
            });
        }

        private void addHoverEffectWithOriginalColor(JButton button, Color hoverColor) {
            button.addMouseListener(new MouseAdapter() {
                Color originalColor = button.getForeground();

                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setForeground(hoverColor);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setForeground(originalColor);
                }
            });
        }

        private KeyAdapter enterKeyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    triggerStartButtonAction();
                }
            }
        };

        private void triggerStartButtonAction() {
            if (isTimerFinished) {
                // 当计时器已经结束时的逻辑
                selectTimerMode();
                isTimerFinished = false; // 重置标志
            } else if (isTimerRunning) {
                if (isStopwatchMode) {
                    if (isTimerPaused) {
                        resumeStopwatch();
                    } else {
                        pauseStopwatch();
                    }
                } else {
                    if (isTimerPaused) {
                        resumeCountdownTimer();
                    } else {
                        pauseCountdownTimer();
                    }
                }
            } else {
                selectTimerMode();
            }
        }

        private void selectTimerMode() {
            if (daysField.getText().isEmpty() && hoursField.getText().isEmpty()
                    && minutesField.getText().isEmpty() && secondsField.getText().isEmpty()) {
                startStopwatch();
            } else {
                startCountdownTimer();
            }
        }

        private void updateDisplay(int currentValue, int displayDuration) {
            int displayTime = isStopwatchMode ? currentValue : displayDuration - currentValue;

            int daysLeft = displayTime / (24 * 60 * 60);
            displayTime %= 24 * 60 * 60;
            int hoursLeft = displayTime / (60 * 60);
            displayTime %= 60 * 60;
            int minutesLeft = displayTime / 60;
            int secondsLeft = displayTime % 60;

            countdownRemainingTime
                    .setText(daysLeft + "d " + hoursLeft + "h " + minutesLeft + "m " + secondsLeft + "s");
        }

        private void initializeProgressBar(int duration) {
            progressBar.setMaximum(duration);
            progressBar.setValue(0);
            timeInputPanel.setVisible(false);
            TimeTrackBar.this.mainFrame.revalidate();
        }

        private void pauseStopwatch() {
            if (isStopwatchMode && countdownTimer != null && isTimerRunning && !isTimerPaused) {
                countdownTimer.stop();
                isTimerPaused = true;
                startButton.setText("▶"); // ▶ \u25B6
                startButton.setForeground(Color.BLUE);
            }
        }

        private void pauseCountdownTimer() {
            if (!isStopwatchMode && countdownTimer != null && isTimerRunning && !isTimerPaused) {
                countdownTimer.stop();
                isTimerPaused = true;
                startButton.setText("▶"); // ▶ \u25B6
                startButton.setForeground(Color.MAGENTA);
            }
        }

        private void resumeStopwatch() {
            if (isStopwatchMode && countdownTimer != null && isTimerPaused) {
                countdownTimer.start();
                isTimerPaused = false;
                startButton.setText("\u23F8"); // ⏸ \u23F8
                startButton.setForeground(Color.BLUE);
            }
        }

        private void resumeCountdownTimer() {
            if (!isStopwatchMode && countdownTimer != null && isTimerPaused) {
                countdownTimer.start();
                isTimerPaused = false;
                startButton.setText("\u23F8"); // ⏸ \u23F8
                startButton.setForeground(Color.MAGENTA);
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
                JOptionPane.showMessageDialog(mainFrame,
                        "请输入有效的时间数值。 \n Please enter a valid time value. \n Vennligst skriv inn en gyldig tidverdi.");
                daysField.setText("");
                hoursField.setText("");
                minutesField.setText("");
                secondsField.setText("");
                return;
            }

            initializeProgressBar(countdownDuration);
            startButton.setText("\u23F8"); // ⏸ \u23F8
            startButton.setForeground(Color.MAGENTA);
            addHoverEffectToButton(startButton, Color.CYAN, Color.MAGENTA);
            stopButton.setForeground(Color.RED);
            stopButton.setEnabled(true);
            addHoverEffectToButton(stopButton, Color.CYAN, Color.RED);

            countdownTimer = new Timer(1000, e -> {
                int currentValue = progressBar.getValue();
                if (currentValue < countdownDuration) {
                    progressBar.setValue(currentValue + 1);
                    updateDisplay(currentValue + 1, countdownDuration);
                } else {
                    countdownTimer.stop();
                    timeInputPanel.setVisible(true);
                    TimeTrackBar.this.mainFrame.revalidate();
                    if (isSoundEnabled) {
                        playAlarmSound();
                    }
                    isTimerFinished = true;
                }
            });

            // 在开始倒计时之前停止任何正在播放的音乐
            if (clip != null && clip.isActive()) {
                clip.stop();
            }
            countdownTimer.start();
            isTimerRunning = true;
            isTimerPaused = false;
            isTimerFinished = false;
        }

        private void startStopwatch() {
            initializeProgressBar(Integer.MAX_VALUE);
            startButton.setText("\u23F8"); // ⏸ \u23F8
            startButton.setForeground(Color.BLUE);
            addHoverEffectToButton(startButton, Color.CYAN, Color.BLUE);
            stopButton.setForeground(Color.RED);
            stopButton.setEnabled(true);
            addHoverEffectToButton(stopButton, Color.CYAN, Color.RED);

            isStopwatchMode = true;

            countdownTimer = new Timer(1000, e -> {
                int currentValue = progressBar.getValue();
                progressBar.setValue(currentValue + 1);
                updateDisplay(currentValue + 1, Integer.MAX_VALUE);
            });

            countdownTimer.start();
            isTimerRunning = true;
            isTimerPaused = false;
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
            startButton.setText("▶"); // ▶ \u25B6
            startButton.setForeground(Color.GREEN);
            addHoverEffectToButton(startButton, Color.CYAN, Color.GREEN);
            stopButton.setEnabled(false);
            progressBar.setValue(0); // 重置progressBar的值
            timeInputPanel.setVisible(true);
            TimeTrackBar.this.mainFrame.revalidate();
            countdownRemainingTime.setText("0d 0h 0m 0s");
        }

        private void stopStopwatch() {
            // 此处可以加入任何特定于秒表模式的逻辑，如保存秒表时间、记录等
            resetTimerState();
        }

        private void stopCountdownTimer() {
            // 此处可以加入任何特定于倒计时模式的逻辑，如显示倒计时完成的消息等
            resetTimerState();
        }

        private void toggleSound() {
            if (clip != null) {
                clip.stop();
            }
            isSoundEnabled = !isSoundEnabled;

            if (isSoundEnabled) {
                soundToggleButton.setText("\u266B"); // ♫ \u266B
                soundToggleButton.setForeground(Color.ORANGE);

            } else {
                soundToggleButton.setText("\u263D"); // ☽ \u263D
                soundToggleButton.setForeground(Color.BLUE);
            }
        }

        private void playAlarmSound() {
            if (!isSoundEnabled) {
                return;
            }

            try {
                if (isSoundEnabled && clip == null) {
                    URL audioURL = TimeTrackBar.class.getResource("/timbre_whaaat.wav");
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioURL);
                    clip = AudioSystem.getClip();
                    clip.open(audioStream);
                }
                clip.setFramePosition(0); // 将音乐重置到开始位置
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "无法播放声音。\n Unable to play sound.\n Kan ikke spille av lyd.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "发生了其他错误。\n An error occurred.\n Det oppstod en feil.");
            }
        }

    }

    public static void main(String[] args) {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        SwingUtilities.invokeLater(TimeTrackBar::new);
    }
}
