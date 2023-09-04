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

    // 初始化窗口
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

    // 添加新任务
    private void addNewTimerTask(boolean isFirst) {
        TimerTaskPanel timerTask = new TimerTaskPanel(isFirst);
        taskPanel.add(timerTask);
        mainFrame.setSize(900, mainFrame.getHeight() + 45);
    }

    // 删除一行任务
    private void removeTimerTask(TimerTaskPanel timerTask) {
        taskPanel.remove(timerTask);
        mainFrame.setSize(900, mainFrame.getHeight() - 45);
        mainFrame.revalidate();
        mainFrame.repaint();
    }

    // TimerTaskPanel 内部类的构造函数。初始化面板的布局并设置相关的UI组件，绑定相关事件的监听器。
    private class TimerTaskPanel extends JPanel {
        private JProgressBar progressBar;
        private JTextField nameField, daysField, hoursField, minutesField, secondsField;
        private JLabel countdownRemainingTime;
        private int countdownDuration;
        private Timer countdownTimer; // 一个timer
        private JPanel timeInputPanel; // 新增的面板，用于容纳时间设置部分
        private JButton soundToggleButton; // 警报声音按钮
        private JButton startPauseButton, stopButton;
        private boolean isTimerRunning = false; // 表示计时器是否正在运行，不论它是倒计时还是秒表。
        private Clip clip;
        private boolean isSoundEnabled = true; // 初始状态为非静音
        private boolean isTimerMode = false; // 表示计时器的模式。如果它是true，计时器是秒表模式，否则它是倒计时模式。
        private boolean isTimerPaused = false; // 用于跟踪计时器是否已暂停

        // TimerTaskPanel 构造函数: 设置面板的外观和功能。添加 "+" 或 "-" 按钮用于添加或移除任务。添加计时器设置部分。
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

            // 开始按键 ▶
            countdownRemainingTime = new JLabel("0d 0h 0m 0s");
            startPauseButton = new JButton("▶");
            startPauseButton.setForeground(Color.GREEN);
            startPauseButton.setPreferredSize(new Dimension(40, 30));
            startPauseButton.addActionListener(e -> startCountdownTimer());

            // 隐藏输入栏按键 ⏲
            JButton toggleButton = new JButton("⏲"); // 使用时钟字符，用于显示/隐藏时间设置部分
            toggleButton.setPreferredSize(new Dimension(40, 30));
            toggleButton.setForeground(Color.MAGENTA);
            toggleButton.addActionListener(e -> {
                timeInputPanel.setVisible(!timeInputPanel.isVisible());
                TimeTrackBar.this.mainFrame.revalidate(); // 确保面板重新布局
            });
            eastPanel.add(toggleButton);

            // 声音按键
            soundToggleButton = new JButton("\u266B"); // 默认为有声音的状态
            Font emojiFont = new Font("Apple Color Emoji", Font.PLAIN, 12); // For macOS
            soundToggleButton.setFont(emojiFont);
            soundToggleButton.setForeground(Color.ORANGE);
            System.out.println(soundToggleButton.getFont());

            soundToggleButton.setPreferredSize(new Dimension(40, 30));
            soundToggleButton.addActionListener(e -> toggleSound());
            eastPanel.add(soundToggleButton);

            // 时间显示面板
            eastPanel.add(countdownRemainingTime);
            eastPanel.add(startPauseButton);
            add(eastPanel, BorderLayout.EAST);

            // 停止按键 ⏹
            stopButton = new JButton("⏹"); // 使用停止符号
            stopButton.setForeground(Color.GRAY);
            stopButton.setPreferredSize(new Dimension(40, 30));
            stopButton.addActionListener(e -> stopCountdownTimer());
            eastPanel.add(stopButton);
            add(eastPanel, BorderLayout.EAST);

        }

        // 开始倒计时 - 如果没有设置时间，则默认为秒表模式。如果有时间设置，就进入倒计时模式。
        private void startCountdownTimer() {
            // 检查是否是秒表
            if (isTimerRunning) {
                if (isTimerMode) {
                    stopTimer();
                } else {
                    stopCountdownTimer();
                }
                return;
            }
            // 检查是否有输入时间
            if (daysField.getText().isEmpty() && hoursField.getText().isEmpty() && minutesField.getText().isEmpty()
                    && secondsField.getText().isEmpty()) {
                startTimer();
                return;
            }
            // 如果有时间设置
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
            // 设置计时数值int
            progressBar.setMaximum(countdownDuration);
            progressBar.setValue(0);

            // 当计时开始时，隐藏timeInputPanel
            timeInputPanel.setVisible(false);
            TimeTrackBar.this.mainFrame.revalidate();

            // 计时器运算，每1秒刷新
            countdownTimer = new Timer(1000, e -> {
                int currentValue = progressBar.getValue();
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
                    ((Timer) e.getSource()).stop();
                    if (isSoundEnabled) {
                        playAlarmSound();
                    }
                }
            });
            // 事件逻辑
            countdownTimer.start();
            isTimerRunning = true;
            startPauseButton.setText("\u23F8");
            startPauseButton.setForeground(Color.MAGENTA);
            stopButton.setForeground(Color.RED);
            startPauseButton.removeActionListener(startPauseButton.getActionListeners()[0]); // 移除旧的监听器
            startPauseButton.addActionListener(e -> togglePauseResume()); // 添加一个新的监听器来停止计时

            startPauseButton.setEnabled(true); // 启用暂停/恢复按钮

            printSizes();
        }

        // 停止倒计时 - 如果停止音乐播放，它也会被停止。
        private void stopCountdownTimer() {
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            if (clip != null) {
                clip.stop();
            }
            isTimerRunning = false;
            startPauseButton.setText("▶");
            startPauseButton.setForeground(Color.GREEN);
            stopButton.setForeground(Color.BLUE);
            startPauseButton.removeActionListener(startPauseButton.getActionListeners()[0]); // 移除旧的监听器
            startPauseButton.addActionListener(e -> startCountdownTimer()); // 添加回原始的监听器

            // 重置进度条和时间显示
            // progressBar.setValue(0);
            // countdownRemainingTime.setText("0d 0h 0m 0s");
            // stopButton.addActionListener(e -> stopTimer());

        }

        // 开始秒表
        private void startTimer() {
            progressBar.setMaximum(Integer.MAX_VALUE); // 设置一个大的最大值
            progressBar.setValue(0);
            isTimerMode = true;

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

            countdownTimer.start();
            isTimerRunning = true;
            startPauseButton.setText("\u23F8");
            // startPauseButton.setForeground(Color.PINK);
            stopButton.setForeground(Color.RED);

            // 当计时开始时，隐藏timeInputPanel
            timeInputPanel.setVisible(false);
            TimeTrackBar.this.mainFrame.revalidate();

            startPauseButton.setEnabled(true); // 启用暂停/恢复按钮
        }

        // 停止秒表
        private void stopTimer() {
            if (countdownTimer != null) {
                countdownTimer.stop();
            }
            isTimerMode = false;
            isTimerRunning = false;
            startPauseButton.setText("▶");
            stopButton.setForeground(Color.BLACK);

            // 重置进度条和时间显示
            // progressBar.setValue(0);
            // countdownRemainingTime.setText("0d 0h 0m 0s");

        }

        // 播放/暂停-按键样式转换
        private void togglePauseResume() {
            if (!isTimerRunning)
                return;

            if (!isTimerPaused) {
                countdownTimer.stop();
                startPauseButton.setText("▶");
                // startPauseButton.setForeground(Color.GREEN);

            } else {
                countdownTimer.start();
                startPauseButton.setText("⏸");
                // startPauseButton.setForeground(Color.ORANGE);

            }
            isTimerPaused = !isTimerPaused;
        }

        // 声音开关转换
        private void toggleSound() {
            if (clip != null && clip.isActive()) { // 如果音乐正在播放
                clip.stop();
            }

            isSoundEnabled = !isSoundEnabled;
            updateSoundButtonAppearance();
        }

        // 有声/静音-按键样式转换
        private void updateSoundButtonAppearance() {
            if (isSoundEnabled) {
                soundToggleButton.setText("\u266B");
                soundToggleButton.setForeground(Color.ORANGE);
            } else {
                soundToggleButton.setText("\u263D");
                soundToggleButton.setForeground(Color.BLUE);
            }
        }

        // 如果有声则播放
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

    private void printSizes() {
        System.out.println("mainFrame Size: " + mainFrame.getSize());
        System.out.println("taskPanel Size: " + taskPanel.getSize());

        for (Component component : taskPanel.getComponents()) {
            if (component instanceof TimerTaskPanel) {
                TimerTaskPanel timerTaskPanel = (TimerTaskPanel) component;
                System.out.println("progressBar Size: " + timerTaskPanel.progressBar.getSize());
                System.out.println("nameField Size: " + timerTaskPanel.nameField.getSize());
                System.out.println("daysField Size: " + timerTaskPanel.daysField.getSize());
                System.out.println("hoursField Size: " + timerTaskPanel.hoursField.getSize());
                System.out.println("minutesField Size: " + timerTaskPanel.minutesField.getSize());
                System.out.println("secondsField Size: " + timerTaskPanel.secondsField.getSize());
                System.out.println("remainingTimeLabel Size: " + timerTaskPanel.countdownRemainingTime.getSize());
                System.out.println("timeInputPanel Size: " + timerTaskPanel.timeInputPanel.getSize());
                if (timerTaskPanel.countdownTimer != null) {
                    System.out.println("Timer's Initial Delay: " + timerTaskPanel.countdownTimer.getInitialDelay());
                    System.out.println("Timer's Delay: " + timerTaskPanel.countdownTimer.getDelay());
                } else {
                    System.out.println("Timer is null");
                }
                System.out.println("countdownDuration Value: " + timerTaskPanel.countdownDuration);
            }
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimeTrackBar::new);
    }
}
