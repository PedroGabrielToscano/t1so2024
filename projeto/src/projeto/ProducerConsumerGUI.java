package projeto;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.concurrent.*;

class Producer implements Runnable {
    private BlockingQueue<Integer> queue;
    private int speed; // de 1 (lento) a 10 (rápido)
    private boolean running = true;
    private ProducerConsumerGUI gui;

    public Producer(BlockingQueue<Integer> queue, int speed, ProducerConsumerGUI gui) {
        this.queue = queue;
        this.speed = speed;
        this.gui = gui;
    }

    public void run() {
        int item = 0;
        while (running) {
            try {
                int minDelay = 100; // milissegundos
                int maxDelay = 2000; // milissegundos
                int delay = maxDelay - ((speed - 1) * (maxDelay - minDelay) / 9);
                Thread.sleep(delay);
                queue.put(item++);
                gui.refreshBufferDisplay();
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void stop() {
        running = false;
    }
}

class Consumer implements Runnable {
    private BlockingQueue<Integer> queue;
    private int speed; // de 1 (lento) a 10 (rápido)
    private boolean running = true;
    private ProducerConsumerGUI gui;

    public Consumer(BlockingQueue<Integer> queue, int speed, ProducerConsumerGUI gui) {
        this.queue = queue;
        this.speed = speed;
        this.gui = gui;
    }

    public void run() {
        while (running) {
            try {
                int minDelay = 100; // milissegundos
                int maxDelay = 2000; // milissegundos
                int delay = maxDelay - ((speed - 1) * (maxDelay - minDelay) / 9);
                Thread.sleep(delay);
                int item = queue.take();
                gui.refreshBufferDisplay();
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    public void stop() {
        running = false;
    }
}

public class ProducerConsumerGUI extends JFrame {

    private BlockingQueue<Integer> queue;
    private Producer producer;
    private Consumer consumer;
    private Thread producerThread;
    private Thread consumerThread;

    private int bufferSize = 5;
    private int producerSpeed = 5; // de 1 (lento) a 10 (rápido)
    private int consumerSpeed = 5; // de 1 (lento) a 10 (rápido)

    private JPanel bufferPanel;
    private JLabel[] bufferSlots;
    private JTextField bufferSizeField;
    private JSlider producerSpeedSlider;
    private JSlider consumerSpeedSlider;
    private JButton startButton;
    private JButton stopButton;

    public ProducerConsumerGUI() {
        setTitle("Problema Produtor-Consumidor com Passagem de Mensagem");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setupControls();
        setupBufferDisplay();

        setVisible(true);
    }

    private void setupControls() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(4, 2));

        // Tamanho do buffer
        controlPanel.add(new JLabel("Tamanho do Buffer:"));
        bufferSizeField = new JTextField(String.valueOf(bufferSize));
        controlPanel.add(bufferSizeField);

        // Velocidade do produtor
        controlPanel.add(new JLabel("Velocidade do Produtor (1-10):"));
        producerSpeedSlider = new JSlider(1, 10, producerSpeed);
        producerSpeedSlider.setMajorTickSpacing(1);
        producerSpeedSlider.setPaintTicks(true);
        producerSpeedSlider.setPaintLabels(true);
        controlPanel.add(producerSpeedSlider);

        // Velocidade do consumidor
        controlPanel.add(new JLabel("Velocidade do Consumidor (1-10):"));
        consumerSpeedSlider = new JSlider(1, 10, consumerSpeed);
        consumerSpeedSlider.setMajorTickSpacing(1);
        consumerSpeedSlider.setPaintTicks(true);
        consumerSpeedSlider.setPaintLabels(true);
        controlPanel.add(consumerSpeedSlider);

        // Botões
        startButton = new JButton("Iniciar");
        stopButton = new JButton("Parar");
        stopButton.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // Ações dos botões
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startSimulation();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopSimulation();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });

        add(controlPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupBufferDisplay() {
        bufferPanel = new JPanel();
        bufferPanel.setLayout(new GridLayout(1, bufferSize));
        bufferSlots = new JLabel[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            bufferSlots[i] = new JLabel("", SwingConstants.CENTER);
            bufferSlots[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            bufferSlots[i].setOpaque(true);
            bufferSlots[i].setBackground(Color.WHITE);
            bufferPanel.add(bufferSlots[i]);
        }
        add(bufferPanel, BorderLayout.CENTER);
    }

    public void refreshBufferDisplay() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Object[] items = queue.toArray();
                for (int i = 0; i < bufferSize; i++) {
                    if (i < items.length) {
                        bufferSlots[i].setText(String.valueOf(items[i]));
                        bufferSlots[i].setBackground(Color.GREEN);
                    } else {
                        bufferSlots[i].setText("");
                        bufferSlots[i].setBackground(Color.WHITE);
                    }
                }
            }
        });
    }

    private void startSimulation() {
        try {
            bufferSize = Integer.parseInt(bufferSizeField.getText());
            if (bufferSize <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tamanho do buffer inválido");
            return;
        }

        producerSpeed = producerSpeedSlider.getValue();
        consumerSpeed = consumerSpeedSlider.getValue();

        queue = new ArrayBlockingQueue<>(bufferSize);
        producer = new Producer(queue, producerSpeed, this);
        consumer = new Consumer(queue, consumerSpeed, this);

        producerThread = new Thread(producer);
        consumerThread = new Thread(consumer);

        bufferPanel.removeAll();
        bufferPanel.setLayout(new GridLayout(1, bufferSize));
        bufferSlots = new JLabel[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            bufferSlots[i] = new JLabel("", SwingConstants.CENTER);
            bufferSlots[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            bufferSlots[i].setOpaque(true);
            bufferSlots[i].setBackground(Color.WHITE);
            bufferPanel.add(bufferSlots[i]);
        }
        bufferPanel.revalidate();
        bufferPanel.repaint();

        producerThread.start();
        consumerThread.start();
    }

    private void stopSimulation() {
        producer.stop();
        consumer.stop();
        producerThread.interrupt();
        consumerThread.interrupt();
        queue.clear();
    }

    public static void main(String[] args) {
        new ProducerConsumerGUI();
    }
}
