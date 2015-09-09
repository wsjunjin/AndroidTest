# AndroidTest
Gallery - 监测APP的Cpu，Memory使用情况
Features
输入监测App包名，监测Cpu，Memory使用情况
折线图采用achartengine-1.1.0.jar
监测信息存储在sdcard的Gallery目录下；Cpu的监测日志为CpuInfo.txt，Memory的监测日志为MemInfo.txt.
How to use Gallery
Step1.输入监测App包名，包名可以通过adb shell pm list packages获取
Step2.点击“创建日志”清空日志信息
Step3.点击“写日志”会写入日志信息，目前采样数据间隔为1min，可以通过Utils.DELAYTIME设置采样间隔。
Step4.点击“分析日志”会分别获取Cpu，Memory采样信息，Cpu采样信息单位为%，Memory采样信息单位转为对应字节(形如MB)
Step5.点击Cpu信息获取Cpu使用情况，点击Memory信息获取Memory使用情况；若折线图显示超出可视范围，可通过手动触摸调节显示范围
主界面截图

Cpu使用情况截图                            

 
Memory使用截图




