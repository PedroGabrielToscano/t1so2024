package projeto2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class Consumer extends JFrame {
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private volatile boolean running = false;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader in;
    private Thread acceptThread;

    public Consumer() {
        setTitle("Consumidor");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        startButton = new JButton("Iniciar Consumo");
        stopButton = new JButton("Parar Consumo");
        stopButton.setEnabled(false);

        JPanel panel = new JPanel();
        panel.add(startButton);
        panel.add(stopButton);
        add(panel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startConsumption();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopConsumption();
            }
        });

        setVisible(true);
    }

    private void startConsumption() {
        try {
            serverSocket = new ServerSocket(5000);
            logArea.append("Aguardando conexão do produtor...\n");
            running = true;

            acceptThread = new Thread(() -> {
                while (running) {
                    try {
                        clientSocket = serverSocket.accept();
                        logArea.append("Produtor conectado.\n");

                        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                        Thread consumerThread = new Thread(() -> {
                            try {
                                String message;
                                while (running && (message = in.readLine()) != null) {
                                    logArea.append("Consumido: " + message + "\n");
                                }
                            } catch (IOException e) {
                                if (running) {
                                    e.printStackTrace();
                                }
                            } finally {
                                try {
                                    if (in != null) in.close();
                                    if (clientSocket != null) clientSocket.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                logArea.append("Conexão com o produtor encerrada.\n");
                            }
                        });
                        consumerThread.start();
                        consumerThread.join(); // Aguarda o término do thread de consumo
                    } catch (IOException | InterruptedException e) {
                        if (running) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            });
            acceptThread.start();

            startButton.setEnabled(false);
            stopButton.setEnabled(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Erro ao iniciar o servidor.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopConsumption() {
        running = false;
        try {
            if (acceptThread != null) {
                acceptThread.interrupt();
                acceptThread = null;
            }
            if (in != null) {
                in.close();
                in = null;
            }
            if (clientSocket != null) {
                clientSocket.close();
                clientSocket = null;
            }
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
            logArea.append("Consumo parado.\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }

    public static void main(String[] args) {
        new Consumer();
    }
}
