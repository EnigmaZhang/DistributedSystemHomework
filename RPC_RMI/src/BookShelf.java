import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 @author <a href = "https://github.com/EnigmaZhang/"> Zhang Xiaotian </a>
 */
interface BookShelf extends Remote
{
    boolean add(Book nook) throws RemoteException;
    ArrayList<Book> searchByName(String bookName) throws RemoteException;
    Book searchByID(String bookNumber) throws RemoteException;
    boolean deleteByID(String bookNumber) throws RemoteException;
}

/**
 @author <a href = "https://github.com/EnigmaZhang/"> Zhang Xiaotian </a>
 */
class Book implements Serializable
{
    private String bookName;
    private String bookNumber;
    private String bookWriter;

    Book(String bookName, String bookNumber, String bookWriter)
    {
        this.bookName = bookName;
        this.bookNumber = bookNumber;
        this.bookWriter = bookWriter;
    }

    public String toString()
    {
        return this.bookName + '\t' + this.bookNumber + '\t' + this.bookWriter + '\n';
    }

    @SuppressWarnings("unused")
    public String getBookName()
    {
        return this.bookName;
    }

    public String getBookNumber()
    {
        return this.bookNumber;
    }

    @SuppressWarnings("unused")
    public String getBookWriter()
    {
        return this.bookWriter;
    }

    static public String getPath() throws IOException
    {
        File file = new File(".");
        return file.getCanonicalPath() + "\\Books\\Books.txt";
    }
}

