import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

/**
 @author <a href = "https://github.com/EnigmaZhang/"> Zhang Xiaotian </a>
 */
public class MyRMIClient
{
    @SuppressWarnings("all")
    public static void main(String[] args)
    {
        try
        {
            String name = "BookShelf";
            String serverIP = "127.0.0.1";  // My AliYun ECS Server
            int serverPort = 1099;

            Registry registry = LocateRegistry.getRegistry(serverIP, serverPort);
            BookShelf bookShelf = (BookShelf) registry.lookup(name);

            System.out.println("Press 1 for add, 2 for search by book name,");
            System.out.println("3 for search by book ID, 4 for delete by ID, and q to quit");

            while (true)
            {
                Scanner scan = new Scanner(System.in);
                String in = scan.nextLine();

                switch (in)
                {
                    case "1":
                    {
                        System.out.println("Please input book name:");
                        String bookName = scan.nextLine();
                        System.out.println("Please input book number:");
                        String bookNumber = scan.nextLine();
                        System.out.println("Please input book writer:");
                        String bookWriter = scan.nextLine();

                        Book book = new Book(bookName, bookNumber, bookWriter);
                        boolean result = bookShelf.add(book);

                        if (result)
                            System.out.println("Adding succeeded!");
                        else
                            System.out.println("Adding failed!");
                        break;
                    }

                    case "2":
                    {
                        System.out.println("Please input book name:");
                        String bookName = scan.nextLine();
                        ArrayList<Book> result = bookShelf.searchByName(bookName);
                        if (result != null)
                            for (Book book:result)
                                System.out.print(book.toString());
                        else
                            System.out.println("No such book.");

                        break;
                    }

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

                    case "4":
                    {
                        System.out.println("Please input the book number to delete.");
                        String bookNumber = scan.nextLine();
                        if (bookShelf.deleteByID(bookNumber))
                            System.out.println("Delete.");
                        else
                            System.out.println("No such book.");

                        break;
                    }

                    default:
                        System.out.println("Wrong input!");
                }
            }
        } catch (Exception e)
        {
            System.err.println("??? exception:");
            e.printStackTrace();
        }
    }
}
