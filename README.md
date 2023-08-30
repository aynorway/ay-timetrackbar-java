[中文 Chinese](README_cn.md)

# TimeTrackBar Package

TimeTrackBar is a Java package designed to calculate time and display progress, specifically for use on a MacBook. This package provides a simple interface with a clean progress bar, the ability to add and remove timing tasks, and a play button to initiate the countdown.

## Features

- Time calculation and progress display
- Clean and intuitive progress bar interface
- Add and remove timing tasks
- Start and pause functionality

## Requirements

- Java 11 or higher
- macOS operating system

## Installation and Usage

1. Ensure your MacBook has Java 11 or higher installed.
2. Download the source code or the pre-compiled jar file of this package.

### Using the Source Code

1. Import the source code folder into your IDE (e.g., VSCode).
2. Open the `TimeTrackBar.java` file.
3. Compile and run the program within your IDE.

### Using the Pre-Compiled Jar File

1. Open a terminal.
2. Use the `cd` command to navigate to the directory containing the jar file.
3. Run the command `java -jar TimeTrackBar.jar`.

## Interface Explanation

- Each task row includes a naming area, remaining time, total time, and a delete button.
- "+" button: Click to add a new task row.
- "-" button: Click to delete the corresponding task row.
- ▶ button: Click to start the countdown.

## Example Code

Below is an example of how to use the TimeTrackBar package:

```java
import com.example.timetrackbar.TimeTrackBar;

public class Main {
    public static void main(String[] args) {
        TimeTrackBar timeTrackBar = new TimeTrackBar();
        // Add timing tasks
        timeTrackBar.addTask("Task 1", 60); // Task name: "Task 1", total time: 60 seconds
        timeTrackBar.addTask("Task 2", 120); // Task name: "Task 2", total time: 120 seconds
        // Start the user interface
        timeTrackBar.startUI();
    }
}
