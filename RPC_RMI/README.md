# 分布式系统第三次实验思路说明

西安电子科技大学 17030140014 张笑天

******

[代码地址](https://github.com/EnigmaZhang/DistributedSystemHomework/)

## 基础设计

本程序客户端使用交互式输入输出，服务端使用文件读写来存储与修改图书信息。

## 问题与解决

1. ClassNotFound问题

我出现本问题主要在IDEA IDE下使用，在Powershell中无此问题。

经过研究与询问，[Stackoverflow](https://stackoverflow.com/questions/9531158/java-rmi-serverexception-remoteexception-occurred-in-server-thread-classnotfou)的一个回答比较好的回答了这个问题。

解决方法：不使用Package，因为IDEA的class出现在out文件夹中，因此在对应文件夹启动rmi，保证rmi可以找到类的路径。

2. 类的转化问题
   
在客户端与服务端通信时，发现Book类如果不手动cast会报错，报错信息与Class有关，推测为因为共用Book导致的情况。

解决代码：

```java
    case "3":
    {
        System.out.println("Please input book number:");
        String bookNumber = scan.nextLine();
        Book ret = (Book)bookShelf.searchByID(bookNumber);
        if (ret != null)
            System.out.println(ret.toString());
        else
            System.out.println("No such book.");

        break;
    }
```

3. 文件存储方式

使用每行存储一个Book对象（利用自身的toString方法转化为字符串形式），Book对象各原理利用tab隔开，从而方便查找，利用split方法即可。

在删除时，实际上是将文件逐行读取写入，但不写入要被删除的对象。

示例代码：文件写入

```java
    File file = new File(Book.getPath());

    BufferedWriter out = new BufferedWriter(new FileWriter(file, true));

    if (searchByID(book.getBookNumber()) == null)
    {
        System.out.println("Writing to the file.");
        out.write(book.toString());
        out.flush();
        out.close();
        return true;
    }
```

文件搜索
```java
    File file = new File(Book.getPath());
    BufferedReader in = new BufferedReader(new FileReader(file));
    ArrayList<Book> ret = new ArrayList<>();
    System.out.println("Searching " + bookName);
    for (String line = in.readLine(); line != null; line = in.readLine())
    {
        if (line.split("\t")[0].equals(bookName))
        // The book elements are separated by tab.
        {
            ret.add(new Book(
                    line.split("\t")[0],
                    line.split("\t")[1],
                    line.split("\t")[2]));
        }
    }
    in.close();

    return ret;
```

删除对象
```java
    boolean findFlag = false;
    ArrayList<String> lines = new ArrayList<>();
    File file = new File(Book.getPath());
    BufferedReader in = new BufferedReader(new FileReader(file));
    System.out.println("Deleting " + bookNumber);
    for (String line = in.readLine(); line != null; line = in.readLine())
    {
        if (!line.split("\t")[1].equals(bookNumber))
        {
            lines.add(line);
            findFlag = true;
        }
    }
    in.close();

    if (!findFlag)
        return false;
    
    else
    {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        for (String line : lines)
        {
            out.write(line + '\n');
        }
        out.close();

        return true;
    }
```

## 实验结果展示

客户端
```text
Press 1 for add, 2 for search by book name,
3 for search by book ID, 4 for delete by ID, and q to quit
1
Please input book name:
Java
Please input book number:
001
Please input book writer:
Zhang
Adding succeeded!
1
Please input book name:
Python
Please input book number:
002
Please input book writer:
Mark
Adding succeeded!
1
Please input book name:
C++
Please input book number:
003
Please input book writer:
BS
Adding succeeded!
1
Please input book name:
Java
Please input book number:
004
Please input book writer:
Edward
Adding succeeded!
2
Please input book name:
Java
Java	001	Zhang
Java	004	Edward
3
Please input book number:
003
C++	003	BS

4
Please input the book number to delete.
001
Delete.
```

服务端
```text
Registering Shelf Object
Searching 001
Writing to the file.
Searching Mark
Writing to the file.
Searching 003
Writing to the file.
Searching 004
Writing to the file.
Searching Java
Searching 003
Deleting 001
```

文件
```text
Python	002	Mark
C++	003	BS
Java	004	Edward
```