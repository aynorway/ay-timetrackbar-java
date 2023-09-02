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
        private JLabel remainingTimeLabel;
        private int duration;
        private Timer timer;
        private JPanel timeInputPanel; // 新增的面板，用于容纳时间设置部分
        private JButton soundToggleButton; // 警报声音按钮
        private JButton startButton;
        private boolean isTimerRunning = false;
        private Clip clip;
        private boolean isSoundEnabled = true;  // 初始状态不静音


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

            remainingTimeLabel = new JLabel("0d 0h 0m 0s");
            startButton = new JButton("▶");
            startButton.setForeground(Color.GREEN);
            startButton.setPreferredSize(new Dimension(40, 30));
            startButton.addActionListener(e -> startCountdown());

            JButton toggleButton = new JButton("⏲"); // 使用时钟字符，用于显示/隐藏时间设置部分
            toggleButton.setPreferredSize(new Dimension(40, 30));
            toggleButton.addActionListener(e -> {
                timeInputPanel.setVisible(!timeInputPanel.isVisible());
                TimeTrackBar.this.mainFrame.revalidate(); // 确保面板重新布局
            });
            eastPanel.add(toggleButton);

            soundToggleButton = new JButton("\u266B"); // 默认为有声音的状态
            Font emojiFont = new Font("Apple Color Emoji", Font.PLAIN, 12); // For macOS
            soundToggleButton.setFont(emojiFont);
            System.out.println(soundToggleButton.getFont());

            soundToggleButton.setPreferredSize(new Dimension(40, 30));
            soundToggleButton.addActionListener(e -> toggleSound());
            eastPanel.add(soundToggleButton);

            eastPanel.add(remainingTimeLabel);
            eastPanel.add(startButton);
            add(eastPanel, BorderLayout.EAST);

        }

        private void toggleSound() {
            if (clip != null && clip.isActive()) { // 如果音乐正在播放
                clip.stop();
                soundToggleButton.setText("\u266B");
                soundToggleButton.setBackground(null);
            } else {
                isSoundEnabled = !isSoundEnabled;
                if (isSoundEnabled) {
                    soundToggleButton.setText("\u266B");
                    soundToggleButton.setBackground(null);
                } else {
                    soundToggleButton.setText("\u263D");
                    soundToggleButton.setBackground(Color.RED);
                }
            }
        }

        private void startCountdown() {
            if (timer != null) {
                timer.stop();
            }

            try {
                int days = daysField.getText().isEmpty() ? 0 : Integer.parseInt(daysField.getText());
                int hours = hoursField.getText().isEmpty() ? 0 : Integer.parseInt(hoursField.getText());
                int minutes = minutesField.getText().isEmpty() ? 0 : Integer.parseInt(minutesField.getText());
                int seconds = secondsField.getText().isEmpty() ? 0 : Integer.parseInt(secondsField.getText());
                duration = seconds + minutes * 60 + hours * 60 * 60 + days * 24 * 60 * 60;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(mainFrame, "请输入有效的时间数值.");
                return;
            }

            progressBar.setMaximum(duration);
            progressBar.setValue(0);

            // 当计时开始时，隐藏timeInputPanel
            timeInputPanel.setVisible(false);
            TimeTrackBar.this.mainFrame.revalidate();

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
                    remainingTimeLabel
                            .setText(daysLeft + "d " + hoursLeft + "h " + minutesLeft + "m " + secondsLeft + "s");
                } else {
                    ((Timer) e.getSource()).stop();
                    if (isSoundEnabled) {
                        playAlarmSound();
                    }
                }
            });

            timer.start();
            isTimerRunning = true;
            startButton.setText("⏹"); // ⏹ 是停止符号
            startButton.setForeground(Color.RED);
            startButton.removeActionListener(startButton.getActionListeners()[0]); // 移除旧的监听器
            startButton.addActionListener(e -> stopCountdown()); // 添加一个新的监听器来停止计时

            printSizes();
        }

        private void stopCountdown() {
            if (timer != null) {
                timer.stop();
            }
            if (clip != null) {
                clip.stop();
            }
            isTimerRunning = false;
            startButton.setText("▶");
            startButton.setForeground(Color.GREEN);
            startButton.removeActionListener(startButton.getActionListeners()[0]); // 移除旧的监听器
            startButton.addActionListener(e -> startCountdown()); // 添加回原始的监听器
        }

        private void playAlarmSound() {
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
                System.out.println("remainingTimeLabel Size: " + timerTaskPanel.remainingTimeLabel.getSize());
                System.out.println("timeInputPanel Size: " + timerTaskPanel.timeInputPanel.getSize());
                if (timerTaskPanel.timer != null) {
                    System.out.println("Timer's Initial Delay: " + timerTaskPanel.timer.getInitialDelay());
                    System.out.println("Timer's Delay: " + timerTaskPanel.timer.getDelay());
                } else {
                    System.out.println("Timer is null");
                }
                System.out.println("duration Value: " + timerTaskPanel.duration);
            }
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimeTrackBar::new);
    }
}
