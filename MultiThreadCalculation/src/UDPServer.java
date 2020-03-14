import java.io.*;
import java.math.RoundingMode;
import java.net.*;
import java.util.Collections;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href = "https://github.com/EnigmaZhang/"> Zhang Xiaotian </a>
 * The program uses multi-thread to calculate and count".
 * This class will send both received data and the information of packets to the calculation threads.
 */

@SuppressWarnings("InfiniteLoopStatement")
public class UDPServer
{
    public static void main(String[] args)
    {
        int port = 9000;
        System.out.println("Server starts.");
        {
            try (DatagramSocket socket = new DatagramSocket(port))
            {
                ReplyThread replyThread = new ReplyThread(socket);
                replyThread.start();
                CalculateThread[] calculateThreads = new CalculateThread[4];
                for (int i = 0; i < 4; i++)
                {
                    calculateThreads[i] = new CalculateThread(i);
                    calculateThreads[i].start();
                }

                while (true)
                {
                    byte[] receive = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);
                    socket.receive(receivePacket);
                    String calculation = new String(receive).substring(0, ServerPublic.trueLength(new String(receive)));
                    System.out.println(calculation);
                    StringDataPair stringDataPair = new StringDataPair(calculation, receivePacket);
                    if (!ServerPublic.receiveQueue.offer(stringDataPair, 10000, TimeUnit.MILLISECONDS))
                    {
                        System.out.println("The Queue is full.");
                        socket.close();
                    }
                }
            } catch (IOException e)
            {
                System.out.println("IO exception:" + e.getMessage());
            } catch (InterruptedException e)
            {
                System.out.println("Interrupted Exception:" + e.getMessage());
            }
        }
    }

}

/**
 * This is a class for public vars which can be used in all threads.
 */
class ServerPublic
{
    static private int capacity = 1024;
    static public LinkedBlockingQueue<StringDataPair> receiveQueue = new LinkedBlockingQueue<>(capacity);
    static public LinkedBlockingQueue<StringDataPair> sendQueue = new LinkedBlockingQueue<>(capacity);
    static private int plusCount = 0;
    static private int subCount = 0;
    static private int multiCount = 0;
    static private int divideCount = 0;

    synchronized public static void addPlusCount()
    {
       ServerPublic.plusCount++;
    }

    synchronized public static void addSubCount()
    {
        ServerPublic.subCount++;
    }

    synchronized public static void addMultiCount()
    {
        ServerPublic.multiCount++;
    }

    synchronized public static void addDivisionCount()
    {
        ServerPublic.divideCount++;
    }

    public static void printCount()
    {
        System.out.println("Add: " + plusCount + "\tSub: " + subCount);
        System.out.println("Multi: " + multiCount + "\tDivide: " + divideCount);
    }


    static int trueLength(String s)
    {
        int len = 0;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) != 0)
                len++;
        return len;

    }
}

@SuppressWarnings("InfiniteLoopStatement")
class ReplyThread extends Thread
{
    DatagramSocket socket;

    ReplyThread(DatagramSocket socket)
    {
        this.socket = socket;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                StringDataPair getInformation = ServerPublic.sendQueue.take();
                DatagramPacket sendPacket = getInformation.getDatagramPacket();
                String sendResult = getInformation.getString();
                socket.send(sendPacket);
                System.out.println("Return answer: " + sendResult);
                ServerPublic.printCount();
            } catch (InterruptedException e)
            {
                System.out.println("Interrupted Exception" + e.getMessage());
            } catch (IOException e)
            {
                System.out.println("IO exception:" + e.getMessage());
            }
        }
    }
}

class StringDataPair
{
    String s;
    DatagramPacket data;

    StringDataPair(String s, DatagramPacket data)
    {
        this.s = s;
        this.data = data;
    }

    String getString()
    {
        return this.s;
    }

    DatagramPacket getDatagramPacket()
    {
        return this.data;
    }
}

/**
 * @author <a href = "https://www.cnblogs.com/woider/p/5331391.html/"> woider </a>
 * @author <a href = "https://github.com/EnigmaZhang/"> Zhang Xiaotian </a>
 * This class will send the result of calculation and information of datagrampacket to the send thread.
 * At the same time, it will count the ops.
 * The core algorithms refers to https://www.cnblogs.com/woider/p/5331391.html.
 * 算数表达式求值
 * 直接调用Calculator的类方法conversion()
 * 传入算数表达式，将返回一个浮点值结果
 * 如果计算过程错误，将返回一个NaN
 */
@SuppressWarnings("InfiniteLoopStatement")
class CalculateThread extends Thread
{
    private Stack<String> postfixStack = new Stack<>(); // 后缀式栈
    private Stack<Character> opStack = new Stack<>(); // 运算符栈
    private int[] operatorPriority = new int[]{0, 3, 2, 1, -1, 1, 0, 2}; // 运用运算符ASCII码-40做索引的运算符优先级
    private int threadNum;

