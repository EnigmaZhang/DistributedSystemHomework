import java.io.*;
import java.util.ArrayList;

/**
 @author <a href = "https://github.com/EnigmaZhang/"> Zhang Xiaotian </a>
 */
public class BookShelfImpl implements BookShelf
{
    @Override
    public boolean add(Book book)
    {
        try
        {
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
            else
            {
                System.out.println("The same ID already exists, please delete it first.");
                out.flush();
                out.close();
                return false;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public ArrayList<Book> searchByName(String bookName)
    {
        try
        {
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
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Book searchByID(String bookNumber)
    {
        try
        {
            File file = new File(Book.getPath());
            BufferedReader in = new BufferedReader(new FileReader(file));
            System.out.println("Searching " + bookNumber);
            for (String line = in.readLine(); line != null; line = in.readLine())
            {
                if (line.split("\t")[1].equals(bookNumber))
                {
                    return new Book(
                            line.split("\t")[0],
                            line.split("\t")[1],
                            line.split("\t")[2]);
                }
            }
            in.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public boolean deleteByID(String bookNumber)
    {
        try
        {
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
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }
}


