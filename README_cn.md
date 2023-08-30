[English 英文](README.md)

# TimeTrackBar 程序包

TimeTrackBar 是一个用于计算时间并显示进度的Java程序包，可以在MacBook上使用。该程序包提供一个简单的界面，其中包含干净的进度条，计时任务的添加和删除功能，以及开始计时的播放按钮。

## 功能特性

- 计算时间并显示进度
- 进度条界面，清晰易懂
- 添加和删除计时任务
- 开始计时和暂停功能

## 环境要求

- Java 11 或更高版本
- macOS 系统

## 安装与使用

1. 确保你的MacBook已经安装了Java 11。
2. 下载本程序包的源代码或jar文件。

### 使用源代码

1. 将源代码文件夹导入你的IDE（例如VSCode）。
2. 打开 `TimeTrackBar.java` 文件。
3. 在IDE中编译和运行程序。

### 使用已编译的jar文件

1. 打开终端。
2. 使用 `cd` 命令切换到jar文件所在的目录。
3. 运行命令 `java -jar TimeTrackBar.jar`。

## 界面说明

- 每行计时任务：包括一个命名区域、剩余时间、总时间以及删除按钮。
- "+" 按钮：点击可以添加新的计时任务行。
- "-" 按钮：点击可以删除对应的计时任务行。
- ▶ 按钮：点击可以开始计时。

## 示例代码

以下是如何使用 TimeTrackBar 程序包的示例代码：

```java
import com.example.timetrackbar.TimeTrackBar;

public class Main {
    public static void main(String[] args) {
        TimeTrackBar timeTrackBar = new TimeTrackBar();
        // 添加计时任务
        timeTrackBar.addTask("Task 1", 60); // 任务名为 "Task 1"，总时间为 60 秒
        timeTrackBar.addTask("Task 2", 120); // 任务名为 "Task 2"，总时间为 120 秒
        // 启动界面
        timeTrackBar.startUI();
    }
}

