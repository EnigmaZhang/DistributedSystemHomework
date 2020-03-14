# 分布式系统第二次实验思路说明

西安电子科技大学 17030140014 张笑天

******

[代码地址](https://github.com/EnigmaZhang/DistributedSystemHomework/)

## 基础设计

表达式形式：(1+2)*3+4/2\n

服务端使用ServerPublic静态类来实现LinkedBlockingQueue的定义与访问以及计数变量的声明和操作

计数变量的修改应用ServerPublic中synchronized public static void addPlusCount()方法，确保同步

自定义了StringDataPair类，来实现在LinkedBlockingQueue中同时传送字符串与DatagramPacket的信息

服务端使用了修改自<"https://www.cnblogs.com/woider/p/5331391.html/"> woider </a> 的类CalculateThread和ArithmeticHelper实现表达式计算

本程序开启了中有六个线程，接收线程（主进程），一个发送线程，四个计算线程

在CalculateThread的run中利用异常处理，确保每个UDP包均有对应回复
（第395行）

```java
                catch (Exception e)
                {
                    System.out.println("Calculator Exception: " + e.getMessage());
                    byte[] buf = "Wrong Expression!".getBytes();
                    DatagramPacket originalPacket = stringDataPair.getDatagramPacket();
                    DatagramPacket sendPacket = new DatagramPacket(
                            buf, buf.length, originalPacket.getAddress(), originalPacket.getPort());
                    stringDataPair = new StringDataPair("Wrong Expression!", sendPacket);
                    ServerPublic.sendQueue.offer(stringDataPair, 10000, TimeUnit.MILLISECONDS);
                }
```

******

## 问题与解决

如何确保接收信息与发送信息对应：自定义StringDataPair类，同时传送字符串信息与DatagramPacket信息，在CalculationThread类中替换DatagramPacket中字符串信息，最后在发送线程发出

算数运算时java.math.BigDecimal b2 = new java.math.BigDecimal(v2)报错，错误信息为需要指数e字符
经过研究，发现这是bytes[]转为String时'/0'字符也被算入String导致Decimal最后一个字符串转化时无法转化，而后其设计中最后一个异常的else为e缺失，导致报错

解决方案：去除字符串后0字符
 String calculation = new String(receive).substring(0, ServerPublic.trueLength(new String(receive)));

如果Server端运算发生异常如(0/0)，那么服务端因为无法接受信息导致卡住

解决：增加针对CalculateThread的异常处理，发生运算异常，发送Wrong Expression信息

副作用：导致该部分代码有些丑陋

******

## 结果演示

客户端：

```text
Please input the calculation, The format is "6+7\n.
1+2
Answer: 3.0


4.5*3
Answer: 13.5

0/0
Answer: Wrong Expression!

(1+4)*4+3/6
Answer: 20.5
```

服务端：

```text
Server starts.
1+2
Thread 0 Result: 3.0
Return answer: 3.0
Add: 1	Sub: 0
Multi: 0	Divide: 0
4.5*3
Thread 3 Result: 13.5
Return answer: 13.5
Add: 1	Sub: 0
Multi: 1	Divide: 0
0/0
Calculator Exception: / by zero
Return answer: Wrong Expression!
Add: 1	Sub: 0
Multi: 1	Divide: 0
(1+4)*4+3/6
Thread 1 Result: 20.5
Return answer: 20.5
Add: 3	Sub: 0
Multi: 2	Divide: 1
```
