import java.io.*;
import java.net.*;
import java.util.Scanner;

class SocketRequest implements Serializable {
  private String action;
  private String data;
  private byte[] fileData;

  public SocketRequest(String action, String data, byte[] fileData) {
    this.action = action;
    this.data = data;
    this.fileData = fileData;
  }

  public String getAction() {
    return action;
  }

  public String getData() {
    return data;
  }

  public byte[] getFileData() {
    return fileData;
  }
}

public class Client {
  public static void main(String[] args) {
    try (Scanner scanner = new Scanner(System.in)) {
      InetAddress host = InetAddress.getLocalHost();
      Socket socket = new Socket(host.getHostName(), 7777);

      printMenu();
      String option = scanner.nextLine();

      switch (option) {
        case "1":
          searchFiles(socket, scanner);
          break;
        case "2":
          insertFile(socket, scanner);
          break;
        case "3":
          listFiles(socket);
          break;
        case "4":
          removeFile(socket, scanner);
          break;
        case "5":
          socket.close();
          return;
        default:
          System.out.println("Opção inválida");
      }

    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void printMenu() {
    System.out.print(
        "\nEscolha uma opção: \n" +
            "1 - Pesquisar nos arquivos\n" +
            "2 - Inserir arquivo\n" +
            "3 - Listar arquivos\n" +
            "4 - Remover arquivo\n" +
            "5 - Sair\n\n");
  }

  private static void searchFiles(Socket socket, Scanner scanner) throws IOException {
    try {
      System.out.print("Digite as palavras chave: ");
      String key = scanner.nextLine();

      sendRequest(socket, new SocketRequest("search", key, null));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void insertFile(Socket socket, Scanner scanner) throws IOException {
    try {
      System.out.print("Digite o caminho do arquivo: ");
      String filePath = scanner.nextLine();

      File file = new File(filePath);
      if (!file.exists()) {
        System.out.println("Arquivo não encontrado.");
        return;
      }

      byte[] fileData = readBytesFromFile(file);
      SocketRequest request = new SocketRequest("insert", file.getName(), fileData);
      sendRequest(socket, request);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void listFiles(Socket socket) throws IOException {
    try {
      sendRequest(socket, new SocketRequest("list", null, null));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void removeFile(Socket socket, Scanner scanner) throws IOException {
    try {
      System.out.print("Digite o nome do arquivo: ");
      String fileName = scanner.nextLine();

      sendRequest(socket, new SocketRequest("remove", fileName, null));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static void sendRequest(Socket socket, SocketRequest request) throws IOException, ClassNotFoundException {
    try {
      ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
      oos.writeObject(request);

      ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
      String response = (String) ois.readObject();
      System.out.println("\n\n##### RESPOSTA DO SERVIDOR #####\n" + response + "\n");

      ois.close();
      oos.close();
    } catch (IOException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static byte[] readBytesFromFile(File file) throws IOException {
    FileInputStream fileInputStream = null;
    byte[] bytesArray = new byte[(int) file.length()];

    try {
      fileInputStream = new FileInputStream(file);
      fileInputStream.read(bytesArray);
    } finally {
      if (fileInputStream != null) {
        fileInputStream.close();
      }
    }

    return bytesArray;
  }

}