    CalculateThread(int i)
    {
        this.threadNum = i;
    }

    /**
     * 将表达式中负数的符号更改
     *
     * @param expression 例如-2+-1*(-3E-2)-(-1) 被转为 ~2+~1*(~3E~2)-(~1)
     */
    private static String transform(String expression)
    {
        char[] arr = expression.toCharArray();
        for (int i = 0; i < arr.length; i++)
        {
            if (arr[i] == '-')
            {
                if (i == 0)
                {
                    arr[i] = '~';
                }
                else
                {
                    char c = arr[i - 1];
                    if (c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == 'E' || c == 'e')
                    {
                        arr[i] = '~';
                    }
                }
            }
        }
        if (arr[0] == '~' || arr[1] == '(')
        {
            arr[0] = '-';
            return "0" + new String(arr);
        } else
        {
            return new String(arr);
        }
    }

    /**
     * 按照给定的表达式计算
     * @param expression 要计算的表达式例如:5+12*(3+5)/7
     */
    public double calculate(String expression)
    {
        Stack<String> resultStack = new Stack<>();
        prepare(expression);
        Collections.reverse(postfixStack); // 将后缀式栈反转
        String firstValue, secondValue, currentValue; // 参与计算的第一个值，第二个值和算术运算符
        while (!postfixStack.isEmpty())
        {
            currentValue = postfixStack.pop();
            if (!isOperator(currentValue.charAt(0)))
            {
                // 如果不是运算符则存入操作数栈中
                currentValue = currentValue.replace("~", "-");
                resultStack.push(currentValue);
            }
            else
            {
                // 如果是运算符则从操作数栈中取两个值和该数值一起参与运算
                secondValue = resultStack.pop();
                firstValue = resultStack.pop();

                // 将负数标记符改为负号
                firstValue = firstValue.replace("~", "-");
                secondValue = secondValue.replace("~", "-");

                String tempResult = calculate(firstValue, secondValue, currentValue.charAt(0));
                resultStack.push(tempResult);
            }
        }
        return Double.parseDouble(resultStack.pop());
    }

    /**
     * 数据准备阶段将表达式转换成为后缀式栈
     */
    private void prepare(String expression)
    {
        opStack.push(',');  // 运算符放入栈底元素逗号，此符号优先级最低
        char[] arr = expression.toCharArray();
        int currentIndex = 0;   // 当前字符的位置
        int count = 0;  // 上次算术运算符到本次算术运算符的字符的长度便于或者之间的数值
        char currentOp, peekOp; // 当前操作符和栈顶操作符
        for (int i = 0; i < arr.length; i++)
        {
            currentOp = arr[i];
            if (isOperator(currentOp))
            {
                // 如果当前字符是运算符
                if (count > 0)
                {
                    postfixStack.push(new String(arr, currentIndex, count));    // 取两个运算符之间的数字
                }
                peekOp = opStack.peek();
                if (currentOp == ')')
                {
                    // 遇到反括号则将运算符栈中的元素移除到后缀式栈中直到遇到左括号
                    while (opStack.peek() != '(')
                    {
                        postfixStack.push(String.valueOf(opStack.pop()));
                    }
                    opStack.pop();
                } else
                {
                    while (currentOp != '(' && peekOp != ',' && compare(currentOp, peekOp))
                    {
                        postfixStack.push(String.valueOf(opStack.pop()));
                        peekOp = opStack.peek();
                    }
                    opStack.push(currentOp);
                }
                count = 0;
                currentIndex = i + 1;
            } else
            {
                count++;
            }
        }
        if (count > 1 || (count == 1 && !isOperator(arr[currentIndex])))
        {
            // 最后一个字符不是括号或者其他运算符的则加入后缀式栈中
            postfixStack.push(new String(arr, currentIndex, count));
        }

        while (opStack.peek() != ',')
        {
            postfixStack.push(String.valueOf(opStack.pop()));
            // 将操作符栈中的剩余的元素添加到后缀式栈中
        }
    }

    /**
     * 判断是否为算术符号
     */
    private boolean isOperator(char c)
    {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '(' || c == ')';
    }

    /**
     * 利用ASCII码-40做下标去算术符号优先级
     */
    public boolean compare(char cur, char peek)
    {
        // 如果是peek优先级高于cur，返回true，默认都是peek优先级要低
        boolean result = false;
        if (operatorPriority[(peek) - 40] >= operatorPriority[(cur) - 40])
        {
            result = true;
        }
        return result;
    }

