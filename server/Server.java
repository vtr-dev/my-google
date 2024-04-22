import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
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

public class Server {
  private ServerSocket server;
  private int port = 7777;

  public Server() {
    try {
      server = new ServerSocket(port);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Server example = new Server();
    example.handleConnection();
  }

  public void handleConnection() {
    System.out.println("Servidor rodando na porta: " + port);
    while (true) {
      try {
        Socket socket = server.accept();
        new ConnectionHandler(socket).start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}

class ConnectionHandler extends Thread {
  private Socket socket;

  public ConnectionHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

      SocketRequest request = (SocketRequest) ois.readObject();
      String action = request.getAction();
      String data = request.getData();

      System.out.println("\nSolicitação recebida:\nComando: " + action + "\nInformação: " + data + "\n\n");

      switch (action) {
        case "search":
          handleSearch(data, oos);
          break;
        case "insert":
          handleInsert(request, oos);
          break;
        case "list":
          handleList(oos);
          break;
        case "remove":
          handleRemove(data, oos);
          break;
        default:
          oos.writeObject("Comando inválido!");
      }

    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void handleSearch(String data, ObjectOutputStream oos) throws IOException {
    List<String> results = new ArrayList<>();
    File folder = new File("database");
    File[] listOfFiles = folder.listFiles();

    int MAX_RESULTS = 100;
    int totalResults = 0;

    for (File file : listOfFiles) {
      if (file.isFile()) {
        try (Scanner scanner = new Scanner(file)) {
          int lineNum = 1;

          while (scanner.hasNextLine() && results.size() < MAX_RESULTS) {
            String line = scanner.nextLine();

            if (line.contains(data)) {
              String snippet = getSnippetAroundPhrase(line, data);
              String result = "Resultado " + (totalResults + 1) + ":\n" +
                  "Nome do arquivo: " + file.getName() + "\n" +
                  "Linha: " + lineNum + "\n" +
                  "Descrição: " + snippet + "\n\n";
              results.add(result);

              totalResults++;
            }
            lineNum++;
          }
        }
      }
    }

    if (!results.isEmpty()) {
      String message = "Número total de resultados: " + totalResults + "\n\n";
      if (totalResults > MAX_RESULTS) {
        message += String.join("\n", results.subList(0, MAX_RESULTS));
        oos.writeObject(message);
      } else {
        message += String.join("\n", results);
        oos.writeObject(message);
      }
    } else {
      oos.writeObject("Palavras-chave não encontradas!");
    }
  }

  private void handleInsert(SocketRequest request, ObjectOutputStream oos) throws IOException {
    String fileName = request.getData();
    byte[] fileData = request.getFileData();
    String filePath = "database/" + fileName;

    try (FileOutputStream fos = new FileOutputStream(filePath)) {
      fos.write(fileData);
      oos.writeObject("Arquivo '" + fileName + "' inserido com sucesso.");
    } catch (IOException e) {
      e.printStackTrace();
      oos.writeObject("Erro ao inserir o arquivo.");
    }
  }

  private void handleList(ObjectOutputStream oos) throws IOException {
    File folder = new File("database");
    File[] listOfFiles = folder.listFiles();

    StringBuilder filesList = new StringBuilder();
    for (File file : listOfFiles) {
      if (file.isFile()) {
        filesList.append(file.getName()).append("\n");
      }
    }

    if (filesList.length() == 0) {
      oos.writeObject("Nenhum arquivo encontrado.");
      return;
    }

    oos.writeObject(filesList.toString());
  }

  private void handleRemove(String fileName, ObjectOutputStream oos) throws IOException {
    File folder = new File("database");
    File[] listOfFiles = folder.listFiles();

    boolean found = false;
    for (File file : listOfFiles) {
      if (file.isFile() && file.getName().equals(fileName)) {
        file.delete();
        oos.writeObject("Arquivo '" + fileName + "' removido com sucesso.");
        found = true;
        break;
      }
    }

    if (!found) {
      oos.writeObject("Arquivo '" + fileName + "' não encontrado.");
    }
  }

  private String getSnippetAroundPhrase(String line, String keyWords) {
    int SNIPPET_SIZE = 255;
    String[] words = keyWords.trim().split("\\s+");

    List<String> snippetWords = new ArrayList<>();

    for (int i = 0; i < words.length; i++) {
      int startIndex = line.indexOf(words[i]);
      if (startIndex != -1) {
        int snippetStart = Math.max(0, startIndex - SNIPPET_SIZE);
        int snippetEnd = Math.min(line.length(), startIndex + words[i].length() + SNIPPET_SIZE);

        String snippet = line.substring(snippetStart, snippetEnd);

        snippetWords.add(snippet);

        return String.join(" ", snippetWords);
      }
    }

    return "";
  }
}
