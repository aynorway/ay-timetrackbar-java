[☞ 中文 Chinese](README_cn.md)

---

[English:](README.md)

# TimeTrackBar Package

Hello everyone, I'm Adrain Y, also known as "老6" ("Old 6" in English). This timer software is one of my favorite software projects. For a long time, I couldn't find an appropriate compact timer application, and I was unsatisfied with the overcomplicated UI and redundant features of the timer apps available in the market. Therefore, I developed this timer app myself. Currently, I use it almost every day to track time, especially to understand how I spend my working hours. In order to facilitate compilation for different platforms simultaneously, the project was written using 100% Java. It's available for both MacBook and Windows. The code is compact and fast, and to ensure compatibility across devices, I specifically used raw Unicode characters. Additionally, it records correctly even when a Mac is in sleep mode. I welcome everyone who values their time to use and test it. This software is open source forever, and I hope it helps everyone.

**Adrian "Old 6"**: Life is precious, don't waste it.

---

## Features

- Extremely user-friendly logic.
- Automatic time calculation with progress display.
- Clear progress bar interface.
- Ability to add/delete multiple timing tasks.
- Start/pause timer functionality.
- Sound alert/mute feature.
- Auto detection of stopwatch/countdown mode.

---

## Installation and Usage

**Windows users**: [Download .exe]()  

**macOS users**: [Download .app]()  

**Developers (Java)**: Ensure Java 11 is installed and [download the jar or source code](https://raw.githubusercontent.com/aynorway/timetrackbar/master/Jar/TimeTrackBar.jar).

---

## User Interface Guide

From left to right:

- **"+" Button**: Add a new timing task.
- **"-" Button**: Delete a specific timing task.
- **Text Input Box**: Supports up to 7 characters, suitable for brief notes or memos.
- **Progress Bar**: Displays the timing progress.
- **⏲ Icon Button**: Click to expand or hide the time setting panel.
- **Sound Toggle Button**: Default is a musical note (♫). Clicking it changes to a crescent moon (☽) indicating mute. Through this button, you can decide whether to sound an alert when the timer ends.
- **Remaining Time Display**: Shows the remaining time for the timer or countdown.
- **▶ Button**: Start or pause the timer. Blue in stopwatch mode and magenta in countdown mode.
- **⏹ Button**: Stop and reset the timer completely.

---

## User Guide 

1. If you click ▶ without inputting a number (setting a time), it will activate the stopwatch mode.
2. After correctly entering a time and clicking ▶ (or pressing the Enter key), you'll enter countdown mode.
3. You can freely input any time, such as 999 hours 333 minutes. The software will automatically calculate the total duration and convert it to a standard format.
4. Use the ⏹ button to stop, and the timer will automatically reset.

---

## Directory Tree 

```
.
├── ClearableTextField$1.class
├── ClearableTextField$2.class
├── ClearableTextField.class
├── ClearableTextField.java
├── META-INF
│   └── MANIFEST.MF
├── README.md
├── README_cn.md
├── TimeTrackBar$TimerTaskPanel$1.class
├── TimeTrackBar$TimerTaskPanel.class
├── TimeTrackBar.class
├── TimeTrackBar.jar
├── TimeTrackBar.java
└── timbre_whaaat.wav
```

---

Thank you for your support and usage! If you encounter any problems or have any suggestions during use, please feel free to contact me.  
Work Email: adrianyangbiz@gmail.com