    /**
     * 按照给定的算术运算符做计算
     * At the same time, count the ops.
     */
    private String calculate(String firstValue, String secondValue, char currentOp)
    {
        String result = "";
        switch (currentOp)
        {
            case '+':
                result = String.valueOf(ArithmeticHelper.add(firstValue, secondValue));
                ServerPublic.addPlusCount();
                break;
            case '-':
                result = String.valueOf(ArithmeticHelper.sub(firstValue, secondValue));
                ServerPublic.addSubCount();
                break;
            case '*':
                result = String.valueOf(ArithmeticHelper.mul(firstValue, secondValue));
                ServerPublic.addMultiCount();
                break;
            case '/':
                result = String.valueOf(ArithmeticHelper.div(firstValue, secondValue));
                ServerPublic.addDivisionCount();
                break;
        }
        return result;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try
            {
                StringDataPair stringDataPair;
                stringDataPair = ServerPublic.receiveQueue.take();
                try
                {
                    String calculation = stringDataPair.getString();
                    calculation = transform(calculation);
                    double result = this.calculate(calculation);
                    String resultString = Double.toString(result);
                    System.out.println("Thread " + threadNum + " Result: " + resultString);
                    byte[] buf = resultString.getBytes();
                    DatagramPacket originalPacket = stringDataPair.getDatagramPacket();
                    DatagramPacket sendPacket = new DatagramPacket(
                            buf, buf.length, originalPacket.getAddress(), originalPacket.getPort());
                    stringDataPair = new StringDataPair(resultString, sendPacket);
                    ServerPublic.sendQueue.offer(stringDataPair, 10000, TimeUnit.MILLISECONDS);
                }
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
            }

            catch (InterruptedException e)
            {
                System.out.println("Interrupted Exception: " + e.getMessage());
            }
        }
    }
}

/**
 * @author <a href = "https://www.cnblogs.com/woider/p/5331391.html/"> woider </a>
 */
abstract class ArithmeticHelper
{

    // 默认除法运算精度
    private static final int DEF_DIV_SCALE = 16;


    /**
     * 提供精确的加法运算。
     *
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    @SuppressWarnings("unused")
    public static double add(double v1, double v2)
    {
        java.math.BigDecimal b1 = new java.math.BigDecimal(Double.toString(v1));
        java.math.BigDecimal b2 = new java.math.BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }

    public static double add(String v1, String v2)
    {
        java.math.BigDecimal b1 = new java.math.BigDecimal(v1);
        java.math.BigDecimal b2 = new java.math.BigDecimal(v2);

        return b1.add(b2).doubleValue();
    }

    /**
     * 提供精确的减法运算。
     *
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    @SuppressWarnings("unused")
    public static double sub(double v1, double v2)
    {
        java.math.BigDecimal b1 = new java.math.BigDecimal(Double.toString(v1));
        java.math.BigDecimal b2 = new java.math.BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }

    public static double sub(String v1, String v2)
    {
        java.math.BigDecimal b1 = new java.math.BigDecimal(v1);
        java.math.BigDecimal b2 = new java.math.BigDecimal(v2);
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 提供精确的乘法运算。
     *
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    @SuppressWarnings("unused")
    public static double mul(double v1, double v2)
    {
        java.math.BigDecimal b1 = new java.math.BigDecimal(Double.toString(v1));
        java.math.BigDecimal b2 = new java.math.BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }

    public static double mul(String v1, String v2)
    {
        java.math.BigDecimal b1 = new java.math.BigDecimal(v1);
        java.math.BigDecimal b2 = new java.math.BigDecimal(v2);
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到 小数点以后10位，以后的数字四舍五入。
     *
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    @SuppressWarnings("unused")
    public static double div(double v1, double v2)
    {
        return div(v1, v2, DEF_DIV_SCALE);
    }

    public static double div(String v1, String v2)
    {
        java.math.BigDecimal b1 = new java.math.BigDecimal(v1);
        java.math.BigDecimal b2 = new java.math.BigDecimal(v2);
        return b1.divide(b2, DEF_DIV_SCALE, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指 定精度，以后的数字四舍五入。
     *
     * @param v1    被除数
     * @param v2    除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */

    public static double div(double v1, double v2, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException("The   scale   must   be   a   positive   integer   or   zero");
        }
        java.math.BigDecimal b1 = new java.math.BigDecimal(Double.toString(v1));
        java.math.BigDecimal b2 = new java.math.BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 提供精确的小数位四舍五入处理。
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    @SuppressWarnings("unused")
    public static double round(double v, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException("The   scale   must   be   a   positive   integer   or   zero");
        }
        java.math.BigDecimal b = new java.math.BigDecimal(Double.toString(v));
        java.math.BigDecimal one = new java.math.BigDecimal("1");
        return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
    }

    @SuppressWarnings("unused")
    public static double round(String v, int scale)
    {
        if (scale < 0)
        {
            throw new IllegalArgumentException("The   scale   must   be   a   positive   integer   or   zero");
        }
        java.math.BigDecimal b = new java.math.BigDecimal(v);
        java.math.BigDecimal one = new java.math.BigDecimal("1");
        return b.divide(one, scale, RoundingMode.HALF_UP).doubleValue();
    }
}